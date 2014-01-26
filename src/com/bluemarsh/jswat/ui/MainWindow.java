/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: MainWindow.java 1883 2005-08-04 06:41:25Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.config.ConfigureListener;
import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.AppSettings;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.action.ActionTable;
import com.bluemarsh.jswat.action.SessionAction;
import com.bluemarsh.jswat.util.StringUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Stack;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * This class implements the main window of the application.
 *
 * @author  Nathan Fiedler
 */
public class MainWindow extends JFrame implements ConfigureListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Suffix added to commnd strings to find images. */
    private static final String IMAGE_SUFFIX = "Image";
    /** Suffix added to commnd strings to find labels. */
    private static final String LABEL_SUFFIX = "Label";
    /** Suffix added to commnd strings to find tooltips. */
    private static final String TIP_SUFFIX = "Tooltip";
    /** Default window width. */
    private static final int DEFAULT_WIDTH = 800;
    /** Default window height. */
    private static final int DEFAULT_HEIGHT = 600;
    /** The menu bar. */
    protected JMenuBar menubar;
    /** The tool bar. */
    protected JToolBar toolbar;

    /**
     * Creates a MainWindow object and puts up the main window.
     * Also creates a window listener to close the program when the
     * close button is activated (using the ExitAction class).
     *
     * @param  title                title for the main window.
     * @param  sessionActionAdapter SessionActionAdapter.
     */
    public MainWindow(String title,
                      SessionActionAdapter sessionActionAdapter) {
        // Call superclass for default behavior.
        super(title);

        getContentPane().setLayout(new MainLayout());

        // Let the ExitAction decide when to close us.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Set the size and position of the main window.
        AppSettings props = AppSettings.instanceOf();
        int width = props.getInteger("windowWidth", DEFAULT_WIDTH);
        int height = props.getInteger("windowHeight", DEFAULT_HEIGHT);
        setSize(width, height);
        int top = props.getInteger("windowTop", 10);
        int left = props.getInteger("windowLeft", 10);
        setLocation(left, top);

        // Register as a configuration change listener.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        config.addListener(this);

        try {
            // Set the icon for the window.
            ImageIcon ii = new ImageIcon(Bundle.getResource("houseflyImage"));
            setIconImage(ii.getImage());

            // Create a menubar and add all the menus to it.
            menubar = createMenubar(sessionActionAdapter);

            // Create a toolbar and add all the buttons to it.
            toolbar = createToolbar(sessionActionAdapter);
        } catch(Exception e) {
            // Catch any and all exceptions. This allows our
            // caller to still invoke "show" on us. That way,
            // even if we failed to build out the UI the user
            // can close the program via the window close button.
            e.printStackTrace();
        }
    } // MainWindow

    /**
     * Specialized layout for tracking the toolbar's placement.
     */
    protected class MainLayout extends BorderLayout {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Adds the component to this layout manager.
         *
         * @param  comp         component.
         * @param  constraints  constraints.
         */
        public void addLayoutComponent(Component comp, Object constraints) {
            if (comp == toolbar) {
                AppSettings props = AppSettings.instanceOf();
                props.setString("toolbarConstaint", (String) constraints);
            }
            super.addLayoutComponent(comp, constraints);
        } // addLayoutComponent
    } // MainLayout

    /**
     * We have been asked to close down this window. Need to do a
     * little clean up and then we will disappear and dispose of
     * ourselves.
     */
    public void close() {
        // We don't need to be a listener anymore.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        config.removeListener(this);
        // Remove the window and go away.
        setVisible(false);
        dispose();
    } // close

    /**
     * Invoked whenever the preferences have changed.
     * We take this opportunity to reset the shortcuts.
     */
    public void configurationChanged() {
        resetShortcuts();
    } // configurationChanged

    /**
     * Create a menu for the app. By default this pulls the
     * definition of the menu from the associated resource file.
     *
     * @param  key  menu key in resource bundle
     * @param  saa  SessionActionAdapter
     * @return  menu built out
     */
    protected JMenu createMenu(String key, SessionActionAdapter saa) {
        String[] itemKeys = StringUtils.tokenize(Bundle.getString(key));
        JMenu menu = new JMenu(Bundle.getString(key + LABEL_SUFFIX));
        int i = 0;
        JConfigure config = JSwat.instanceOf().getJConfigure();
        while (i < itemKeys.length) {
            if (itemKeys[i].equals("-")) {
                // A "-" means insert a separator.
                menu.addSeparator();
            } else if (itemKeys[i].equals(">")) {
                // A ">" signals that the next key is a submenu name.
                menu.add(createMenu(itemKeys[++i], saa));
            } else if (itemKeys[i].startsWith("@")) {
                // Item is a special menu.
                itemKeys[i] = itemKeys[i].substring(1);
                if (itemKeys[i].equals("lookAndFeel")) {
                    // User wants the look & feel menu.
                    menu.add(new LookAndFeelMenu(
                        Bundle.getString("lookAndFeelLabel"), this));
                }
            } else {
                // A normal menu item...
                menu.add(createMenuItem(itemKeys[i], config, saa));
            }
            i++;
        }
        return menu;
    } // createMenu

    /**
     * Create the menubar for the app. By default this pulls the
     * definition of the menu from the associated resource file.
     *
     * @param  saa  SessionActionAdapter
     * @return  menu bar with menus built out
     */
    protected JMenuBar createMenubar(SessionActionAdapter saa) {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = StringUtils.tokenize(Bundle.getString("menubar"));
        for (int i = 0; i < menuKeys.length; i++) {
            JMenu m = createMenu(menuKeys[i], saa);
            if (m != null) {
                mb.add(m);
            }
        }
        return mb;
    } // createMenubar

    /**
     * This is the hook through which all menu items are
     * created. Using the <code>cmd</code> string it finds
     * the menu item label and image (if any) in the resource
     * bundle.
     *
     * @param  cmd     action command string for this menu item;
     *                 used to get the label and image.
     * @param  config  reference to JConfigure for keyboard shortcuts.
     * @param  saa     SessionActionAdapter.
     * @return  new menu item
     * @see #createMenu
     */
    protected JMenuItem createMenuItem(String cmd, JConfigure config,
                                       SessionActionAdapter saa) {
        // Create menu item and set image and text.
        JMenuItem mi = new JMenuItem(
            Bundle.getString(cmd + LABEL_SUFFIX));
        URL url = Bundle.getResource(cmd + IMAGE_SUFFIX);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        // Set menu action command.
        mi.setActionCommand(cmd);
        Action a = ActionTable.getAction(cmd);
        if (a != null) {
            // Set up the action to listen for events.
            mi.addActionListener(a);
            a.addPropertyChangeListener(new ActionChangedListener(mi));
            mi.setEnabled(a.isEnabled());
            if (a instanceof SessionAction) {
                saa.addComponent(mi);
            }
        } else {
            // No action! Disable menu item.
            mi.setEnabled(false);
        }

        // Set keyboard shortcut of the menu item.
        String mnemonic = config.getProperty("keys." + cmd);
        if (mnemonic != null) {
            try {
                if (mnemonic.length() > 0) {
                    mi.setAccelerator(KeyStroke.getKeyStroke(mnemonic));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mi;
    } // createMenuItem

    /**
     * Create the toolbar. By default this reads the 
     * resource file for the definition of the toolbar.
     *
     * @param  saa  SessionActionAdapter
     * @return  new toolbar built out
     */
    protected JToolBar createToolbar(SessionActionAdapter saa) {
        JToolBar toolbar = new JToolBar();
        String[] toolKeys = StringUtils.tokenize(Bundle.getString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                toolbar.add(createToolbarButton(toolKeys[i], saa));
            }
        }
        toolbar.add(Box.createHorizontalStrut(10));
        return toolbar;
    } // createToolbar

    /**
     * Create a button to go inside of the toolbar. By default this
     * will load an image resource. The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     * 
     * @param  key  key in resource bundle for tool
     * @param  saa  SessionActionAdapter
     * @return  new toolbar button
     */
    protected JButton createToolbarButton(String key,
                                          SessionActionAdapter saa) {
        // create the button
        URL url = Bundle.getResource(key + IMAGE_SUFFIX);
        JButton b = url != null ?
            new JButton(new ImageIcon(url)) {
                /** silence the compiler warnings */
                private static final long serialVersionUID = 1L;
                public float getAlignmentY() {
                    return 0.5f;
                }
            }
            : new JButton(key);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        // get action and attach it to button as a listener
        b.setActionCommand(key);
        Action a = ActionTable.getAction(key);
        if (a != null) {
            b.addActionListener(a);
            a.addPropertyChangeListener(new ActionChangedListener(b));
            b.setEnabled(a.isEnabled());
            if (a instanceof SessionAction) {
                saa.addComponent(b);
            }
        } else {
            b.setEnabled(false);
        }

        // attach tooltip to button
        String tip = Bundle.getString(key + TIP_SUFFIX);
        if (tip != null) {
            // Use HTML for multi-line tooltips.
            // The font seems awfully big, so let's shrink it.
            tip = "<html><font size=\"-1\">" + tip + "</font></html>";
            b.setToolTipText(tip);
        }
        return b;
    } // createToolbarButton

    /**
     * Tests if this container contains the given component.
     *
     * @param  child  component to search for.
     * @return  true if the child is a member of this container.
     */
    protected boolean hasComponent(Component child) {
        Container pane = getContentPane();
        Component[] children = pane.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == child) {
                return true;
            }
        }
        return false;
    } // hasComponent

    /**
     * Hides the menu bar if it is not already invisible.
     */
    void hideMenubar() {
        if (getJMenuBar() != null) {
            // Set the menubar to null to remove it.
            setJMenuBar(null);
        }
    } // hideMenubar

    /**
     * Hide the toolbar if it is not already invisible.
     */
    void hideToolbar() {
        if (hasComponent(toolbar)) {
            // Remove the toolbar to the window.
            Container pane = getContentPane();
            pane.remove(toolbar);
        }
    } // hideToolbar

    /**
     * Uses the configured shortcut settings to set all of the
     * menu item accelerators. Called when the user has changed
     * the keyboard shortcuts at runtime.
     */
    protected void resetShortcuts() {
        // Create a stack with the menubar as the initial element.
        Stack stack = new Stack();
        stack.push(menubar);

        // Traverse the menu element tree, setting each menu item's
        // keyboard accelerator using the configured shortcuts.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        while (!stack.empty()) {
            // Get the next menu element from the stack.
            MenuElement elem = (MenuElement) stack.pop();
            // Push all of its subelements onto the stack.
            MenuElement elems[] = elem.getSubElements();
            for (int i = elems.length - 1; i >= 0; i--) {
                stack.push(elems[i]);
            }

            // If this is a menu item, set it's keyboard shortcut.
            if (elem instanceof JMenuItem) {
                JMenuItem mi = (JMenuItem) elem;
                String cmd = mi.getActionCommand();
                String mnemonic = config.getProperty("keys." + cmd);
                if (mnemonic != null) {
                    try {
                        // Work-around for JFC bug: set to null first
                        // before setting to a new value to clear out
                        // the previously set shortcut.
                        mi.setAccelerator(null);
                        if (mnemonic.length() > 0) {
                            mi.setAccelerator(
                                KeyStroke.getKeyStroke(mnemonic));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    } // resetShortcuts

    /**
     * Shows the menu bar if it is not already visible.
     */
    void showMenubar() {
        if (getJMenuBar() == null) {
            // Add the existing menu bar to the window.
            setJMenuBar(menubar);
        }
    } // showMenubar

    /**
     * Show the toolbar if it is not already visible.
     */
    void showToolbar() {
        if (!hasComponent(toolbar)) {
            // Add the toolbar to the window.
            Container pane = getContentPane();
            AppSettings props = AppSettings.instanceOf();
            String constraint = props.getString("toolbarConstaint");
            if ((constraint != null) && (constraint.length() > 0)) {
                pane.add(toolbar, constraint);
                // JToolBar has some bug with the orientation.
                int orientation = toolbar.getOrientation();
                if (constraint.equals(BorderLayout.EAST) ||
                    constraint.equals(BorderLayout.WEST)) {
                    orientation = JToolBar.VERTICAL;
                } else if (constraint.equals(BorderLayout.NORTH) ||
                           constraint.equals(BorderLayout.SOUTH)) {
                    orientation = JToolBar.HORIZONTAL;
                }
                toolbar.setOrientation(orientation);
            } else {
                pane.add(toolbar, BorderLayout.NORTH);
            }
        }
    } // showToolbar

    /**
     * Watches for changes in the actions and deals with them by
     * changing the corresponding menu items or toolbar buttons.
     */
    protected class ActionChangedListener implements PropertyChangeListener {
        /** Component we are associated with. */
        JComponent component;

        /**
         * Constructor for our action change listener.
         *
         * @param  c  component we are to associate with.
         */
        public ActionChangedListener(JComponent c) {
            super();
            component = c;
        } // ActionChangedListener

        /**
         * Handles changes in the action. If the action name
         * changed we change our menu name. If the action changed
         * it's enabled state, we change our component's state.
         *
         * @param  e  property change event
         */
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                if (component instanceof JMenuItem) {
                    String text = (String) e.getNewValue();
                    ((JMenuItem) component).setText(text);
                }
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                component.setEnabled(enabledState.booleanValue());
            }
        } // propertyChange
    } // ActionChangedListener
} // MainWindow

/**
 * Specialized menu class that implements the look & feel menu for
 * this program. It automatically builds out the menu for selecting
 * the available look and feels.
 * <p>
 * This is one of the available special menus. It is requested in
 * the resources file using the "@lookAndFeel" special menu tag.
 *
 * @author  Nathan Fiedler
 */
class LookAndFeelMenu extends JMenu implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Parent window, used to set the busy cursor. */
    protected Frame win;
    /** Array of LookAndFeel information objects. */
    protected UIManager.LookAndFeelInfo[] lafInfo;

    /**
     * One-arg constructor which creates new look & feel menu.
     *
     * @param  name  title for this menu
     * @param  win  parent window for displaying dialogs
     */
    public LookAndFeelMenu(String name, Frame win) {
        super(name, true);
        this.win = win;

        // Create menu items for changing Look & Feel.
        ButtonGroup group = new ButtonGroup();
        lafInfo = UIManager.getInstalledLookAndFeels();
        String curLAFName = UIManager.getLookAndFeel().getName();
        for (int i = 0; i < lafInfo.length; i++) {
            String lafName = lafInfo[i].getName();
            JRadioButtonMenuItem lafMenuItem =
                new JRadioButtonMenuItem(lafName);
            if (curLAFName.equals(lafName)) {
                lafMenuItem.setSelected(true);
            }
            add(lafMenuItem);
            group.add(lafMenuItem);
            lafMenuItem.addItemListener(this);
        }
    } // LookAndFeelMenu

    /**
     * One of the look & feels was selected. See which one it was
     * and switch the entire user interface to that look & feel.
     *
     * @param  e  Indicates which item was selected.
     */
    public void itemStateChanged(ItemEvent e) {
        JRadioButtonMenuItem rb = (JRadioButtonMenuItem)e.getSource();
        if (rb.isSelected()) {
            win.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                for (int i = 0; i < lafInfo.length; i++) {
                    if (rb.getText().equals(lafInfo[i].getName())) {
                        // Switch to the selected look & feel.
                        UIManager.setLookAndFeel(
                            lafInfo[i].getClassName());
                        // Save the setting in the preferences.
                        AppSettings props = AppSettings.instanceOf();
                        props.setString("lookAndFeel",
                                        lafInfo[i].getClassName());
                        break;
                    }
                }
                SwingUtilities.updateComponentTreeUI(win);
            } catch (Exception exc) {
                rb.setEnabled(false);
                System.err.print("Error loading look & feel \"");
                System.err.print(rb.getText());
                System.err.print("\": ");
                System.err.println(exc);
            }
            win.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    } // itemStateChanged
} // LookAndFeelMenu
