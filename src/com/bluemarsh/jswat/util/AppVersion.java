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
 * MODULE:      JSwat Utils
 * FILE:        AppVersion.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/07/01        Initial version
 *      nf      09/11/01        Do not exit on error
 *
 * DESCRIPTION:
 *      This file defines the application version support.
 *
 * $Id: AppVersion.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Implements the program version support.
 *
 * @author  Nathan Fiedler
 */
public class AppVersion {
    /** The resource bundle contained in this object. */
    private static ResourceBundle resourceBundle;

    static {
        resourceBundle = ResourceBundle.getBundle(
            "com.bluemarsh.jswat.util.version");
    }

    /**
     * Retrieves the version number for the application.
     *
     * @return  application version number string.
     */
    public static String getVersion() {
        try {
            return resourceBundle.getString("version");
        } catch (MissingResourceException mre) {
            return "x.y";
        }
    } // getVersion
} // AppVersion
