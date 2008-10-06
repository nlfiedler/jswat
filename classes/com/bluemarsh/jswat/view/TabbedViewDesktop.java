/*********************************************************************
 *
 *      Copyright (C) 2003-2005 Nathan Fiedler
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
 * $Id: TabbedViewDesktop.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * Class TabbedViewDesktop implements a ViewDesktop using a tabbed pane
 * component.
 *
 * @author  Nathan Fiedler
 */
class TabbedViewDesktop implements ViewDesktop {
    /** The preferences node. */
    private static Preferences preferences;
    /** The tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Panel to provide the desired background color. */
    private JPanel backgroundPanel;
    /** Our view manager instance. */
    private ViewManager viewManager;
    /** Map of View objects to Component objects. */
    private Hashtable viewToComponentMap;
    /** Map of Component objects to View objects. */
    private Hashtable componentToViewMap;

    static {
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/graphical");
    }

    /**
     * Constructs a TabbedViewDesktop.
     *
     * @param  vm  view manager.
     */
    public TabbedViewDesktop(ViewManager vm) {
        viewManager = vm;
        tabbedPane = new JTabbedPane();
        setPreferences();

        backgroundPanel = new BackgroundPanel();
        // Don't add the panel just yet, or it completely covers our
        // beautiful background color.
        viewToComponentMap = new Hashtable();
        componentToViewMap = new Hashtable();

        // Add the popup menu for the tabbed pane.
        TabbedPopup popupMenu = new TabbedPopup();
        tabbedPane.addMouseListener(popupMenu);
    } // TabbedViewDesktop

    /**
     * Adds the appropriate display component for the given view.
     *
     * @param  view  view to display.
     * @throws  ViewException
     *          if there was a problem displaying the view.
     */
    public void addView(View view) throws ViewException {
        if (viewToComponentMap.size() == 0) {
            // Add the tabbed pane to our panel so we can start showing
            // views.
            backgroundPanel.add(tabbedPane);
        }
        Component comp = view.getUI();
        viewToComponentMap.put(view, comp);
        componentToViewMap.put(comp, view);
        tabbedPane.addTab(view.getTitle(), null, comp, view.getLongTitle());
        tabbedPane.setSelectedComponent(comp);
    } // addView

    /**
     * Prepare this view desktop for non-use. Free any allocated
     * resources, empty collections, and set any references to null.
     */
    public void dispose() {
        tabbedPane.removeAll();
        tabbedPane = null;
        backgroundPanel = null;
        viewManager = null;
        viewToComponentMap.clear();
        viewToComponentMap = null;
        componentToViewMap.clear();
        componentToViewMap = null;
    } // dispose

    /**
     * Returns the menu for this view desktop, if any. The menu is
     * positioned in the menu structure in place of the @window special
     * menu.
     *
     * @return  window menu, or null if none.
     */
    public JMenu getMenu() {
        return null;
    } // getMenu

    /**
     * Returns the mode of this view desktop.
     *
     * @return  one of the ViewDesktopFactory <code>MODE_</code> constants.
     */
    public int getMode() {
        return ViewDesktopFactory.MODE_TABBED;
    } // getMode

    /**
     * Retreives the currently selected view.
     *
     * @return  selected view, or null if none.
     */
    public View getSelectedView() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp != null) {
            return (View) componentToViewMap.get(comp);
        } else {
            return null;
        }
    } // getSelectedView

    /**
     * Retreives the visual component for this desktop.
     *
     * @return  interface component.
     */
    public JComponent getUI() {
        return backgroundPanel;
    } // getUI

    /**
     * Removes the given view from the desktop, as well as from the view
     * manager.
     *
     * @param  view  view to remove.
     */
    protected void removeView(View view) {
        Component comp = (Component) viewToComponentMap.remove(view);
        componentToViewMap.remove(comp);
        tabbedPane.remove(comp);
        viewManager.removeView(view);
        if (viewToComponentMap.size() == 0) {
            // Remove the tabbed pane from our panel so we can see the
            // pretty background. Need to force a repaint, too.
            backgroundPanel.remove(tabbedPane);
            backgroundPanel.repaint();
        }
    } // removeView

    /**
     * Set the given view as the selected one. This should make the view
     * component visible to the user.
     *
     * @param  view  view to be made active.
     * @throws  ViewException
     *          if a problem occurred.
     */
    public void selectView(View view) throws ViewException {
        tabbedPane.setSelectedComponent(view.getUI());
    } // selectView

    /**
     * Called when the preferences have changed. The view desktop may
     * want to update its cached settings based on the changes.
     */
    public void setPreferences() {
        if (preferences.getBoolean("oneRowTabs",
                                   Defaults.VIEW_SINGLE_ROW_TABS)) {
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } else {
            tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        }
    } // setPreferences

    /**
     * Class BackgroundPanel provides a nice looking background color
     * for our tabbed pane.
     *
     * @author  Nathan Fiedler
     */
    protected class BackgroundPanel extends JPanel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a BackgroundPanel instance that uses a GridLayout
         * as its layout manager.
         */
        public BackgroundPanel() {
            // Use a grid layout to stretch the tabbed pane to fit the panel.
            super(new GridLayout(1, 1));
            setBackground(UIManager.getColor("Desktop.background"));
        } // BackgroundPanel

        /**
         * Called when the look and feel is changing.
         */
        public void updateUI() {
            super.updateUI();
            setBackground(UIManager.getColor("Desktop.background"));
            if (getComponentCount() == 0) {
                // Update the disconnected tabbed pane.
                tabbedPane.updateUI();
            }
        } // updateUI
    } // BackgroundPanel

    /**
     * Class TabbedPopup defines a popup menu that works specifically
     * for the tabbed pane view desktop.
     *
     * @author  Nathan Fiedler
     */
    protected class TabbedPopup extends SmartPopupMenu
        implements ActionListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Suffix added to command string to retrieve menu labels. */
        private static final String LABEL_SUFFIX = "Label";
        /** Command name for close action. */
        private static final String CMD_CLOSE = "close";
        /** Command name for close all action. */
        private static final String CMD_CLOSE_ALL = "closeAll";
        /** Command name for close all others action. */
        private static final String CMD_CLOSE_OTHERS = "closeOthers";
        /** Index of the tab the mouse was clicked on. */
        private int selectedTabIndex;
        /** Menu item to close one view. */
        private JMenuItem closeMenuItem;
        /** Menu item to close other views. */
        private JMenuItem closeOthersMenuItem;

        /**
         * Create a TabbedPopup.
         */
        public TabbedPopup() {
            super(Bundle.getString("TabbedPopup.title"));
            closeMenuItem = createMenuItem(CMD_CLOSE);
            add(closeMenuItem);
            add(createMenuItem(CMD_CLOSE_ALL));
            closeOthersMenuItem = createMenuItem(CMD_CLOSE_OTHERS);
            add(closeOthersMenuItem);
        } // TabbedPopup

        /**
         * One of the menu items we're listening to was activated.
         *
         * @param  ae  action event.
         */
        public void actionPerformed(ActionEvent ae) {
            // Get the source of the event (it is a JMenuItem).
            JMenuItem menuItem = (JMenuItem) ae.getSource();
            // Get the action command.
            String cmd = menuItem.getActionCommand();

            if (cmd.equals(CMD_CLOSE)) {
                // Close just one view.
                Component comp = tabbedPane.getComponentAt(selectedTabIndex);
                removeView((View) componentToViewMap.get(comp));
            } else if (cmd.equals(CMD_CLOSE_ALL)) {
                // Close all of the views.
                for (int ii = tabbedPane.getTabCount() - 1; ii >= 0; ii--) {
                    Component comp = tabbedPane.getComponentAt(ii);
                    removeView((View) componentToViewMap.get(comp));
                }
            } else if (cmd.equals(CMD_CLOSE_OTHERS)) {
                // Close all of the views but the selected.
                Component selected = tabbedPane.getComponentAt(
                    selectedTabIndex);
                for (int ii = tabbedPane.getTabCount() - 1; ii >= 0; ii--) {
                    Component comp = tabbedPane.getComponentAt(ii);
                    if (!comp.equals(selected)) {
                        removeView((View) componentToViewMap.get(comp));
                    }
                }
            }
        } // actionPerformed

        /**
         * This is the hook through which all menu items are created. Using
         * the <code>cmd</code> string it finds the menu item label in the
         * resource bundle.
         *
         * @param  cmd  name for this menu item, used to get the label
         * @return  new menu item
         */
        protected JMenuItem createMenuItem(String cmd) {
            // Create menu item and set the text label.
            JMenuItem mi = new JMenuItem(Bundle.getString(cmd + LABEL_SUFFIX));
            // Set menu action command.
            mi.setActionCommand(cmd);
            // Set up the action to listen for events.
            mi.addActionListener(this);
            return mi;
        } // createMenuItem

        /**
         * Set the popup menu items enabled or disabled depending on
         * whether the mouse is over a tab or not.
         *
         * @param  e  mouse event.
         */
        protected void setMenuItemsForEvent(MouseEvent e) {
            selectedTabIndex = tabbedPane.indexAtLocation(e.getX(), e.getY());
            boolean enable = selectedTabIndex > -1;
            closeMenuItem.setEnabled(enable);
            closeOthersMenuItem.setEnabled(enable);
        } // setMenuItemsForEvent

        /**
         * Determine which line the user clicked on and find any breakpoints
         * at that line. If none found, show the "Add breakpoint" menu. If
         * there's a breakpoint, show a popup that provides breakpoint
         * management features.
         *
         * @param  evt  mouse event.
         */
        protected void showPopup(MouseEvent evt) {
            setMenuItemsForEvent(evt);
            show(evt.getComponent(), evt.getX(), evt.getY());
        } // showPopup
    } // TabbedPopup
} // TabbedViewDesktop
