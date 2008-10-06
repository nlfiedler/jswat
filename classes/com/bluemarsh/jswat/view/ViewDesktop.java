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
 * MODULE:      View
 * FILE:        ViewDesktop.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/12/03        Initial version
 *
 * $Id: ViewDesktop.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import javax.swing.JComponent;
import javax.swing.JMenu;

/**
 * A ViewDesktop is responsible for displaying the views. There are a
 * number of concrete implementations that organize the views
 * differently.
 *
 * @author  Nathan Fiedler
 */
public interface ViewDesktop {

    /**
     * Adds the appropriate display component for the given view.
     *
     * @param  view  view to display.
     * @throws  ViewException
     *          if there was a problem displaying the view.
     */
    void addView(View view) throws ViewException;

    /**
     * Prepare this view desktop for non-use. Free any allocated
     * resources, empty collections, and set any references to null.
     */
    void dispose();

    /**
     * Returns the menu for this view desktop, if any. The menu is
     * positioned in the menu structure in place of the @window special
     * menu.
     *
     * @return  window menu, or null if none.
     */
    JMenu getMenu();

    /**
     * Returns the mode of this view desktop.
     *
     * @return  one of the ViewDesktopFactory <code>MODE_</code> constants.
     */
    int getMode();

    /**
     * Retreives the currently selected view.
     *
     * @return  selected view.
     */
    View getSelectedView();

    /**
     * Retreives the visual component for this desktop.
     *
     * @return  interface component.
     */
    JComponent getUI();

    /**
     * Set the given view as the selected one. This should make the view
     * component visible to the user.
     *
     * @param  view  view to be made active.
     * @throws  ViewException
     *          if a problem occurred.
     */
    void selectView(View view) throws ViewException;

    /**
     * Called when the preferences have changed. The view desktop may
     * want to update its cached settings based on the changes.
     */
    void setPreferences();
} // ViewDesktop
