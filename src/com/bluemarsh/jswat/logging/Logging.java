/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * MODULE:      Logging
 * FILE:        Logging.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/12/02        Initial version
 *
 * $Id: Logging.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Class Logging provides static methods for changing the logging
 * settings within the program.
 *
 * @author  Nathan Fiedler
 */
public class Logging {
    /** Our preferences node. */
    private static Preferences prefs;

    static {
        // Initialize the logging module.
        prefs = Preferences.userRoot().node("com/bluemarsh/jswat/logging");
    }

    /**
     * We are not constructed.
     */
    private Logging() {
        // no one instantiates us
    } // Logging

    /**
     * Disable the Logger by the given name.
     *
     * @param  name  name of logger to disable.
     */
    public static void disable(String name) {
        Logger logger = Logger.getLogger(name);
        // Just disable messages below warning level.
        logger.setLevel(Level.WARNING);
        prefs.putBoolean(name, false);
    } // disable

    /**
     * Enable the Logger by the given name.
     *
     * @param  name  name of logger to enable.
     */
    public static void enable(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.ALL);
        prefs.putBoolean(name, true);
    } // enable

    /**
     * Check if the Logger by the given name is enabled.
     *
     * @param  name  name of logger to check.
     * @return  true if enabled, false otherwise.
     */
    public static boolean isEnabled(String name) {
        return prefs.getBoolean(name, false);
    } // isEnabled

    /**
     * Set the initial state (enabled or disabled) of the given Logger.
     *
     * @param  logger  logger to enable or disable.
     */
    public static void setInitialState(Logger logger) {
        String name = logger.getName();
        if (!isEnabled(name)) {
            disable(name);
        }
        // Nothing to do otherwise.
    } // setInitialState
} // Logging
