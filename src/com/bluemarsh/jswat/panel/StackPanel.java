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
 * $Id: StackPanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Class StackPanel is responsible for displaying a list of
 * stack frames in the debuggee VM's current thread.
 *
 * @author  Nathan Fiedler
 */
public class StackPanel extends JSwatPanel implements ContextListener, MouseListener {
    /** Our table component - displays the stack frames */
    protected JTable table;
    /** Our UI component - scrollable panel */
    protected JScrollPane uicomp;
    /** Handy reference to the Session that owns us. */
    protected Session owningSession;
    /** Thing that renders the rows of our table. */
    protected StackRenderer stackRenderer;
    /** Handy reference to the Session's ContextManager. */
    protected ContextManager contextManager;
    // Constants for the table column numbers.
    private static final int NUMBER_COLUMN = 0;
    private static final int METHOD_COLUMN = 1;
    private static final int LINE_COLUMN = 2;
    private static final int INVISIBLE_COLUMN = 3;

    /**
     * Constructs a new StackPanel with the default table.
     */
    public StackPanel() {
        String[] columnNames = new String[3];
        columnNames[0] = Bundle.getString("Stack.numberColumn");
        columnNames[1] = Bundle.getString("Stack.methodColumn");
        columnNames[2] = Bundle.getString("Stack.lineColumn");
        // Construct a table model with one invisible column.
        table = new JTable(new ViewTableModel(columnNames, 4));
        table.addMouseListener(this);
	stackRenderer = new StackRenderer();
	table.setDefaultRenderer(Object.class, stackRenderer);

        // Set the column widths to some hard-coded values.
        TableColumn column = table.getColumnModel().getColumn(0);
        // Give the number column a reasonable maximum width so it
        // doesn't resize way too big.
        column.setMaxWidth(80);
        column.setPreferredWidth(40);
        column = table.getColumnModel().getColumn(2);
        column.setMaxWidth(120);
        column.setPreferredWidth(60);

        uicomp = new JScrollPane(table);
    } // StackPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        // Add ourselves as a context change listener.
        contextManager.addContextListener(this);
    } // activate

    /**
     * Called when the Session is closing down this panel, generally
     * just after the panel has been removed from the Session.
     *
     * @param  session  Session closing the panel.
     */
    public void close(Session session) {
        owningSession = null;
        contextManager = null;
    } // close

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
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
        // Remove ourselves as a context change listener.
        contextManager.removeContextListener(this);

        // Update the UI to show nothing.
        ViewTableModel model = (ViewTableModel)table.getModel();
        synchronized (model) {
            model.clear();
        }
    } // deactivate

    /**
     * Describes the given stack frame into the table.
     *
     * @param  frame  StackFrame to describe.
     * @param  model  Table model to describe frame into.
     * @param  row    Row of table to describe to.
     */
    protected void describeFrame(StackFrame frame,
                                 ViewTableModel model,
                                 int row) {
        Location loc = frame.location();
        Method method = loc.method();
        StringBuffer buff = new StringBuffer(64);

        // Show the method class/interface type.
        String cname = method.declaringType().name();
        cname = ClassUtils.justTheName(cname);
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
     * Describes the given thread stack, placing the results in
     * the given table model.
     *
     * @param  thread  Thread to describe.
     * @param  model   Table model for stack description.
     */
    protected void describeStack(ThreadReference thread,
                                 ViewTableModel model) {
        List stack = null;
        // Check for possible error conditions.
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            // Silently give up.
            return;
        } catch (ObjectCollectedException oce) {
            // Silently give up.
            return;
        }
        if (stack == null) {
            return;
        }

        // Find the number of stack frames.
        int nFrames = stack.size();
        if (nFrames == 0) {
            return;
        }

        // For each stack frame, display its information.
        for (int i = 0; i < nFrames; i++) {
            StackFrame frame = (StackFrame)stack.get(i);
            int row = model.addRow();
            model.setValueNoEvent(Integer.toString(i + 1), row,
                                  NUMBER_COLUMN);
            describeFrame(frame, model, row);
        }
    } // describeStack

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
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        owningSession = session;
        contextManager = (ContextManager)
            session.getManager(ContextManager.class);
    } // init

    /**
     * Invoked when the mouse has been clicked on a component.
     * We use this to take action on the table.
     *
     * @param  e  Mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            // Try to set the stack frame.
            try {
                contextManager.setCurrentFrame(row);
            } catch (IncompatibleThreadStateException itse) {
                JOptionPane.showMessageDialog
                    (null, swat.getResourceString("threadNotSuspended"),
                     swat.getResourceString("Dialog.Error.title"),
                     JOptionPane.ERROR_MESSAGE);
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
            ReferenceType clazz = (ReferenceType)
                model.getValueAt(row, INVISIBLE_COLUMN);
            if (clazz != null) {
                PathManager pathman = (PathManager)
                    owningSession.getManager(PathManager.class);
                SourceSource src = null;
                try {
                    src = pathman.mapSource(clazz);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                if (src != null) {

                    // Try to show the source line for this location.
                    String linestr = (String)
                        model.getValueAt(row, LINE_COLUMN);
                    int linenum = 0;
                    if (linestr != null) {
                        try {
                            linenum = Integer.parseInt(linestr);
                        } catch (NumberFormatException nfe) {
                        }
                    }

                    // Open source view.
                    UIAdapter adapter = owningSession.getUIAdapter();
                    adapter.showFile(src, linenum, 0);
                } else {
                    JOptionPane.showMessageDialog
                        (null, swat.getResourceString("couldntMapSrcFile"),
                         swat.getResourceString("Dialog.Error.title"),
                         JOptionPane.ERROR_MESSAGE);
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
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        // Clear the table model.
        ViewTableModel model = (ViewTableModel) table.getModel();

        // Must synchronize to avoid problems with events that
        // fire off asynchronously.
        synchronized (model) {
            model.clear();

            // Get the current thread, if any.
            ThreadReference thrd = contextManager.getCurrentThread();
            if (thrd != null) {
                try {
                    describeStack(thrd, model);
                    if (model.getRowCount() > 0) {
                        // Set the highlighted row based on the frame number,
                        // but only when there are some frames.
                        int current = contextManager.getCurrentFrame();
                        stackRenderer.setCurrentFrame(current);
                    }
                } catch (InvalidStackFrameException isfe) {
                    model.setMessage(Bundle.getString("noframeInParen"),
                                     METHOD_COLUMN);
                }
            } else if (session.isActive()) {
                model.setMessage(Bundle.getString("nothreadInParen"),
                                 METHOD_COLUMN);
            }
        }

        // Notify the listeners that the table has changed.
        model.fireTableDataChanged();
    } // refresh

    /**
     * Class StackRenderer renders the display of frames in the Stacks
     * JSwat panel. The current frame is painted in a different color so
     * it stands out from the other frames.
     *
     * @author  Nathan Fiedler
     */
    class StackRenderer extends DefaultTableCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Current frame number as a String. */
        protected String currentFrame;

        /**
         * Returns a reference to this component after setting the foreground
         * color based on whether this row corresponds to the current frame.
         * The current frame's row is drawn in red.  Other rows are drawn
         * in the default color.
         *
         * <p>See javax.swing.table.DefaultTableCellRenderer for a description
         * of the parameters.</p>
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus,row,column);
            if (value != null) {
                String thisFrame = (String)
                    table.getModel().getValueAt(row, NUMBER_COLUMN);
                if (thisFrame != null && thisFrame.equals(currentFrame)) {
                    setForeground(java.awt.Color.red);
                } else {
                    setForeground(null);
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
