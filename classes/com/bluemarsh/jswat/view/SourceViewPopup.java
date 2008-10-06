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
 * $Id: SourceViewPopup.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LocatableBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.SourceNameBreakpoint;
import com.bluemarsh.jswat.breakpoint.ui.EditorDialog;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * Class SourceViewPopup defines a popup menu that works specifically for
 * the source view. It constructs a context-sensitive menu based on the
 * existence of breakpoints at the given line of the source view. Like any
 * other popup menu, you must add this popup as a child to the view area in
 * question. It also must be added as a mouse listener to the view area
 * component.
 *
 * @author  Nathan Fiedler
 */
public class SourceViewPopup extends SmartPopupMenu implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name for add breakpoint menu item. */
    private static final String ADD_STRING = "addBreakpoint";
    /** Name for runto breakpoint menu item. */
    private static final String RUNTO_STRING = "runtoBreakpoint";
    /** Name for properties breakpoint menu item. */
    private static final String PROPS_STRING = "propsBreakpoint";
    /** Name for remove breakpoint menu item. */
    private static final String REMOVE_STRING = "removeBreakpoint";
    /** Name for enable breakpoint menu item. */
    private static final String ENABLE_STRING = "enableBreakpoint";
    /** Name for disable breakpoint menu item. */
    private static final String DISABLE_STRING = "disableBreakpoint";
    /** Name for "bad line number" menu item. */
    private static final String BAD_LINE_STRING = "badLine";
    /** Name for launch editor menu item. */
    protected static final String EDITOR_STRING = "launchEditor";
    /** Suffix added to command string to retrieve menu labels. */
    protected static final String LABEL_SUFFIX = "Label";
    /** Hashtable of menu items keyed by their names (eg. 'add'). */
    protected Hashtable menuItemTable;
    /** If non-zero, the line the user last right-clicked on. */
    protected int lastClickedLine;
    /** Source code object. */
    protected SourceSource sourceSrc;
    /** If non-null, breakpoint user last right-clicked on. */
    private Breakpoint lastClickedBreakpoint;

    /**
     * Create a SourceViewPopup.
     *
     * @param  src  source object.
     */
    public SourceViewPopup(SourceSource src) {
        super(Bundle.getString("SourceViewPopup.title"));
        sourceSrc = src;
        menuItemTable = new Hashtable();
        menuItemTable.put(EDITOR_STRING, createMenuItem(EDITOR_STRING));
        menuItemTable.put(ADD_STRING, createMenuItem(ADD_STRING));
        menuItemTable.put(RUNTO_STRING, createMenuItem(RUNTO_STRING));
        menuItemTable.put(PROPS_STRING, createMenuItem(PROPS_STRING));
        menuItemTable.put(REMOVE_STRING, createMenuItem(REMOVE_STRING));
        menuItemTable.put(ENABLE_STRING, createMenuItem(ENABLE_STRING));
        menuItemTable.put(DISABLE_STRING, createMenuItem(DISABLE_STRING));
        menuItemTable.put(BAD_LINE_STRING,
                          new JMenuItem(Bundle.getString(
                                            "SourceViewPopup.badLineNumMsg")));
        add((JMenuItem) menuItemTable.get(EDITOR_STRING));
    } // SourceViewPopup

    /**
     * One of the menu items we're listening to was activated.
     *
     * @param  ae  action event.
     */
    public void actionPerformed(ActionEvent ae) {
        // Get the source of the event (it is a JMenuItem).
        JMenuItem menuItem = (JMenuItem) ae.getSource();
        Session session = SessionFrameMapper.getSessionForEvent(ae);
        BreakpointManager bpman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        // Get the action command.
        String cmd = menuItem.getActionCommand();

        // Decide what to do.
        if (cmd.equals(EDITOR_STRING)) {
            // Launch the external source editor.
            String sourcePath = sourceSrc.getPath();
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            String command = prefs.get("extSourceEditor",
                                       Defaults.SOURCE_EDITOR);
            if (command == null || command.length() == 0) {
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR,
                    Bundle.getString("SourceViewPopup.noEditorSet"));
            } else if (sourcePath == null) {
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR,
                    Bundle.getString("SourceViewPopup.notFile"));
            } else {
                // Replace %f with the filepath and %l with the line number.
                try {
                    int fi = command.indexOf("%f");
                    if (fi >= 0) {
                        command = command.substring(0, fi)
                            + sourcePath + command.substring(fi + 2);
                    }
                    int li = command.indexOf("%l");
                    if (li >= 0) {
                        command = command.substring(0, li)
                            + lastClickedLine + command.substring(li + 2);
                    }
                    Runtime.getRuntime().exec(command);
                } catch (IOException ioe) {
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_ERROR,
                        Bundle.getString("SourceViewPopup.editorError")
                        + ' ' + ioe.getMessage());
                }
            }
        } else if (cmd.equals(ADD_STRING) || cmd.equals(RUNTO_STRING)) {

            // Handle the "Add breakpoint" menu item.
            Breakpoint bp = addBreakpoint(session, bpman);
            if (bp != null && cmd.equals(RUNTO_STRING)) {
                // Make this breakpoint a little special.
                bp.deleteOnExpire();
                bp.setExpireCount(1);
                // Resume the debuggee so we do the "run" part.
                if (session.isActive()) {
                    session.resumeVM(this, false, false);
                }
            }

        } else if (cmd.equals(PROPS_STRING)) {

            // Handle the "Properties..." menu item.
            Component invoker = getInvoker();
            Frame frame = SessionFrameMapper.getOwningFrame(invoker);
            EditorDialog editor = new EditorDialog(
                frame, lastClickedBreakpoint);
            editor.setLocationRelativeTo(frame);
            editor.setVisible(true);
        } else if (cmd.equals(REMOVE_STRING)) {
            // Delete the breakpoint using the breakpoint manager.
            bpman.removeBreakpoint(lastClickedBreakpoint);
        } else if (cmd.equals(ENABLE_STRING)) {
            // Enable the breakpoint using the breakpoint manager.
            bpman.enableBreakpoint(lastClickedBreakpoint);
        } else if (cmd.equals(DISABLE_STRING)) {
            // Disable the breakpoint using the breakpoint manager.
            bpman.disableBreakpoint(lastClickedBreakpoint);

        } else {

            // It must be one of the method menu items.
            try {
                int line = Integer.parseInt(cmd);
                UIAdapter adapter = session.getUIAdapter();
                adapter.showFile(sourceSrc, line, 0);
            } catch (NumberFormatException nfe) {
                // Or maybe not...
            }
        }
    } // actionPerformed

    /**
     * Try to add a breakpoint at the last clicked class and line.
     * Displays an appropriate error message if needed.
     *
     * @param  session  Session.
     * @param  bpman    breakpoint manager.
     * @return  new breakpoint if successful, null if error.
     */
    protected Breakpoint addBreakpoint(Session session,
                                       BreakpointManager bpman) {
        try {
            Breakpoint bp = new SourceNameBreakpoint(
                sourceSrc.getPackage(), sourceSrc.getName(),
                lastClickedLine);
            bpman.addNewBreakpoint(bp);
            return bp;
        } catch (ClassNotFoundException cnfe) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                Bundle.getString("AddBreak.invalidClassMsg") + ' '
                + sourceSrc.getPackage() + '.' + sourceSrc.getName());
            return null;
        } catch (ResolveException re) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR, re.errorMessage());
            return null;
        }
    } // addBreakpoint

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
     * Attempt to find the breakpoint the user has clicked on, using as
     * much information as is available to this popup.
     *
     * @param  bpman  breakpoint manager to search through.
     * @return  breakpoint if found, null if not.
     * @throws  ViewException
     *          if the line does not contain code.
     */
    protected Breakpoint findBreakpoint(BreakpointManager bpman)
        throws ViewException {
        String pkgname = sourceSrc.getPackage();
        String srcname = sourceSrc.getName();
        Iterator iter = bpman.getDefaultGroup().breakpoints(true);
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
                if (lbp.getLineNumber() == lastClickedLine
                    && lbp.matchesSource(pkgname, srcname)) {
                    return bp;
                }
            }
        }
        return null;
    } // findBreakpoint

    /**
     * Invoked when a mouse button has been released on a component. We
     * use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        if (!e.isConsumed()) {
            // Is this within the gutter area and the popup is not visible?
            SourceViewTextArea textArea = (SourceViewTextArea) e.getSource();
            if (textArea.isWithinGutter(e.getX()) && !isVisible()) {
                // Yes, create/disable/delete breakpoint at this line.

                // Get the class at this line.
                lastClickedLine = textArea.viewToLine(e.getPoint());

                // Find the breakpoint the user clicked on.
                Session session = SessionFrameMapper.getSessionForEvent(e);
                BreakpointManager bpman = (BreakpointManager)
                    session.getManager(BreakpointManager.class);
                try {
                    lastClickedBreakpoint = findBreakpoint(bpman);
                } catch (ViewException ve) {
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("SourceViewPopup.nonCodeLine"));
                    return;
                }

                if (lastClickedBreakpoint == null) {
                    // No breakpoint at this location, add a new one.
                    addBreakpoint(session, bpman);
                } else {
                    // Breakpoint exists.
                    if (lastClickedBreakpoint.isEnabled()) {
                        // Breakpoint is enabled, disable it.
                        bpman.disableBreakpoint(lastClickedBreakpoint);
                    } else {
                        // Breakpoint is disabled, remove it.
                        bpman.removeBreakpoint(lastClickedBreakpoint);
                    }
                }
            } else {
                super.mouseReleased(e);
            }
        }
    } // mouseReleased

    /**
     * Remove all of the children and add the ever-present items.
     */
    protected void removeAllItems() {
        removeAll();
        add((JMenuItem) menuItemTable.get(EDITOR_STRING));
    } // removeAllItems

    /**
     * Set the popup menu items enabled or disabled depending on which
     * line of the source view area the mouse button has been pressed.
     *
     * @param  e        Mouse event.
     * @param  session  Session to operate on.
     */
    protected void setMenuItemsForEvent(MouseEvent e, Session session) {
        // Reset the popup menu by removing all children.
        removeAllItems();

        // Use mouse position to determine line number.
        SourceViewTextArea textArea = (SourceViewTextArea) e.getSource();
        lastClickedLine = textArea.viewToLine(e.getPoint());
        if (lastClickedLine < 0) {
            add((JMenuItem) menuItemTable.get(BAD_LINE_STRING), 0);
            return;
        }

        // Find the breakpoint the user clicked on.
        BreakpointManager bpman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            lastClickedBreakpoint = findBreakpoint(bpman);
        } catch (ViewException ve) {
            // Add nothing to the menu.
            return;
        }

        if (lastClickedBreakpoint == null) {
            // Could not find a breakpoint at this location.
            add((JMenuItem) menuItemTable.get(ADD_STRING), 0);
            add((JMenuItem) menuItemTable.get(RUNTO_STRING), 1);
        } else {
            // Breakpoint exists, add "Breakpoint properties..." item.
            add((JMenuItem) menuItemTable.get(PROPS_STRING), 0);
            // Breakpoint exists, add "Remove breakpoint" item.
            add((JMenuItem) menuItemTable.get(REMOVE_STRING), 1);
            if (lastClickedBreakpoint.isEnabled()) {
                // Breakpoint is enabled, add "Disable breakpoint".
                add((JMenuItem) menuItemTable.get(DISABLE_STRING), 2);
            } else {
                // Breakpoint is disabled, add "Enable breakpoint".
                add((JMenuItem) menuItemTable.get(ENABLE_STRING), 2);
            }
        }
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
        Session session = SessionFrameMapper.getSessionForEvent(evt);
        setMenuItemsForEvent(evt, session);
        show(evt.getComponent(), evt.getX(), evt.getY());
    } // showPopup

    /**
     * Resets the UI property to a value from the current look and feel.
     */
    public void updateUI() {
        super.updateUI();
        // Now update our disconnected children.
        if (menuItemTable != null) {
            Enumeration menus = menuItemTable.elements();
            while (menus.hasMoreElements()) {
                JComponent jcomp = (JComponent) menus.nextElement();
                jcomp.updateUI();
            }
        }
    } // updateUI
} // SourceViewPopup
