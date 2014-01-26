/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        JSwat.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/22/99        Initial version
 *      nf      08/13/00        Moved the main() method to Main class
 *      nf      06/02/01        Moved tokenize() to StringUtils
 *      nf      08/05/01        Removed windows code
 *      nf      09/11/01        Do not exit on error
 *
 * DESCRIPTION:
 *      The main class of the program, JSwat, is defined in this
 *      file. It provides some basic services for other classes.
 *
 * $Id: JSwat.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.config.JConfigure;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class provides access to the localized resource bundle.
 *
 * <p>This class is a Singleton which means only one instance of this
 * class exists. You use the instanceOf() method to access that
 * instance.</p>
 *
 * @author  Nathan Fiedler
 */
public class JSwat {
    /** Reference to the single instance of this class. */
    protected static JSwat instance;
    /** String and object resources for our program.
     * Used to get localized strings and objects (such as
     * images) for user interface components. */
    protected ResourceBundle resources;
    /** Application configuration. */
    protected JConfigure jConfigure;

    /**
     * No-arg constructor for JSwat class. It is protected since this
     * class is a singleton. To get the single instance of this class
     * use the instanceOf() method.
     */
    protected JSwat() {
        try {
            resources = ResourceBundle.getBundle
                ("com.bluemarsh.jswat.resources.JSwat");
        } catch (MissingResourceException mre) {
            System.err.println("resources/JSwat.properties not found");
        }
        jConfigure = new JConfigure();
    } // JSwat

    /**
     * Returns a reference to the JConfigure object.
     */
    public JConfigure getJConfigure() {
        return jConfigure;
    } // getJConfigure

    /**
     * Retrieves a string from the localized resource bundle.
     * The resources are located in the appropriate
     * resources/JSwat.properties file.
     *
     * @param  name  key name of the string resource to find
     * @return  string resource or null if not found
     * @see #getResource
     */
    public String getResourceString(String name) {
        String str = null;
        try {
            str = resources.getString(name);
        } catch (MissingResourceException mre) {
            // This will happen frequently since many actions
            // will lack images.
        }
        return str;
    } // getResourceString

    /**
     * Retrieves an object from the localized resource bundle.
     * In most cases this is an image.
     * The resources are located in the appropriate
     * resources/JSwat.properties file.
     *
     * @param  key  key name of the resource to find
     * @return  URL pointing to the object resource
     * @see #getResourceString
     */
    public URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            URL url = this.getClass().getResource(name);
            return url;
        }
        return null;
    } // getResource

    /**
     * Returns the reference to the single instance of this class.
     * If an instance does not exist it will be created.
     *
     * @return  the instance of this class.
     */
    public static JSwat instanceOf() {
        // Yeah yeah, double-checked locking isn't perfect.
        // If you can find a better solution, tell the world.
        if (instance == null) {
            synchronized (JSwat.class) {
                if (instance == null) {
                    instance = new JSwat();
                }
            }
        }
        return instance;
    } // instanceOf
} // JSwat
