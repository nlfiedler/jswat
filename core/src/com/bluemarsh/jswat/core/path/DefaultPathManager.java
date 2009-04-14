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

package com.bluemarsh.jswat.core.path;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The default implementation of the PathManager interface. Stores the
 * path values in the session properties.
 *
 * @author Nathan Fiedler
 */
public class DefaultPathManager extends AbstractPathManager {
    /** The classpath setting, if specified by the user (read-only). */
    private List<String> classPath;
    /** The sourcepath setting, if specified by the user (read-only). */
    private List<String> sourcePath;

    /**
     * Creates a new instance of DefaultPathManager.
     */
    public DefaultPathManager() {
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

    /**
     * Find the named file in the sourcepath or classpath. If at first the
     * file cannot be found, any leading path will be trimmed and the search
     * will commense once more, if and only if the fuzzy parameter is true.
     *
     * @param  filename  name of file to locate.
     * @param  fuzzy     apply a fuzzy search.
     * @return  file object, or null if not found.
     */
    protected PathEntry findFile(String filename, boolean fuzzy) { // TODO
        // ClassPath wants / no matter which platform we are on.
        filename = filename.replace('\\', '/');
//        FileObject fo = null;
//        if (sourcePathLookup != null) {
//            fo = sourcePathLookup.findResource(filename);
//        }
//        if (fo == null && classPathLookup != null) {
//            fo = classPathLookup.findResource(filename);
//        }
//        if (fo == null && fuzzy) {
//            int last = filename.lastIndexOf('/');
//            if (last > 0) {
//                filename = filename.substring(last + 1);
//                fo = findFile(filename, false);
//            }
//        }
//        return fo;
        return null;
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
    public void setClassPath(List<String> roots) { // TODO
        List<String> oldPath = classPath;
        if (roots == null || roots.size() == 0) {
            classPath = null;
        } else {
            classPath = Collections.unmodifiableList(roots);
//            // Convert the list of Strings to a list of FileObjects, if possible.
//            List<FileObject> foRoots = new LinkedList<FileObject>();
//            for (String path : roots) {
//                File file = new File(path);
//                if (file.exists()) {
//                    file = FileUtil.normalizeFile(file);
//                    FileObject fo = FileUtil.toFileObject(file);
//                    // Check if the files are actually archives, and get the
//                    // root of the archive as a file object; the classpath
//                    // support does not like receiving the archive file as-is.
//                    if (FileUtil.isArchiveFile(fo)) {
//                        fo = FileUtil.getArchiveRoot(fo);
//                    }
//                    foRoots.add(fo);
//                }
//            }
//            if (foRoots.size() > 0) {
//                // Create a lookup of the available paths.
//                FileObject[] arr = new FileObject[foRoots.size()];
//                arr = foRoots.toArray(arr);
//                try {
//                    classPathLookup = ClassPathSupport.createClassPath(arr);
//                } catch (IllegalArgumentException iae) {
//                    ErrorManager em = ErrorManager.getDefault();
//                    em.annotate(iae, NbBundle.getMessage(
//                            DefaultPathManager.class,
//                            "ERR_PathManager_BadPathEntry", iae));
//                    em.notify(iae);
//                }
//            }
        }
        firePropertyChange(PROP_CLASSPATH, oldPath, classPath);
    }

    @Override
    public void setSourcePath(List<String> roots) { // TODO
        List<String> oldPath = sourcePath;
        if (roots == null || roots.size() == 0) {
            sourcePath = null;
        } else {
//            // Check if the files are actually archives, and get the root
//            // of the archive as a file object; the classpath support does
//            // not like receiving the archive file itself.
//            List<FileObject> nrts = new ArrayList<FileObject>();
//            for (FileObject root : roots) {
//                if (FileUtil.isArchiveFile(root)) {
//                    root = FileUtil.getArchiveRoot(root);
//                }
//                nrts.add(root);
//            }
            sourcePath = Collections.unmodifiableList(roots); // XXX: was nrts
//            FileObject[] arr = new FileObject[nrts.size()];
//            arr = nrts.toArray(arr);
//            try {
//                sourcePathLookup = ClassPathSupport.createClassPath(arr);
//            } catch (IllegalArgumentException iae) {
//                sourcePathLookup = null;
//                ErrorManager em = ErrorManager.getDefault();
//                em.annotate(iae, NbBundle.getMessage(
//                        DefaultPathManager.class,
//                        "ERR_PathManager_BadPathEntry", iae));
//                em.notify(iae);
//            }
        }
        firePropertyChange(PROP_SOURCEPATH, oldPath, sourcePath);
    }

    /**
     * A FilePathEntry is a concrete implementation of PathEntry in
     * which the source is backed by a <code>java.io.File</code> instance.
     *
     * @author  Nathan Fiedler
     */
    private class FilePathEntry implements PathEntry {
        /** The file that contains the source object. */
        private final File fileSource;

        /**
         * Constructs a FilePathEntry instance for the given File.
         *
         * @param  src  file path entry.
         */
        public FilePathEntry(File src) {
            if (src == null) {
                throw new NullPointerException("src must be non-null");
            }
            fileSource = src;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FilePathEntry) {
                FilePathEntry ofs = (FilePathEntry) o;
                return ofs.fileSource.equals(fileSource);
            }
            return false;
        }

//        @Override
//        public boolean exists() {
//            return fileSource.exists();
//        }

        @Override
        public String getDisplayName() {
            return fileSource.getName();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(fileSource);
        }

        @Override
        public String getName() {
            return fileSource.getName();
        }

        @Override
        public String getPath() {
            try {
                return fileSource.getCanonicalPath();
            } catch (IOException ioe) {
                // In this case, the path would be useless anyway.
                return null;
            }
        }

        @Override
        public URL getURL() { // TODO
            return null;
        }

        @Override
        public int hashCode() {
            // Nifty hash function generated by NetBeans.
            int hash = 5;
            hash = 79 * hash + (this.fileSource != null ? this.fileSource.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean isSame(Object o) {
            if (o instanceof File) {
                File f = (File) o;
                return f.equals(fileSource);
            }
            return false;
        }
    }

    /**
     * A PathEntry based on an entry in a zip file.
     *
     * @author  Nathan Fiedler
     */
    private class ZipPathEntry implements PathEntry {
        /** Zip file. */
        private ZipFile zipFile;
        /** Entry in zip file. */
        private ZipEntry zipEntry;
        /** The last part of the zip entry name. */
        private final String entryName;
        /** Used for getting just the name of the zip entry. */
        private final File entryAsFile;

        /**
         * Constructs a ZipPathEntry from the given file and entry.
         *
         * @param  file   zip file.
         * @param  entry  zip file entry.
         */
        public ZipPathEntry(ZipFile file, ZipEntry entry) {
            if (file == null || entry == null) {
                throw new IllegalArgumentException("arguments must be non-null");
            }
            zipFile = file;
            zipEntry = entry;
            // Convert the name to the local file system form, stripping
            // away everything but the name of the file itself (i.e.
            // "path/to/entry.file" becomes "entry.file").
            entryName = new File(zipEntry.getName()).getName();
            // Use both zip file name and entry name to make a
            // unique identifier for the hashCode() method.
            entryAsFile = new File(file.getName(), entry.getName());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ZipPathEntry) {
                ZipPathEntry zpe = (ZipPathEntry) o;
                return zpe.entryAsFile.equals(entryAsFile);
            }
            return false;
        }

//        @Override
//        public boolean exists() {
//            return true;
//        }

        @Override
        public String getDisplayName() {
            return entryName;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            try {
                return zipFile.getInputStream(zipEntry);
            } catch (IllegalStateException ise) {
                // Zip file was apparently closed.
                // Try to reopen the zip file.
                zipFile = new ZipFile(zipFile.getName());
                zipEntry = zipFile.getEntry(zipEntry.getName());
                return zipFile.getInputStream(zipEntry);
            }
        }

        @Override
        public String getName() {
            return entryName;
        }

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public URL getURL() { // TODO
            return null;
        }

        @Override
        public int hashCode() {
            // Nifty hash function generated by NetBeans.
            int hash = 3;
            hash = 41 * hash + (this.entryAsFile != null ? this.entryAsFile.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean isSame(Object o) {
            if (o instanceof ZipEntry) {
                ZipEntry ze = (ZipEntry) o;
                return ze.equals(zipEntry);
            }
            return false;
        }
    }
}
