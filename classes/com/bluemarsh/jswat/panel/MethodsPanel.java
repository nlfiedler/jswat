/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Michael Swartzendruber
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
 * $Id: MethodsPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.ui.JSwatTable;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Class MethodsPanel is responsible for displaying the methods in the
 * 'this' object in the current stack frame.
 *
 * @author  Michael Swartzendruber
 * @author  Nathan Fiedler
 */
public class MethodsPanel extends AbstractPanel
    implements ContextListener, MouseListener  {
    /** Our UI component - scrollable pane */
    private JScrollPane uicomp;
    /** Our table component - displays the stack frames */
    private JTable table;
    /** 'this' object's class; may be null. */
    private ReferenceType thisClass;
    /** Compares Method objects and sorts them by name. */
    private MethodComparator methodComparator;

    /**
     * Constructs a new MethodsPanel with the default text area.
     */
    public MethodsPanel() {
        methodComparator = new MethodComparator();

        // Rebuild the table column model from user preferences.
        TableColumnModel colmod = new DefaultTableColumnModel();
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/panel/method");
        int[] columnWidths = new int[] { 60, 150 };
        String[] columnNames = new String[2];
        columnNames[0] = Bundle.getString("Methods.column.number");
        columnNames[1] = Bundle.getString("Methods.column.name");
        columnNames = restoreTable(colmod, prefs, columnWidths, columnNames);
        // Column names are now in adjusted order.

        table = new MethodsTable(new ViewTableModel(columnNames), colmod);
        table.addMouseListener(this);
        table.setSelectionModel(null);
        ToolTipManager.sharedInstance().registerComponent(table);
        uicomp = new JScrollPane(table);
    } // MethodsPanel

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        // Add ourselves as a context change listener.
        ContextManager conman = (ContextManager)
            sevt.getSession().getManager(ContextManager.class);
        conman.addContextListener(this);
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/panel/method");
        saveTable(table.getColumnModel(), prefs);
        super.closing(sevt);
    } // closing

    /**
     * Invoked when the current context has changed. The context change
     * event identifies which aspect of the context has changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        if (!cce.isBrief()) {
            // Not a brief event, refresh the display.
            refreshLater();
        }
    } // contextChanged

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        // Remove ourselves as a context change listener.
        ContextManager conman = (ContextManager)
            sevt.getSession().getManager(ContextManager.class);
        conman.removeContextListener(this);

        // Update the UI to show nothing.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ViewTableModel model = (ViewTableModel) table.getModel();
                    model.clear();
                }
            });
    } // deactivated

    /**
     * Returns a reference to the peer UI component. In many cases this
     * is a JList, JTree, or JTable, depending on the type of data being
     * displayed in the panel.
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
     * Gets the name of the given method.
     *
     * @param  method  method from which to get name.
     * @return  name of the method.
     */
    protected String methodName(Method method) {
        if (method.isConstructor()) {
            String name = method.location().declaringType().name();
            return name.substring(name.lastIndexOf('.') + 1);
        } else {
            return method.name();
        }
    } // methodName

    /**
     * Invoked when the mouse has been clicked on a component. We use
     * this to take action on the table.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (thisClass == null) {
            // Nothing to do.
            return;
        }

        if (e.getClickCount() == 2) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());

            // Try to open the source for this frame.
            ViewTableModel model = (ViewTableModel) table.getModel();
            String lineNumber = (String) model.getValueAt(row, 0);
            if (lineNumber != null) {

                // Map the 'this' class to a source file.
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = null;
                try {
                    src = pathman.mapSource(thisClass);
                } catch (IOException ioe) {
                    // This case is handled below.
                }
                if (src != null && src.exists()) {
                    // Try to show the source line for this location.
                    int line = 0;
                    try {
                        line = Integer.parseInt(lineNumber);
                    } catch (NumberFormatException nfe) {
                        // Defaults to zero then.
                    }
                    UIAdapter adapter = owningSession.getUIAdapter();
                    adapter.showFile(src, line, 0);
                } else {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_ERROR,
                        Bundle.getString("couldntMapSrcFile"));
                }
            }
        }
    } // mouseClicked

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  Mouse event.
     */
    public void mousePressed(MouseEvent e) {
    } // mousePressed

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param  e  Mouse event.
     */
    public void mouseReleased(MouseEvent e) {
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
     * Update the display on the screen. Use the given Session to fetch
     * the desired data. This must be run on the AWT event dispatching
     * thread.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        ViewTableModel model = (ViewTableModel) table.getModel();
        model.clear();
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference currentThread = conman.getCurrentThread();
        if (currentThread == null) {
            if (session.isActive()) {
                model.setMessage(Bundle.getString("error.threadNotSet"), 1);
            }
            return;
        }

        // Get the stack frame.
        StackFrame frame;
        try {
            frame = currentThread.frame(conman.getCurrentFrame());
            if (frame == null) {
                model.setMessage(Bundle.getString("error.noStackFrame"), 1);
                return;
            }

            ReferenceType refType = frame.location().declaringType();
            thisClass = refType;
            // Sort the methods by name first.
            List methods = new ArrayList(refType.methods());
            Collections.sort(methods, methodComparator);

            StringBuffer buffer = new StringBuffer(128);
            // Iterate over the methods and build out the table.
            Iterator iter = methods.iterator();
            while (iter.hasNext()) {
                Method aMethod = (Method) iter.next();

                if (aMethod.isStaticInitializer()) {
                    // Skip static initializers, they're not methods.
                    continue;
                }

                // Add a new row to the model.
                int row = model.addRow();
                Location location = aMethod.location();
                if (location != null) {
                    // Set the line number for this entry.
                    model.setValueNoEvent(
                        String.valueOf(location.lineNumber()), row, 0);
                } else {
                    model.setValueNoEvent("??", row, 0);
                }

                // Ignore the following basically useless information.

                // Get the method modifiers.
//                 int modifiers = aMethod.modifiers();
//                 String modifier = Modifier.toString(modifiers);
//                 if (modifier.length() > 0) {
//                     buffer.append(modifier);
//                     buffer.append(' ');
//                 }

                // Get the method return type.
//                 String returnType = aMethod.returnTypeName();
//                 returnType = returnType.substring(
//                     returnType.lastIndexOf('.') + 1);
//                 buffer.append(returnType);
//                 buffer.append(' ');

                // Get the method name.
                buffer.append(methodName(aMethod));
                buffer.append('(');

                // Get the method arguments.
                List args = aMethod.argumentTypeNames();
                for (int x = 0; x < args.size(); x++) {
                    String anArg = (String) args.get(x);
                    boolean isArray = false;
                    if (anArg.indexOf('[') != -1) {
                        isArray = true;
                    }
                    anArg = anArg.substring(anArg.lastIndexOf('.') + 1);
                    buffer.append(anArg);
                    if (isArray) {
                        buffer.append("[]");
                    }
                    if (x < (args.size() - 1)) {
                        buffer.append(", ");
                    }
                }
                buffer.append(')');
                model.setValueNoEvent(buffer.toString(), row, 1);
                buffer = new StringBuffer(128);

                // Notify the listeners that the table has changed.
                model.fireTableDataChanged();
            }
        } catch (IncompatibleThreadStateException itse) {
            model.setMessage(Bundle.getString("error.threadRunning"), 1);
        } catch (IndexOutOfBoundsException ioobe) {
            model.setMessage(Bundle.getString("error.noStackFrame"), 1);
        } catch (NativeMethodException nme) {
            model.setMessage(Bundle.getString("error.nativeMethod"), 1);
        } catch (VMDisconnectedException vmde) {
            return;
        }
    } // refresh

    /**
     * Custom JSwatTable to show custom tooltip.
     */
    protected class MethodsTable extends JSwatTable {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        private String defaultTip;

        /**
         * Constructs a MethodsTable with the specified models.
         *
         * @param  model   TableModel to use.
         * @param  colmod  TableColumnModel to use.
         */
        public MethodsTable(TableModel model, TableColumnModel colmod) {
            super(model, colmod);
            defaultTip = Bundle.getString("Methods.tooltip");
        } // MethodsTable

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  me  Mouse event.
         * @return  Custom tooltip text.
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
    } // MethodsTable

    /**
     * A Comparator for Method objects.
     *
     * @author  Nathan Fiedler
     */
    protected class MethodComparator implements Comparator {

        /**
         * Compares its two arguments for order. Returns a negative
         * integer, zero, or a positive integer as the first argument is
         * less than, equal to, or greater than the second.
         *
         * @param  o1  the first object to be compared.
         * @param  o2  the second object to be compared.
         * @return  a negative integer, zero, or a positive integer as
         *          the first argument is less than, equal to, or greater
         *          than the second.
         */
        public int compare(Object o1, Object o2) {
            Method m1 = (Method) o1;
            Method m2 = (Method) o2;
            String n1 = methodName(m1);
            String n2 = methodName(m2);
            return n1.compareTo(n2);
        } // compare
    } // MethodComparator
} // MethodsPanel
