/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Names.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.CoreSettings;
import java.io.File;
import java.util.StringTokenizer;

/**
 * Class defining utility methods for class names and element names.
 *
 * @author  Nathan Fiedler
 */
public class Names {

    /**
     * None shall instantiate us.
     */
    private Names() {
    }

    /**
     * Turn the package name into a file path using simple character
     * substitution. Strips off any inner-class names before making the
     * conversion. The source file extension from CoreSettings will be
     * added to the end of the return value.
     *
     * <p>Note that this class may have come from a source file that
     * had some other name, in which case the return value will be
     * meaningless. The ideal solution is to use the appropriate methods
     * of Location and ReferenceType to get the true source name and
     * then call the two-argument form of this method.</p>
     *
     * @param  clsname  fully-qualified name of the class, possibly
     *                  including an inner-class specification.
     * @return  path and filename of a source file.
     */
    public static String classnameToFilename(String clsname) {
        int idx = clsname.indexOf('$');
        if (idx > 0) {
            clsname = clsname.substring(0, idx);
        }
        clsname = clsname.replace('.', File.separatorChar);
        CoreSettings cs = CoreSettings.getDefault();
        clsname += cs.getSourceExtension();
        return clsname;
    }

    /**
     * Converts a class name, with the given source file name, into a path
     * and filename of the source file for the class.
     *
     * @param  clsname  fully-qualified name of class.
     * @param  srcname  name of source file containing class.
     * @return  path and filename of source file.
     */
    public static String classnameToFilename(String clsname, String srcname) {
        String filename = classnameToFilename(clsname);
        int lastbit = filename.lastIndexOf(File.separatorChar);
        if (lastbit > -1) {
            filename = filename.substring(0, lastbit);
            filename = filename + File.separator + srcname;
        } else {
            // Class without a path, just use the source name.
            filename = srcname;
        }
        return filename;
    }

    /**
     * Returns just the package name of the class.
     *
     * @param  name  fully-qualified class name.
     * @return  package name (may be empty), or null if name is null.
     */
    public static String getPackageName(String name) {
        if (name == null) {
            return null;
        }
        int lastdot = name.lastIndexOf('.');
        if (lastdot > 0) {
            return name.substring(0, lastdot);
        } else {
            return "";
        }
    }

    /**
     * Returns just the name of the class, without the package name.
     *
     * @param  name  fully-qualified class name.
     * @return  just the class name, or null if name is null.
     */
    public static String getShortClassName(String name) {
        if (name == null) {
            return null;
        }
        int lastdot = name.lastIndexOf('.');
        if (lastdot > 0) {
            return name.substring(lastdot + 1);
        } else {
            return name;
        }
    }

    /**
     * Test whether a given string is a valid Java identifier. Unlike
     * org.openide.util.Utilities, this does not consider keywords to
     * be valid identifiers (because they are not identifiers).
     *
     * @param  id  String which should be checked.
     * @return  true if a valid identifier, false otherwise.
     */
    public static boolean isJavaIdentifier(String id) {
        if (id == null) {
            return false;
        }
        if (id.length() == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(id.charAt(0))) {
            return false;
        }
        for (int ii = 1; ii < id.length(); ii++) {
            if (!Character.isJavaIdentifierPart(id.charAt(ii))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if the given string is a valid method identifier.
     *
     * @param  s  string to validate.
     * @return  true if string is a valid method identifier.
     */
    public static boolean isMethodIdentifier(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        return isJavaIdentifier(s) || s.equals("<init>") || s.equals("<clinit>");
    }

    /**
     * Determines if the given String represents a valid class name.
     * The string is split into tokens delimited by a period (.). The
     * first or last token may be an asterisk (*) to represent a
     * wild-card for the purpose of matching multiple classes (for use
     * in setting breakpoints), if allowWild is true.
     *
     * @param  s          String to validate.
     * @param  allowWild  true to allow wildcards, false to treat as invalid.
     * @return  true if name is valid, false otherwise.
     */
    public static boolean isValidClassname(String s, boolean allowWild) {
        if (s == null || s.length() == 0) {
            return false;
        }
        boolean valid = true;
        StringTokenizer tokenizer = new StringTokenizer(s, ".");
        int numTokens = tokenizer.countTokens();
        int curToken = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            curToken++;
            if (allowWild && token.equals("*")) {
                if (curToken == 1) {
                    continue;
                } else if (curToken == numTokens) {
                    break;
                }
            }
            if (!isJavaIdentifier(token)) {
                valid = false;
                break;
            }
        }
        return valid;
    }
}
