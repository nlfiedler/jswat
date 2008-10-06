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
 * $Id: MainWindow.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.action.ActionTable;
import com.bluemarsh.jswat.action.SessionAction;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.SessionActionAdapter;
import com.bluemarsh.jswat.util.Strings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Stack;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.GrayFilter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.WindowConstants;

/**
 * This class implements the main window of the application.
 *
 * @author  Nathan Fiedler
 */
public class MainWindow extends JFrame implements PreferenceChangeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Suffix added to command strings to find menu images. */
    private static final String MENU_IMAGE_SUFFIX = "MenuImage";
    /** Suffix added to command strings to find toolbar images. */
    private static final String TOOLBAR_IMAGE_SUFFIX = "ToolbarImage";
    /** Suffix added to command strings to small toolbar images. */
    private static final String SMALLBAR_IMAGE_SUFFIX = "MenuImage";
    /** Suffix added to command strings to find labels. */
    private static final String LABEL_SUFFIX = "Label";
    /** Suffix added to find menu mnemonics. */
    private static final String MNEMONIC_SUFFIX = "Mnemonic";
    /** Suffix added to command strings to find tooltips. */
    private static final String TIP_SUFFIX = "Tooltip";
    /** Code for adding a special menu. */
    private static final int MENU_ADD = 1;
    /** Code for removing a special menu. */
    private static final int MENU_REMOVE = 2;
    /** Default window width. */
    private static final int DEFAULT_WIDTH = 800;
    /** Default window height. */
    private static final int DEFAULT_HEIGHT = 600;
    /** The menu bar. */
    private JMenuBar theMenubar;
    /** The tool bar. */
    private JToolBar theToolbar;
    /** User preferences for this package. */
    private Preferences preferences;
    /** The content pane where the toolbar goes, along with most
     * everything else. */
    private JPanel innerContentPane;

    /**
     * Creates a MainWindow object and puts up the main window.
     * Also creates a window listener to close the program when the
     * close button is activated (using the ExitAction class).
     *
     * @param  title  title for the main window.
     * @param  saa    the SessionActionAdapter instance.
     */
    public MainWindow(String title, SessionActionAdapter saa) {
        // Call superclass for default behavior.
        super(title);

        innerContentPane = new JPanel(new MainLayout());
        getContentPane().add(innerContentPane, BorderLayout.CENTER);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Register as a preference change listener.
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/graphical");

        // Compute the window position, default to centered.
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxBounds = ge.getMaximumWindowBounds();
        int width = Math.min(DEFAULT_WIDTH, maxBounds.width);
        int height = Math.min(DEFAULT_HEIGHT, maxBounds.height);
        width = preferences.getInt("windowWidth", width);
        height = preferences.getInt("windowHeight", height);
        setSize(width, height);

        Point centerPt = ge.getCenterPoint();
        int xpos = ((int) centerPt.getX()) - width / 2;
        int ypos = ((int) centerPt.getY()) - height / 2;
        xpos = preferences.getInt("windowLeft", xpos);
        ypos = preferences.getInt("windowTop", ypos);
        setLocation(xpos, ypos);

        // Set the icon for the window.
        ImageIcon ii = new ImageIcon(Bundle.getResource("houseflyImage"));
        setIconImage(ii.getImage());

        // Create a menubar and add all the menus to it.
        theMenubar = createMenubar(saa);
        setJMenuBar(theMenubar);

        // Create a toolbar and add all the buttons to it.
        theToolbar = createToolbar(saa);

        // Become a listener after the menu has been built out.
        preferences.addPreferenceChangeListener(this);
    } // MainWindow

    /**
     * Adds the named special menu to the menu structure.
     *
     * @param  name  codename of the menu.
     * @param  menu  the special menu.
     */
    protected void addSpecialMenu(String name, JMenu menu) {
        mangleMenu(theMenubar, "menubar", '@' + name, menu, MENU_ADD);
    } // addSpecialMenu

    /**
     * Assign the appropriate shortcut to the given menu item.
     *
     * @param  key  key for finding shortcut definition.
     * @param  mi   menu item receiving shortcut.
     */
    protected void assignShortcut(String key, JMenuItem mi) {
        // Work-around for JFC bug: set to null first before setting to a
        // new value to clear out the previously set shortcut.
        mi.setAccelerator(null);
        key = "keys." + key;
        String accelerator = preferences.get(
            key, (String) Defaults.KEYBOARD_SHORTS.get(key));
        KeyStroke stroke = KeyStroke.getKeyStroke(accelerator);
        if (stroke != null) {
            mi.setAccelerator(stroke);
        }
    } // assignShortcut

    /**
     * Look up the tooltip and shortcut key for the named action and assign
     * those as the tooltip of the component.
     *
     * @param  key   the action command for the button.
     * @param  comp  component to which to assign tooltip.
     */
    protected void assignTooltip(String key, JComponent comp) {
        String tip = Bundle.getString(key + TIP_SUFFIX, true);
        if (tip == null || tip.length() == 0) {
            // Do nothing in this case, as we want to either leave the
            // custom tooltips in place, and would prefer not to display
            // anything when there are no tooltips.
            return;
        }
        String cmd = "keys." + key;
        String accelerator = preferences.get(
            cmd, (String) Defaults.KEYBOARD_SHORTS.get(cmd));
        if (accelerator != null) {
            tip = tip + " [" + accelerator + ']';
        }
        if (tip != null) {
            // Use HTML for multi-line tooltips.
            // The font seems awfully big, so let's shrink it.
            tip = "<html><small>" + tip + "</small></html>";
            comp.setToolTipText(tip);
        }
    } // assignTooltip

    /**
     * We have been asked to close down this window. Need to do a
     * little clean up and then we will disappear and dispose of
     * ourselves.
     */
    public void close() {
        // We don't need to be a listener anymore.
        preferences.removePreferenceChangeListener(this);
        // Remove the window and go away.
        if (isShowing()) {
            dispose();
        }
    } // close

    /**
     * Create a menu for the app. By default this pulls the
     * definition of the menu from the associated resource file.
     *
     * @param  key  menu key in resource bundle
     * @param  saa  the SessionActionAdapter instance.
     * @return  menu built out.
     */
    protected JMenu createMenu(String key, SessionActionAdapter saa) {
        // Create the menu.
        JMenu menu = new JMenu(Bundle.getString(key + LABEL_SUFFIX));
        URL url = Bundle.getResource(key + MENU_IMAGE_SUFFIX);
        if (url != null) {
            menu.setHorizontalTextPosition(JButton.RIGHT);
            menu.setIcon(new ImageIcon(url));
        }

        String mnemonic = Bundle.getString(key + MNEMONIC_SUFFIX);
        if (mnemonic != null && mnemonic.length() > 0) {
            menu.setMnemonic(mnemonic.charAt(0));
        }

        // Scan for menu items for this menu.
        int i = 0;
        String[] itemKeys = Strings.tokenize(Bundle.getString(key));
        while (i < itemKeys.length) {
            if (itemKeys[i].equals("-")) {
                // A "-" means insert a separator.
                menu.addSeparator();
            } else if (itemKeys[i].equals(">")) {
                // A ">" signals that the next key is a submenu name.
                menu.add(createMenu(itemKeys[++i], saa));
            } else if (itemKeys[i].startsWith("@")) {
                // Item is a special menu.
                String name = itemKeys[i].substring(1);
                try {
                    JMenu smenu = SpecialMenuTable.getMenu(name);
                    if (smenu != null) {
                        menu.add(smenu);
                        url = Bundle.getResource("clearImage");
                        if (url != null) {
                            smenu.setHorizontalTextPosition(JButton.RIGHT);
                            smenu.setIcon(new ImageIcon(url));
                        }
                    }
                } catch (Exception e) {
                    // In that highly unlikely case we don't add the menu.
                    // It will be pretty obvious if this happens.
                }
            } else {
                // A normal menu item.
                menu.add(createMenuItem(itemKeys[i], saa));
            }
            i++;
        }
        return menu;
    } // createMenu

    /**
     * Create the menubar for the app. By default this pulls the
     * definition of the menu from the associated resource file.
     *
     * @param  saa  the SessionActionAdapter instance.
     * @return  menu bar with menus built out
     */
    protected JMenuBar createMenubar(SessionActionAdapter saa) {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = Strings.tokenize(Bundle.getString("menubar"));
        for (int i = 0; i < menuKeys.length; i++) {
            String name = menuKeys[i];
            if (name.startsWith("@")) {
                // Item is a special menu.
                name = name.substring(1);
                try {
                    JMenu smenu = SpecialMenuTable.getMenu(name);
                    if (smenu != null) {
                        mb.add(smenu);
                    }
                } catch (Exception e) {
                    // In that highly unlikely case we don't add the menu.
                    // It will be pretty obvious if this happens.
                }
            } else {
                mb.add(createMenu(name, saa));
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
     * @param  cmd  action command string for this menu item;
     *              used to get the label and image.
     * @param  saa  the SessionActionAdapter instance.
     * @return  new menu item
     * @see #createMenu
     */
    protected JMenuItem createMenuItem(String cmd, SessionActionAdapter saa) {
        // Create menu item and set image and text.
        JMenuItem mi = new JMenuItem(
            Bundle.getString(cmd + LABEL_SUFFIX));
        URL url = Bundle.getResource(cmd + MENU_IMAGE_SUFFIX);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }

        String mnemonic = Bundle.getString(cmd + MNEMONIC_SUFFIX);
        if (mnemonic != null && mnemonic.length() > 0) {
            mi.setMnemonic(mnemonic.charAt(0));
        }

        // Set menu action command.
        mi.setActionCommand(cmd);
        Action a = null;
        try {
            a = ActionTable.getAction(cmd);
        } catch (Exception e) {
            // ignored
        }
        if (a != null) {
            // Set up the action to listen for events.
            mi.addActionListener(a);
            a.addPropertyChangeListener(new ActionChangedListener(mi));
            mi.setEnabled(a.isEnabled());
            if (a instanceof SessionAction) {
                saa.addComponent((SessionAction) a, mi);
            }
        } else {
            // No action! Disable menu item.
            mi.setEnabled(false);
        }

        // Set keyboard shortcut of the menu item.
        assignShortcut(cmd, mi);
        assignTooltip(cmd, mi);
        return mi;
    } // createMenuItem

    /**
     * Create the toolbar. By default this reads the resource file
     * for the definition of the toolbar.
     *
     * @param  saa  the SessionActionAdapter instance.
     * @return  new toolbar built out
     */
    protected JToolBar createToolbar(SessionActionAdapter saa) {
        JToolBar toolbar = new JToolBar();
        String[] toolKeys = Strings.tokenize(Bundle.getString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.addSeparator();
            } else {
                toolbar.add(createToolbarButton(toolKeys[i], saa));
            }
        }
        toolbar.setRollover(true);
        return toolbar;
    } // createToolbar

    /**
     * Create a button to go inside of the toolbar. By default this
     * will load an image resource. The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param  key  key in resource bundle for tool
     * @param  saa  the SessionActionAdapter instance.
     * @return  new toolbar button
     */
    protected JButton createToolbarButton(String key,
                                          SessionActionAdapter saa) {
        // create the button
        Icon icon = getToolbarIcon(key);
        JButton b = icon == null ? new JButton(key) : new JButton(icon);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        // get action and attach it to button as a listener
        b.setActionCommand(key);
        Action a = null;
        try {
            a = ActionTable.getAction(key);
        } catch (Exception e) {
            // ignored
        }
        if (a != null) {
            b.addActionListener(a);
            a.addPropertyChangeListener(new ActionChangedListener(b));
            b.setEnabled(a.isEnabled());
            if (a instanceof SessionAction) {
                saa.addComponent((SessionAction) a, b);
            }
        } else {
            b.setEnabled(false);
        }

        // Attach the tooltip to button.
        assignTooltip(key, b);
        return b;
    } // createToolbarButton

    /**
     * Retrieves the inner content pane, to which the primary window
     * contents should be added as the <code>BorderLayout.CENTER</code>
     * component only. The content pane of the window is where other
     * components may be added (also managed by a
     * <code>BorderLayout</code>).
     *
     * @return  inner content pane.
     */
    public Container getInnerPane() {
        return innerContentPane;
    } // getInnerPane

    /**
     * Create the icon for the toolbar button with the given action
     * command.
     *
     * @param  cmd  action command string.
     * @return  image icon for button.
     */
    protected ImageIcon getToolbarIcon(String cmd) {
        URL url;
        if (preferences.getBoolean("smallToolbarButtons",
                                   Defaults.SMALL_TOOLBAR_BUTTONS)) {
            url = Bundle.getResource(cmd + SMALLBAR_IMAGE_SUFFIX);
        } else {
            url = Bundle.getResource(cmd + TOOLBAR_IMAGE_SUFFIX);
        }
        return url == null ? null : new ImageIcon(url);
    } // getToolbarIcon

    /**
     * Tests if this container contains the given component.
     *
     * @param  child  component to search for.
     * @return  true if the child is a member of this container.
     */
    protected boolean hasComponent(Component child) {
        Component[] children = innerContentPane.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] == child) {
                return true;
            }
        }
        return false;
    } // hasComponent

    /**
     * Hide the toolbar if it is not already invisible.
     */
    void hideToolbar() {
        if (hasComponent(theToolbar)) {
            // Remove the toolbar to the window.
            innerContentPane.remove(theToolbar);
        }
    } // hideToolbar

    /**
     * Traverses the menu structure, adding, removing, or replacing a
     * special menu in the process.
     *
     * @param  parent  parent component, likely a menu.
     * @param  key     menu name, from properties bundle.
     * @param  name    codename of the menu to find (with '@' prefix).
     * @param  menu    the special menu to add, remove, replace.
     * @param  oper    operation to perform (one of MENU_ constants)
     */
    protected void mangleMenu(Container parent, String key, String name,
                              JMenu menu, int oper) {
        Component[] items = parent.getComponents();
        String[] keys = Strings.tokenize(Bundle.getString(key));
        int ii = 0;
        int ki = 0;

        while (ki < keys.length && ii < items.length) {
            key = keys[ki];
            if (key.equals(name)) {
                if (oper == MENU_ADD) {
                    parent.add(menu, ii);
                } else if (oper == MENU_REMOVE) {
                    parent.remove(ii);
                }
                break;
            } else if (key.equals(">")) {
                // A ">" signals that the next key is a submenu name.
                key = keys[++ki];
            }

            if (items[ii] instanceof JMenu) {
                mangleMenu((Container) items[ii], key, name, menu, oper);
            }
            ii++;
            ki++;
        }

        // Catch the odd case of an empty menu.
        if (ii == 0 && ki < keys.length && keys[ki].equals(name)) {
            parent.add(menu);
        }
    } // mangleMenu

    /**
     * This method gets called when a preference is added, removed or
     * when its value is changed.
     *
     * @param  evt  A PreferenceChangeEvent object describing the event
     *              source and the preference that has changed.
     */
    public void preferenceChange(PreferenceChangeEvent evt) {
        resetShortcuts();
    } // preferenceChange

    /**
     * Removes the named special menu from the menu structure.
     *
     * @param  name  codename of the menu.
     * @param  menu  the special menu.
     */
    protected void removeSpecialMenu(String name, JMenu menu) {
        mangleMenu(theMenubar, "menubar", '@' + name, menu, MENU_REMOVE);
    } // removeSpecialMenu

    /**
     * Uses the configured shortcut settings to set all of the
     * menu item accelerators. Called when the user has changed
     * the keyboard shortcuts at runtime.
     */
    protected void resetShortcuts() {
        // Create a stack with the menubar as the initial element.
        Stack stack = new Stack();
        stack.push(theMenubar);

        // Traverse the menu element tree, setting each menu item's
        // keyboard accelerator using the configured shortcuts.
        while (!stack.empty()) {
            // Get the next menu element from the stack.
            MenuElement elem = (MenuElement) stack.pop();
            // Push all of its subelements onto the stack.
            MenuElement[] elems = elem.getSubElements();
            for (int i = elems.length - 1; i >= 0; i--) {
                stack.push(elems[i]);
            }

            // If this is a menu item, and not a menu, set it's keyboard
            // shortcut.
            if (elem instanceof JMenuItem && !(elem instanceof JMenu)) {
                JMenuItem mi = (JMenuItem) elem;
                String key = mi.getActionCommand();
                assignShortcut(key, mi);
                assignTooltip(key, mi);
            }
        }

        // Have to update the tooltips of the toolbar buttons.
        int tbCount = theToolbar.getComponentCount();
        for (int ii = 0; ii < tbCount; ii++) {
            Object o = theToolbar.getComponentAtIndex(ii);
            if (o instanceof JButton) {
                JButton b = (JButton) o;
                String key = b.getActionCommand();
                assignTooltip(key, b);
            }
        }
    } // resetShortcuts

    /**
     * Show the toolbar if it is not already visible. Additionally, the
     * size of the toolbar buttons is also set here.
     */
    void showToolbar() {
        // Set the size of the toolbar buttons.
        int count = theToolbar.getComponentCount();
        for (int ii = 0; ii < count; ii++) {
            Object o = theToolbar.getComponent(ii);
            if (o instanceof JButton) {
                JButton button = (JButton) o;
                ImageIcon icon = getToolbarIcon(button.getActionCommand());
                if (icon != null) {
                    ImageIcon disabledIcon = new ImageIcon(
                        GrayFilter.createDisabledImage(icon.getImage()));
                    button.setIcon(icon);
                    button.setDisabledIcon(disabledIcon);
                }
            }
        }

        if (!hasComponent(theToolbar)) {
            // Add the toolbar to the window.
            String constraint = preferences.get("toolbarConstaint", null);
            if (constraint != null && constraint.length() > 0) {
                innerContentPane.add(theToolbar, constraint);
                // JToolBar has some bug with the orientation.
                int orientation = theToolbar.getOrientation();
                if (constraint.equals(BorderLayout.EAST)
                    || constraint.equals(BorderLayout.WEST)) {
                    orientation = JToolBar.VERTICAL;
                } else if (constraint.equals(BorderLayout.NORTH)
                           || constraint.equals(BorderLayout.SOUTH)) {
                    orientation = JToolBar.HORIZONTAL;
                }
                theToolbar.setOrientation(orientation);
            } else {
                innerContentPane.add(theToolbar, BorderLayout.NORTH);
            }
        }
    } // showToolbar

    /**
     * Watches for changes in the actions and deals with them by
     * changing the corresponding menu items or toolbar buttons.
     */
    protected class ActionChangedListener implements PropertyChangeListener {
        /** Component we are associated with. */
        private JComponent component;

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
            if (comp == theToolbar) {
                preferences.put("toolbarConstaint", (String) constraints);
            }
            super.addLayoutComponent(comp, constraints);
        } // addLayoutComponent
    } // MainLayout
} // MainWindow
