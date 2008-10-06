/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: ClassName.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang;

import com.bluemarsh.jswat.Defaults;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Represents the name of a class. This deals with the inner class naming
 * scheme changes from one version of Java to the next. It also implements
 * some convenient operations.
 *
 * @author  Nathan Fiedler
 */
public class ClassName implements Comparable {
    /** Our preferences node. */
    private static Preferences preferences;
    /** The name of the class as given in the constructor. */
    private String originalName;
    /** The normalized name of the class. */
    private String classname;
    /** Name of the source file that defines this class, without
     * leading path. */
    private String sourcename;

    static {
        preferences = Preferences.userRoot().node("com/bluemarsh/jswat");
    }

    /**
     * Creates a new instance of ClassName.
     *
     * @param  name  name of class.
     */
    public ClassName(String name) {
        this(name, null);
    } // ClassName

    /**
     * Creates a new instance of ClassName with the given name and name
     * of its source file.
     *
     * @param  name  name of class.
     * @param  file  name of source file this class is defined in.
     */
    public ClassName(String name, String file) {
        originalName = name;
        classname = normalizeName(name);
        sourcename = file;
    } // ClassName

    /**
     * Compares this object with the specified object for order. Returns
     * a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     *
     * @param  o  the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this
     *          object is less than, equal to, or greater than the
     *          specified object.
     */
    public int compareTo(Object o) {
        if (o instanceof ClassName) {
            return classname.compareTo(((ClassName) o).classname);
        } else if (o instanceof String) {
            return classname.compareTo(normalizeName((String) o));
        } else {
            throw new ClassCastException();
        }
    } // compareTo

    /**
     * Returns the original class name as was passed to the constructor.
     *
     * @return  original, display-worthy, class name.
     */
    public String getDisplayName() {
        return originalName;
    } // getDisplayName

    /**
     * Returns the normalized name of the class. If the class name has
     * an anonymous inner class, the separator character will be a plus
     * sign (+) instead of a dollar sign ($).
     *
     * @return  normalized class name.
     */
    public String getName() {
        return classname;
    } // getName

    /**
     * Normalizes the name for internal use. The name is assumed to be
     * of valid form such that any '$' in the name is followed by more
     * characters.
     *
     * @param  name  name of class to be normalized.
     * @return  normalized class name.
     */
    protected static String normalizeName(String name) {
        int idx = name.indexOf('$');
        if (idx > 0) {
            // Replace the $ with a + if the inner class is anonymous.
            if (Character.isDigit(name.charAt(idx + 1))) {
                name = name.replace('$', '+');
            }
        }
        return name;
    } // normalizeName

    /**
     * Converts the class name to the file name of the source file.
     *
     * <p>Note that this class may have come from a source file that
     * had some other name. In that case, the two argument constructor
     * must be used to provide the correct source name.</p>
     *
     * @return  file name form of class name.
     */
    public String toFilename() {
        if (sourcename == null) {
            return toFilename(classname);
        } else {
            return toFilename(classname, sourcename);
        }
    } // toFilename

    /**
     * Turn the package name into a file path using simple character
     * substitution. Strips off any inner-class names before making the
     * conversion.
     *
     * <p>Note that this class may have come from a source file that
     * had some other name, in which case the return value will be
     * meaningless.</p>
     *
     * @param  clsname  fully-qualified name of the class, possibly
     *                  including an inner-class specification.
     * @return  path and filename of source file.
     */
    public static String toFilename(String clsname) {
        // Used for all inner classes in Java 1.4 and earlier.
        // Used for named inner classes in Java 1.5 and later.
        int idx = clsname.indexOf('$');
        if (idx > 0) {
            clsname = clsname.substring(0, idx);
        }
        // Used for anonymous inner classes in Java 1.5 and later.
        idx = clsname.indexOf('+');
        if (idx > 0) {
            clsname = clsname.substring(0, idx);
        }
        clsname = clsname.replace('.', File.separatorChar);
        clsname += preferences.get("defaultFileExtension",
                                   Defaults.FILE_EXTENSION);
        return clsname;
    } // toFilename

    /**
     * Converts a class name, with the given source file name, into a path
     * and filename of the source file for the class.
     *
     * @param  clsname  fully-qualified name of class.
     * @param  srcname  name of source file containing class.
     * @return  path and filename of source file.
     */
    public static String toFilename(String clsname, String srcname) {
        String filename = toFilename(clsname);
        int lastbit = filename.lastIndexOf(File.separatorChar);
        if (lastbit > -1) {
            filename = filename.substring(0, lastbit);
            filename = filename + File.separator + srcname;
        } else {
            // Class without a path, just use the source name.
            filename = srcname;
        }
        return filename;
    } // toFilename

    /**
     * Returns the string representation of this.
     *
     * @return  normalized class name.
     */
    public String toString() {
        return getName();
    } // toString
} // ClassName
