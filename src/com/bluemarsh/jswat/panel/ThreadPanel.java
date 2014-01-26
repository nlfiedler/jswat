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
 * $Id: ThreadPanel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.MenuSelectionManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Class ThreadPanel is responsible for displaying a table of
 * threads in the VM.
 *
 * @author  Nathan Fiedler
 */
public class ThreadPanel extends JSwatPanel implements VMEventListener, ContextListener {
    /** Table displaying the threads. */
    private JTable table;
    /** Our UI component - scrollable panel */
    private JScrollPane uicomp;
    /** Session that owns us. */
    private Session owningSession;
    /** table cell renderer */
    private ThreadRenderer renderer;
    // Constants for the table column numbers.
    private static final int ID_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int STATUS_COLUMN = 2;

    /**
     * Constructs a new ThreadPanel with the default table.
     */
    public ThreadPanel() {
        String[] columnNames = new String[3];
        columnNames[0] = Bundle.getString("Thread.idColumn");
        columnNames[1] = Bundle.getString("Thread.nameColumn");
        columnNames[2] = Bundle.getString("Thread.statusColumn");
        table = new JTable(new ViewTableModel(columnNames));
	renderer = new ThreadRenderer();
	table.setDefaultRenderer(Object.class, renderer);

        // Set the column widths to some hard-coded values.
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(20);
        column = table.getColumnModel().getColumn(2);
        column.setPreferredWidth(50);

        uicomp = new JScrollPane(table);

        // Set up a mouse listener to switch the sessions thread when
        // the thread is double clicked on in the panel
        MouseListener ml = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
		    int row = table.rowAtPoint(e.getPoint());
		    // Make sure to get column 0 from the _model_
		    // otherwise moving table columns will give us the
		    // wrong value.
		    String threadId  = (String) 
			table.getModel().getValueAt(row, 0);
		    setCurrentThread(threadId);
		    renderer.setCurrentThreadId(threadId);
		    table.updateUI();
                }
            };
        table.addMouseListener(ml);

        ThreadsPopup popup = new ThreadsPopup();
        table.add(popup);
        table.addMouseListener(popup);
    } // ThreadPanel

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Panels are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        VirtualMachine vm = session.getVM();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        // Listen to all thread start/death events.
        ThreadDeathRequest tdr =
            vm.eventRequestManager().createThreadDeathRequest();
        tdr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        tdr.enable();
        vmeman.addListener(ThreadDeathEvent.class, this,
                           VMEventListener.PRIORITY_DEFAULT);
        ThreadStartRequest tsr =
            vm.eventRequestManager().createThreadStartRequest();
        tsr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        tsr.enable();
        vmeman.addListener(ThreadStartEvent.class, this,
                           VMEventListener.PRIORITY_DEFAULT);
        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);
        // Have to explicitly update ourselves.
        refresh(session);
    } // activate

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
	if (cce.isType(ContextChangeEvent.TYPE_THREAD)) {
            try {
                refresh(owningSession);
            } catch (VMDisconnectedException vmde) {
                // this happens sometimes
                return;
            }
	}
    } // contextChanged

    /**
     * Called when the Session is closing down this panel, generally
     * just after the panel has been removed from the Session.
     *
     * @param  session  Session closing the panel.
     */
    public void close(Session session) {
        owningSession = null;
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Panels are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ThreadDeathEvent.class, this);
        vmeman.removeListener(ThreadStartEvent.class, this);

        // Update the UI to show nothing.
        ViewTableModel model = (ViewTableModel) table.getModel();
        synchronized (model) {
            model.clear();
        }
    } // deactivate

    /**
     * Builds out a thread description into the row of the given
     * table model.
     *
     * @param  model  Table model.
     * @param  thrd   Thread reference.
     */
    private void describeThread(ViewTableModel model,
                                ThreadReference thrd) {
        int row = model.addRow();
        model.setValueNoEvent(String.valueOf(thrd.uniqueID()), row,
                              ID_COLUMN);
        String statusStr;
        model.setValueNoEvent(thrd.name(), row, NAME_COLUMN);
        int statusInt = thrd.status();
        if (statusInt == ThreadReference.THREAD_STATUS_MONITOR) {
            statusStr = swat.getResourceString("threadStatusMonitor");
        } else if (statusInt == ThreadReference.THREAD_STATUS_RUNNING) {
            statusStr = swat.getResourceString("threadStatusRunning");
        } else if (statusInt == ThreadReference.THREAD_STATUS_SLEEPING) {
            statusStr = swat.getResourceString("threadStatusSleeping");
        } else if (statusInt == ThreadReference.THREAD_STATUS_WAIT) {
            statusStr = swat.getResourceString("threadStatusWait");
        } else if (statusInt == ThreadReference.THREAD_STATUS_ZOMBIE) {
            statusStr = swat.getResourceString("threadStatusZombie");
        } else if (statusInt ==
                   ThreadReference.THREAD_STATUS_NOT_STARTED) {
            statusStr = swat.getResourceString("threadStatusNotStarted");
        } else {
            statusStr = swat.getResourceString("threadStatusUnknown");
        }
        model.setValueNoEvent(statusStr, row, STATUS_COLUMN);
    } // describeThread

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        ViewTableModel model = (ViewTableModel)table.getModel();
        if (e instanceof ThreadDeathEvent) {
            ThreadReference thrd = ((ThreadDeathEvent)e).thread();
            String val = String.valueOf(thrd.uniqueID());
            // Must synchronize to avoid problems with events that
            // fire off asynchronously.
            synchronized (model) {
                // Remove the row with a matching thread ID value.
                model.removeRow(val, ID_COLUMN);
            }
        } else if (e instanceof ThreadStartEvent) {
            ThreadReference thrd = ((ThreadStartEvent)e).thread();
            String val = String.valueOf(thrd.uniqueID());
            // Must synchronize to avoid problems with events that
            // fire off asynchronously.
            synchronized (model) {
                // Check if the thread has already been added.
                // This happens for threads that exist at the time the
                // table is refreshed, and are then started when the
                // debuggee VM resumes.
                if (!model.rowExists(val, ID_COLUMN)) {
                    try {
                        describeThread(model, thrd);
                        // Notify the listeners that the table has changed.
                        model.fireTableDataChanged();
                    } catch (VMDisconnectedException vmde) {
                        // This happens to the thread panel frequently.
                        return false;
                    } catch (ObjectCollectedException oce) {
                        // Oh well, remove it then I guess.
                        model.removeRow(val, ID_COLUMN);
                    }
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
     * Called when the Session is ready to initialize this panel,
     * generally just after the panel has been added to the Session.
     *
     * @param  session  Session initializing this panel.
     */
    public void init(Session session) {
        owningSession = session;
    } // init

    /**
     * Update the display on the screen. Use the given VM
     * to fetch the desired data.
     *
     * @param  session  Debugging Session object.
     */
    public void refresh(Session session) {
        // Clear the table model.
        ViewTableModel model = (ViewTableModel) table.getModel();

        // Set the current thread in the thread renderer.
	ThreadReference threadRef = session.getCurrentThread();
	if (threadRef != null) {
	    long uniqueID = threadRef.uniqueID();
	    String idString = String.valueOf(uniqueID);
	    renderer.setCurrentThreadId(idString);
	} else {
	    renderer.setCurrentThreadId(null);
        }

        // Must synchronize to avoid problems with events that
        // fire off asynchronously.
        synchronized (model) {
            model.clear();

            // Get the list of all threads in the VM.
            VirtualMachine vm = session.getVM();
            if (vm != null) {
                List threadList = vm.allThreads();
                Iterator iter = threadList.iterator();
                model.ensureCapacity(threadList.size());
                while (iter.hasNext()) {
                    ThreadReference thrd = (ThreadReference) iter.next();
                    try {
                        describeThread(model, thrd);
                    } catch (ObjectCollectedException oce) {
                        // Oh well, thread is gone.
                    }
                }
            }
        }

        // Notify the listeners that the table has changed.
        model.fireTableDataChanged();
    } // refresh

    /**
     * Sets the session's thread to be the given threadId.
     *
     * @param threadId a <code>String</code> value
     */
    private void setCurrentThread(String threadId) {
        try {
            // Find the thread by the ID number.
            ThreadReference thread = ThreadUtils.getThreadByID(
                owningSession, threadId);
            if (thread != null) {
                // Set the current thread.
                ContextManager contextManager = (ContextManager)
                    owningSession.getManager(ContextManager.class);
                contextManager.setCurrentThread(thread);
                owningSession.getStatusLog().writeln(
                    Bundle.getString("Thread.currentThreadSet") + ' ' +
                    threadId);
            } else {
                // This simply cannot happen.
                owningSession.getStatusLog().writeln(
                    swat.getResourceString("threadNotFound") + ' ' + threadId);
            }
        } catch (NotActiveException nae) {
            // This could not happen.
            nae.printStackTrace();
	}
    } // setCurrentThread

    /**
     * Class ThreadRenderer renders the display of threads in the threads
     * JSwat panel. The current thread is painted in a different color so
     * it stands out from the other threads.
     *
     * @author  Bill Smith
     */
    class ThreadRenderer extends DefaultTableCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Current thread's id. Should always be non-null. */
        private String currentThreadId = "";

        /**
         * Returns a reference to this component after setting the foreground
         * color based on whether this row corresponds to the current thread.
         * The current thread's row is drawn in red.  Other rows are drawn
         * in the default color.
         *<p>
         * See javax.swing.table.DefaultTableCellRenderer for a description of
         * the parameters.
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus,row,column);
            if (value != null) {
                String thisThreadId = (String)
                    table.getModel().getValueAt(row, 0);
                if (currentThreadId.equals(thisThreadId)) {
                    setForeground(Color.red);
                } else {
                    // Have to reset the cell's color in case it used
                    // to be red.
                    setForeground(null);
                }
            }
            return this;
        } // getTableCellRendererComponent

        /**
         * Sets the current thread id.
         *
         * @param  currentThreadId	The current thread's id.
         */
        public void setCurrentThreadId(String currentThreadId) {
            if (currentThreadId == null) {
                this.currentThreadId = "";
            } else {
                this.currentThreadId = currentThreadId;
            }
        } // setCurrentThreadId
    } // ThreadRenderer

    /**
     * Class ThreadsPopup is a popup menu that allows the user to
     * suspend, resume, and interrupt threads in the debuggee.
     */
    protected class ThreadsPopup extends JPopupMenu implements ActionListener, MouseListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Menu item to resume all threads. */
        protected JMenuItem resumeAllItem;
        /** Menu item to suspend all threads. */
        protected JMenuItem suspendAllItem;
        /** Menu item to resume one thread. */
        protected JMenuItem resumeOneItem;
        /** Menu item to suspend one thread. */
        protected JMenuItem suspendOneItem;
        /** Menu item to interrupt one thread. */
        protected JMenuItem interruptOneItem;
        /** Menu item indicating user did not click on a thread. */
        protected JMenuItem badRowItem;
        /** Menu item indicating session is inactive. */
        protected JMenuItem inactiveSessionItem;
        /** Thread the user clicked on. */
        protected ThreadReference selectedThread;

        /**
         * Constructs an ThreadsPopup that interacts with the panel.
         *
         * @param  panel  the watch panel.
         */
        ThreadsPopup() {
            super(Bundle.getString("ThreadsPopup.label"));

            resumeAllItem = new JMenuItem(
                Bundle.getString("ThreadsPopup.resumeAll"));
            resumeAllItem.addActionListener(this);
            suspendAllItem = new JMenuItem(
                Bundle.getString("ThreadsPopup.suspendAll"));
            suspendAllItem.addActionListener(this);

            resumeOneItem = new JMenuItem("N/A");
            resumeOneItem.addActionListener(this);
            suspendOneItem = new JMenuItem("N/A");
            suspendOneItem.addActionListener(this);
            interruptOneItem = new JMenuItem("N/A");
            interruptOneItem.addActionListener(this);

            badRowItem = new JMenuItem(
                Bundle.getString("ThreadsPopup.badRow"));
            inactiveSessionItem = new JMenuItem(
                Bundle.getString("ThreadsPopup.inactiveSession"));
        } // ThreadsPopup

        /**
         * Invoked when a menu item has been selected.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            Session session = SessionFrameMapper.getSessionForEvent(e);
            Object src = e.getSource();
            if (src == resumeAllItem) {
                try {
                    session.resumeVM();
                } catch (NotActiveException nae) {
                    // ignored
                }
            } else if (src == suspendAllItem) {
                try {
                    session.suspendVM();
                } catch (NotActiveException nae) {
                    // ignored
                }

            } else if (selectedThread != null) {
                if (src == resumeOneItem) {
                    selectedThread.resume();
                } else if (src == suspendOneItem) {
                    selectedThread.suspend();
                } else if (src == interruptOneItem) {
                    selectedThread.interrupt();
                }
            } else {
                session.getStatusLog().writeln(
                    "Thread panel popup menu item selected without thread.");
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
         * Set the popup menu items enabled or disabled depending on
         * which line of the source view area the mouse button has
         * been pressed.
         *
         * @param  e        Mouse event.
         * @param  session  Session to operate on.
         */
        protected void setMenuItemsForEvent(MouseEvent e, Session session) {
            // Reset the popup menu by removing all children.
            removeAll();

            // Use mouse position to determine thread.
            int row = table.rowAtPoint(e.getPoint());
            String threadId  = (String) table.getModel().getValueAt(row, 0);
            selectedThread = null;
            try {
                selectedThread = ThreadUtils.getThreadByID(session, threadId);
            } catch (NotActiveException nae) {
                // Table is only visible when the session is active,
                // so this is next to impossible.
                add(inactiveSessionItem);
                return;
            }

            if (selectedThread == null) {
                // Popup only appears on the table rows,
                // so this is next to impossible.
                add(badRowItem);
                return;
            }

            if (selectedThread.isSuspended()) {
                resumeOneItem.setText(
                    Bundle.getString("ThreadsPopup.resume") + ' ' +
                    selectedThread.uniqueID());
                add(resumeOneItem);
            } else {
                suspendOneItem.setText(
                    Bundle.getString("ThreadsPopup.suspend") + ' ' +
                    selectedThread.uniqueID());
                add(suspendOneItem);
                interruptOneItem.setText(
                    Bundle.getString("ThreadsPopup.interrupt") + ' ' +
                    selectedThread.uniqueID());
                add(interruptOneItem);
            }

            // Add the 'all' menu items.
            add(resumeAllItem);
            add(suspendAllItem);
        } // setMenuItemsForEvent

        /**
         * Decide whether or not to show the popup menu.
         *
         * @param  e  Mouse event.
         */
        protected void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                // Build out the popup menu.
                Session session = SessionFrameMapper.getSessionForEvent(e);
                setMenuItemsForEvent(e, session);
                // Show the popup menu.
                show(e.getComponent(), e.getX(), e.getY());
                e.consume();
            } else {
                // Process the mouse event normally.
                MenuSelectionManager.defaultManager().processMouseEvent(e);
                // Make the menu disappear.
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        } // showPopup
    } // ThreadsPopup
} // ThreadPanel
