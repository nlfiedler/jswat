/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * $Id: Names.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.Defaults;
import java.util.prefs.Preferences;

/**
 *
 * @author  Nathan Fiedler
 */
public class Names {
    /** Our Preferences node. */
    private static Preferences preferences;

    static {
        preferences = Preferences.userRoot().node("com/bluemarsh/jswat/util");
    }

    /**
     * None shall instantiate us.
     */
    private Names() {
    } // Names

    /**
     * Determine if the given string is a valid Java identifier.
     *
     * @param  s  string to validate.
     * @return  true if string is a valid Java identifier.
     */
    public static boolean isJavaIdentifier(String s) {
        if (s == null || s.length() == 0) {
            return false;
        }
        // First character of identifier is a special case.
        if (!Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        // Now check all other characters of the identifier.
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    } // isJavaIdentifier

    /**
     * Determine if the given string is a valid method identifier.
     *
     * @param  s  string to validate.
     * @return  true if string is a valid method identifier.
     */
    public static boolean isMethodIdentifier(String s) {
        return isJavaIdentifier(s) || s.equals("<init>")
            || s.equals("<clinit>");
    } // isMethodIdentifier

    /**
     * Returns just the name of the class, without the package name.
     *
     * @param  cname  Name of class, possibly fully-qualified.
     * @return  Just the class name, or null if cname is null.
     */
    public static String justTheName(String cname) {
        if (cname == null) {
            return null;
        }

        if (preferences.getBoolean("shortClassNames",
                                   Defaults.SHORT_CLASS_NAMES)) {
            int i = cname.lastIndexOf('.');
            if (i > 0) {
                return cname.substring(i + 1);
            }
        }
        return cname;
    } // justTheName
} // Names
