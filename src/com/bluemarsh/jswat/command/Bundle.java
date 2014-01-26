/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      (duplicated in all of them)
 * FILE:        Bundle.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/01        Initial version
 *
 * DESCRIPTION:
 *      The resource bundle accessor class for this package.
 *
 * $Id: Bundle.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class Bundle contains a <code>java.util.ResourceBundle</code> and
 * provides some simple access methods to it. The idea is that this
 * class exists in each package that requires resources. This class
 * contains the resources defined in the Bundle.properties file within
 * this package. Only the classes in this package will use this particular
 * class. Identical Bundle classes will exist in all of the packages in
 * which resources are needed.
 * Granted, this implies that the class is reproduced verbatim from
 * one package to the next, but it is very fast and efficient.
 *
 * @author  Nathan Fiedler
 */
public class Bundle {
    /** The resource bundle contained in this object. */
    private static ResourceBundle resourceBundle;

    static {
        // Retrieve the resource bundle for this package.
        // Fortunately the properties file name is the same
        // as this class name (very convenient).
        resourceBundle = ResourceBundle.getBundle
            (Bundle.class.getName());
    }

    /**
     * Retrieves the resource bundle for this package. Provided in the
     * event the caller wants direct access to the resource bundle.
     *
     * @return  resource bundle for this package.
     */
    public static ResourceBundle getBundle() {
        return resourceBundle;
    } // getBundle

    /**
     * Retrieves an object from the localized resource bundle.
     * In most cases this is an image.
     *
     * @param  key  key name of the resource to find.
     * @return  URL pointing to the object resource.
     */
    public static URL getResource(String key) {
        String name = getString(key);
        return name == null ? null : Bundle.class.getResource(name);
    } // getResource

    /**
     * Retrieves the String resource from this bundle.
     *
     * @param  key  name of String resource to retrieve.
     * @return  resource bundle for this package.
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException mre) {
            // This will happen frequently since there are no
            // images for many actions.
            return null;
        }
    } // getString
} // Bundle
