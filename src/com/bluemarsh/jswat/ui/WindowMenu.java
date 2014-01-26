/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: WindowMenu.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * This is a specialized menu class that implements the windows menu.
 * This menu will hold a list of titles representing the internal windows
 * contained in the desktop pane. By selecting a menu item you will cause
 * the corresponding window to move to the front.
 *
 * <p>This menu will listen for changes in the desktop pane and update
 * itself as internal frames are added and removed.</p>
 *
 * @author  Nathan Fiedler
 */
class WindowMenu extends JMenu implements ActionListener, ContainerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** List of internal frames we represent in our menu. */
    protected JInternalFrame[] frameList;
    /** Label for the Minimize All menu item. */
    protected String minimizeLabel;
    /** Label for the Maximize All menu item. */
    protected String maximizeLabel;
    /** The desktop pane we are listening to. */
    protected JDesktopPane desktopPane;

    /**
     * Constructor for this class, creates the menu and adds some
     * basic window-management functions.
     *
     * @param  name  title for this menu.
     * @param  pane  desktop pane.
     */
    public WindowMenu(String name, JDesktopPane pane) {
        super(name, true);

        // Build the menu items.
        minimizeLabel = Bundle.getString("minimizeAllLabel");
        JMenuItem mi = new JMenuItem(minimizeLabel);
        add(mi);
        mi.addActionListener(this);
        maximizeLabel = Bundle.getString("maximizeAllLabel");
        mi = new JMenuItem(maximizeLabel);
        add(mi);
        mi.addActionListener(this);
        addSeparator();

        // Listen for changes to the desktop pane.
        pane.addContainerListener(this);
        desktopPane = pane;
        buildList(desktopPane.getAllFrames());
    } // WindowMenu

    /**
     * A menu item was selected, we should bring the corresponding
     * window to the front.
     *
     * @param  e  the event that was triggered
     */
    public void actionPerformed(ActionEvent e) {
        // if list is null, nothing to do
        if (frameList == null) {
            return;
        }
        // get the name of the menu item
        String label = e.getActionCommand();
        // The act of de/iconifying windows will cause add/remove events,
        // so we need our own copy of the array.
        JInternalFrame[] localFrameList = frameList;

        // see if it's one of the action menu items
        if (label.equals(minimizeLabel)) {
            // minimize all the windows
            for (int i = 0; i < localFrameList.length; i++) {
                try {
                    localFrameList[i].setIcon(true);
                } catch (java.beans.PropertyVetoException pve) {
                    pve.printStackTrace();
                }
            }
        } else if (label.equals(maximizeLabel)) {
            // maximize all the windows
            for (int i = 0; i < localFrameList.length; i++) {
                try {
                    // Have to make sure window is de-iconified before
                    // maximizing will take effect.
                    localFrameList[i].setIcon(false);
                    localFrameList[i].setMaximum(true);
                } catch (java.beans.PropertyVetoException pve) {
                    pve.printStackTrace();
                }
            }
        } else {
            // find the matching window and bring it to the front
            for (int i = 0; i < localFrameList.length; i++) {
                if (label.equals(localFrameList[i].getTitle())) {
                    // setting the window selected brings it forward
                    try {
                        // Have to make sure window is de-iconified so
                        // we can set it selected.
                        localFrameList[i].setIcon(false);
                        localFrameList[i].setSelected(true);
                    } catch (java.beans.PropertyVetoException pve) {
                        pve.printStackTrace();
                    }
                    break;
                }
            }
        }
    } // actionPerformed

    /**
     * This builds the menu items for this menu, using the passed
     * array of internal frames to get the menu item titles.
     */
    protected void buildList(JInternalFrame[] list) {
        // remove all the current "window" menu items
        if (frameList != null) {
            int count = getItemCount() - 1;
            int min = count - frameList.length;
            for (int i = count; i > min; i--) {
                remove(i);
            }
        }
        // save the list for use in actionPerformed()
        frameList = list;
        if (list != null) {
            // add the new menu items, one for each internal frame
            for (int i = 0; i < list.length; i++) {
                JMenuItem mi = new JMenuItem(list[i].getTitle());
                mi.addActionListener(this);
                add(mi);
            }
        }
    } // buildList

    /**
     * Invoked when a component has been added to the container.
     *
     * @param  e container event.
     */
    public void componentAdded(ContainerEvent e) {
        buildList(desktopPane.getAllFrames());
    } // componentAdded

    /**
     * Invoked when a component has been removed from the container.
     *
     * @param  e container event.
     */    
    public void componentRemoved(ContainerEvent e) {
        buildList(desktopPane.getAllFrames());
    } // componentRemoved
} // WindowMenu
