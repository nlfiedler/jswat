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
 * $Id: ThreadPanel.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.panel.ViewTreeTableModel.Node;
import com.bluemarsh.jswat.ui.JTreeTable;
import com.bluemarsh.jswat.ui.SmartPopupMenu;
import com.bluemarsh.jswat.ui.TreeTableModel;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Class ThreadPanel is responsible for displaying a table of threads.
 *
 * @author  Nathan Fiedler
 */
public class ThreadPanel extends AbstractPanel
    implements ContextListener, MouseListener, VMEventListener {
    /** Thread identifier column number. */
    private static final int ID_COLUMN = 0;
    /** Thread name column number. */
    private static final int NAME_COLUMN = 1;
    /** Thread status column number. */
    private static final int STATUS_COLUMN = 2;
    /** Tree-table, displays the threads. */
    private JTreeTable treeTable;
    /** Our UI component - scrollable panel */
    private JScrollPane uicomp;
    /** table cell renderer */
    private ThreadRenderer renderer;
    /** Thread death event request. */
    private ThreadDeathRequest deathRequest;
    /** Thread start event request. */
    private ThreadStartRequest startRequest;
    /** Our user preferences node. */
    private Preferences preferences;
    /** True if we are automatically refreshing the tree. */
    private boolean autoRefreshing;
    /** List of threads that recently started and have not yet been
     * integrated into the tree-table. */
    private Vector startedThreads;
    /** List of threads that recently died and have not yet been removed
     * from the tree-table. */
    private Vector deadThreads;

    /**
     * Constructs a new ThreadPanel with the default table.
     */
    public ThreadPanel() {
        // Rebuild the table column model from user preferences.
        TableColumnModel colmod = new DefaultTableColumnModel();
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/panel/thread");
        int[] columnWidths = { 20, 75, 50 };
        String[] columnNames = new String[3];
        columnNames[ID_COLUMN] = Bundle.getString("Thread.column.id");
        columnNames[NAME_COLUMN] = Bundle.getString("Thread.column.name");
        columnNames[STATUS_COLUMN] = Bundle.getString("Thread.column.status");
        columnNames = restoreTable(colmod, preferences, columnWidths,
                                   columnNames);
        // Column names are now in adjusted order.
        Class[] columnTypes = { TreeTableModel.class,
                                String.class,
                                String.class };

        // Create the tree-table and all that goes with it.
        TreeTableModel treeTableModel = new ThreadModel(
            columnNames, columnTypes);
        treeTable = new ThreadTreeTable(treeTableModel);
        treeTable.addMouseListener(this);
        treeTable.setColumnModel(colmod);
        treeTable.setSelectionModel(null);
        JTree tree = treeTable.getTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        renderer = new ThreadRenderer();
        treeTable.setDefaultRenderer(String.class, renderer);
        uicomp = new JScrollPane(treeTable);
        ToolTipManager.sharedInstance().registerComponent(treeTable);

        // Add the popup menu.
        ThreadsPopup popup = new ThreadsPopup();
        treeTable.addMouseListener(popup);

        startedThreads = new Vector();
        deadThreads = new Vector();
    } // ThreadPanel

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        // Clean out the thread queues before we start listening for
        // thread events (otherwise leftover thread deaths from the
        // previous session will cause the panel to be left empty).
        startedThreads.clear();
        deadThreads.clear();

        enableAutoRefresh(preferences.getBoolean("threads_autoRefresh", true));

        // Add ourselves as a context change listener.
        ContextManager ctxtMgr = (ContextManager)
            owningSession.getManager(ContextManager.class);
        ctxtMgr.addContextListener(this);

        // Have to explicitly update ourselves.
        refreshLater();
    } // activated

    /**
     * Invoked when the current context has changed. The context change
     * event identifies which aspect of the context has changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        if (cce.isType(ContextChangeEvent.TYPE_THREAD)) {
            ContextManager contextManager = (ContextManager)
                owningSession.getManager(ContextManager.class);
            ThreadReference thrd = contextManager.getCurrentThread();
            if (thrd != null) {
                renderer.setCurrentThreadId(String.valueOf(thrd.uniqueID()));
            } else {
                renderer.setCurrentThreadId(null);
            }
            treeTable.repaint();
        }
    } // contextChanged

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
        saveTable(treeTable.getColumnModel(), preferences);
        super.closing(sevt);
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
        enableAutoRefresh(false);

        // Update the UI to show nothing.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ThreadModel model = (ThreadModel)
                        treeTable.getTreeTableModel();
                    model.clear();
                }
            });
    } // deactivated

    /**
     * Builds out a thread description and adds it to the tree-table.
     *
     * @param  model  tree-table model.
     * @param  thrd   thread reference.
     */
    protected void describeThread(ThreadModel model, ThreadReference thrd) {
        boolean hideZombies = preferences.getBoolean(
            "hideZombies", Defaults.THREADS_HIDE_ZOMBIES);
        if (hideZombies
            && thrd.status() == ThreadReference.THREAD_STATUS_ZOMBIE) {
            // Do not display zombie threads.
            return;
        }

        Object node = model.addPath(thrd);

        // See if we are to expand the newly created groups.
        boolean expandAll = preferences.getBoolean(
            "alwaysExpandAll", Defaults.THREADS_EXPAND_ALL);
        if (expandAll) {
            // Cannot expand leaves, so use the last parent node.
            TreePath path = new TreePath(model.getPathToRoot(
                                             ((TreeNode) node).getParent()));
            treeTable.getTree().expandPath(path);
        }

        String name = thrd.name();
        if (name == null || name.length() == 0) {
            name = Bundle.getString("Thread.noname");
        }
        model.setValueAt(name, node, NAME_COLUMN);
        model.setValueAt(String.valueOf(thrd.uniqueID()), node, ID_COLUMN);
        String status = Threads.threadStatus(thrd);
        model.setValueAt(status, node, STATUS_COLUMN);
    } // describeThread

    /**
     * Enable or disable the automatic refresh feature.
     *
     * @param  auto  true to automatically refresh; false otherwise.
     */
    protected void enableAutoRefresh(boolean auto) {
        if (auto && !autoRefreshing) {
            // Create the thread start/death requests.
            VirtualMachine vm = owningSession.getConnection().getVM();
            EventRequestManager erm = vm.eventRequestManager();
            deathRequest = erm.createThreadDeathRequest();
            deathRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            deathRequest.enable();
            startRequest = erm.createThreadStartRequest();
            startRequest.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            startRequest.enable();

            // Register for the thread events.
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.addListener(ThreadDeathEvent.class, this,
                               VMEventListener.PRIORITY_DEFAULT);
            vmeman.addListener(ThreadStartEvent.class, this,
                               VMEventListener.PRIORITY_DEFAULT);
        } else if (!auto && autoRefreshing) {
            VMEventManager vmeman = (VMEventManager)
                owningSession.getManager(VMEventManager.class);
            vmeman.removeListener(ThreadDeathEvent.class, this);
            vmeman.removeListener(ThreadStartEvent.class, this);
        }
        autoRefreshing = auto;
    } // enableAutoRefresh

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        EventRequest er = e.request();
        if (er.equals(startRequest) || er.equals(deathRequest)
            && autoRefreshing) {
            // Add the thread reference to the end of the queue.
            if (e instanceof ThreadStartEvent) {
                // Put the starting threads in one queue...
                startedThreads.add(((ThreadStartEvent) e).thread());
            } else if (e instanceof ThreadDeathEvent) {
                // and the dying threads in another.
                deadThreads.add(((ThreadDeathEvent) e).thread());
            }
            // Cause the actual update to happen later.
            refreshLater();
        }
        return true;
    } // eventOccurred

    /**
     * Integrates the recently started and terminated threads with the
     * existing tree-table model.
     */
    protected void integrateRecent() {
        // Indicates if we must re-expand the branches again.
        boolean expandAll = false;
        ThreadModel model = (ThreadModel) treeTable.getTreeTableModel();

        // Process the dead threads first so we reduce the clutter.
        while (!deadThreads.isEmpty()) {
            ThreadReference thrd = (ThreadReference) deadThreads.remove(0);
            Object node = model.findNode(thrd);
            if (node != null) {
                model.removeAndPrune((MutableTreeNode) node);
                expandAll = true;
            }
        }

        // Now process the new threads by adding them to the tree.
        while (!startedThreads.isEmpty()) {
            ThreadReference thrd = (ThreadReference) startedThreads.remove(0);
            Object node = model.findNode(thrd);
            // Must look for the thread first to avoid adding an
            // existing thread more than once. This happens if a
            // thread existed at startup, then later starts running.
            if (node == null) {
                try {
                    describeThread(model, thrd);
                } catch (ObjectCollectedException oce) {
                    // Find it and remove it.
                    node = model.findNode(thrd);
                    if (node != null) {
                        model.removeAndPrune((MutableTreeNode) node);
                        expandAll = true;
                    }
                }
            }
        }

        if (expandAll) {
            // For some unknown reason, the tree will collapse some
            // branches when nodes are removed.
            expandAll = preferences.getBoolean(
                "alwaysExpandAll", Defaults.THREADS_EXPAND_ALL);
            if (expandAll) {
                JTree threadTree = treeTable.getTree();
                for (int row = 0; row < threadTree.getRowCount(); row++) {
                    threadTree.expandRow(row);
                }
            }
        }
    } // integrateRecent

    /**
     * Returns a reference to the peer UI component. In many
     * cases this is a JList, JTree, or JTable, depending on
     * the type of data being displayed in the panel.
     *
     * @return  peer ui component object
     */
    public JComponent getPeer() {
        return treeTable;
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
     * User is attempting to switch threads.
     *
     * @param  e  mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        int row = treeTable.rowAtPoint(e.getPoint());
        TreePath path = treeTable.getTree().getPathForRow(row);
        ThreadModel model = (ThreadModel) treeTable.getTreeTableModel();
        // We only consider threads, not thread groups.
        if (model.getChildCount(path.getLastPathComponent()) == 0) {
            // Make sure to get ID_COLUMN from the model otherwise moving
            // table columns will give us the wrong value.
            String threadId  = (String) model.getValueAt(path, ID_COLUMN);
            renderer.setCurrentThreadId(threadId);
            setCurrentThread(threadId);
            // We don't need to do this because setCurrentThread() will
            // fire off an event that causes us to refresh anyway.
            //treeTable.tableChanged(new TableModelEvent(
            //                           treeTable.getModel(), row, row,
            //                           ID_COLUMN, TableModelEvent.UPDATE));
        }
    } // mouseClicked

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param  e  mouse event.
     */
    public void mousePressed(MouseEvent e) { }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param  e  mouse event.
     */
    public void mouseReleased(MouseEvent e) { }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param  e  mouse event.
     */
    public void mouseEntered(MouseEvent e) { }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param  e  mouse event.
     */
    public void mouseExited(MouseEvent e) { }

    /**
     * Rebuilds the entire tree-table model to represent all of the
     * threads and their groups.
     */
    protected void rebuildTree() {
        // Set the current thread in the thread renderer.
        ContextManager contextManager = (ContextManager)
            owningSession.getManager(ContextManager.class);
        ThreadReference threadRef = contextManager.getCurrentThread();
        if (threadRef != null) {
            renderer.setCurrentThreadId(
                String.valueOf(threadRef.uniqueID()));
        } else {
            renderer.setCurrentThreadId(null);
        }

        // Remember the expanded tree paths.
        ThreadModel model = (ThreadModel) treeTable.getTreeTableModel();
        Object rootNode = model.getRoot();
        TreePath rootPath = new TreePath(rootNode);
        JTree threadTree = treeTable.getTree();
        Enumeration expaths = threadTree.getExpandedDescendants(rootPath);
        // Copy these to be safe in case the enumeration chokes when
        // the backing data source changes.
        List expandedPaths = null;
        if (expaths != null) {
            expandedPaths = new ArrayList();
            while (expaths.hasMoreElements()) {
                expandedPaths.add(expaths.nextElement());
            }
        }

        // Must synchronize to avoid problems with events that
        // fire off asynchronously.
        model.clear();

        if (owningSession.isActive()) {
            // Get the list of all threads in the VM.
            VirtualMachine vm = owningSession.getConnection().getVM();
            if (vm != null) {
                try {
                    List threadList = vm.allThreads();
                    Iterator iter = threadList.iterator();
                    while (iter.hasNext()) {
                        ThreadReference thrd =
                            (ThreadReference) iter.next();
                        describeThread(model, thrd);
                    }
                } catch (ObjectCollectedException oce) {
                    // Oh well, thread is gone.
                }
            }
        }

        // Weird, but this is what has to be done.
        threadTree.setRootVisible(true);
        threadTree.expandRow(0);
        threadTree.setRootVisible(false);

        // Expand all the previously expanded paths.
        if (expandedPaths != null) {
            for (int i = 0; i < expandedPaths.size(); i++) {
                threadTree.expandPath((TreePath) expandedPaths.get(i));
            }
        }

        // Make the current thread visible.
        if (threadRef != null) {
            MutableTreeNode node = (MutableTreeNode)
                model.findNodeFast(threadRef);
            if (node != null) {
                TreePath path = new TreePath(model.getPathToRoot(node));
                threadTree.makeVisible(path);
            }
        }

        // Notify the listeners that the table has changed.
        AbstractTableModel tmodel = (AbstractTableModel)
            treeTable.getModel();
        tmodel.fireTableDataChanged();
    } // rebuildTree

    /**
     * Update the display on the screen. Use the given Session to fetch
     * the desired data. This must be run on the AWT event dispatching
     * thread.
     *
     * @param  session  session for which to refresh.
     */
    public void refresh(Session session) {
        try {
            if (startedThreads.isEmpty() && deadThreads.isEmpty()) {
                rebuildTree();
            } else {
                integrateRecent();
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // This happens sometimes, especially during unit tests.
        } catch (VMDisconnectedException vmde) {
            // This happens a lot.
        }
    } // refresh

    /**
     * Sets the session's thread to be the given threadId.
     *
     * @param threadId a <code>String</code> value
     */
    protected void setCurrentThread(String threadId) {
        // Find the thread by the ID number.
        VirtualMachine vm = owningSession.getConnection().getVM();
        ThreadReference thread = Threads.getThreadByID(vm, threadId);
        if (thread != null) {
            // Set the current thread.
            ContextManager contextManager = (ContextManager)
                owningSession.getManager(ContextManager.class);
            contextManager.setCurrentThread(thread);
            owningSession.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_NOTICE,
                Bundle.getString("Thread.currentThreadSet") + ' '
                + threadId);
        }
        // Else, it is a thread group and we don't care.
    } // setCurrentThread

    /**
     * Class ThreadTreeTable extends JTreeTable in order to provide
     * tooltips.
     */
    class ThreadTreeTable extends JTreeTable {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Default tooltip text. */
        private String defaultTip;

        /**
         * Constructs a tree table for the threads panel.
         *
         * @param  model  tree-table model.
         */
        public ThreadTreeTable(TreeTableModel model) {
            super(model);
            defaultTip = Bundle.getString("Thread.tooltip");
        } // ThreadTreeTable

        /**
         * Returns custom tooltip text for this tree.
         *
         * @param  me  Mouse event.
         * @return  Custom tooltip text.
         */
        public String getToolTipText(MouseEvent me) {
            int row = rowAtPoint(me.getPoint());
            TreePath path = treeTable.getTree().getPathForRow(row);
            String value = null;
            if (path != null) {
                int col = columnAtPoint(me.getPoint());
                ThreadModel model = (ThreadModel) getTreeTableModel();
                value = (String) model.getValueAt(path, col);
            }
            if (value == null || value.length() == 0) {
                return defaultTip;
            } else {
                return value;
            }
        } // getToolTipText
    } // ThreadTreeTable

    /**
     * Class ThreadRenderer renders the display of threads in the threads
     * JSwat panel. The current thread is painted in a different font style
     * so it stands out from the other threads. Thread groups (nodes with
     * children) are drawn with the name in bold.
     *
     * @author  Bill Smith
     * @author  Nathan Fiedler
     */
    class ThreadRenderer extends DefaultTableCellRenderer {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Current thread's id. Should always be non-null. */
        private String currentThreadId = "";

        /**
         * Returns a reference to this component after setting the font
         * style based on whether this row corresponds to the current
         * thread. The current thread's row is drawn in red. Other rows are
         * drawn in the default style.
         *
         * @param  table       table being rendered.
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
            super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (column == NAME_COLUMN) {
                TreePath path = treeTable.getTree().getPathForRow(row);
                // Somehow, sometimes, path can be null.
                if (path != null) {
                    ThreadModel model = (ThreadModel)
                        treeTable.getTreeTableModel();
                    // Make sure to get ID_COLUMN from the model otherwise
                    // moving table columns will give us the wrong value.
                    String threadId  = (String) model.getValueAt(
                        path, ID_COLUMN);
                    Font font = getFont();
                    if (currentThreadId.equals(threadId)) {
                        font = font.deriveFont(Font.ITALIC);
                    }
                    // If the node is a non-leaf, draw it in bold. Leaf nodes
                    // are threads while parent nodes are thread groups.
                    if (model.getChildCount(path.getLastPathComponent()) > 0) {
                        font = font.deriveFont(Font.BOLD);
                    }
                    setFont(font);
                }
            }
            return this;
        } // getTableCellRendererComponent

        /**
         * Sets the current thread id.
         *
         * @param  currentThreadId  the current thread's id.
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
     * Class ThreadModel is a specialized tree-table model that deals
     * with threads.
     */
    protected class ThreadModel extends ViewTreeTableModel {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs an empty ThreadModel. The number of columns in the
         * table is determined by the length of the column names array.
         *
         * @param  colNames  names for the columns.
         * @param  colTypes  types for the columns.
         */
        public ThreadModel(String[] colNames, Class[] colTypes) {
            super(colNames, colTypes);
        } // ThreadModel

        /**
         * Builds out the appropriate tree path and ensures that this
         * thread is a part of the tree model. If the path already
         * exists, nothing will have changed.
         *
         * @param  thrd  thread to be added to tree.
         * @return  newly added node.
         */
        public Node addPath(ThreadReference thrd) {
            // Make a list of the parent groups, starting from the top.
            Stack stack = new Stack();
            ThreadGroupReference group = thrd.threadGroup();
            while (group != null) {
                stack.push(group);
                group = group.parent();
            }

            Node node = (Node) getRoot();
            // Ensure that a node for each parent group exists.
            while (!stack.empty()) {
                group = (ThreadGroupReference) stack.pop();
                String tid = String.valueOf(group.uniqueID());
                // Find a node by this unique ID.
                Node next = (Node) node.getChild(ID_COLUMN, tid);
                if (next == null) {
                    // Node doesn't exist, create it now.
                    next = new Node();
                    // Insert with event notification.
                    insertNodeInto(next, node, 0);
                    next.setData(ID_COLUMN, tid);
                    String name = group.name();
                    if (name == null || name.length() == 0) {
                        name = Bundle.getString("Thread.noname");
                    }
                    next.setData(NAME_COLUMN, name);
                }
                node = next;
            }

            // Now add the thread node.
            String tid = String.valueOf(thrd.uniqueID());
            // Find a node by this unique ID.
            Node next = (Node) node.getChild(ID_COLUMN, tid);
            if (next == null) {
                // Node doesn't exist, create it now.
                next = new Node();
                // Insert with event notification.
                insertNodeInto(next, node, 0);
                // The data will get populated by describeThread().
            }

            return next;
        } // addPath

        /**
         * Returns the node in the tree that corresponds to this thread,
         * if any.
         *
         * @param  thrd  thread to locate in tree.
         * @return  node if found, null if not.
         */
        public Node findNode(ThreadReference thrd) {
            // Dying threads may not have thread groups, so we must
            // exhaustively search the tree to find our dead thread
            // by only it's unique id number.
            String tid = String.valueOf(thrd.uniqueID());
            Node node = (Node) getRoot();
            Enumeration enmr = node.depthFirstEnumeration();
            while (enmr.hasMoreElements()) {
                node = (Node) enmr.nextElement();
                Object val = getValueAt(node, ID_COLUMN);
                if (val != null && val.equals(tid)) {
                    return node;
                }
            }
            return null;
        } // findNode

        /**
         * Finds the node for the given thread quickly by using the
         * thread group links.
         *
         * @param  thrd  thread to be added to tree.
         * @return  newly added node.
         */
        public Node findNodeFast(ThreadReference thrd) {
            // Make a list of the parent groups, starting from the top.
            Stack stack = new Stack();
            ThreadGroupReference group = thrd.threadGroup();
            while (group != null) {
                stack.push(group);
                group = group.parent();
            }

            // Traverse the groups first.
            Node node = (Node) getRoot();
            while (node != null && !stack.empty()) {
                group = (ThreadGroupReference) stack.pop();
                String tid = String.valueOf(group.uniqueID());
                node = (Node) node.getChild(ID_COLUMN, tid);
            }
            if (node != null) {
                String tid = String.valueOf(thrd.uniqueID());
                node = (Node) node.getChild(ID_COLUMN, tid);
            }
            return node;
        } // findNodeFast

        /**
         * Retrieve the data in column <code>column</code> in the last
         * path component of the given path.
         *
         * @param  path    path to element.
         * @param  column  column of data to retrieve.
         * @return  object in the column of the last path component.
         */
        public Object getValueAt(TreePath path, int column) {
            Node node = (Node) path.getLastPathComponent();
            return node.getData(column);
        } // getValueAt
    } // ThreadModel

    /**
     * Class ThreadsPopup is a popup menu that allows the user to
     * suspend, resume, and interrupt threads in the debuggee.
     */
    protected class ThreadsPopup extends SmartPopupMenu
        implements ActionListener, ItemListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Menu item to resume all threads. */
        private JMenuItem resumeAllItem;
        /** Menu item to suspend all threads. */
        private JMenuItem suspendAllItem;
        /** Menu item to resume one thread. */
        private JMenuItem resumeOneItem;
        /** Menu item to suspend one thread. */
        private JMenuItem suspendOneItem;
        /** Menu item to interrupt one thread. */
        private JMenuItem interruptOneItem;
        /** Menu item indicating user did not click on a thread. */
        private JMenuItem badRowItem;
        /** Menu item indicating session is inactive. */
        private JMenuItem inactiveSessionItem;
        /** The always-expand-all menu item. */
        private JCheckBoxMenuItem alwaysExpandAllItem;
        /** Thread the user clicked on. */
        private ThreadReference selectedThread;
        /** The auto-refresh menu item. */
        private JCheckBoxMenuItem autoRefreshMenuItem;
        /** The hide zombies menu item. */
        private JCheckBoxMenuItem hideZombiesMenuItem;

        /**
         * Constructs an ThreadsPopup that interacts with the panel.
         */
        ThreadsPopup() {
            super(Bundle.getString("Thread.menu.label"));

            resumeAllItem = new JMenuItem(
                Bundle.getString("Thread.menu.resumeAll"));
            resumeAllItem.addActionListener(this);
            suspendAllItem = new JMenuItem(
                Bundle.getString("Thread.menu.suspendAll"));
            suspendAllItem.addActionListener(this);

            resumeOneItem = new JMenuItem("N/A");
            resumeOneItem.addActionListener(this);
            suspendOneItem = new JMenuItem("N/A");
            suspendOneItem.addActionListener(this);
            interruptOneItem = new JMenuItem("N/A");
            interruptOneItem.addActionListener(this);

            badRowItem = new JMenuItem(
                Bundle.getString("Thread.menu.badRow"));
            inactiveSessionItem = new JMenuItem(
                Bundle.getString("Thread.menu.inactiveSession"));

            alwaysExpandAllItem = new JCheckBoxMenuItem(
                Bundle.getString("Thread.menu.expandAllLabel"),
                preferences.getBoolean("alwaysExpandAll",
                                       Defaults.THREADS_EXPAND_ALL));
            alwaysExpandAllItem.addItemListener(this);

            autoRefreshMenuItem = new JCheckBoxMenuItem(
                Bundle.getString("Thread.menu.autoRefreshLabel"),
                preferences.getBoolean("threads_autoRefresh", true));
            autoRefreshMenuItem.addItemListener(this);

            hideZombiesMenuItem = new JCheckBoxMenuItem(
                Bundle.getString("Thread.menu.hideZombiesLabel"),
                preferences.getBoolean("hideZombies",
                                       Defaults.THREADS_HIDE_ZOMBIES));
            hideZombiesMenuItem.addItemListener(this);
        } // ThreadsPopup

        /**
         * Invoked when a menu item has been selected.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            if (src == resumeAllItem) {
                try {
                    owningSession.resumeVM(this, false, false);
                } catch (IllegalStateException ise) {
                    // ignored
                }
            } else if (src == suspendAllItem) {
                try {
                    owningSession.suspendVM(this);
                } catch (IllegalStateException ise) {
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
            }
        } // actionPerformed

        /**
         * Invoked when an item has been selected or deselected by the user.
         *
         * @param  e  Indicates which item was selected.
         */
        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            if (src == alwaysExpandAllItem) {
                // Always expand all of the paths.
                preferences.putBoolean("alwaysExpandAll", selected);
                ThreadPanel.this.refreshLater();
            } else if (src == autoRefreshMenuItem) {
                // Enable or disable auto refresh mode.
                preferences.putBoolean("threads_autoRefresh", selected);
                enableAutoRefresh(selected);
            } else if (src == hideZombiesMenuItem) {
                // Enable or disable hide zombies mode.
                preferences.putBoolean("hideZombies", selected);
                refreshLater();
            }
        } // itemStateChanged

        /**
         * Set the popup menu items enabled or disabled depending on
         * which line of the source view area the mouse button has
         * been pressed.
         *
         * @param  e  mouse event.
         */
        protected void setMenuItemsForEvent(MouseEvent e) {
            // Reset the popup menu by removing all children.
            removeAll();

            // Use mouse position to determine thread.
            int row = treeTable.rowAtPoint(e.getPoint());
            TreePath path = treeTable.getTree().getPathForRow(row);
            ThreadModel model = (ThreadModel) treeTable.getTreeTableModel();
            // Make sure to get ID_COLUMN from the model otherwise
            // moving table columns will give us the wrong value.
            String threadId  = (String) model.getValueAt(path, ID_COLUMN);
            VirtualMachine vm = owningSession.getConnection().getVM();
            selectedThread = Threads.getThreadByID(vm, threadId);

            if (selectedThread == null) {
                // Popup only appears on the table rows,
                // so this is next to impossible.
                add(badRowItem);
                return;
            }

            if (selectedThread.isSuspended()) {
                resumeOneItem.setText(
                    Bundle.getString("Thread.menu.resume") + ' '
                    + selectedThread.uniqueID());
                add(resumeOneItem);
            } else {
                suspendOneItem.setText(
                    Bundle.getString("Thread.menu.suspend") + ' '
                    + selectedThread.uniqueID());
                add(suspendOneItem);
                interruptOneItem.setText(
                    Bundle.getString("Thread.menu.interrupt") + ' '
                    + selectedThread.uniqueID());
                add(interruptOneItem);
            }

            // Add the 'all' menu items.
            add(resumeAllItem);
            add(suspendAllItem);

            // Add the option menu items.
            add(alwaysExpandAllItem);
            add(autoRefreshMenuItem);
            add(hideZombiesMenuItem);
        } // setMenuItemsForEvent

        /**
         * Show the popup menu.
         *
         * @param  e  mouse event.
         */
        protected void showPopup(MouseEvent e) {
            setMenuItemsForEvent(e);
            show(e.getComponent(), e.getX(), e.getY());
        } // showPopup

        /**
         * Resets the UI property to a value from the current look and
         * feel.
         */
        public void updateUI() {
            super.updateUI();
            // Now update our disconnected children.
            if (resumeAllItem != null) {
                resumeAllItem.updateUI();
            }
            if (suspendAllItem != null) {
                suspendAllItem.updateUI();
            }
            if (resumeOneItem != null) {
                resumeOneItem.updateUI();
            }
            if (suspendOneItem != null) {
                suspendOneItem.updateUI();
            }
            if (interruptOneItem != null) {
                interruptOneItem.updateUI();
            }
            if (badRowItem != null) {
                badRowItem.updateUI();
            }
            if (inactiveSessionItem != null) {
                inactiveSessionItem.updateUI();
            }
            if (alwaysExpandAllItem != null) {
                alwaysExpandAllItem.updateUI();
            }
            if (autoRefreshMenuItem != null) {
                autoRefreshMenuItem.updateUI();
            }
            if (hideZombiesMenuItem != null) {
                hideZombiesMenuItem.updateUI();
            }
        } // updateUI
    } // ThreadsPopup
} // ThreadPanel
