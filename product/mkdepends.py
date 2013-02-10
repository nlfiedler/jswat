#!/usr/bin/env python3
#
# Python script that scans a directory tree looking for NetBeans module
# projects files (named project.xml) and extracting the dependency
# information, then displaying the set of NetBeans modules which are declared
# as being required. This list is then inverted to produce the "disabled"
# modules list, and can be written to the suite/nbproject/platform.properties
# file. Within NetBeans, open the suite properties, select "Libraries", and
# click the Resolve button to get the minimum set of required modules.
#
# TODO: N.B. that the --build option is not producing correct results, the
# list seems to be extra short. That said, perhaps now is a good time to look
# at using NetBeans to produce an installer for us. Would save an awful lot of
# trouble (already wasted days writing this script), especially with
# maintaining the build.xml which now does nothing more than create the
# installer.
#

import argparse
import configparser
import os
import os.path
import sys
from xml.etree.ElementTree import ElementTree

DIR_EXCLUDES = ['.git', '.hg', '.svn']
PROJECT_XML = "project.xml"
MOD_EXCLUDE = 'com.bluemarsh.jswat'
NAME_SPACE = "http://www.netbeans.org/ns/nb-module-project/"
CNB_TAG = "code-name-base"
# Our application is only concerned with these two clusters.
CLUSTERS = ['ide', 'java']


def iterate_depends(filename):
    """
    Parse the project file looking for module dependencies, yielding the
    resulting tag text (i.e. module name).
    """
    with open(filename) as proj:
        tree = ElementTree()
        doc = tree.parse(proj)
        for vers in range(1, 5):
            tagname = "{{{0}{1}}}{2}".format(NAME_SPACE, vers, CNB_TAG)
            for elem in doc.iter(tagname):
                yield elem.text


def iterate_projects(path):
    """
    Walk the directory tree looking for project files that are not in
    source control directories, yielding the resulting (relative) paths.
    """
    for root, dirs, files in os.walk(path):
        if PROJECT_XML in files:
            yield os.path.relpath(os.path.join(root, PROJECT_XML))
        for exclusion in DIR_EXCLUDES:
            if exclusion in dirs:
                dirs.remove(exclusion)


def find_depends_set(path):
    """
    Scan for module dependences within the modules found in the given
    path, returning the module names in a set.
    """
    results = set()
    for fname in iterate_projects(path):
        for mname in iterate_depends(fname):
            if not mname.startswith(MOD_EXCLUDE):
                results.add(mname)
    return results


def get_module_name(filename):
    """
    Parses the module definition file and returns the module name.
    """
    with open(filename) as proj:
        tree = ElementTree()
        doc = tree.parse(proj)
        # Ensure this is really a module descriptor.
        if doc.tag == 'module':
            return doc.get('name')
    return None


def find_all_modules(path):
    """
    Scan for the names of all modules found in the given path, returning
    the names of the modules in a set.
    """
    paths = [os.path.join(path, cluster, 'config', 'Modules') for cluster in CLUSTERS]
    mods = set()
    for path_ in paths:
        for root, dirs, files in os.walk(path_):
            for name in files:
                if name.endswith('.xml'):
                    mod = get_module_name(os.path.join(root, name))
                    if mod:
                        mods.add(mod)
    return mods


def read_properties(filename):
    """
    Read a Java properties file in which lines consist of name and value
    pairs separated by an equals sign (=), and values may be continued
    on subsequent lines if a trailing backslash (\) is present. Lines
    beginning with a hash (#) are treated as comments.
    """
    with open(filename) as f:
        text = '[netbeans]\n' + f.read()
    parser = configparser.ConfigParser(delimiters='=', comment_prefixes='#')
    props = dict()
    parser.read_string(text)
    opts = parser.options('netbeans')
    for opt in opts:
        props[opt] = parser.get('netbeans', opt)
    return props


def read_disabled_modules():
    """
    Read the list of disabled modules from the suite platform.properties file.
    """
    NAME = 'disabled.modules'
    props = read_properties(os.path.join('suite', 'nbproject', 'platform.properties'))
    if NAME in props:
        # configparser doesn't seem to know \ continuation marker
        return set(name.strip('\\').strip() for name in props[NAME].split(','))
    return None


def find_module_files(nbpath, modules):
    """
    Given the path to the NetBeans installation and the list of desired modules,
    return a tuple of lists of their corresponding jar and xml configuration files.
    """
    jar_names = set(mod.replace('.', '-') + '.jar' for mod in modules)
    xml_names = set(mod.replace('.', '-') + '.xml' for mod in modules)
    jars = []
    configs = []
    mkpath = lambda root, name: os.path.relpath(os.path.join(root, name), nbpath)
    for root, dirs, files in os.walk(nbpath):
        for fname in files:
            if fname in jar_names:
                jars.append(mkpath(root, fname))
                jar_names.remove(fname)
            elif fname in xml_names:
                configs.append(mkpath(root, fname))
                xml_names.remove(fname)
        if 'ModuleAutoDeps' in dirs:
            dirs.remove('ModuleAutoDeps')
    if len(jars) != len(modules) or len(configs) != len(modules):
        missing = ", ".join(jar_names) + "; " + ", ".join(xml_names)
        raise Exception('Missing NetBeans module files: {}'.format(missing))
    return (jars, configs)


def organize_files(jars, configs):
    """
    Given the collection of module jar files (with relative path) and their
    configuration files, return a map of common relative paths and the files
    contained therein.
    """
    buckets = dict()
    for path in jars + configs:
        base, name = os.path.split(path)
        if base not in buckets:
            buckets[base] = []
        buckets[base].append(name)
    return buckets


def main():
    """
    Scan the directory tree looking for project files, displaying all the
    NetBeans module dependencies found therein.
    """
    parser = argparse.ArgumentParser(description='Generate module dependency list.')
    parser.add_argument('-n', '--nbpath', help='path to NetBeans', metavar='path', required=True)
    parser.add_argument('-s', '--srcpath', help='path to application source tree; defaults to cwd',
        metavar='path')
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('-d', '--disabled', action='store_true', help='generate the disabled modules list')
    group.add_argument('-b', '--build', action='store_true', help='generate the modules list for build.xml')
    args = parser.parse_args()

    if args.disabled:
        srcpath = args.srcpath if args.srcpath else os.getcwd()
        depends = find_depends_set(srcpath)
        if not depends:
            sys.stderr.write('Found no module dependencies! Are you in the source directory?')
            sys.exit(1)
        mods = find_all_modules(args.nbpath)
        # Find the set of modules that we do _not_ depend on.
        disabled = list(mods - depends)
        disabled.sort()
        # Print out the list of disabled modules in a format suitable for pasting
        # into the platform.properties file.
        print("disabled.modules=\\")
        for mod in disabled[:-1]:
            print("    {},\\".format(mod))
        print("    {}".format(disabled[-1]))

    if args.build:
        disabled = read_disabled_modules()
        all_mods = find_all_modules(args.nbpath)
        enabled = all_mods - disabled
        jars, configs = find_module_files(args.nbpath, enabled)
        buckets = organize_files(jars, configs)
        for k, l in buckets.items():
            # TODO: format this for easy of pasting into build.xml
            print(k)
            for n in l:
                print("   {}".format(n))

if __name__ == "__main__":
    main()
