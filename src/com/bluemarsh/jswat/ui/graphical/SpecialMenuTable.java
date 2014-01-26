/*********************************************************************
 *
 *	Copyright (C) 2002 Nathan Fiedler
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
 * PROJECT:	JSwat
 * MODULE:	JSwat UI
 * FILE:	SpecialMenuTable.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *	Name	Date		Description
 *	----	----		-----------
 *	nf	12/27/02	Initial version
 *
 * $Id: SpecialMenuTable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.ui.Bundle;
import java.util.Hashtable;
import javax.swing.JMenu;

/**
 * This class holds the special menu and provides access to them.
 *
 * @author  Nathan Fiedler
 */
class SpecialMenuTable {
    /** Table of special menus. Other classes can look up menus by their
     * codename string using the <code>getMenu()</code> method. */
    private static Hashtable menuTable = new Hashtable();

    /**
     * Add a menu to the table using a particular name.
     *
     * @param  name  name of the menu.
     * @param  menu  menu to add.
     */
    public static void addMenu(String name, JMenu menu) {
        menuTable.put(name, menu);
    } // addMenu

    /**
     * Retrieves the menu corresponding to the given name.
     *
     * @param  name  code name of menu to get.
     * @return  menu, or null if not found.
     * @throws  ClassNotFoundException
     *          if the menu class could not be found.
     * @throws  IllegalAccessException
     *          if constructor has protected access.
     * @throws  InstantiationException
     *          if an menu instance could not be instantiated.
     */
    public static JMenu getMenu(String name)
        throws ClassNotFoundException,
               IllegalAccessException,
               InstantiationException {
        JMenu m = (JMenu) menuTable.get(name);
        if (m == null) {
            // All the menus are specified in the ui package's Bundle.
            String menuClass = Bundle.getString(name + "Class");
            if (menuClass != null) {
                Class clazz = Class.forName(menuClass);
                m = (JMenu) clazz.newInstance();
                menuTable.put(name, m);
            }
        }
        return m;
    } // getMenu
} // SpecialMenuTable
