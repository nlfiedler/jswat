/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.plugin.path;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.AbstractPathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.util.Names;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * The NetBeans plugin implementation of the PathManager interface. Uses the
 * global path registry defined by the Java projects to find files. Does not
 * modify anything, nor store any values to persistent storage.
 *
 * @author Nathan Fiedler
 */
public class NbPathManager extends AbstractPathManager
        implements GlobalPathRegistryListener {
    /** The classpath setting, if specified by the user (read-only). */
    private List<String> classPath;
    /** The sourcepath setting, if specified by the user (read-only). */
    private List<FileObject> sourcePath;
    /** The classpath as a ClassPath, used to look up resources. */
    private ClassPath classPathLookup;
    /** The sourcepath as a ClassPath, used to look up resources. */
    private ClassPath sourcePathLookup;

    /**
     * Creates a new instance of NbPathManager.
     */
    public NbPathManager() {
    }

    public boolean canSetSourcePath() {
        return false;
    }

    public boolean canSetClassPath() {
        return false;
    }

    @Override
    public void closing(SessionEvent event) {
        super.closing(event);
        GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
    }

    public FileObject findByteCode(ReferenceType clazz) {
        String filename = clazz.name();
        filename = filename.replace('.', File.separatorChar);
        filename += ".class";
        return findFile(filename, false);
    }

    /**
     * Find the named file in the sourcepath or classpath. If at first the
     * file cannot be found, any leading path will be trimmed and the search
     * will commense once more, if and only if the fuzzy parameter is true.
     *
     * @param  filename  name of file to locate.
     * @param  fuzzy     apply a fuzzy search.
     * @return  file object, or null if not found.
     */
    protected FileObject findFile(String filename, boolean fuzzy) {
        // ClassPath wants / no matter which platform we are on.
        filename = filename.replace('\\', '/');
        FileObject fo = null;
        if (sourcePathLookup != null) {
            fo = sourcePathLookup.findResource(filename);
        }
        if (fo == null && classPathLookup != null) {
            fo = classPathLookup.findResource(filename);
        }
        if (fo == null && fuzzy) {
            int last = filename.lastIndexOf('/');
            if (last > 0) {
                filename = filename.substring(last + 1);
                fo = findFile(filename, false);
            }
        }
        return fo;
    }

    public FileObject findSource(String name) {
        String filename = Names.classnameToFilename(name);
        FileObject fo = findFile(filename, true);
        if (fo == null) {
            try {
                // Try to locate the .class file and read the source name
                // attribute from the bytecode, then get that source file.
                // (note ClassPath wants / for all platforms)
                filename = name.replace('.', '/') + ".class";
                fo = findFile(filename, false);
                if (fo != null) {
                    File file = FileUtil.toFile(fo);
                    ClassFile cf = new ClassFile(file, false);
                    String srcname = cf.getSourceFileName();
                    filename = Names.classnameToFilename(name, srcname);
                    fo = findFile(filename, true);
                }
            } catch (IOException ioe) {
                // fall through...
            }
        }
        return fo;
    }

    public FileObject findSource(Location location) {
        try {
            String source = location.sourceName();
            String classname = location.declaringType().name();
            String filename = Names.classnameToFilename(classname, source);
            FileObject fo = findFile(filename, true);
            if (fo == null) {
                // Common case failed, try another method.
                VirtualMachine vm = location.virtualMachine();
                if (vm.canGetSourceDebugExtension()) {
                    String path = location.sourcePath(null);
                    fo = findFile(path, true);
                }
            }
            if (fo != null) {
                return fo;
            }
        } catch (AbsentInformationException aie) {
            // fall through...
        }
        // Fall back to using only the class information.
        return findSource(location.declaringType());
    }

    public FileObject findSource(ReferenceType clazz) {
        String classname = clazz.name();
        String filename;
        try {
            // Try to use the source filename as given by the class.
            String srcname = clazz.sourceName();
            filename = Names.classnameToFilename(classname, srcname);
        } catch (AbsentInformationException aie) {
            filename = Names.classnameToFilename(classname);
        }
        FileObject fo = findFile(filename, true);
        if (fo == null) {
            // Common case failed, try another method.
            VirtualMachine vm = clazz.virtualMachine();
            if (vm.canGetSourceDebugExtension()) {
                try {
                    List paths = clazz.sourcePaths(null);
                    Iterator iter = paths.iterator();
                    while (iter.hasNext()) {
                        String path = (String) iter.next();
                        fo = findFile(path, true);
                        if (fo != null) {
                            break;
                        }
                    }
                } catch (AbsentInformationException aie) {
                    // fall through...
                }
            }
        }
        return fo;
    }

    public List<String> getClassPath() {
        Session session = PathProvider.getSession(this);
        String ignore = session.getProperty(PROP_IGNORE_DEBUGGEE);
        // If we are not to ignore the classpath reported by the debuggee,
        // then retrieve that instead of using the classpath defined by the
        // user, since generally the debuggee value is more authoritative.
        if (ignore == null || ignore.length() == 0) {
            JvmConnection vmConnection = session.getConnection();
            if (vmConnection != null) {
                VirtualMachine vm = vmConnection.getVM();
                if (vm instanceof PathSearchingVirtualMachine) {
                    return ((PathSearchingVirtualMachine) vm).classPath();
                }
            }
        }

        return classPath;
    }

    public List<FileObject> getSourcePath() {
        return sourcePath;
    }

    protected List<String> getUserClassPath() {
        return classPath;
    }

    protected void loadPaths(Session session) {
// This does not find anything in the open projects.
//        // Load the classpath setting from the global path registry.
//        Set classPaths = GlobalPathRegistry.getDefault().getPaths(
//                ClassPath.EXECUTE);
//        if (classPaths.size() == 0) {
//            classPath = null;
//            classPathLookup = null;
//        } else {
//            List<FileObject> roots = new ArrayList<FileObject>();
//            List<String> paths = new ArrayList<String>();
//            Iterator<ClassPath> iter = classPaths.iterator();
//            while (iter.hasNext()) {
//                ClassPath path = iter.next();
//                for (FileObject root : path.getRoots()) {
//                    // ClassPath does not like getting archive files.
//                    if (FileUtil.isArchiveFile(root)) {
//                        root = FileUtil.getArchiveRoot(root);
//                    }
//                    File file = FileUtil.toFile(root);
//                    if (file != null) {
//                        paths.add(file.getAbsolutePath());
//                    }
//                    roots.add(root);
//                }
//            }
//            classPath = Collections.unmodifiableList(paths);
//            FileObject[] arr = (FileObject[]) roots.toArray(
//                    new FileObject[roots.size()]);
//            classPathLookup = ClassPathSupport.createClassPath(arr);
//        }
        // XXX: should we merge all open projects?
        Project project = OpenProjects.getDefault().getMainProject();
        AntArtifactProvider aap = (AntArtifactProvider) project.
                getLookup().lookup(AntArtifactProvider.class);
        if (aap != null) {
            AntArtifact[] artifacts = aap.getBuildArtifacts();
            List<FileObject> roots = new ArrayList<FileObject>();
            List<String> paths = new ArrayList<String>();
            for (AntArtifact artifact : artifacts) {
                // XXX: fallback on type folder if jar is not available
                if (artifact.getType().equals(
                        JavaProjectConstants.ARTIFACT_TYPE_JAR)) {
                    File script = artifact.getScriptLocation();
                    URI[] locations = artifact.getArtifactLocations();
                    for (URI uri : locations) {
                        File file = new File(script.toURI().resolve(uri));
                        file = FileUtil.normalizeFile(file);
                        paths.add(file.getAbsolutePath());
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            // ClassPath does not like getting archive files.
                            if (FileUtil.isArchiveFile(fo)) {
                                fo = FileUtil.getArchiveRoot(fo);
                            }
                            roots.add(fo);
                        }
                    }
                }
            }
            classPath = Collections.unmodifiableList(paths);
            FileObject[] arr = (FileObject[]) roots.toArray(
                    new FileObject[roots.size()]);
            classPathLookup = ClassPathSupport.createClassPath(arr);
        }

        // Load the sourcepath setting from the global path registry.
        Set sourceRoots = GlobalPathRegistry.getDefault().getSourceRoots();
        if (sourceRoots.size() == 0) {
            sourcePath = null;
            sourcePathLookup = null;
        } else {
            FileObject[] roots = new FileObject[sourceRoots.size()];
            Iterator<FileObject> iter = sourceRoots.iterator();
            int index = 0;
            while (iter.hasNext()) {
                FileObject root = iter.next();
                // ClassPath does not like getting archive files.
                if (FileUtil.isArchiveFile(root)) {
                    root = FileUtil.getArchiveRoot(root);
                }
                roots[index] = root;
                index++;
            }
            sourcePath = Collections.unmodifiableList(Arrays.asList(roots));
            sourcePathLookup = ClassPathSupport.createClassPath(roots);
        }
    }

    @Override
    public void opened(Session session) {
        super.opened(session);
        GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(this);
    }

    public void pathsAdded(GlobalPathRegistryEvent event) {
        loadPaths(PathProvider.getSession(this));
    }

    public void pathsRemoved(GlobalPathRegistryEvent event) {
        loadPaths(PathProvider.getSession(this));
    }

    protected void savePaths(Session session) {
        // We do not save anything, as the Java projects have all we need.
    }

    public void setClassPath(List<String> roots) {
    }

    public void setSourcePath(List<FileObject> roots) {
    }
}
