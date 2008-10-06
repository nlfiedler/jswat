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
 * PROJECT:     JSwat
 * MODULE:      Utilities
 * FILE:        SessionSettings.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/08/03        Initial version
 *
 * $Id: SessionSettings.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class SessionSettings provides a set of utility functions for opening,
 * copying, and deleting named sets of session properties.
 *
 * @author  Nathan Fiedler
 */
public class SessionSettings {

    /**
     * Copies the current session properties to the given name.
     *
     * @param  name  session name.
     * @throws  BackingStoreException
     *          if the preferences had a problem.
     */
    public static void copySettings(String name) throws BackingStoreException {
        if (name == null || name.length() == 0) {
            return;
        }

        // Get all the node instances that we need.
        Preferences preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/Session");
        String defaultName = preferences.get("defaultName", "default");
        Preferences oldPrefs = preferences.node(defaultName);
        Preferences newPrefs = preferences.node(name);
        // Copy the current preferences values to the new node.
        String[] children = oldPrefs.keys();
        for (int ii = 0; ii < children.length; ii++) {
            String value = oldPrefs.get(children[ii], null);
            newPrefs.put(children[ii], value);
        }
        // Remember this name as the new default session.
        preferences.put("defaultName", name);
    } // copySettings

    /**
     * Returns the name of the current session.
     *
     * @return  session name.
     */
    public static String currentSettings() {
        Preferences preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/Session");
        return preferences.get("defaultName", "default");
    } // currentSettings

    /**
     * Delete the named set of session properties.
     *
     * @param  name  name of the set.
     * @throws  BackingStoreException
     *          if the preferences had a problem.
     */
    public static void deleteSettings(String name)
        throws BackingStoreException {
        if (name == null || name.length() == 0) {
            return;
        }

        Preferences preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/Session");
        Preferences delPrefs = preferences.node(name);
        String defaultName = preferences.get("defaultName", "default");
        Preferences oldPrefs = preferences.node(defaultName);
        // Remove the named node.
        delPrefs.removeNode();

        // If the default session was deleted, fall back on the 'default'.
        if (defaultName.equals(name)) {
            preferences.put("defaultName", "default");
            oldPrefs = preferences.node("default");
        }
    } // deleteSettings

    /**
     * Returns an array of the currently available named sets of session
     * properties. There will always be at least one set, named
     * "default".
     *
     * @return  array of setting names.
     * @throws  BackingStoreException
     *          if the preferences had a problem.
     */
    public static String[] getAvailableSettings()
        throws BackingStoreException {
        Preferences preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/Session");
        return preferences.childrenNames();
    } // getAvailableSettings

    /**
     * Load the named set of session properties.
     *
     * @param  name  name of the set.
     */
    public static void loadSettings(String name) {
        if (name == null || name.length() == 0) {
            return;
        }

        Preferences preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/Session");
        // Remember this name as the new default session.
        preferences.put("defaultName", name);
    } // loadSettings
} // SessionSettings
