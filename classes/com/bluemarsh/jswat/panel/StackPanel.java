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
 * $Id: StackPanel.java 14 2007-06-02 23:50:55Z nfiedler $
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
import com.bluemarsh.jswat.util.Names;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Class StackPanel is responsible for displaying a list of stack frames
 * in the debuggee VM's current thread.
 *
 * @author  Nathan Fiedler
 */
public class StackPanel extends AbstractPanel
    implements ContextListener, MouseListener {
    /** Number of column for stack frame. */
    private static final int NUMBER_COLUMN = 0;
    /** Number of column for method name. */
    private static final int METHOD_COLUMN = 1;
    /** Number of column for line number. */
    private static final int LINE_COLUMN = 2;
    /** Number of column for extra data. */
    private static final int INVISIBLE_COLUMN = 3;
    /** Our table component - displays the stack frames */
    private JTable table;
    /** Our UI component - scrollable panel */
    private JScrollPane uicomp;
    /** Thing that renders the rows of our table. */
    private StackRenderer stackRenderer;

    /**
     * Constructs a new StackPanel with the default table.
     */
    public StackPanel() {
        // Rebuild the table column model from user preferences.
        TableColumnModel colmod = new DefaultTableColumnModel();
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/panel/stack");
        int[] columnWidths = new int[] { 40, 150, 60 };
        String[] columnNames = new String[3];
        columnNames[0] = Bundle.getString("Stack.column.number");
        columnNames[1] = Bundle.getString("Stack.column.method");
        columnNames[2] = Bundle.getString("Stack.column.line");
        columnNames = restoreTable(colmod, prefs, columnWidths, columnNames);
        // Column names are now in adjusted order.

        // Construct a table model with one invisible column.
        table = new StackTable(new ViewTableModel(columnNames, 4), colmod);
        table.addMouseListener(this);
        table.setSelectionModel(null);
        ToolTipManager.sharedInstance().registerComponent(table);
        stackRenderer = new StackRenderer();
        table.setDefaultRenderer(Object.class, stackRenderer);
        uicomp = new JScrollPane(table);
    } // StackPanel

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
            "com/bluemarsh/jswat/panel/stack");
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
     * Describes the given stack frame into the table.
     *
     * @param  frame  StackFrame to describe.
     * @param  model  Table model to describe frame into.
     * @param  row    Row of table to describe to.
     */
    protected void describeFrame(StackFrame frame, ViewTableModel model,
                                 int row) {
        Location loc = frame.location();
        Method method = loc.method();
        StringBuffer buff = new StringBuffer(64);

        // Show the method class/interface type.
        String cname = method.declaringType().name();
        cname = Names.justTheName(cname);
        buff.append(cname);
        buff.append('.');
        buff.append(method.name());
        buff.append("()");
        model.setValueNoEvent(buff.toString(), row, METHOD_COLUMN);
        buff.setLength(0);

        if (method.isNative()) {
            // Method is native.
            buff.append("native method");
        } else if (loc.lineNumber() != -1) {
            // Write the source code line number.
            buff.append(Integer.toString(loc.lineNumber()));
        }
        model.setValueNoEvent(buff.toString(), row, LINE_COLUMN);
        buff.setLength(0);

        // Save the reference type for use for viewing source code.
        model.setValueNoEvent(method.declaringType(), row, INVISIBLE_COLUMN);
    } // describeFrame

    /**
     * Describes the given thread stack, placing the results in the
     * given table model.
     *
     * @param  thread  Thread to describe.
     * @param  model   Table model for stack description.
     * @throws  IncompatibleThreadStateException
     *          if the thread is running.
     */
    protected void describeStack(ThreadReference thread,
                                 ViewTableModel model)
        throws IncompatibleThreadStateException {
        List stack = thread.frames();
        if (stack != null) {
            int nFrames = stack.size();
            if (nFrames > 0) {
                for (int i = 0; i < nFrames; i++) {
                    StackFrame frame = (StackFrame) stack.get(i);
                    int row = model.addRow();
                    model.setValueNoEvent(Integer.toString(i + 1), row,
                                          NUMBER_COLUMN);
                    describeFrame(frame, model, row);
                }
            }
        }
    } // describeStack

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
     * Invoked when the mouse has been clicked on a component. We use
     * this to take action on the table.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            // Try to set the stack frame.
            try {
                ContextManager conman = (ContextManager)
                    owningSession.getManager(ContextManager.class);
                conman.setCurrentFrame(row);
            } catch (IncompatibleThreadStateException itse) {
                owningSession.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_ERROR,
                    Bundle.getString("Stack.error.threadNotSuspended"));
                return;
            } catch (IndexOutOfBoundsException ioobe) {
                // Silently ignore this.
                return;
            } catch (IllegalStateException ise) {
                // This happens when the current thread is not set.
                return;
            }

            // Try to open the source for this frame.
            ViewTableModel model = (ViewTableModel) table.getModel();
            ReferenceType clazz = (ReferenceType) model.getValueAt(
                row, INVISIBLE_COLUMN);
            if (clazz != null) {
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = null;
                try {
                    src = pathman.mapSource(clazz);
                } catch (IOException ioe) {
                    // This case is handled below.
                }
                if (src != null && src.exists()) {

                    // Try to show the source line for this location.
                    String linestr = (String)
                        model.getValueAt(row, LINE_COLUMN);
                    int linenum = 0;
                    if (linestr != null) {
                        try {
                            linenum = Integer.parseInt(linestr);
                        } catch (NumberFormatException nfe) {
                            // Defaults to zero then.
                        }
                    }

                    // Open source view.
                    UIAdapter adapter = owningSession.getUIAdapter();
                    if (!adapter.showFile(src, linenum, 0)) {
                        String msg = MessageFormat.format(
                            Bundle.getString("couldntOpenFileMsg"),
                            new Object[] { src.getName() });
                        owningSession.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_ERROR, msg);
                    }
                } else {
                    owningSession.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_ERROR,
                        Bundle.getString("couldntMapSrcFile"));
                    return;
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
        // Clear the table model.
        ViewTableModel model = (ViewTableModel) table.getModel();
        model.clear();

        // Get the current thread, if any.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thrd = conman.getCurrentThread();
        boolean isSuspended = false;
        if (thrd != null) {
            try {
                isSuspended = thrd.isSuspended();
                describeStack(thrd, model);
                if (model.getRowCount() > 0) {
                    // Set the highlighted row based on the frame number,
                    // but only when there are some frames.
                    int current = conman.getCurrentFrame();
                    stackRenderer.setCurrentFrame(current);
                } else {
                    model.setMessage(Bundle.getString("error.noStackFrame"),
                        METHOD_COLUMN);
                }
            } catch (IncompatibleThreadStateException itse) {
                if (isSuspended) {
                    model.setMessage(
                        Bundle.getString("error.illegalThreadState"),
                        METHOD_COLUMN);
                } else {
                    model.setMessage(Bundle.getString("error.threadRunning"),
                        METHOD_COLUMN);
                }
            } catch (ObjectCollectedException oce) {
                // Silently give up.
            } catch (InvalidStackFrameException isfe) {
                model.setMessage(Bundle.getString("error.noStackFrame"),
                    METHOD_COLUMN);
            } catch (VMDisconnectedException vmde) {
                // This happens a lot.
            }
        } else if (session.isActive()) {
            model.setMessage(Bundle.getString("error.threadNotSet"),
                METHOD_COLUMN);
        }

        // Notify the listeners that the table has changed.
        model.fireTableDataChanged();
    } // refresh

    /**
     * Custom JSwatTable to show custom tooltip.
     */
    protected class StackTable extends JSwatTable {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        private String defaultTip;

        /**
         * Constructs a StackTable with the specified models.
         *
         * @param  model   TableModel to use.
         * @param  colmod  TableColumnModel to use.
         */
        public StackTable(TableModel model, TableColumnModel colmod) {
            super(model, colmod);
            defaultTip = Bundle.getString("Stack.tooltip");
        } // StackTable

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
    } // StackTable

    /**
     * Class StackRenderer renders the display of frames in the Stacks
     * JSwat panel. The current frame is rendered in a different font
     * style so it stands out from the other frames.
     *
     * @author  Nathan Fiedler
     */
    class StackRenderer extends DefaultTableCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Current frame number as a String. */
        private String currentFrame;

        /**
         * Returns a reference to this component after setting the font
         * style based on whether this row corresponds to the current
         * frame. The current frame's row is drawn in red. Other rows
         * are drawn in the default style.
         *
         * @param  table       tree-table being rendered.
         * @param  value       value of the cell being rendered.
         * @param  isSelected  true if cell is selected.
         * @param  hasFocus    true if cell has focus.
         * @param  row         table row.
         * @param  column      table column.
         * @return  rendering component.
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            if (value != null) {
                ViewTableModel model = (ViewTableModel) table.getModel();
                String thisFrame = (String) model.getValueAt(
                    row, NUMBER_COLUMN);
                if (thisFrame != null && thisFrame.equals(currentFrame)) {
                    Font font = getFont();
                    font = font.deriveFont(Font.BOLD);
                    setFont(font);
                }
            }
            return this;
        } // getTableCellRendererComponent

        /**
         * Sets the current frame number.
         *
         * @param  frame  new frame number (zero-based).
         */
        public void setCurrentFrame(int frame) {
            // Add one to translate to one-based.
            currentFrame = String.valueOf(frame + 1);
        } // setCurrentFrame
    } // StackRenderer
} // StackPanel
