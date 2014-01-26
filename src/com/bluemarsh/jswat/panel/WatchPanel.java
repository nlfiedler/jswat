/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: WatchPanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.FieldAndValue;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.jswat.util.VariableUtils;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.MenuSelectionManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

/**
 * Class WatchPanel watches the modification events of variables
 * and displays the changed values in a table.
 *
 * <p>This works by maintaining a blank row in the watch table for the
 * user to add new watchpoints. That is, the user will type the name
 * of a variable to watch into the blank row. To remove a watchpoint,
 * the user must clear the name from that row.</p>
 *
 * @author  Nathan Fiedler
 */
public class WatchPanel extends JSwatPanel implements ContextListener, TableModelListener, VMEventListener {
    /** Table displaying the threads. */
    protected JTable table;
    /** Our UI component - scrollable panel */
    protected JScrollPane uicomp;
    /** Reference to the session. */
    protected Session owningSession;
    /** List of the existing watchpoint requests. */
    protected List watchpointList;
    // Constants for the table column numbers.
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;

    /**
     * Constructs a WatchPanel with the default table.
     */
    public WatchPanel() {
        watchpointList = new ArrayList();
	String[] columnNames = new String[2];
	columnNames[0] = Bundle.getString("Watch.nameColumn");
	columnNames[1] = Bundle.getString("Watch.valueColumn");
        boolean[] editableCols = new boolean[2];
        editableCols[0] = true;
        editableCols[1] = false;
        ViewTableModel model = new ViewTableModel(columnNames, editableCols);
        model.addTableModelListener(this);
        table = new JTable(model);

        // Set the column widths to some hard-coded values.
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(50);

        uicomp = new JScrollPane(table);

        WatchPopup popup = new WatchPopup();
        table.add(popup);
        table.addMouseListener(popup);
    } // WatchPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        owningSession = session;
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        // Listen to all modification watchpoint events.
        vmeman.addListener(ModificationWatchpointEvent.class, this,
                           VMEventListener.PRIORITY_DEFAULT);

        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);

        // Read the previously set watchpoints from the session properties.
        ViewTableModel model = (ViewTableModel) table.getModel();
        for (int n = 1; true; n++) {
            String key = "watchpoint" + Integer.toString(n);
            String value = session.getProperty(key);
            if (value == null) {
                // No watchpoint for n; that marks the end.
                break;
            }
            // Add and resolve the watchpoint, if possible.
            int row = addBlankRow();
            model.setValueNoEvent(value, row, NAME_COLUMN);
            addWatchpoint(value, row, false);
        }

        // Add a row to the table that will be used to create new
        // watchpoints.
        addBlankRow();
    } // activate

    /**
     * Adds the blank row to the model, to allow entry of a new
     * watchpoint.
     *
     * @return  new row number.
     */
    protected int addBlankRow() {
        ViewTableModel model = (ViewTableModel) table.getModel();
        int row = model.addRow();
        model.setValueNoEvent("", row, NAME_COLUMN);
        model.setValueNoEvent("", row, VALUE_COLUMN);
        // Notify the listeners that the table has changed.
        model.fireTableDataChanged();
        return row;
    } // addBlankRow

    /**
     * Add the named variable to the watch list. This may or may not
     * display a value for the variable immediately.
     *
     * @param  expr      name of variable to watch.
     * @param  row       row to add to.
     * @param  addBlank  true to add a blank row as needed.
     */
    protected void addWatchpoint(String expr, int row, boolean addBlank) {
        // Save the data the user entered in case it works later.
        WatchpointListEntry entry = new WatchpointListEntry(expr);
        if (watchpointList.size() > row) {
            watchpointList.set(row, entry);
        } else {
            watchpointList.add(entry);
            if (addBlank) {
                // Add the blank row for new watchpoint entry.
                addBlankRow();
            }
        }
        // Resolve the watchpoint.
        resolveWatchpoint(row, entry);
    } // addWatchpoint

    /**
     * Removes all of the present watchpoints.
     */
    protected void clearAllWatches() {
        if (table.isEditing()) {
            table.removeEditor();
        }
        ViewTableModel model = (ViewTableModel) table.getModel();
        model.clear();
        watchpointList.clear();
        addBlankRow();
    } // clearAllWatches

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        resolveWatchpoints();
        refresh(owningSession);
    } // contextChanged

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        // Remove ourselves as a VM event listener.
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ModificationWatchpointEvent.class, this);

        // Remove ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        ctxtMgr.removeContextListener(this);

        if (table.isEditing()) {
            // For some reason this happens; seems to be a JTable bug.
            table.removeEditor();
        }

        // Update the UI to show nothing.
        ViewTableModel model = (ViewTableModel) table.getModel();
        model.clear();

        // Save the current watchpoints to the session properties.
        // These will be read from the session properties and set
        // the next time the session activates.
        int n = 1;
        for (int i = 0; i < watchpointList.size(); i++) {
            WatchpointListEntry wle = (WatchpointListEntry)
                watchpointList.get(i);
            session.setProperty("watchpoint" + Integer.toString(n),
                                wle.watchName);
            n++;
        }

        // Remove any other watchpoints from the properties.
        Object old;
        do {
            old = session.setProperty("watchpoint" + Integer.toString(n),
                                      null);
            n++;
        } while (old != null);

        watchpointList.clear();
    } // deactivate

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        if (e instanceof ModificationWatchpointEvent) {
            ModificationWatchpointEvent mwpe = (ModificationWatchpointEvent) e;
            Value value = mwpe.valueToBe();
            ObjectReference object = mwpe.object();

            // Find the row this event concerns.
            ModificationWatchpointRequest mwpr =
                (ModificationWatchpointRequest) mwpe.request();
            int row = -1;
            for (int i = 0; i < watchpointList.size(); i++) {
                WatchpointListEntry entry = (WatchpointListEntry)
                    watchpointList.get(i);
                if (entry.isFieldVar() && entry.isResolved()) {
                    // Watchpoint requests are not associated with a specific
                    // instance, so need to check that ourselves.
                    if (entry.watchRequest.equals(mwpr) &&
                        (((object == null) && (entry.watchObject == null)) ||
                         ((entry.watchObject != null) &&
                          (entry.watchObject.equals(object))))) {
                        row = i;
                        break;
                    }
                }
            }

            if (row >= 0) {
                // Set cell in value column of that row to new value.
                ViewTableModel model = (ViewTableModel) table.getModel();
                if (value == null) {
                    model.setValueAt("null", row, VALUE_COLUMN);
                } else {
                    model.setValueAt(StringUtils.cleanForPrinting(
                        valueToString(value), 100), row, VALUE_COLUMN);
                }
            }
        }
        return true;
    } // eventOccurred

    /**
     * Returns a reference to the peer UI component. In many
     * cases this is a JList, JTree, or JTable, depending on
     * the type of data being displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return table;
    } // getPeer

    /**
     * Returns a reference to the UI component.
     *
     * @return  ui component object
     */
    public JComponent getUI() {
        return uicomp;
    } // getUI

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        ThreadReference thread = session.getCurrentThread();
        String errorMsg = null;
        if (thread == null) {
            errorMsg = Bundle.getString("nothreadInParen");
        }

        try {
            if (thread != null) {
                // Get current stack frame.
                ContextManager ctxtMgr = (ContextManager)
                    session.getManager(ContextManager.class);
                StackFrame frame = thread.frame(ctxtMgr.getCurrentFrame());
                if (frame == null) {
                    errorMsg = Bundle.getString("noframeInParen");
                }

                // Get the visible local variables in this frame.
                List localVars = frame.visibleVariables();

                // First get a list of entries to be updated.
                Object[] updates = new Object[watchpointList.size()];
                for (int ii = watchpointList.size() - 1; ii >= 0; ii--) {
                    WatchpointListEntry entry = (WatchpointListEntry)
                        watchpointList.get(ii);
                    if (entry.isLocalVar()) {
                        LocalVariable lvar = entry.watchLocal;
                        boolean isVisible = false;
                        try {
                            isVisible = lvar.isVisible(frame);
                        } catch (IllegalArgumentException iae) {
                            // I don't know how this happens, but it
                            // means the variable is not visible.
                        }
                        if (isVisible) {
                            Value val = frame.getValue(lvar);
                            if (val == null) {
                                updates[ii] = "null";
                            } else {
                                // By not being a String, this entry
                                // will be updated in the second phase.
                                updates[ii] = val;
                            }
                        } else {
                            updates[ii] =
                                Bundle.getString("undefinedInParen");
                        }
                    }
                }

                // Then update the local variable table entries. This
                // two-phase approach is necessary because ClassUtils'
                // callToString() method invalidates the stack frame.
                ViewTableModel model = (ViewTableModel) table.getModel();
                for (int ii = updates.length - 1; ii >= 0; ii--) {
                    Object o = updates[ii];
                    if (o == null) {
                        // An empty entry.
                        continue;
                    }
                    String valueStr = null;
                    if (o instanceof String) {
                        valueStr = (String) o;
                    } else {
                        // It must be a Value then.
                        valueStr = StringUtils.cleanForPrinting(
                            valueToString((Value) o), 100);
                    }
                    // Set cell in value column of that row to new value.
                    model.setValueAt(valueStr, ii, VALUE_COLUMN);
                }
            }

        } catch (AbsentInformationException aie) {
            // Skip the local variables when not available.
            errorMsg = Bundle.getString("noinfoInParen");
        } catch (IncompatibleThreadStateException itse) {
            errorMsg = Bundle.getString("threadRunningInParen");
        } catch (IndexOutOfBoundsException ioobe) {
            // If thread has no frame this exception gets thrown.
            errorMsg = Bundle.getString("noframeInParen");
        } catch (InvalidStackFrameException isfe) {
            errorMsg = Bundle.getString("invalidframeInParen");
        } catch (NativeMethodException nme) {
            errorMsg = Bundle.getString("nativeInParen");
        } catch (VMDisconnectedException vmde) {
            // Do nothing, just return.
            return;
        } catch (Exception e) {
            // All other exceptions result in an immediate abort.
            e.printStackTrace();
            errorMsg = Bundle.getString("errorInParen");
        }

        if (errorMsg != null) {
            // Whatever the error was, it affects all local variables.
            ViewTableModel model = (ViewTableModel) table.getModel();
            for (int ii = watchpointList.size() - 1; ii >= 0; ii--) {
                WatchpointListEntry entry = (WatchpointListEntry)
                    watchpointList.get(ii);
                if (entry.isLocalVar()) {
                    model.setValueAt(errorMsg, ii, VALUE_COLUMN);
                }
            }
        }
    } // refresh

    /**
     * Try to resolve a watchpoint.
     *
     * @param  row    row number in table.
     * @param  entry  watchpoint list entry.
     */
    protected void resolveWatchpoint(int row, WatchpointListEntry entry) {
        ViewTableModel model = (ViewTableModel) table.getModel();
        String errorMsg = null;
        try {
            // Figure out what field the user is referring to.
            ContextManager ctxtman = (ContextManager)
                owningSession.getManager(ContextManager.class);
            ThreadReference thrd = ctxtman.getCurrentThread();
            if (thrd == null) {
                model.setValueAt(Bundle.getString("nothreadInParen"),
                                 row, VALUE_COLUMN);
                return;
            }

            int frameNum = ctxtman.getCurrentFrame();
            FieldAndValue fieldValue = VariableUtils.getField
                (entry.watchName, thrd, frameNum);
            if (fieldValue.field != null) {
                // Create a watchpoint request for this field.
                EventRequestManager erm =
                    owningSession.getVM().eventRequestManager();
                ModificationWatchpointRequest mwpr =
                    (ModificationWatchpointRequest)
                    erm.createModificationWatchpointRequest(fieldValue.field);
                mwpr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
                mwpr.enable();

                // Remember both the request and the object we're watching
                // so it will be refreshed for each modification event.
                entry.watchRequest = mwpr;
                entry.watchObject = fieldValue.object;
            } else {
                // Save the LocalVariable for refreshing later.
                entry.watchLocal = fieldValue.localVar;
            }

            // Show the field value in the value column.
            String valueStr = null;
            if (fieldValue.value == null) {
                valueStr = "null";
            } else {
                valueStr = StringUtils.cleanForPrinting(
                    valueToString(fieldValue.value), 100);
            }
            model.setValueAt(valueStr, row, VALUE_COLUMN);
            return;

        } catch (AbsentInformationException aie) {
            errorMsg = Bundle.getString("noinfoInParen");
        } catch (ClassNotPreparedException cnpe) {
            errorMsg = Bundle.getString("classnotloadedInParen");
        } catch (FieldNotObjectException fnoe) {
            errorMsg = Bundle.getString("fieldnotobjectInParen");
        } catch (IllegalThreadStateException itse) {
            errorMsg = Bundle.getString("illegalthreadstateInParen");
        } catch (IncompatibleThreadStateException itse) {
            errorMsg = Bundle.getString("threadRunningInParen");
        } catch (IndexOutOfBoundsException ioobe) {
            // If thread has no frame this exception gets thrown.
            errorMsg = Bundle.getString("noframeInParen");
        } catch (InvalidStackFrameException isfe) {
            errorMsg = Bundle.getString("invalidframeInParen");
        } catch (NativeMethodException nme) {
            errorMsg = Bundle.getString("nativeInParen");
        } catch (NoSuchFieldException nsfe) {
            // May also indicate that the local variable is not
            // yet visible at this location.
            errorMsg = Bundle.getString("undefinedInParen");
        } catch (ObjectCollectedException oce) {
            errorMsg = Bundle.getString("objectcollectedInParen");
        }

        // If we're here, indicate a problem.
        if (errorMsg != null) {
            model.setValueAt(errorMsg, row, VALUE_COLUMN);
        } else {
            model.setValueAt(Bundle.getString("errorInParen"),
                             row, VALUE_COLUMN);
        }
    } // resolveWatchpoint

    /**
     * Iterate the list of watchpoints and try to resolve any unresolved
     * entries.
     */
    protected void resolveWatchpoints() {
        Iterator iter = watchpointList.iterator();
        int row = 0;
        while (iter.hasNext()) {
            WatchpointListEntry entry = (WatchpointListEntry) iter.next();
            if (!entry.isResolved()) {
                resolveWatchpoint(row, entry);
            }
            row++;
        }
    } // resolveWatchpoints

    /**
     * Invoked whenever the table model changes.
     *
     * @param  e  Table model event.
     */
    public void tableChanged(TableModelEvent e) {
        // We only care about table data changes.
        if (e.getType() != TableModelEvent.UPDATE) {
            return;
        }

        // Get the new field value from the cell.
        int row = e.getFirstRow();
        int col = e.getColumn();
        if ((row < 0) || (col != NAME_COLUMN)) {
            // Invalid row and column in event, bail.
            return;
        }
        EventRequestManager erm = owningSession.getVM().eventRequestManager();

        ViewTableModel model = (ViewTableModel) e.getSource();
        String expr = (String) model.getValueAt(row, col);
        if (expr.length() == 0) {
            try {
                // See if the user is removing an entry.
                WatchpointListEntry entry = (WatchpointListEntry)
                    watchpointList.remove(row);
                if (entry.isFieldVar() && entry.isResolved()) {
                    entry.watchRequest.disable();
                    erm.deleteEventRequest(entry.watchRequest);
                }
                // Remove the row from the table model.
                model.removeRow(row);
            } catch (IndexOutOfBoundsException ioobe) {
                // It was not a watchpoint, so we won't remove
                // the blank row from the model.
            }
            return;
        }

        try {
            // Find the current watchpoint and disable it.
            // Do not remove it from the watchpoint list; the entry
            // will be overwritten by the addWatchpoint() method.
            WatchpointListEntry entry = (WatchpointListEntry)
                watchpointList.get(row);
            if (entry.isFieldVar() && entry.isResolved()) {
                entry.watchRequest.disable();
                erm.deleteEventRequest(entry.watchRequest);
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // It was not a watchpoint, so do nothing.
        }

        // Add and resolve the new watchpoint.
        addWatchpoint(expr, row, true);
    } // tableChanged

    /**
     * Returns the string representing the value. If the Value is an
     * object (but not a String), then call the toString() method on
     * that object. Otherwise, return the toString() of the Value.
     *
     * <p>Note that this method will invalidate the current stack
     * frame. That is, JDI will believe the thread has resumed when
     * in fact it was but is now suspended. Thus, the current stack
     * frame is in an unknown state and must be retrieved again.</p>
     *
     * @param  value  value to be converted to a string.
     * @return  String if successful, null if error.
     */
    protected String valueToString(Value value) {
        if ((value instanceof ObjectReference) &&
            !(value instanceof StringReference)) {
            ContextManager ctxtman = (ContextManager)
                owningSession.getManager(ContextManager.class);
            // XXX - ought to use an arbitrary thread in debuggee
            ThreadReference thread = ctxtman.getCurrentThread();
            if (thread == null) {
                return null;
            }
            String s = ClassUtils.callToString(
                (ObjectReference) value, thread);
            if (s == null) {
                return value.toString();
            } else {
                return s;
            }
        } else {
            return value.toString();
        }
    } // valueToString

    /**
     * Class WatchpointListEntry represents any type of watchpoint the
     * user has specified. This includes field variables of any object
     * as well as any local variable.
     */
    protected class WatchpointListEntry {
        /** Name entered by the user to specify the watchpoint. */
        public String watchName;
        /** WatchpointRequest, if resolved. */
        public WatchpointRequest watchRequest;
        /** Object reference, if a field variable. */
        public ObjectReference watchObject;
        /** Local variable, if not a field variable. */
        public LocalVariable watchLocal;

        /**
         * Constructs a WatchpointListEntry object.
         *
         * @param  name  Name of variable to watch.
         */
        public WatchpointListEntry(String name) {
            this.watchName = name;
        } // WatchpointListEntry

        /**
         * Indicates if this watchpoint entry is a field variable or not.
         *
         * @return  true if this entry is a field variable.
         */
        public boolean isFieldVar() {
            // Can't check the object since it may be a static field.
            return watchLocal == null;
        } // isFieldVar

        /**
         * Indicates if this watchpoint entry is a local variable or not.
         *
         * @return  true if this entry is a local variable.
         */
        public boolean isLocalVar() {
            return watchLocal != null;
        } // isLocalVar

        /**
         * Indicates if this watchpoint entry has been resolved.
         *
         * @return  true if this entry has been resolved.
         */
        public boolean isResolved() {
            return (watchRequest != null) || isLocalVar();
        } // isResolved
    } // WatchpointListEntry

    /**
     * Class WatchPopup is a popup menu that allows the user to
     * clear all of the entries from the watchpoints list.
     */
    protected class WatchPopup extends JPopupMenu implements ActionListener, MouseListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The Clear menu item. */
        protected JMenuItem clearMenuItem;

        /**
         * Constructs an WatchPopup that interacts with the given text
         * component. The popup can offer pasting as well as clearing
         * of the text component. By default, the popup will allow
         * copying the selected text to the clipboard.
         *
         * @param  panel  the watch panel.
         */
        WatchPopup() {
            super(Bundle.getString("WatchPopup.label"));
            clearMenuItem = new JMenuItem(
                Bundle.getString("WatchPopup.clearLabel"));
            clearMenuItem.addActionListener(this);
            add(clearMenuItem);
        } // WatchPopup

        /**
         * Invoked when a menu item has been selected.
         *
         * @param  event  action event
         */
        public void actionPerformed(ActionEvent event) {
            JMenuItem source = (JMenuItem) event.getSource();
            if (source == clearMenuItem) {
                clearAllWatches();
            }
        } // actionPerformed

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
            // Must check this in both 'pressed' and 'released'.
            showPopup(e);
        } // mousePressed

        /**
         * Invoked when a mouse button has been released on a component.
         * We use this opportunity to show the popup menu.
         *
         * @param  e  Mouse event.
         */
        public void mouseReleased(MouseEvent e) {
            // Must check this in both 'pressed' and 'released'.
            showPopup(e);
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
         * Decide whether or not to show the popup menu.
         *
         * @param  e  Mouse event.
         */
        protected void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // Show the popup menu.
                show(e.getComponent(), e.getX(), e.getY());
            } else {
                // Process the mouse event normally.
                MenuSelectionManager.defaultManager().processMouseEvent(e);
                // Make the menu disappear.
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        } // showPopup
    } // WatchPopup
} // WatchPanel
