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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nbcore.path;

import com.bluemarsh.jswat.core.PlatformProvider;
import com.bluemarsh.jswat.core.PlatformService;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.AbstractPathManager;
import com.bluemarsh.jswat.core.path.PathEntry;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * The default implementation of the PathManager interface. Stores the
 * path values in the session properties.
 *
 * @author Nathan Fiedler
 */
public class NetBeansPathManager extends AbstractPathManager {
    /** The classpath setting, if specified by the user (read-only). */
    private List<String> classPath;
    /** The sourcepath setting, if specified by the user (read-only). */
    private List<String> sourcePath;
    /** The classpath as a ClassPath, used to look up resources. */
    private ClassPath classPathLookup;
    /** The sourcepath as a ClassPath, used to look up resources. */
    private ClassPath sourcePathLookup;

    /**
     * Creates a new instance of DefaultPathManager.
     */
    public NetBeansPathManager() {
    }

    @Override
    public boolean canSetSourcePath() {
        return true;
    }

    @Override
    public boolean canSetClassPath() {
        return true;
    }

    @Override
    public PathEntry findByteCode(ReferenceType clazz) {
        String filename = clazz.name();
        filename = filename.replace('.', File.separatorChar);
        filename += ".class";
        return findFile(filename, false);
    }

    @Override
    public PathEntry findFile(String filename) {
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
    protected PathEntry findFile(String filename, boolean fuzzy) {
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
                return findFile(filename, false);
            }
        }
        return new FileObjectPathEntry(fo);
    }

    @Override
    public PathEntry findSource(String name) {
        String filename = Names.classnameToFilename(name);
        PathEntry pe = findFile(filename, true);
        if (pe == null) {
            try {
                // Try to locate the .class file and read the source name
                // attribute from the bytecode, then get that source file.
                // (note ClassPath wants / for all platforms)
                filename = name.replace('.', '/') + ".class";
                pe = findFile(filename, false);
                if (pe != null) {
                    PlatformService platform = PlatformProvider.getPlatformService();
                    InputStream is = pe.getInputStream();
                    String srcname = platform.getSourceName(is, name);
                    if (srcname != null) {
                        filename = Names.classnameToFilename(name, srcname);
                        pe = findFile(filename, true);
                    }
                }
            } catch (IOException ioe) {
                // fall through...
            }
        }
        return pe;
    }

    @Override
    public PathEntry findSource(Location location) {
        try {
            String source = location.sourceName();
            String classname = location.declaringType().name();
            String filename = Names.classnameToFilename(classname, source);
            PathEntry pe = findFile(filename, true);
            if (pe == null) {
                // Common case failed, try another method.
                VirtualMachine vm = location.virtualMachine();
                if (vm.canGetSourceDebugExtension()) {
                    String path = location.sourcePath(null);
                    pe = findFile(path, true);
                }
            }
            if (pe != null) {
                return pe;
            }
        } catch (AbsentInformationException aie) {
            // fall through...
        }
        // Fall back to using only the class information.
        return findSource(location.declaringType());
    }

    @Override
    public PathEntry findSource(ReferenceType clazz) {
        String classname = clazz.name();
        String filename;
        try {
            // Try to use the source filename as given by the class.
            String srcname = clazz.sourceName();
            filename = Names.classnameToFilename(classname, srcname);
        } catch (AbsentInformationException aie) {
            filename = Names.classnameToFilename(classname);
        }
        PathEntry pe = findFile(filename, true);
        if (pe == null) {
            // Common case failed, try another method.
            VirtualMachine vm = clazz.virtualMachine();
            if (vm.canGetSourceDebugExtension()) {
                try {
                    List paths = clazz.sourcePaths(null);
                    Iterator iter = paths.iterator();
                    while (iter.hasNext()) {
                        String path = (String) iter.next();
                        pe = findFile(path, true);
                        if (pe != null) {
                            break;
                        }
                    }
                } catch (AbsentInformationException aie) {
                    // fall through...
                }
            }
        }
        return pe;
    }

    @Override
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

    @Override
    public List<String> getSourcePath() {
        return sourcePath;
    }

    @Override
    protected List<String> getUserClassPath() {
        return classPath;
    }

    @Override
    protected void loadPaths(Session session) {
        // Load the classpath setting from the session properties.
        String cp = session.getProperty(PROP_CLASSPATH);
        // As long as the session properties are not moved to a system that
        // uses a different path separator than the one on which they were
        // saved, this will work correctly.
        if (cp != null) {
            setClassPath(Strings.stringToList(cp, File.pathSeparator));
        }

        // Load the sourcepath setting from the session properties.
        String sp = session.getProperty(PROP_SOURCEPATH);
        if (sp != null) {
            setSourcePath(Strings.stringToList(sp, File.pathSeparator));
        }
    }

    /**
     * Replace the current ClassPath.COMPILE entries with the one contained
     * in classPathLookup. The intent is that the Java editor will be able
     * to find the classes when parsing open source files, and avoid having
     * the error stripe in the source viewer.
     */
    @SuppressWarnings("unchecked")
    private void registerClassPath() {
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        Set paths = gpr.getPaths(ClassPath.COMPILE);
        if (paths.size() > 0) {
            ClassPath[] arr = (ClassPath[]) paths.toArray(
                    new ClassPath[paths.size()]);
            gpr.unregister(ClassPath.COMPILE, arr);
        }
        if (classPathLookup != null) {
            ClassPath[] arr = new ClassPath[] { classPathLookup };
            gpr.register(ClassPath.COMPILE, arr);
        }
    }

    /**
     * Replace the current ClassPath.SOURCE entries with the one contained
     * in sourcePathLookup. The intent is that the Java editor will be able
     * to find the classes when parsing open source files, and avoid having
     * the error stripe in the source viewer.
     */
    @SuppressWarnings("unchecked")
    private void registerSourcePath() {
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        Set paths = gpr.getPaths(ClassPath.SOURCE);
        if (paths.size() > 0) {
            ClassPath[] arr = (ClassPath[]) paths.toArray(
                    new ClassPath[paths.size()]);
            gpr.unregister(ClassPath.SOURCE, arr);
        }
        if (sourcePathLookup != null) {
            ClassPath[] arr = new ClassPath[] { sourcePathLookup };
            gpr.register(ClassPath.SOURCE, arr);
        }
    }

    @Override
    protected void savePaths(Session session) {
        // Save the classpath setting to the session properties.
        if (classPath == null || classPath.size() == 0) {
            session.setProperty(PROP_CLASSPATH, null);
        } else {
            String cp = Strings.listToString(classPath, File.pathSeparator);
            session.setProperty(PROP_CLASSPATH, cp);
        }

        // Save the sourcepath setting to the session properties.
        if (sourcePath == null || sourcePath.size() == 0) {
            session.setProperty(PROP_SOURCEPATH, null);
        } else {
            String sp = Strings.listToString(sourcePath, File.pathSeparator);
            session.setProperty(PROP_SOURCEPATH, sp);
        }
    }

    @Override
    public void setClassPath(List<String> roots) {
        List<String> oldPath = classPath;
        // Wipe out the lookup and rebuild if possible.
        classPathLookup = null;
        if (roots == null || roots.size() == 0) {
            classPath = null;
        } else {
            classPath = Collections.unmodifiableList(roots);
            // Convert the list of Strings to a list of FileObjects, if possible.
            List<FileObject> foRoots = new LinkedList<FileObject>();
            for (String path : roots) {
                File file = new File(path);
                if (file.exists()) {
                    file = FileUtil.normalizeFile(file);
                    FileObject fo = FileUtil.toFileObject(file);
                    // Check if the files are actually archives, and get the
                    // root of the archive as a file object; the classpath
                    // support does not like receiving the archive file as-is.
                    if (FileUtil.isArchiveFile(fo)) {
                        fo = FileUtil.getArchiveRoot(fo);
                    }
                    foRoots.add(fo);
                }
            }
            if (foRoots.size() > 0) {
                // Create a lookup of the available paths.
                FileObject[] arr = new FileObject[foRoots.size()];
                arr = foRoots.toArray(arr);
                try {
                    classPathLookup = ClassPathSupport.createClassPath(arr);
                } catch (IllegalArgumentException iae) {
                    ErrorManager em = ErrorManager.getDefault();
                    em.annotate(iae, NbBundle.getMessage(
                            NetBeansPathManager.class,
                            "ERR_PathManager_BadPathEntry", iae));
                    em.notify(iae);
                }
            }
        }
        registerClassPath();
        firePropertyChange(PROP_CLASSPATH, oldPath, classPath);
    }

    @Override
    public void setSourcePath(List<String> roots) {
        List<String> oldPath = sourcePath;
        if (roots == null || roots.size() == 0) {
            sourcePath = null;
            sourcePathLookup = null;
        } else {
            // Check if the files are actually archives, and get the root
            // of the archive as a file object; the classpath support does
            // not like receiving the archive file itself.
            Set<FileObject> nrts = new HashSet<FileObject>();
            for (String root : roots) {
                FileObject fobj = PathConverter.toFileObject(root);
                if (fobj != null) {
                    fobj = PathConverter.convertToRoot(fobj);
                    nrts.add(fobj);
                }
            }
            List<FileObject> fobjs = new ArrayList<FileObject>(nrts);
            sourcePath = PathConverter.toStrings(fobjs);
            FileObject[] arr = new FileObject[nrts.size()];
            arr = nrts.toArray(arr);
            try {
                sourcePathLookup = ClassPathSupport.createClassPath(arr);
            } catch (IllegalArgumentException iae) {
                sourcePathLookup = null;
                ErrorManager em = ErrorManager.getDefault();
                em.annotate(iae, NbBundle.getMessage(
                        NetBeansPathManager.class,
                        "ERR_PathManager_BadPathEntry", iae));
                em.notify(iae);
            }
        }
        registerSourcePath();
        firePropertyChange(PROP_SOURCEPATH, oldPath, sourcePath);
    }

    /**
     * A PathEntry that is based on FileObject from NetBeans.
     *
     * @author  Nathan Fiedler
     */
    private class FileObjectPathEntry implements PathEntry {
        /** The FileObject representing this path entry. */
        private FileObject fileObject;

        /**
         * Constructs a new instance of FileObjectPathEntry.
         *
         * @param  fobj  the file object.
         */
        public FileObjectPathEntry(FileObject fobj) {
            fileObject = fobj;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FileObjectPathEntry) {
                FileObjectPathEntry fope = (FileObjectPathEntry) o;
                return fope.fileObject.equals(fileObject);
            }
            return false;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return fileObject.getInputStream();
        }

        @Override
        public String getDisplayName() {
            return FileUtil.getFileDisplayName(fileObject);
        }

        @Override
        public String getName() {
            return fileObject.getName();
        }

        @Override
        public String getPath() {
            return fileObject.getPath();
        }

        @Override
        public URL getURL() {
            try {
                return fileObject.getURL();
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
            }
            return null;
        }

        @Override
        public int hashCode() {
            // Nifty hash function generated by NetBeans.
            int hash = 5;
            hash = 29 * hash + (this.fileObject != null ? this.fileObject.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean isSame(Object o) {
            if (o instanceof FileObject) {
                FileObject fo = (FileObject) o;
                return fo.equals(fileObject);
            }
            return false;
        }
    }
}
