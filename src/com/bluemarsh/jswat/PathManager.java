/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: PathManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.lang.ClassName;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Location;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.structures.AttributeInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.attributes.SourceFileAttribute;
import org.gjt.jclasslib.structures.constants.ConstantUtf8Info;

/**
 * Class PathManager is responsible for managing the classpath and
 * sourcepath. It uses a given classpath and sourcepath, along with
 * class names and package names to find source files for requested
 * classes. If both a classpath and sourcepath are provided, the path
 * manager will search both to find a source file, searching the
 * sourcepath first.
 *
 * @author  Nathan Fiedler
 * @author  Marko van Dooren
 */
public class PathManager implements Manager {
    /** Session that owns us. */
    private Session owningSession;
    /** Generator of source objects. */
    private SourceFactory sourceFactory;

    /**
     * Constructs a PathManager object.
     */
    public PathManager() {
        sourceFactory = SourceFactory.getInstance();
    } // PathManager

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
    } // deactivated

    /**
     * Attempt to determine the name of the package that the given path
     * belongs to, using the sourcepath and classpath settings.
     *
     * @param  path  full path to source file.
     * @return  the package this source file belongs to; empty string
     *          if there is no actual package; null if error or path
     *          is not in the sourcepath or classpath.
     */
    public String findPackageName(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }

        // Must canonicalize the path first.
        try {
            path = new File(path).getCanonicalPath();
        } catch (IOException ioe) {
            return null;
        }
        // Search the sourcepath.
        String[] paths = getSourcePath();
        for (int ii = 0; ii < paths.length; ii++) {
            String apath = paths[ii];
            if (path.startsWith(apath)) {
                // Remove the prefix and convert to a package name.
                if (apath.length() < path.length()) {
                    path = path.substring(apath.length() + 1);
                    return path.replace(File.separatorChar, '.');
                } else if (apath.length() == path.length()) {
                    return "";
                } else {
                    return null;
                }
            }
        }

        // Search the classpath.
        paths = getClassPath();
        for (int ii = 0; ii < paths.length; ii++) {
            String apath = paths[ii];
            if (path.startsWith(apath)) {
                // Remove the prefix and convert to a package name.
                if (apath.length() < path.length()) {
                    path = path.substring(apath.length() + 1);
                    return path.replace(File.separatorChar, '.');
                } else if (apath.length() == path.length()) {
                    return "";
                } else {
                    return null;
                }
            }
        }

        return null;
    } // findPackageName

    /**
     * Returns the array of classpath directories, if any.
     *
     * @return  Array of Strings containing classpath directories;
     *          may be empty.
     */
    public String[] getClassPath() {
        StringTokenizer st = new StringTokenizer(
            getClassPathAsString(), File.pathSeparator);
        int size = st.countTokens();
        String[] result = new String[size];
        for (int ii = 0; ii < size; ii++) {
            result[ii] = st.nextToken();
        }
        return result;
    } // getClassPath

    /**
     * Returns the classpath as a String.
     *
     * @return  String of classpath, or empty string if not set.
     */
    public String getClassPathAsString() {
        // Prefer the classpath from the debuggee VM since that is more
        // accurate than anything else.
        VMConnection vmConnection = owningSession.getConnection();
        if (vmConnection != null) {
            VirtualMachine vm = vmConnection.getVM();
            if (vm instanceof PathSearchingVirtualMachine) {
                // Use the classpath from the VM, since it supports it.
                PathSearchingVirtualMachine psvm =
                    (PathSearchingVirtualMachine) vm;
                List list = psvm.classPath();
                if (list != null) {
                    StringBuffer buf = new StringBuffer();
                    if (list.size() > 0) {
                        buf.append(list.get(0));
                    }
                    for (int ii = 1; ii < list.size(); ii++) {
                        buf.append(File.pathSeparatorChar);
                        buf.append(list.get(ii));
                    }
                    return buf.toString();
                }
            }
        }

        String classpath = owningSession.getProperty("classpath");
        return classpath == null ? "" : classpath;
    } // getClassPathAsString

    /**
     * Returns the array of sourcepath directories, if any.
     *
     * @return  Array of Strings containing sourcepath directories;
     *          may be empty.
     */
    public String[] getSourcePath() {
        StringTokenizer st = new StringTokenizer(
            getSourcePathAsString(), File.pathSeparator);
        int size = st.countTokens();
        String[] result = new String[size];
        for (int ii = 0; ii < size; ii++) {
            result[ii] = st.nextToken();
        }
        return result;
    } // getSourcePath

    /**
     * Returns the sourcepath as a String.
     *
     * @return  String of sourcepath, or empty string if not set.
     */
    public String getSourcePathAsString() {
        String sourcepath = owningSession.getProperty("sourcepath");
        return sourcepath == null ? "" : sourcepath;
    } // getSourcePathAsString

    /**
     * Searches for the .class file of the given class and returns
     * a SourceSource representing that .class file.
     *
     * @param  clazz  class for which to find class file.
     * @return  object representing the desired class file, or null
     *          if class file not found.
     */
    public SourceSource mapClass(ReferenceType clazz) {
        return mapClass(clazz.name());
    } // mapClass

    /**
     * Searches for the .class file of the given name and returns
     * a SourceSource representing that .class file.
     *
     * @param  name  name of class for which to find class file.
     * @return  object representing the desired class file, or null
     *          if class file not found.
     */
    public SourceSource mapClass(String name) {
        String filename = name.replace('.', File.separatorChar) + ".class";
        SourceSource src = null;
        String[] classpathArray = getClassPath();
        if (classpathArray != null) {
            for (int ii = 0; ii < classpathArray.length; ii++) {
                src = mapFile(classpathArray[ii], filename);
                if (src != null) {
                    break;
                }
            }
        }
        return src;
    } // mapClass

    /**
     * Returns a SourceSource for the relatively named file (file path is
     * relative to sourcepath or classpath), scanning the sourcepath and
     * classpath to find the absolute location of the file.
     *
     * @param  filename  relative path and name of file.
     * @return  matching file source, if found.
     */
    public SourceSource mapFile(String filename) {
        String[] sourcepathArray = getSourcePath();
        // Scan through the sourcepath list, if available.
        if (sourcepathArray != null) {
            for (int ii = 0; ii < sourcepathArray.length; ii++) {
                SourceSource src = mapFile(sourcepathArray[ii], filename);
                if (src != null) {
                    return src;
                }
            }
        }

        String[] classpathArray = getClassPath();
        // Scan through the classpath list, if available.
        if (classpathArray != null) {
            for (int ii = 0; ii < classpathArray.length; ii++) {
                SourceSource src = mapFile(classpathArray[ii], filename);
                if (src != null) {
                    return src;
                }
            }
        }

        // Did not find a matching source file.
        return null;
    } // mapFile

    /**
     * Look for the file in the given classpath or sourcepath entry. This
     * method deals with zip and jar archives, as well as the usual
     * directory paths.
     *
     * @param  path      source or classpath entry.
     * @param  filename  path and name of file to look for.
     * @return  source file, if found, null otherwise.
     */
    protected SourceSource mapFile(String path, String filename) {
        if (path == null) {
            return null;
        }

        File f = new File(path);
        if (f.isFile() && (path.endsWith(".zip") || path.endsWith(".jar"))) {
            // Zip/jar path entry.
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(path);
            } catch (IOException ioe) {
                return null;
            }
            Enumeration entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String entryName = zipEntry.getName();
                // Convert the name to the local file system form so we
                // can compare it to the filename argument.
                entryName = new File(entryName).getPath();
                if (entryName.equals(filename)) {
                    return sourceFactory.create(zipFile, zipEntry);
                }
            }

        } else {
            // Directory path entry.
            f = new File(path, filename);
            if (f.exists()) {
                return sourceFactory.create(path, filename, this);
            }
        }

        return null;
    } // mapFile

    /**
     * Return a SourceSource corresponding to the fully-qualified class
     * name. Return null if the source was not found.
     *
     * @param  classname  fully-qualified class name.
     * @return  source containing the desired location.
     * @throws  IOException
     *          if an I/O error occurred.
     */
    public SourceSource mapSource(String classname) throws IOException {
        // First try the primitive means of finding the source.
        String filename = ClassName.toFilename(classname);
        SourceSource source = mapFile(filename);
        if (source == null) {
            // Now try using jclasslib to get the SourceFile attribute from
            // the .class file itself.
            SourceSource clazz = mapClass(classname);
            if (clazz != null) {
                InputStream is = clazz.getInputStream();
                String sourcename = null;
                try {
                    ClassFile classFile =
                        ClassFileReader.readFromInputStream(is);
                    AttributeInfo[] ainfo = classFile.getAttributes();
                    for (int ii = 0; ii < ainfo.length; ii++) {
                        if (ainfo[ii] instanceof SourceFileAttribute) {
                            SourceFileAttribute sfa =
                                (SourceFileAttribute) ainfo[ii];
                            int index = sfa.getSourcefileIndex();
                            ConstantUtf8Info cpinfo = (ConstantUtf8Info)
                                classFile.getConstantPoolEntry(
                                    index, ConstantUtf8Info.class);
                            sourcename = new String(cpinfo.getString());
                            break;
                        }
                    }

                } catch (InvalidByteCodeException ibce) {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("PathManager.badbytes") + ' '
                        + classname);
                } catch (IOException ioe) {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("PathManager.ioExc") + ' ' + ioe);
                }
                if (sourcename != null) {
                    filename = ClassName.toFilename(classname, sourcename);
                    source = mapFile(filename);
                }
            }
        }

        if (source == null) {
            // Maybe we can find the class and make a bytecode view of it.
            VirtualMachine vm = owningSession.getVM();
            if (vm != null) {
                List classes = vm.classesByName(classname);
                if (classes.size() == 1) {
                    ReferenceType clazz = (ReferenceType) classes.get(0);
                    source = sourceFactory.create(clazz);
                }
            }
        }
        return source;
    } // mapSource

    /**
     * Return a SourceSource corresponding to the given location.
     * Return null if the source was not found. This method is preferred
     * over the other mapSource() methods because the Location provides
     * the most accurate information.
     *
     * @param  location  location for which to find source file.
     * @return  source containing the desired location.
     * @throws  IOException
     *          if an I/O error occurred.
     */
    public SourceSource mapSource(Location location) throws IOException {
        try {
            String source = location.sourceName();
            String classname = location.declaringType().name();
            String filename = ClassName.toFilename(classname, source);
            SourceSource src = mapFile(filename);
            if (src == null) {
                // Common case failed, try harder to find the source.
                VirtualMachine vm = location.virtualMachine();
                if (vm.canGetSourceDebugExtension()) {
                    String path = location.sourcePath(null);
                    src = mapFile(path);
                }
            }
            if (src != null) {
                return src;
            }
        } catch (AbsentInformationException aie) {
            // fall through...
        }

        // Fall back to using only the class information.
        return mapSource(location.declaringType());
    } // mapSource

    /**
     * Return a SourceSource corresponding to the given class. Return
     * null if the source was not found. This method is preferred over
     * using the mapSource(String) method, but is not as good as the
     * mapSource(Location) method.
     *
     * @param  clazz  class for which to find source file.
     * @return  source containing the desired location.
     * @throws  IOException
     *          if an I/O error occurred.
     */
    public SourceSource mapSource(ReferenceType clazz) throws IOException {
        String classname = clazz.name();
        String filename;
        // Try to use the source filename as given by the class.
        try {
            String srcname = clazz.sourceName();
            filename = ClassName.toFilename(classname, srcname);
        } catch (AbsentInformationException aie) {
            filename = ClassName.toFilename(classname);
        }

        SourceSource src = mapFile(filename);
        if (src == null) {
            // Common case failed, try harder to find the source.
            VirtualMachine vm = clazz.virtualMachine();
            if (vm.canGetSourceDebugExtension()) {
                try {
                    List paths = clazz.sourcePaths(null);
                    Iterator iter = paths.iterator();
                    while (iter.hasNext()) {
                        String path = (String) iter.next();
                        src = mapFile(path);
                        if (src != null) {
                            break;
                        }
                    }
                } catch (AbsentInformationException aie) {
                    // Fall through
                }
            }
            if (src == null) {
                src = sourceFactory.create(clazz);
            }
        }
        return src;
    } // mapSource

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
        owningSession = session;
        // This is the end-all-be-all solution for handling the paths:

        // If java.source.path is defined at startup, set that as the
        // sourcepath session property value. But only do this at
        // startup or you run into bug 490.
        String sourcepath = System.getProperty("java.source.path");
        if (sourcepath != null && sourcepath.length() > 0) {
            setSourcePath(sourcepath);
        }

        // If the classpath session property is undefined, use the
        // java.class.path System property as the classpath value.
        String classpath = getClassPathAsString();
        if (classpath.length() == 0) {
            // This is guaranteed not to be empty.
            classpath = System.getProperty("java.class.path");
            setClassPath(classpath);
        }
    } // opened

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Sets the classpath this source manager uses. The previous
     * classpath is discarded in favor of the new one. The classpath is
     * used together with the sourcepath, if any.
     *
     * @param  classpath  Classpath for VM.
     */
    public void setClassPath(String classpath) {
        StringTokenizer st = new StringTokenizer(
            classpath, File.pathSeparator);
        int size = st.countTokens();
        StringBuffer sb = new StringBuffer(80);
        for (int ii = 0; ii < size; ii++) {
            String entry = st.nextToken();
            entry = entry.trim();
            File f = new File(entry);
            try {
                sb.append(f.getCanonicalPath());
            } catch (IOException ioe) {
                sb.append(entry);
            }
            sb.append(File.pathSeparator);
        }
        // Remove the last path separator.
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        owningSession.setProperty("classpath", sb.toString());
    } // setClassPath

    /**
     * Sets the sourcepath this source manager uses. The previous
     * sourcepath is discarded in favor of the new one. The source path
     * is used together with the classpath, if any.
     *
     * @param  sourcepath  Sourcepath for VM.
     */
    public void setSourcePath(String sourcepath) {
        StringTokenizer st = new StringTokenizer(
            sourcepath, File.pathSeparator);
        int size = st.countTokens();
        StringBuffer sb = new StringBuffer(80);
        for (int ii = 0; ii < size; ii++) {
            String entry = st.nextToken();
            entry = entry.trim();
            File f = new File(entry);
            try {
                sb.append(f.getCanonicalPath());
            } catch (IOException ioe) {
                sb.append(entry);
            }
            sb.append(File.pathSeparator);
        }
        // Remove the last path separator.
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        owningSession.setProperty("sourcepath", sb.toString());
    } // setSourcePath

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // PathManager
