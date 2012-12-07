/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.nodes.threads.ThreadConstants;
import com.bluemarsh.jswat.nodes.threads.ThreadNode;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.swing.Action;
import javax.swing.JScrollPane;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class ThreadsView displays the thread groups and their threads.
 *
 * @author  Nathan Fiedler
 */
public class ThreadsView extends AbstractView
        implements ExplorerManager.Provider, SessionListener,
        SessionManagerListener, ThreadConstants {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The singleton instance of this class. */
    private static ThreadsView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "threads";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentOutlineView nodeView;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a new instance of ThreadsView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public ThreadsView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the threads view.
        nodeView = new PersistentOutlineView();
        nodeView.getOutline().setRootVisible(false);
        columns = new Node.Property[] {
            new Column(PROP_NAME, String.class, true, true, false),
            new Column(PROP_STATUS, String.class, false, true, false),
            new Column(PROP_ID, Long.class, false, true, true),
            new Column(PROP_CLASS, String.class, false, true, true),
        };
        nodeView.setProperties(columns);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     *
     * @param  kids  root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids) {
            @Override
            public Action[] getActions(boolean b) {
                return new Action[] {
                    SystemAction.get(RefreshAction.class),
                };
            }
        };
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                ThreadsView.class, "CTL_ThreadsView_Column_Name_" + PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                ThreadsView.class, "CTL_ThreadsView_Column_Desc_" + PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the group/thread node tree for the current session.
     */
    private void buildTree() {
        // Ensure that the tree is built on the AWT thread to avoid slowing
        // the JDI event dispatching thread, and the session connect process.
        if (EventQueue.isDispatchThread()) {
            buildTreeInAWTThread();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    buildTreeInAWTThread();
                }
            });
        }
    }

    /**
     * Builds the group/thread node tree for the current session on
     * the AWT event dispatching thread.
     */
    private void buildTreeInAWTThread() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        List<Node> list = new LinkedList<Node>();
//        Node rootNode = explorerManager.getRootContext();
//        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        if (session.isConnected()) {
            VirtualMachine vm = session.getConnection().getVM();
            List<ThreadGroupReference> groups = vm.topLevelThreadGroups();
            NodeFactory factory = NodeFactory.getDefault();
            for (ThreadGroupReference group : groups) {
                list.add(factory.createThreadGroupNode(group, dc));
            }
        }
        if (list.size() > 0) {
            Children children = new Children.Array();
            Node[] nodes = list.toArray(new Node[list.size()]);
            children.add(nodes);
            buildRoot(children);
            // Expand the branches that the user had expanded earlier.
            // TODO: get node expansion working
//            expandPaths(expanded, nodeView, rootNode);
            // Expand the path leading to the current thread.
            ThreadReference thread = dc.getThread();
            if (thread != null) {
                Node node = findThreadNode(thread);
                if (node != null) {
                    // Cannot expand leaf nodes, so use its parent.
                    nodeView.expandNode(node.getParentNode());
                }
            }
        } else {
            buildRoot(Children.LEAF);
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        // Clear the tree to release resources.
        buildRoot(Children.LEAF);
        // Stop listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.removeSessionListener(this);
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        buildTree();
        // Start listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.addSessionListener(this);
        }
    }

    @Override
    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Obtain the window instance, first by looking for it in the window
     * system, then if not found, creating the instance.
     *
     * @return  the window instance.
     */
    public static synchronized ThreadsView findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find '" + PREFERRED_ID +
                    "' component in the window system");
            return getDefault();
        }
        if (win instanceof ThreadsView) {
            return (ThreadsView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" +
                PREFERRED_ID + "' ID, this a potential source of errors");
        return getDefault();
    }

    /**
     * Finds the node for the given thread.
     *
     * @param  thread  thread for which to find node.
     * @return  corresponding thread node, or null if none found.
     */
    private ThreadNode findThreadNode(ThreadReference thread) {
        Queue<Node> queue = new LinkedList<Node>();
        queue.offer(explorerManager.getRootContext());
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node instanceof ThreadNode) {
                ThreadNode tn = (ThreadNode) node;
                ThreadReference tr = tn.getThread();
                if (tr.equals(thread)) {
                    return tn;
                }
            }
            Children children = node.getChildren();
            Enumeration enm = children.nodes();
            while (enm.hasMoreElements()) {
                Node child = (Node) enm.nextElement();
                queue.offer(child);
            }
        }
        return null;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * Clients should not call this method, but instead use findInstance().
     *
     * @return  instance of this class.
     */
    public static synchronized ThreadsView getDefault() {
        if (theInstance == null) {
            theInstance = new ThreadsView();
        }
        return theInstance;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ThreadsView.class, "CTL_ThreadsView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-thread-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(ThreadsView.class, "CTL_ThreadsView_Tooltip");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    @Override
    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
    }

    /**
     * A column for the threads table.
     *
     * @author  Nathan Fiedler
     */
    protected class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key       keyword for this column.
         * @param  type      type of the property (e.g. String.class, Long.class).
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         * @param  hidden    true to hide this column initially.
         */
        @SuppressWarnings("unchecked")
        public Column(String key, Class type, boolean tree, boolean sortable, boolean hidden) {
            super(key, type,
                  NbBundle.getMessage(Column.class, "CTL_ThreadsView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_ThreadsView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        @Override
        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Implements the action of refreshing the node tree.
     *
     * @author  Nathan Fiedler
     */
    public static class RefreshAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RefreshAction.class,
                    "LBL_RefreshAction_Name");
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            ThreadsView view = ThreadsView.findInstance();
            view.buildTree();
        }
    }
}
