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
 * The Original Software is JSwat Installer. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Bundle.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.installer;

import java.net.URL;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides convenient access to localized messages.
 *
 * @author Nathan Fiedler
 */
public class Bundle {
    /** The resource bundle for localized messages. */
    private static ResourceBundle bundle;

    static {
        bundle = ResourceBundle.getBundle(Bundle.class.getName());
    }

    /**
     * Creates a new instance of Bundle.
     */
    private Bundle() {
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
            String name = bundle.getString(key);
            return Bundle.class.getResource(name);
        } catch (MissingResourceException mre) {
            return null;
        }
    }

    /**
     * Retrieves the localized string for the given resource key.
     *
     * @param  key  resource key.
     * @return  localized string.
     */
    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException mre) {
            return "MISSING: <<" + key + ">>";
        }
    }

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found, this method returns a string that clearly indicates
     * that it was missing.
     *
     * @param  key  name of String resource to retrieve.
     * @param  arg  argument to be inserted into the string.
     * @return  named resource value.
     */
    public static String getString(String key, Object arg) {
        return getString(key, new Object[] { arg });
    }

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found, this method returns a string that clearly indicates
     * that it was missing.
     *
     * @param  key   name of String resource to retrieve.
     * @param  arg1  argument to be inserted into the string.
     * @param  arg2  argument to be inserted into the string.
     * @return  named resource value.
     */
    public static String getString(String key, Object arg1, Object arg2) {
        return getString(key, new Object[] { arg1, arg2 });
    }

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found, this method returns a string that clearly indicates
     * that it was missing.
     *
     * @param  key   name of String resource to retrieve.
     * @param  arg1  argument to be inserted into the string.
     * @param  arg2  argument to be inserted into the string.
     * @param  arg3  argument to be inserted into the string.
     * @return  named resource value.
     */
    public static String getString(
            String key, Object arg1, Object arg2, Object arg3) {
        return getString(key, new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Retrieves the String resource from this bundle. If the key was
     * not found, this method returns a string that clearly indicates
     * that it was missing.
     *
     * @param  key   name of String resource to retrieve.
     * @param  args  arguments to be inserted into the string.
     * @return  named resource value.
     */
    public static String getString(String key, Object[] args) {
        String rsc = getString(key);
        return MessageFormat.format(rsc, args);
    }
}
