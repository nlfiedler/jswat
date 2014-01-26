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
 * $Id: SourceViewPopup.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.ui.EditorDialog;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;

/**
 * Class SourceViewPopup defines a subclass of JPopupMenu that works
 * specifically for the source view. It constructs a context-sensitive
 * menu based on the existence of breakpoints at the given line of the
 * source view. Like a plain JPopupMenu, you must add this popup as a
 * child to the view area in question. It also must be added as a mouse
 * listener to the view area component.
 *
 * @author  Nathan Fiedler
 */
public class SourceViewPopup extends JPopupMenu implements ActionListener, MouseListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Hashtable of menu items keyed by their names (eg. 'add'). */
    protected Hashtable menuItemTable;
    /** List of class definition objects. */
    protected List classLines;
    /** If non-zero, the line the user last right-clicked on. */
    protected int lastClickedLine;
    /** If non-null, name of class user last right-clicked within. */
    protected String lastClickedClass;
    /** If non-null, breakpoint user last right-clicked on. */
    protected Breakpoint lastClickedBreakpoint;
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
    /** Name for "no class here" menu item. */
    private static final String NO_CLASS_STRING = "noClass";
    /** Name for "Java parser error" menu item. */
    private static final String PARSER_ERROR_STRING = "parserError";
    /** Name for "bad line number" menu item. */
    private static final String BAD_LINE_STRING = "badLine";
    /** Suffix added to command string to retrieve menu labels. */
    private static final String LABEL_SUFFIX = "Label";

    /**
     * Create a SourceViewPopup with the specified title. The file
     * is used to map the filename to a class name.
     *
     * @param  label  label for the popup menu.
     */
    public SourceViewPopup(String label) {
        super(label);
        menuItemTable = new Hashtable();
        menuItemTable.put(ADD_STRING, createMenuItem(ADD_STRING));
        menuItemTable.put(RUNTO_STRING, createMenuItem(RUNTO_STRING));
        menuItemTable.put(PROPS_STRING, createMenuItem(PROPS_STRING));
        menuItemTable.put(REMOVE_STRING, createMenuItem(REMOVE_STRING));
        menuItemTable.put(ENABLE_STRING, createMenuItem(ENABLE_STRING));
        menuItemTable.put(DISABLE_STRING, createMenuItem(DISABLE_STRING));
        menuItemTable.put(NO_CLASS_STRING,
                          new JMenuItem(Bundle.getString(
                              "SourceViewPopup.noClassHereMsg")));
        menuItemTable.put(PARSER_ERROR_STRING,
                          new JMenuItem(Bundle.getString(
                              "SourceViewPopup.parserErrorMsg")));
        menuItemTable.put(BAD_LINE_STRING,
                          new JMenuItem(Bundle.getString(
                              "SourceViewPopup.badLineNumMsg")));
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
        // Get the action command.
        String cmd = menuItem.getActionCommand();
        // Decide what to do.
        if (cmd.equals(ADD_STRING) || cmd.equals(RUNTO_STRING)) {

            // Handle the "Add breakpoint" menu item.
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            try {

                // Construct the line breakpoint manually.
                Breakpoint bp = new LineBreakpoint(lastClickedClass,
                                                   lastClickedLine);
                brkman.addNewBreakpoint(bp);
                session.getStatusLog().writeln(
                    Bundle.getString("AddBreak.breakpointAdded"));

                if (cmd.equals(RUNTO_STRING)) {
                    // Make this breakpoint a little special.
                    bp.deleteOnExpire();
                    bp.setExpireCount(1);
                    // Resume the debuggee so we do the "run" part.
                    try {
                        session.resumeVM();
                    } catch (NotActiveException nae) { }
                }

            } catch (ClassNotFoundException cnfe) {
                JOptionPane.showMessageDialog(
                    null,
                    Bundle.getString("AddBreak.invalidClassMsg") +
                    " " + lastClickedClass,
                    Bundle.getString("AddBreak.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            } catch (ResolveException re) {
                JOptionPane.showMessageDialog(
                    null, re.errorMessage(),
                    Bundle.getString("AddBreak.errorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }

        } else if (cmd.equals(PROPS_STRING)) {

            // Handle the "Properties..." menu item.
            Component invoker = getInvoker();
            Frame frame = SessionFrameMapper.getOwningFrame(invoker);
            EditorDialog editor = new EditorDialog(
                frame, lastClickedBreakpoint);
            editor.setLocationRelativeTo(frame);
            editor.setVisible(true);
        } else {

            BreakpointManager bpman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            if (cmd.equals(REMOVE_STRING)) {
                // Delete the breakpoint using the breakpoint manager.
                bpman.removeBreakpoint(lastClickedBreakpoint);
            } else if (cmd.equals(ENABLE_STRING)) {
                // Enable the breakpoint using the breakpoint manager.
                bpman.enableBreakpoint(lastClickedBreakpoint);
            } else if (cmd.equals(DISABLE_STRING)) {
                // Disable the breakpoint using the breakpoint manager.
                bpman.disableBreakpoint(lastClickedBreakpoint);
            }
        }
    } // actionPerformed

    /**
     * This is the hook through which all menu items are created.
     * Using the <code>cmd</code> string it finds the menu item label
     * in the resource bundle.
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
     * Finds the class defined at this line number.
     *
     * @param  line  Line number.
     * @return  Class name, or null if line not in a class.
     */
    protected String findClassForLine(int line) {
        int begin = Integer.MIN_VALUE;
        int end = Integer.MAX_VALUE;
        String cname = null;
        for (int i = 0; i < classLines.size(); i++) {
            ClassDefinition cd = (ClassDefinition) classLines.get(i);
            if ((cd.getBeginLine() <= line) && (cd.getEndLine() >= line) &&
                (cd.getBeginLine() > begin) && (cd.getEndLine() < end)) {
                cname = cd.getClassName();
                begin = cd.getBeginLine();
                end = cd.getEndLine();
            }
        }
        return cname;
    } // findClassForLine

    /**
     * Invoked when the mouse has been clicked on a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
    } // mouseClicked

    /**
     * Invoked when a mouse button has been pressed on a component.
     * We use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
            // Must check this in both 'pressed' and 'released'.
            showPopup(e);
        }
    } // mousePressed

    /**
     * Invoked when a mouse button has been released on a component.
     * We use this opportunity to show the popup menu.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        if (!e.isConsumed()) {
            // Must check this in both 'pressed' and 'released'.
            showPopup(e);
        }
    } // mouseReleased

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseEntered(MouseEvent e) {
    } // mouseEntered

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseExited(MouseEvent e) {
    } // mouseExited

    /**
     * Set the list of class definitions.
     *
     * @param  lines  List of ClassDefinition objects.
     */
    void setClassDefs(List lines) {
        classLines = lines;
    } // setClassDefs

    /**
     * Set the popup menu items enabled or disabled depending on
     * which line of the source view area the mouse button has
     * been pressed. The Session must be active when this method
     * is called or exceptions will be thrown.
     *
     * @param  e        Mouse event.
     * @param  session  Session to operate on.
     */
    protected void setMenuItemsForEvent(MouseEvent e, Session session) {
        // Reset the popup menu by removing all children.
        removeAll();

        // Use mouse position to determine line number.
        SourceViewArea viewArea = (SourceViewArea) e.getSource();
        lastClickedLine = viewArea.viewToLine(e.getPoint());
        if (lastClickedLine < 0) {
            add((JMenuItem) menuItemTable.get(BAD_LINE_STRING));
            return;
        }

        if (classLines == null) {
            // Parser error when reading source file, thus no class defs.
            add((JMenuItem) menuItemTable.get(PARSER_ERROR_STRING));
            return;
        }

        // Get the class at this line.
        lastClickedClass = findClassForLine(lastClickedLine);
        if (lastClickedClass == null) {
            // No class for this line, let the user know.
            add((JMenuItem) menuItemTable.get(NO_CLASS_STRING));
            return;
        }

         BreakpointManager bpman = (BreakpointManager)
             session.getManager(BreakpointManager.class);
        lastClickedBreakpoint = bpman.getBreakpoint(
            lastClickedClass, lastClickedLine);
        if (lastClickedBreakpoint == null) {
            // Could not find a breakpoint at this location.
            add((JMenuItem) menuItemTable.get(ADD_STRING));
            add((JMenuItem) menuItemTable.get(RUNTO_STRING));
        } else {
            // Breakpoint exists, add "Breakpoint properties..." item.
            add((JMenuItem) menuItemTable.get(PROPS_STRING));
            // Breakpoint exists, add "Remove breakpoint" item.
            add((JMenuItem) menuItemTable.get(REMOVE_STRING));
            if (lastClickedBreakpoint.isEnabled()) {
                // Breakpoint is enabled, add "Disable breakpoint".
                add((JMenuItem) menuItemTable.get(DISABLE_STRING));
            } else {
                // Breakpoint is disabled, add "Enable breakpoint".
                add((JMenuItem) menuItemTable.get(ENABLE_STRING));
            }
        }
    } // setMenuItemsForEvent

    /**
     * Decide whether or not to show the popup menu. If the popup should
     * be shown, determine which line the user clicked on and find any
     * breakpoints at that line. If none found, show the "Add breakpoint"
     * menu. If there's a breakpoint, show a popup that provides breakpoint
     * management features.
     *
     * <p>If the session is inactive, display a menu with a single
     * "no session" menu item that does nothing.</p>
     *
     * @param  e  Mouse event.
     */
    protected void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Session session = SessionFrameMapper.getSessionForEvent(e);
            // Set the popup menu items enabled or disabled.
            setMenuItemsForEvent(e, session);

            // Show the popup menu.
            show(e.getComponent(), e.getX(), e.getY());
            e.consume();
        } else {
            // Process the mouse event normally.
            // (Thanks to Peter Boothe for this bug fix.)
            MenuSelectionManager.defaultManager().processMouseEvent(e);
            // Make the menu disappear.
            MenuSelectionManager.defaultManager().clearSelectedPath();
        }
    } // showPopup
} // SourceViewPopup
