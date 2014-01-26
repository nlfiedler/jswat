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
 * MODULE:      JSwat
 * FILE:        MacroManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/16/01        Initial version

 * DESCRIPTION:
 *      This file defines the class responsible for managing
 *      the macros defined by the user.
 *
 * $Id: MacroManager.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.util.*;

/**
 * Class MacroManager is responsible for managing the macros defined
 * by the user.
 *
 * @author  Nathan Fiedler
 */
public class MacroManager extends DefaultManager {
    /** List of available macros. Key is the macro name, value
     * is a Vector of command strings. */
    protected Hashtable macroList;

    /**
     * Constructs a MacroManager with the default input field.
     */
    public MacroManager() {
        macroList = new Hashtable();
    } // MacroManager

    /**
     * Creates a command macro. Subsequent uses of the macro
     * name will result in executing the matching command.
     *
     * @param  name   name of new macro.
     * @param  cmnds  command strings for macro.
     */
    public void createMacro(String name, Vector cmnds) {
        // Add the command macro to the list.
        macroList.put(name, cmnds);
    } // createMacro

    /**
     * Remove the given command macro.
     *
     * @param  name   name of macro to remove.
     * @return  defined macro, or null if none.
     */
    public Vector getMacro(String name) {
        return (Vector) macroList.get(name);
    } // getMacro

    /**
     * Builds and returns a list of the names of all the defined
     * macros, in alphabetical order.
     *
     * @return  sorted list of macro names, or null if none.
     */
    public Iterator macroNames() {
        if (macroList.isEmpty()) {
            return null;
        }

        // Enumerate the keys in the hashtable and store them
        // in another list.
        Enumeration keys = macroList.keys();
        ArrayList list = new ArrayList();
        while (keys.hasMoreElements()) {
            list.add(keys.nextElement());
        }
        // Sort the list.
        Collections.sort(list);
        return list.iterator();
    } // macroNames

    /**
     * Remove the given command macro.
     *
     * @param  name   name of macro to remove.
     */
    public void removeMacro(String name) {
        // Remove the command macro from the list.
        macroList.remove(name);
    } // removeMacro
} // MacroManager
