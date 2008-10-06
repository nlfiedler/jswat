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
 * $Id: WindowedViewDesktop.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Class WindowedViewDesktop implements a ViewDesktop using internal
 * frames within a JDesktopPane component. It listens for the frame
 * close events and removes the view from the view manager.
 *
 * @author  Nathan Fiedler
 */
class WindowedViewDesktop implements InternalFrameListener, ViewDesktop {
    /** Preferences instance for retreiving options. */
    private static Preferences preferences;
    /** The desktop pane. */
    private JDesktopPane desktopPane;
    /** Our view manager instance. */
    private ViewManager viewManager;
    /** Table of views. Keys are the JInternalFrame objects, values are
     * the view objects. */
    private Hashtable frameToViewMap;
    /** Table of internal frames. Keys are the View objects, values are
     * the JInternalFrame objects. */
    private Hashtable viewToFrameMap;
    /** Our window menu. */
    private JMenu windowMenu;

    static {
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/graphical");
    }

    /**
     * Constructs a WindowedViewDesktop.
     *
     * @param  vm  view manager.
     */
    public WindowedViewDesktop(ViewManager vm) {
        viewManager = vm;
        desktopPane = new JDesktopPane();
        windowMenu = new WindowMenu(desktopPane);
        frameToViewMap = new Hashtable();
        viewToFrameMap = new Hashtable();
    } // WindowedViewDesktop

    /**
     * Adds the appropriate display component for the given view.
     *
     * @param  view  view to display.
     * @throws  ViewException
     *          if there was a problem displaying the view.
     */
    public void addView(View view) throws ViewException {
        final JInternalFrame iframe = new JInternalFrame(
            view.getTitle(), true, true, true, true);
        iframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        iframe.setBounds(10, 10, 200, 200);
        iframe.setContentPane(view.getUI());
        // Add the window to the default layer of the desktop pane.
        desktopPane.add(iframe, JLayeredPane.DEFAULT_LAYER);
        try {
            iframe.setSelected(true);
            // Maximize the window if the user has so chosen.
            boolean maximize = preferences.getBoolean(
                "maximizeView", Defaults.VIEW_MAXIMIZE);
            iframe.setMaximum(maximize);
        } catch (PropertyVetoException pve) {
            throw new ViewException(pve);
        }
        Runnable runnable = new Runnable() {
                public void run() {
                    iframe.setVisible(true);
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ie) {
                // ignored
            } catch (InvocationTargetException ite) {
                // ignored
            }
        }

        // We want to be notified when the window closes so we
        // can clean up.
        iframe.addInternalFrameListener(this);
        frameToViewMap.put(iframe, view);
        viewToFrameMap.put(view, iframe);
    } // addView

    /**
     * Prepare this view desktop for non-use. Free any allocated
     * resources, empty collections, and set any references to null.
     */
    public void dispose() {
        desktopPane = null;
        viewManager = null;
        Enumeration frames = frameToViewMap.keys();
        while (frames.hasMoreElements()) {
            JInternalFrame iframe = (JInternalFrame) frames.nextElement();
            iframe.removeInternalFrameListener(this);
            iframe.dispose();
        }
        frameToViewMap.clear();
        viewToFrameMap = null;
        frameToViewMap.clear();
        viewToFrameMap = null;
        windowMenu = null;
    } // dispose

    /**
     * Returns the menu for this view desktop, if any. The menu is
     * positioned in the menu structure in place of the @window special
     * menu.
     *
     * @return  window menu, or null if none.
     */
    public JMenu getMenu() {
        return windowMenu;
    } // getMenu

    /**
     * Returns the mode of this view desktop.
     *
     * @return  one of the ViewDesktopFactory <code>MODE_</code> constants.
     */
    public int getMode() {
        return ViewDesktopFactory.MODE_IFRAMES;
    } // getMode

    /**
     * Retreives the currently selected view.
     *
     * @return  selected view, or null if none.
     */
    public View getSelectedView() {
        // Find the currently open and active view, if any.
        JInternalFrame iframe = desktopPane.getSelectedFrame();
        if (iframe == null) {
            return null;
        }
        return (View) frameToViewMap.get(iframe);
    } // getSelectedView

    /**
     * Invoked when an internal frame is activated.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#setSelected
     */
    public void internalFrameActivated(InternalFrameEvent e) { }

    /**
     * An internal frame has been closed. Let's remove it
     * from the hashtables.
     *
     * @param  e  internal frame event.
     */
    public void internalFrameClosed(InternalFrameEvent e) {
        View view = (View) frameToViewMap.remove(e.getSource());
        if (view != null) {
            viewToFrameMap.remove(view);
            viewManager.removeView(view);
        }
    } // internalFrameClosed

    /**
     * Invoked when an internal frame is in the process of being closed.
     * The close operation can be overridden at this point.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#setDefaultCloseOperation
     */
    public void internalFrameClosing(InternalFrameEvent e) { }

    /**
     * Invoked when an internal frame is de-activated.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#setSelected
     */
    public void internalFrameDeactivated(InternalFrameEvent e) { }

    /**
     * Invoked when an internal frame is de-iconified.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#setIcon
     */
    public void internalFrameDeiconified(InternalFrameEvent e) { }

    /**
     * Invoked when an internal frame is iconified.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#setIcon
     */
    public void internalFrameIconified(InternalFrameEvent e) { }

    /**
     * Invoked when a internal frame has been opened.
     *
     * @param  e  internal frame event.
     * @see javax.swing.JInternalFrame#show
     */
    public void internalFrameOpened(InternalFrameEvent e) { }

    /**
     * Retreives the visual component for this desktop.
     *
     * @return  interface component.
     */
    public JComponent getUI() {
        return desktopPane;
    } // getUI

    /**
     * Set the given view as the selected one. This should make the view
     * component visible to the user.
     *
     * @param  view  view to be made active.
     * @throws  ViewException
     *          if a problem occurred.
     */
    public void selectView(View view) throws ViewException {
        JInternalFrame iframe = (JInternalFrame) viewToFrameMap.get(view);
        if (iframe == null) {
            throw new ViewException("view not in this desktop");
        }
        iframe.moveToFront();
        try {
            iframe.setIcon(false);
            iframe.setSelected(true);
        } catch (PropertyVetoException pve) {
            throw new ViewException(pve);
        }
    } // selectView

    /**
     * Called when the preferences have changed. The view desktop may
     * want to update its cached settings based on the changes.
     */
    public void setPreferences() {
    } // setPreferences

    /**
     * This is a specialized menu class that implements the windows
     * menu. This menu will hold a list of titles representing the
     * internal windows contained in the desktop pane. By selecting a
     * menu item you will cause the corresponding window to move to the
     * front.
     *
     * <p>This menu will listen for changes in the desktop pane and
     * update itself as internal frames are added and removed.</p>
     *
     * @author  Nathan Fiedler
     */
    class WindowMenu extends JMenu
        implements ActionListener, ContainerListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** List of internal frames we represent in our menu. */
        private JInternalFrame[] frameList;
        /** Menu item for Minimize All. */
        private JMenuItem minimizeMenuItem;
        /** Menu item for Maximize All. */
        private JMenuItem maximizeMenuItem;
        /** Menu item for List. */
        private JMenuItem listMenuItem;
        /** The desktop pane we are listening to. */
        private JDesktopPane desktopPane;

        /**
         * Constructor for this class, creates the menu and adds some
         * basic window-management functions.
         *
         * @param  pane  desktop pane.
         */
        public WindowMenu(JDesktopPane pane) {
            super(Bundle.getString("windowLabel"), true);
            setMnemonic('W');

            // Build the menu items.
            minimizeMenuItem = createMenuItem("WindowMenu.minimizeAll", 'n');
            maximizeMenuItem = createMenuItem("WindowMenu.maximizeAll", 'x');
            listMenuItem = createMenuItem("WindowMenu.list", 'l');
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
            // If no windows, therer's nothing to do.
            if (frameList == null) {
                return;
            }
            Object src = e.getSource();
            // The act of de/iconifying windows will cause add/remove
            // events, so we need our own copy of the array.
            JInternalFrame[] localFrameList = frameList;

            // see if it's one of the action menu items
            if (src == minimizeMenuItem) {
                // minimize all the windows
                for (int i = 0; i < localFrameList.length; i++) {
                    try {
                        localFrameList[i].setIcon(true);
                    } catch (PropertyVetoException pve) {
                        // yeah, like this will ever happen
                    }
                }

            } else if (src == maximizeMenuItem) {
                // maximize all the windows
                for (int i = 0; i < localFrameList.length; i++) {
                    try {
                        // Have to make sure window is de-iconified before
                        // maximizing will take effect.
                        localFrameList[i].setIcon(false);
                        localFrameList[i].setMaximum(true);
                    } catch (PropertyVetoException pve) {
                        // yeah, like this will ever happen
                    }
                }

            } else if (src == listMenuItem) {
                // display a dialog listing the windows
                Frame topFrame = SessionFrameMapper.getOwningFrame(e);
                JDialog dialog = new WindowListDialog(topFrame);
                dialog.setDefaultCloseOperation(
                    WindowConstants.DISPOSE_ON_CLOSE);
                dialog.setSize(300, 200);
                dialog.setLocationRelativeTo(topFrame);
                dialog.setVisible(true);

            } else {
                // Find the matching window and bring it to the front.
                // The action command is the window name.
                String label = e.getActionCommand();
                raiseWindow(label);
            }
        } // actionPerformed

        /**
         * This builds the menu items for this menu, using the passed
         * array of internal frames to get the menu item titles.
         *
         * @param  list  new list of internal frames.
         */
        protected void buildList(JInternalFrame[] list) {
            // Remove all the current window menu items.
            for (int i = getItemCount() - 1; i > 0; i--) {
                Object o = getMenuComponent(i);
                if (o instanceof JSeparator) {
                    // Stop when we hit the separator.
                    break;
                }
                remove(i);
            }

            // Save the reference to the new list.
            frameList = list;

            if (list != null) {
                // Add the new menu items, one for each internal frame.
                int max = Math.min(list.length, 10);
                for (int i = 0; i < max; i++) {
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

        /**
         * Create the menu item for the given action command.
         *
         * @param  key       key for looking up resources.
         * @param  mnemonic  menu keyboard mnemonic.
         * @return  new menu item.
         */
        protected JMenuItem createMenuItem(String key, char mnemonic) {
            JMenuItem mi = new JMenuItem(Bundle.getString(key + "Label"));
            mi.addActionListener(this);
            mi.setToolTipText("<html><small>"
                              + Bundle.getString(key + "Tooltip")
                              + "</small></html>");
            mi.setMnemonic(mnemonic);
            add(mi);
            return mi;
        } // createMenuItem

        /**
         * Raise the given internal frame, deiconifying if necessary.
         *
         * @param  title  title of the window to raise.
         */
        protected void raiseWindow(String title) {
            // The act of de/iconifying windows will cause add/remove
            // events, so we need our own copy of the array.
            JInternalFrame[] localFrameList = frameList;
            for (int i = 0; i < localFrameList.length; i++) {
                if (title.equals(localFrameList[i].getTitle())) {
                    // setting the window selected brings it forward
                    try {
                        // Have to make sure window is de-iconified so
                        // we can set it selected.
                        localFrameList[i].setIcon(false);
                        localFrameList[i].setSelected(true);
                    } catch (PropertyVetoException pve) {
                        // yeah, like this will ever happen
                    }
                    break;
                }
            }
        } // raiseWindow

        /**
         * Class to display the list of windows.
         *
         * @author  Nathan Fiedler
         */
        protected class WindowListDialog extends JDialog
            implements ListSelectionListener {
            /** silence the compiler warnings */
            private static final long serialVersionUID = 1L;
            /** List showing the window titles. */
            private JList windowList;

            /**
             * Constructs a dialog to list the open windows.
             *
             * @param  parent  parent of this dialog.
             */
            public WindowListDialog(Frame parent) {
                super(parent, Bundle.getString("WindowList.title"));

                Container pane = getContentPane();
                GridBagLayout gbl = new GridBagLayout();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(3, 3, 3, 3);
                pane.setLayout(gbl);

                List titles = new ArrayList();
                for (int ii = 0; ii < frameList.length; ii++) {
                    titles.add(frameList[ii].getTitle());
                }
                Collections.sort(titles);

                windowList = new JList(titles.toArray());
                windowList.addListSelectionListener(this);
                windowList.setBorder(BorderFactory.createEtchedBorder());
                gbc.fill = GridBagConstraints.BOTH;
                gbc.gridheight = GridBagConstraints.RELATIVE;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbl.setConstraints(windowList, gbc);
                pane.add(windowList);

                JButton button = new JButton(Bundle.getString("closeLabel"));
                button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                        }
                    });
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridheight = GridBagConstraints.REMAINDER;
                gbc.weightx = 0.0;
                gbc.weighty = 0.0;
                gbl.setConstraints(button, gbc);
                pane.add(button);
            } // WindowListDialog

            /**
             * Called whenever the value of the selection changes.
             *
             * @param  e  the event that characterizes the change.
             */
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String title = (String) windowList.getSelectedValue();
                    if (title != null) {
                        raiseWindow(title);
                        dispose();
                    }
                }
            } // valueChanged
        } // WindowListDialog
    } // WindowMenu
} // WindowedViewDesktop
