/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: LoggingMenu.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.logging.Logging;
import com.bluemarsh.jswat.ui.Bundle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * Specialized menu class that implements the logging menu for this
 * program. It automatically builds out the menu for selecting the
 * available logging categories.
 *
 * <p>This is one of the available special menus. It is requested in the
 * resources file using the "@logging" special menu tag.</p>
 *
 * @author  Nathan Fiedler
 */
class LoggingMenu extends JMenu implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a LoggingMenu.
     */
    public LoggingMenu() {
        super(Bundle.getString("loggingLabel"), true);

        // Create menu items for changing logging options.
        createItem(Bundle.getString("LoggingMenu.breakpoint"),
                   "com.bluemarsh.jswat.breakpoint");
        createItem(Bundle.getString("LoggingMenu.event"),
                   "com.bluemarsh.jswat.event");
        createItem(Bundle.getString("LoggingMenu.monitor"),
                   "com.bluemarsh.jswat.monitor");
        createItem(Bundle.getString("LoggingMenu.session"),
                   "com.bluemarsh.jswat.Session");
        createItem(Bundle.getString("LoggingMenu.session-list"),
                   "com.bluemarsh.jswat.SessionListenerList");
        createItem(Bundle.getString("LoggingMenu.sourceview"),
                   "com.bluemarsh.jswat.view");
        createItem(Bundle.getString("LoggingMenu.viewer"),
                   "com.bluemarsh.jswat.ui.viewer");

        setToolTipText("<html><small>"
                       + Bundle.getString("loggingTooltip")
                       + "</small></html>");
    } // LoggingMenu

    /**
     * Creates and adds a new menu item with the given label and
     * action command string.
     *
     * @param  label  menu item label.
     * @param  name   action command string.
     */
    protected void createItem(String label, String name) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(label);
        menuItem.setActionCommand(name);
        add(menuItem);
        menuItem.setSelected(Logging.isEnabled(name));
        menuItem.addItemListener(this);
    } // createItem

    /**
     * One of the logging buttons was selected. See which one it was
     * and enable or disable that logger.
     *
     * @param  e  Indicates which item was selected.
     */
    public void itemStateChanged(ItemEvent e) {
        JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
        String name = cb.getActionCommand();
        if (cb.isSelected()) {
            Logging.enable(name);
        } else {
            Logging.disable(name);
        }
    } // itemStateChanged
} // LoggingMenu
