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
 * $Id: OptionsMenu.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.ui.Bundle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * Specialized menu class that implements the options menu for this
 * program. It automatically builds out the menu for toggling various
 * options.
 *
 * <p>This is one of the available special menus. It is requested in the
 * resources file using the "@options" special menu tag.</p>
 *
 * @author  Nathan Fiedler
 */
class OptionsMenu extends JMenu implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a OptionsMenu with the given name.
     */
    public OptionsMenu() {
        super(Bundle.getString("optionsLabel"), true);

        // Create menu items for changing options.
        createItem("com/bluemarsh/jswat/view",
                   "colorizeView", Defaults.VIEW_COLORIZE);
        createItem("com/bluemarsh/jswat/view",
                   "parseView", Defaults.VIEW_PARSE);
        createItem("com/bluemarsh/jswat/view",
                   "viewLineNumbers", Defaults.VIEW_LINE_NUMBERS);
        createItem("com/bluemarsh/jswat/ui/graphical",
                   "rememberGeometry", Defaults.REMEMBER_GEOMETRY);
        createItem("com/bluemarsh/jswat/Session",
                   "raiseWindow", Defaults.RAISE_WINDOW);
        createItem("com/bluemarsh/jswat/util",
                   "shortClassNames", Defaults.SHORT_CLASS_NAMES);
        createItem("com/bluemarsh/jswat/ui/graphical",
                   "showToolbar", Defaults.SHOW_TOOLBAR);
        createItem("com/bluemarsh/jswat/ui/graphical",
                   "smallToolbarButtons", Defaults.SMALL_TOOLBAR_BUTTONS);
        createItem("com/bluemarsh/jswat",
                   "useClassicVM", Defaults.USE_CLASSIC_VM);
        createItem("com/bluemarsh/jswat/breakpoint",
                   "addStarDot", Defaults.ADD_STAR_DOT);
        createItem("com/bluemarsh/jswat/breakpoint",
                   "stopOnMain", Defaults.STOP_ON_MAIN);

        setToolTipText("<html><small>"
                       + Bundle.getString("optionsTooltip")
                       + "</small></html>");
    } // OptionsMenu

    /**
     * Creates and adds a new menu item with the given label and
     * action command string.
     *
     * @param  node   preferences node name.
     * @param  name   name of preferences key.
     * @param  defv   default selection value.
     */
    protected void createItem(String node, String name, boolean defv) {
        String label = Bundle.getString("OptionsMenu." + name);
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(label);
        menuItem.setActionCommand(node + "/" + name);
        add(menuItem);
        menuItem.addItemListener(this);
        Preferences prefs = Preferences.userRoot().node(node);
        menuItem.setSelected(prefs.getBoolean(name, defv));
    } // createItem

    /**
     * One of the logging buttons was selected. See which one it was
     * and enable or disable that logger.
     *
     * @param  e  Indicates which item was selected.
     */
    public void itemStateChanged(ItemEvent e) {
        JCheckBoxMenuItem cb = (JCheckBoxMenuItem) e.getSource();
        String action = cb.getActionCommand();
        int lastSlash = action.lastIndexOf('/');
        String node = action.substring(0, lastSlash);
        String name = action.substring(lastSlash + 1);
        Preferences prefs = Preferences.userRoot().node(node);
        prefs.putBoolean(name, cb.isSelected());
    } // itemStateChanged
} // OptionsMenu
