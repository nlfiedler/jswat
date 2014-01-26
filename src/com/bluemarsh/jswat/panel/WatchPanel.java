/*
 *      Copyright (C) 2000-2014 Nathan Fiedler
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
 */
package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.VariableValue;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Class WatchPanel watches the modification events of variables and displays
 * the changed values in a table.
 * <p>
 * <p>
 * This works by maintaining a blank row in the watch table for the user to add
 * new watchpoints. That is, the user will type the name of a variable to watch
 * into the blank row. To remove a watchpoint, the user must clear the name from
 * that row.</p>
 * <p>
 * @author Nathan Fiedler
 */
public class WatchPanel extends AbstractPanel
        implements ContextListener, TableModelListener, VMEventListener {

    /**
     * Variable name column number.
     */
    private static final int NAME_COLUMN = 0;
    /**
     * Variable value column number.
     */
    private static final int VALUE_COLUMN = 1;
    /**
     * Table displaying the threads.
     */
    private final JTable table;
    /**
     * Our UI component - scrollable panel
     */
    private final JScrollPane uicomp;
    /**
     * List of the existing watchpoint requests.
     */
    private final List watchpointList;

    /**
     * Constructs a WatchPanel with the default table.
     */
    public WatchPanel() {
        watchpointList = new ArrayList();

        // Rebuild the table column model from user preferences.
        TableColumnModel colmod = new DefaultTableColumnModel();
        Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/panel/watch");
        int[] columnWidths = new int[]{50, 75};
        String[] columnNames = new String[2];
        columnNames[0] = Bundle.getString("Watch.column.name");
        columnNames[1] = Bundle.getString("Watch.column.value");
        columnNames = restoreTable(colmod, prefs, columnWidths, columnNames);
        // Column names are now in adjusted order.

        boolean[] editableCols = new boolean[]{true, false};
        ViewTableModel model = new ViewTableModel(columnNames, editableCols);
        model.addTableModelListener(this);

        // Create the table and all that goes with it.
        table = new WatchTable(model, colmod);
        ToolTipManager.sharedInstance().registerComponent(table);
        uicomp = new JScrollPane(table);
        WatchPopup popup = new WatchPopup();
        table.addMouseListener(popup);
    } // WatchPanel

    /**
     * Called when the Session has activated. This occurs when the debuggee has
     * launched or has been attached to the debugger.
     * <p>
     * @param sevt session event.
     */
    public void activated(SessionEvent sevt) {
        VMEventManager vmeman = (VMEventManager) owningSession.getManager(VMEventManager.class);
        // Listen to all modification watchpoint events.
        vmeman.addListener(ModificationWatchpointEvent.class, this,
                VMEventListener.PRIORITY_DEFAULT);

        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager) owningSession.getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);

        // Read the previously set watchpoints from the session properties.
        ViewTableModel model = (ViewTableModel) table.getModel();
        for (int n = 1; true; n++) {
            String key = "watchpoint" + Integer.toString(n);
            String value = owningSession.getProperty(key);
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
    } // activated

    /**
     * Adds the blank row to the model, to allow entry of a new watchpoint.
     * <p>
     * @return new row number.
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
     * Add the named variable to the watch list. This may or may not display a
     * value for the variable immediately.
     * <p>
     * @param expr     name of variable to watch.
     * @param row      row to add to.
     * @param addBlank true to add a blank row as needed.
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
     * Called when the Session is about to be closed.
     * <p>
     * @param sevt session event.
     */
    public void closing(SessionEvent sevt) {
        Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/panel/watch");
        saveTable(table.getColumnModel(), prefs);
        super.closing(sevt);
    } // closing

    /**
     * Invoked when the current context has changed. The context change event
     * identifies which aspect of the context has changed.
     * <p>
     * @param cce context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        resolveWatchpoints();
        refreshLater();
    } // contextChanged

    /**
     * Called when the Session has deactivated. The debuggee VM is no longer
     * connected to the Session.
     * <p>
     * @param sevt session event.
     */
    public void deactivated(SessionEvent sevt) {
        // Remove ourselves as a VM event listener.
        VMEventManager vmeman = (VMEventManager) owningSession.getManager(VMEventManager.class);
        vmeman.removeListener(ModificationWatchpointEvent.class, this);

        // Remove ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager) owningSession.getManager(ContextManager.class);
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
            WatchpointListEntry wle = (WatchpointListEntry) watchpointList.get(i);
            owningSession.setProperty("watchpoint" + Integer.toString(n),
                    wle.watchName);
            n++;
        }

        // Remove any other watchpoints from the properties.
        Object old;
        while (true) {
            // By getting the property first, we avoid Windows registry
            // error code 2 in RegDeleteValue(). JRE bug 4709908.
            old = owningSession.getProperty("watchpoint"
                    + Integer.toString(n));
            if (old != null) {
                owningSession.setProperty("watchpoint"
                        + Integer.toString(n), null);
                n++;
            } else {
                break;
            }
        }

        watchpointList.clear();
    } // deactivate

    /**
     * Invoked when a VM event has occurred.
     * <p>
     * @param e VM event
     * @return true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        if (e instanceof ModificationWatchpointEvent) {
            ModificationWatchpointEvent mwpe = (ModificationWatchpointEvent) e;
            Value value = mwpe.valueToBe();
            ObjectReference object = mwpe.object();

            // Find the row this event concerns.
            ModificationWatchpointRequest mwpr
                    = (ModificationWatchpointRequest) mwpe.request();
            int row = -1;
            for (int i = 0; i < watchpointList.size(); i++) {
                WatchpointListEntry entry = (WatchpointListEntry) watchpointList.get(i);
                if (entry.isFieldVar() && entry.watchRequest != null
                        && entry.watchRequest.equals(mwpr)) {
                    row = i;
                    break;
                }
            }

            if (row >= 0) {
                // Set cell in value column of that row to new value.
                ViewTableModel model = (ViewTableModel) table.getModel();
                if (value == null) {
                    model.setValueAt("null", row, VALUE_COLUMN);
                } else {
                    model.setValueAt(Strings.cleanForPrinting(
                            valueToString(value), 100), row, VALUE_COLUMN);
                }
            }
        }
        return true;
    } // eventOccurred

    /**
     * Returns a reference to the peer UI component. In many cases this is a
     * JList, JTree, or JTable, depending on the type of data being displayed in
     * the panel.
     * <p>
     * @return peer ui component object
     */
    public JComponent getPeer() {
        return table;
    } // getPeer

    /**
     * Returns a reference to the UI component.
     * <p>
     * @return ui component object
     */
    public JComponent getUI() {
        return uicomp;
    } // getUI

    /**
     * Update the display on the screen. Use the given Session to fetch the
     * desired data. This must be run on the AWT event dispatching thread.
     * <p>
     * @param session Debugging Session object.
     */
    public void refresh(Session session) {
        ContextManager ctxtman = (ContextManager) session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        String errorMsg = null;
        if (thread == null) {
            errorMsg = Bundle.getString("error.threadNotSet");
        }

        try {
            if (thread != null) {
                // Get current stack frame.
                ContextManager ctxtMgr = (ContextManager) session.getManager(ContextManager.class);
                StackFrame frame = thread.frame(ctxtMgr.getCurrentFrame());
                if (frame == null) {
                    errorMsg = Bundle.getString("error.noStackFrame");
                } else {

                    // First get a list of entries to be updated.
                    Object[] updates = new Object[watchpointList.size()];
                    for (int ii = watchpointList.size() - 1; ii >= 0; ii--) {
                        WatchpointListEntry entry = (WatchpointListEntry) watchpointList.get(ii);
                        if (!entry.isFieldVar()) {
                            LocalVariable lvar = frame.visibleVariableByName(
                                    entry.watchName);
                            if (lvar != null) {
                                Value val = frame.getValue(lvar);
                                if (val == null) {
                                    updates[ii] = "null";
                                } else {
                                    // By not being a String, this entry will
                                    // be updated in the second phase.
                                    updates[ii] = val;
                                }
                            } else {
                                updates[ii] = Bundle.getString(
                                        "Watch.error.invisible");
                            }
                        }
                    }

                    // Then update the local variable table entries. This
                    // two-phase approach is necessary because Classes'
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
                            valueStr = Strings.cleanForPrinting(
                                    valueToString((Value) o), 100);
                        }
                        // Set cell in value column of that row to new value.
                        model.setValueAt(valueStr, ii, VALUE_COLUMN);
                    }
                }
            }

        } catch (AbsentInformationException aie) {
            // Skip the local variables when not available.
            errorMsg = Bundle.getString("error.noDebugInfo");
        } catch (IncompatibleThreadStateException itse) {
            errorMsg = Bundle.getString("error.threadRunning");
        } catch (IndexOutOfBoundsException ioobe) {
            // If thread has no frame this exception gets thrown.
            errorMsg = Bundle.getString("error.noStackFrame");
        } catch (InvalidStackFrameException isfe) {
            errorMsg = Bundle.getString("error.invalidFrame");
        } catch (NativeMethodException nme) {
            errorMsg = Bundle.getString("error.nativeMethod");
        } catch (VMDisconnectedException vmde) {
            // This happens a lot.
        }

        if (errorMsg != null) {
            // Whatever the error was, it affects all local variables.
            ViewTableModel model = (ViewTableModel) table.getModel();
            for (int ii = watchpointList.size() - 1; ii >= 0; ii--) {
                WatchpointListEntry entry = (WatchpointListEntry) watchpointList.get(ii);
                if (!entry.isFieldVar()) {
                    model.setValueAt(errorMsg, ii, VALUE_COLUMN);
                }
            }
        }
    } // refresh

    /**
     * Try to resolve a watchpoint.
     * <p>
     * @param row   row number in table.
     * @param entry watchpoint list entry.
     */
    protected void resolveWatchpoint(int row, WatchpointListEntry entry) {
        ViewTableModel model = (ViewTableModel) table.getModel();
        String errorMsg;
        try {
            // Figure out what field the user is referring to.
            ContextManager ctxtman = (ContextManager) owningSession.getManager(ContextManager.class);
            ThreadReference thrd = ctxtman.getCurrentThread();
            if (thrd == null) {
                model.setValueAt(Bundle.getString("error.threadNotSet"),
                        row, VALUE_COLUMN);
                return;
            }

            int frameNum = ctxtman.getCurrentFrame();
            VariableValue varValue = Variables.getField(entry.watchName, thrd, frameNum);
            if (varValue.field() != null) {
                // Create a watchpoint request for this field.
                EventRequestManager erm
                        = owningSession.getVM().eventRequestManager();
                ModificationWatchpointRequest mwpr
                        = erm.createModificationWatchpointRequest(
                                varValue.field());
                mwpr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
                VirtualMachine vm = mwpr.virtualMachine();
                if (vm.canUseInstanceFilters()) {
                    mwpr.addInstanceFilter(varValue.object());
                } else {
                    // Warn the user about lack of filter support.
                    owningSession.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING,
                            Bundle.getString("Watch.noInstanceFilters"));
                }
                mwpr.enable();
                entry.watchRequest = mwpr;
            } else {
                // Save the LocalVariable for refreshing later.
                entry.watchLocal = varValue.localVariable();
            }

            // Show the field value in the value column.
            String valueStr = null;
            if (varValue.value() == null) {
                valueStr = "null";
            } else {
                valueStr = Strings.cleanForPrinting(
                        valueToString(varValue.value()), 100);
            }
            model.setValueAt(valueStr, row, VALUE_COLUMN);
            return;

        } catch (AbsentInformationException aie) {
            errorMsg = Bundle.getString("error.noDebugInfo");
        } catch (ClassNotPreparedException cnpe) {
            errorMsg = Bundle.getString("Watch.error.classNotLoaded");
        } catch (FieldNotObjectException fnoe) {
            errorMsg = Bundle.getString("Watch.error.fieldNotObject");
        } catch (IllegalThreadStateException itse) {
            errorMsg = Bundle.getString("error.illegalThreadState");
        } catch (IncompatibleThreadStateException itse) {
            errorMsg = Bundle.getString("error.threadRunning");
        } catch (IndexOutOfBoundsException ioobe) {
            // If thread has no frame this exception gets thrown.
            errorMsg = Bundle.getString("error.noStackFrame");
        } catch (InvalidStackFrameException isfe) {
            errorMsg = Bundle.getString("error.invalidFrame");
        } catch (NativeMethodException nme) {
            errorMsg = Bundle.getString("error.nativeMethod");
        } catch (NoSuchFieldException nsfe) {
            // May also indicate that the local variable is not
            // yet visible at this location.
            errorMsg = Bundle.getString("Watch.error.invisible");
        } catch (ObjectCollectedException oce) {
            errorMsg = Bundle.getString("Watch.error.objectCollected");
        }

        // If we're here, indicate a problem.
        if (errorMsg != null) {
            model.setValueAt(errorMsg, row, VALUE_COLUMN);
        } else {
            model.setValueAt(Bundle.getString("Watch.error.unknown"),
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
            if (entry.isFieldVar() && entry.watchRequest == null) {
                resolveWatchpoint(row, entry);
            }
            row++;
        }
    } // resolveWatchpoints

    /**
     * Invoked whenever the table model changes.
     * <p>
     * @param e Table model event.
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
                WatchpointListEntry entry = (WatchpointListEntry) watchpointList.remove(row);
                if (entry.isFieldVar() && entry.watchRequest != null) {
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
            WatchpointListEntry entry = (WatchpointListEntry) watchpointList.get(row);
            if (entry.isFieldVar() && entry.watchRequest != null) {
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
     * Returns the string representing the value. If the Value is an object (but
     * not a String), then call the toString() method on that object. Otherwise,
     * return the toString() of the Value.
     * <p>
     * <p>
     * Note that this method will invalidate the current stack frame. That is,
     * JDI will believe the thread has resumed when in fact it was but is now
     * suspended. Thus, the current stack frame is in an unknown state and must
     * be retrieved again.</p>
     * <p>
     * @param value value to be converted to a string.
     * @return value as String, or error message.
     */
    protected String valueToString(Value value) {
        ContextManager ctxtman = (ContextManager) owningSession.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            return Bundle.getString("error.threadNotSet");
        }
        try {
            return Variables.printValue(value, thread, ", ");
        } catch (Exception e) {
            String s = Strings.exceptionToString(e);
            owningSession.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR, s);
            return e.getMessage();
        }
    } // valueToString

    /**
     * Custom JTable to show custom tooltip.
     */
    protected class WatchTable extends JTable {

        /**
         * silence the compiler warnings
         */
        private static final long serialVersionUID = 1L;
        /**
         * Default tooltip text.
         */
        private String defaultTip;

        /**
         * Constructs a WatchTable with the specified models.
         * <p>
         * @param model  TableModel to use.
         * @param colmod TableColumnModel to use.
         */
        public WatchTable(TableModel model, TableColumnModel colmod) {
            super(model, colmod);
            defaultTip = Bundle.getString("Watch.tooltip");
        } // WatchTable

        /**
         * Returns custom tooltip text for this tree.
         * <p>
         * @param me Mouse event.
         * @return Custom tooltip text.
         */
        public String getToolTipText(MouseEvent me) {
            int row = table.rowAtPoint(me.getPoint());
            int col = table.columnAtPoint(me.getPoint());
            ViewTableModel model = (ViewTableModel) table.getModel();
            String value = (String) model.getValueAt(row, col);
            if (value == null || value.length() == 0) {
                return defaultTip;
            } else {
                return value;
            }
        } // getToolTipText
    } // WatchTable

    /**
     * Class WatchpointListEntry represents any type of watchpoint the user has
     * specified. This includes field variables of any object as well as any
     * local variable.
     */
    protected class WatchpointListEntry {

        /**
         * Name entered by the user to specify the watchpoint.
         */
        private String watchName;
        /**
         * WatchpointRequest, if resolved.
         */
        private WatchpointRequest watchRequest;
        /**
         * Local variable, if not a field variable. Used for comparison only, as
         * the value can become stale after a hotswap.
         */
        private LocalVariable watchLocal;

        /**
         * Constructs a WatchpointListEntry object.
         * <p>
         * @param name Name of variable to watch.
         */
        public WatchpointListEntry(String name) {
            this.watchName = name;
        } // WatchpointListEntry

        /**
         * Indicates if this watchpoint entry is a field variable or not.
         * <p>
         * @return true if this entry is a field variable.
         */
        public boolean isFieldVar() {
            // Can't check the object since it may be a static field.
            return watchLocal == null;
        } // isFieldVar
    } // WatchpointListEntry

    /**
     * Class WatchPopup is a popup menu that allows the user to clear all of the
     * entries from the watchpoints list.
     */
    protected class WatchPopup extends SmartPopupMenu
            implements ActionListener {

        /**
         * silence the compiler warnings
         */
        private static final long serialVersionUID = 1L;
        /**
         * The Clear menu item.
         */
        private JMenuItem clearMenuItem;

        /**
         * Constructs an WatchPopup that interacts with the panel.
         */
        WatchPopup() {
            super(Bundle.getString("Watch.menu.label"));
            clearMenuItem = new JMenuItem(
                    Bundle.getString("Watch.menu.clearLabel"));
            clearMenuItem.addActionListener(this);
            add(clearMenuItem);
        } // WatchPopup

        /**
         * Invoked when a menu item has been selected.
         * <p>
         * @param event action event
         */
        public void actionPerformed(ActionEvent event) {
            JMenuItem source = (JMenuItem) event.getSource();
            if (source == clearMenuItem) {
                if (table.isEditing()) {
                    table.removeEditor();
                }
                ViewTableModel model = (ViewTableModel) table.getModel();
                model.clear();
                watchpointList.clear();
                addBlankRow();
            }
        } // actionPerformed

        /**
         * Show the popup menu.
         * <p>
         * @param e mouse event.
         */
        protected void showPopup(MouseEvent e) {
            show(e.getComponent(), e.getX(), e.getY());
        } // showPopup
    } // WatchPopup
} // WatchPanel
