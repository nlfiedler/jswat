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
 * $Id: PathManager.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.config.JConfigure;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
public class PathManager extends DefaultManager {
    /** Session that owns us. */
    protected Session owningSession;
    /** Table of SourceSource objects, keyed by the classname. */
    protected Hashtable classnameSources;

    /**
     * Constructs a PathManager object.
     */
    public PathManager() {
        classnameSources = new Hashtable();
    } // PathManager

    /**
     * Turn the package name into a file path using simple character
     * subsitution.
     *
     * @param  classname  fully-qualified name of the class, possibly
     *                    including an inner-class specification.
     * @return  path and filename of source file.
     */
    protected String classnameToFilename(String classname) {
        int dollar = classname.indexOf('$');
        if (dollar > 0) {
            // Drop the inner-class specifier.
            classname = classname.substring(0, dollar);
        }
        classname = classname.replace('.', File.separatorChar);
        JConfigure config = JSwat.instanceOf().getJConfigure();
        classname += config.getProperty("files.defaultExtension");
        return classname;
    } // classnameToFilename

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
        if (classpath == null) {
            classpath = "";
        }
        return classpath;
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
        if (sourcepath == null) {
            sourcepath = "";
        }
        return sourcepath;
    } // getSourcePathAsString

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session) {
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
    } // init

    /**
     * Searches for the .class file of the given class and returns
     * a SourceSource representing that .class file.
     *
     * @param  clazz    class for which to find class file.
     * @return  object representing the desired class file, or null
     *          if class file not found.
     */
    public SourceSource mapClass(ReferenceType clazz) {
        String filename = clazz.name().replace('.', File.separatorChar) +
            ".class";

        SourceSource src = null;

        String[] classpathArray = getClassPath();
        if (classpathArray != null) {
            for (int i = 0; i < classpathArray.length; i++) {
                src = mapSource0(classpathArray[i], filename);
                if (src != null) {
                    break;
                }
            }
        }

        return src;
    } // mapClass

    /**
     * Return a SourceSource corresponding to the fully-qualified class
     * name. Return null if the source was not found.
     *
     * @param  classname  fully-qualified class name.
     * @return  source containing the desired location.
     * @exception  IOException
     *             Thrown if an I/O error occurred.
     */
    public SourceSource mapSource(String classname) throws IOException {
        // Check in the classname/File look-up table to see if
        // we've already mapped this classname to a file.
        SourceSource source = (SourceSource) classnameSources.get(classname);
        if (source != null) {
            return source;
        }

        // Use the primitive means of finding the source.
        String filename = classnameToFilename(classname);
        return mapSourceLow(filename, classname);
    } // mapSource

    /**
     * Return a SourceSource corresponding to the given class.
     * Return null if the source was not found.
     *
     * @param  clazz    class for which to find source file.
     * @return  source containing the desired location.
     * @exception  IOException
     *             Thrown if an I/O error occurred.
     */
    public SourceSource mapSource(ReferenceType clazz) throws IOException {
        // Check in the classname/SourceSource look-up table to see if
        // we have already mapped this classname to a source.
        String classname = clazz.name();
        SourceSource source = (SourceSource) classnameSources.get(classname);
        if (source != null) {
            return source;
        }

        String filename = classnameToFilename(classname);

        // Try to use the source filename as given by the class.
        try {
            // Get the source name first so the exception gets thrown
            // now rather than after we modify the filename variable.
            String srcname = clazz.sourceName();
            int bsi = srcname.lastIndexOf('\\');
            int fsi = srcname.lastIndexOf('/');
            // Work-around for bug 4404985 where SourceFile has path.
            if (bsi > -1) {
                srcname = srcname.substring(bsi + 1);
            } else if (fsi > -1) {
                srcname = srcname.substring(fsi + 1);
            }
            int lastbit = filename.lastIndexOf(File.separatorChar);
            if (lastbit > -1) {
                filename = filename.substring(0, lastbit);
                filename = filename + File.separator + srcname;
            } else {
                // Class without a path, just use the source name.
                filename = srcname;
            }
        } catch (AbsentInformationException aie) {
            // If this happens, this method ends up being the same
            // as the version that takes a String argument.
        }

        return mapSourceLow(filename, classname);
    } // mapSource

    /**
     * Looks for a matching entry in either the classpath or sourcepath.
     *
     * @param  filename   name of file to look for.
     * @param  classname  name of class for caching result.
     * @return  matching source, if found.
     */
    protected SourceSource mapSourceLow(String filename, String classname) {
        String[] sourcepathArray = getSourcePath();
        // Scan through the sourcepath list, if available.
        if (sourcepathArray != null) {
            for (int i = 0; i < sourcepathArray.length; i++) {
                SourceSource src = mapSource0(sourcepathArray[i], filename);
                if (src != null) {
                    // We found it, cache it and return the source.
                    classnameSources.put(classname, src);
                    return src;
                }
            }
        }

        String[] classpathArray = getClassPath();
        // Scan through the classpath list, if available.
        if (classpathArray != null) {
            for (int i = 0; i < classpathArray.length; i++) {
                SourceSource src = mapSource0(classpathArray[i], filename);
                if (src != null) {
                    // We found it, cache it and return the source.
                    classnameSources.put(classname, src);
                    return src;
                }
            }
        }

        // Did not find a matching source file.
        return null;
    } // mapSourceLow

    /**
     * Look for the file in the given class or sourcepath entry.
     * This method deals with zip and jar archives, as well as the
     * usual directory paths.
     *
     * @param  path      source or classpath entry.
     * @param  filename  name of file to look for.
     */
    protected SourceSource mapSource0(String path,
                                      String filename) {

        if (path.endsWith(".zip") || path.endsWith(".jar")) {
            // Zip/jar path entry.
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(path);
            } catch (IOException ioe) {
                return null;
            }
            Enumeration enmr = zipFile.entries();
            while (enmr.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enmr.nextElement();
                String entryName = zipEntry.getName();
                // Convert the name to the local file system form so we
                // can compare it to the filename argument.
                entryName = new File(entryName).getPath();
                if (entryName.equals(filename)) {
                    return new ZipSource(zipFile, zipEntry);
                }
            }

        } else {
            // Directory path entry.
            File file = new File(path, filename);
            if (file.exists()) {
                return new FileSource(file);
            }
        }

        return null;
    } // mapSource0

    /**
     * Sets the classpath this source manager uses. The previous
     * classpath is discarded in favor of the new one. The classpath
     * is used together with the sourcepath, if any.
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
     * Sets the classpath this source manager uses. The previous
     * classpath is discarded in favor of the new one. The class
     * path is used together with the sourcepath, if any.
     *
     * @param  list  List of String classpath entries.
     */
    public void setClassPath(List list) {
        int size = list.size();
        if (size == 0) {
            // Special case of empty list.
            owningSession.setProperty("classpath", "");
        } else {
            // Turn the list into a String so we can save it.
            StringBuffer sb = new StringBuffer(80);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                String entry = (String) iter.next();
                File f = new File(entry);
                try {
                    sb.append(f.getCanonicalPath());
                } catch (IOException ioe) {
                    sb.append(entry);
                }
                sb.append(File.pathSeparator);
            }
            // Remove the last path separator.
            sb.deleteCharAt(sb.length() - 1);
            owningSession.setProperty("classpath", sb.toString());
        }
    } // setClassPath

    /**
     * Sets the sourcepath this source manager uses. The previous
     * sourcepath is discarded in favor of the new one. The source
     * path is used together with the classpath, if any.
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
} // PathManager
