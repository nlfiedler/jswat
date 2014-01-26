/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: Bundle.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class Bundle contains a <code>java.util.ResourceBundle</code> and
 * provides some simple access methods to it. The idea is that this
 * class exists in each package that requires resources. This class
 * contains the resources defined in the Bundle.properties file within
 * this package. Only the classes in this package will use this
 * particular class. Identical Bundle classes will exist in all of the
 * packages in which resources are needed. Granted, this implies that
 * the class is reproduced verbatim from one package to the next, but it
 * is very fast and efficient.
 *
 * @author  Nathan Fiedler
 */
public class Bundle {
    /** The resource bundle contained in this object. */
    private static ResourceBundle resourceBundle;

    static {
        // Retrieve the resource bundle for this package.
        resourceBundle = ResourceBundle.getBundle(
            Bundle.class.getName());
    }

    /**
     * Retrieves an object from the localized resource bundle. In most
     * cases this is an image.
     *
     * @param  key  key name of the resource to find.
     * @return  URL pointing to the object resource, or null if it
     *          was not found.
     */
    public static URL getResource(String key) {
        try {
            String name = resourceBundle.getString(key);
            return Bundle.class.getResource(name);
        } catch (MissingResourceException mre) {
            return null;
        }
    } // getResource

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found, this method returns a string that clearly indicates
     * that it was missing.
     *
     * @param  key  name of String resource to retrieve.
     * @return  named resource value.
     */
    public static String getString(String key) {
        return getString(key, false);
    } // getString

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found and <code>missingNull</code> is false, this method
     * returns a string that clearly indicates that it was missing;
     * otherwise it returns null.
     *
     * @param  key          name of String resource to retrieve.
     * @param  missingNull  if true, return null when resource is missing.
     * @return  named resource value.
     */
    public static String getString(String key, boolean missingNull) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            return missingNull ? null : "MISSING STRING: " + key;
        }
    } // getString
} // Bundle
