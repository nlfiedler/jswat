/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ThreadsView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.nodes.BaseNode;
import com.bluemarsh.jswat.ui.nodes.ThreadGroupNode;
import com.bluemarsh.jswat.ui.nodes.ThreadNode;
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
import java.util.Stack;
import javax.swing.Action;
import javax.swing.JScrollPane;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
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
        implements ExplorerManager.Provider, ContextListener, Runnable,
        SessionListener, SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name of the name property. */
    private static final String PROP_NAME = "name";
    /** Name of the status property. */
    private static final String PROP_STATUS = "status";
    /** Name of the identifer property. */
    private static final String PROP_ID = "id";
    /** Name of the class property. */
    private static final String PROP_CLASS = "class";
    /** The singleton instance of this class. */
    private static ThreadsView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "threads";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Expands the tree when requested. */
    private ThreadExpander treeExpander;
    /** Array of actions for our nodes. */
    private Action[] nodeActions;
    /** Array of actions for the root node. */
    private Action[] rootActions;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a new instance of ThreadsView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public ThreadsView() {
        explorerManager = new ExplorerManager();
        nodeActions = new Action[] {
            SystemAction.get(SetCurrentThreadAction.class),
            SystemAction.get(ResumeAction.class),
            SystemAction.get(SuspendAction.class),
            SystemAction.get(InterruptAction.class),
            SystemAction.get(RefreshAction.class),
        };
        rootActions = new Action[] {
            SystemAction.get(RefreshAction.class),
        };
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the threads view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
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
        treeExpander = new ThreadExpander();
    }

    /**
     * Builds out the children for the given thread group.
     * Recursively builds each child group.
     *
     * @param  dc       debugging context.
     * @param  group    thread group to build.
     * @param  showAll  true to include unknown/zombie threads.
     * @return  new Children instance.
     */
    private Children buildGroup(DebuggingContext dc, ThreadGroupReference group,
            boolean showAll) {
        // Iterate over the thread groups.
        List<ThreadGroupReference> groups = group.threadGroups();
        List<Node> newKids = new LinkedList<Node>();
        for (ThreadGroupReference subgroup : groups) {
            Children kids = buildGroup(dc, subgroup, showAll);
            ThreadGroupNode subnode = createThreadGroupNode(kids, dc, subgroup);
            newKids.add(subnode);
        }

        // Iterate over the threads.
        List<ThreadReference> threads = group.threads();
        for (ThreadReference thread : threads) {
            int status = thread.status();
            // Ignore threads that haven't started or have already finished,
            // unless user wants to see them all anyway.
            if (showAll || status != ThreadReference.THREAD_STATUS_NOT_STARTED &&
                    status != ThreadReference.THREAD_STATUS_ZOMBIE) {
                ThreadNode subnode = createThreadNode(dc, thread);
                newKids.add(subnode);
            }
        }

        // Add the new children to the parent.
        Children ch = new Children.Array();
        int size = newKids.size();
        if (size > 0) {
            Node[] nodes = newKids.toArray(new Node[size]);
            ch.add(nodes);
        }
        return ch;
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     *
     * @param  kids  root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        BaseNode rootNode = new BaseNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                ThreadsView.class, "CTL_ThreadsView_Column_Name_" + PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                ThreadsView.class, "CTL_ThreadsView_Column_Desc_" + PROP_NAME));
        rootNode.setActions(rootActions);
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the group/thread node tree for the current session.
     */
    private void buildTree() {
        // Ensure that this method runs on the AWT thread to avoid slowing
        // the event dispatching thread, and the session connect process.
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this);
            return;
        }

        CoreSettings cs = CoreSettings.getDefault();
        boolean showAll = cs.getShowAllThreads();
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        List<Node> list = new LinkedList<Node>();
        Node rootNode = explorerManager.getRootContext();
        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        if (session.isConnected()) {
            DebuggingContext dc = ContextProvider.getContext(session);
            VirtualMachine vm = session.getConnection().getVM();
            List<ThreadGroupReference> groups = vm.topLevelThreadGroups();
            for (ThreadGroupReference group : groups) {
                // Recursively build out the rest of the subtree.
                Children kids = buildGroup(dc, group, showAll);
                ThreadGroupNode groupNode = createThreadGroupNode(kids, dc, group);
                list.add(groupNode);
            }
        }
        if (list.size() > 0) {
            Children children = new Children.Array();
            Node[] nodes = list.toArray(new Node[list.size()]);
            children.add(nodes);
            buildRoot(children);
            EventQueue.invokeLater(treeExpander);
        } else {
            buildRoot(Children.LEAF);
        }

        // Must expand the nodes on the AWT event thread.
        // This is in addition to expanding to show the current thread.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Need to refetch the root in case it was replaced.
                Node rootNode = explorerManager.getRootContext();
                expandPaths(expanded, nodeView, rootNode);
            }
        });
    }

    public void changedFrame(ContextEvent ce) {
    }

    public void changedLocation(ContextEvent ce) {
    }

    public void changedThread(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            // Need to update the icons and names of all the nodes so that
            // the current thread/group indicators are reset to show the
            // new current thread and parent groups.
            Stack<Node> stack = new Stack<Node>();
            stack.push(explorerManager.getRootContext());
            while (!stack.empty()) {
                Node node = stack.pop();
                if (nodeView.isExpanded(node)) {
                    Node[] kids = node.getChildren().getNodes();
                    for (Node kid : kids) {
                        stack.push(kid);
                    }
                }
                BaseNode bn = (BaseNode) node;
                bn.displayNameChanged();
                bn.iconChanged();
            }
        }
    }

    public void closing(SessionEvent sevt) {
    }

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
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.removeContextListener(this);
        }
    }

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
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.addContextListener(this);
        }
    }

    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Create a node for the given thread group.
     *
     * @param  children  children nodes.
     * @param  context   debugging context.
     * @param  group     thread group.
     * @return  thread group node.
     */
    private ThreadGroupNode createThreadGroupNode(Children children,
            DebuggingContext context, ThreadGroupReference group) {
        ThreadGroupNode node = new ThreadGroupNode(children, context, group);
        node.setActions(nodeActions);
        return node;
    }

    /**
     * Create a node for the given thread.
     *
     * @param  context   debugging context.
     * @param  thread    thread reference.
     * @return  thread node.
     */
    private ThreadNode createThreadNode(DebuggingContext context,
            ThreadReference thread) {
        ThreadNode node = new ThreadNode(context, thread);
        node.setActions(nodeActions);
        node.setPreferredAction(SystemAction.get(SetCurrentThreadAction.class));
        return node;
    }

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

    public String getDisplayName() {
        return NbBundle.getMessage(ThreadsView.class, "CTL_ThreadsView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-thread-view");
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String getToolTipText() {
        return NbBundle.getMessage(ThreadsView.class, "CTL_ThreadsView_Tooltip");
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void opened(Session session) {
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    public void resuming(SessionEvent sevt) {
    }

    public void run() {
        buildTree();
    }

    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
    }

    /**
     * Expands the nodes of the tree to show the current thread.
     *
     * @author  Nathan Fiedler
     */
    protected class ThreadExpander implements Runnable {

        public void run() {
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            if (thread != null) {
                Node node = findThreadNode(thread);
                if (node != null) {
                    // Cannot expand leaf nodes, so use parent.
                    node = node.getParentNode();
                    nodeView.expandNode(node);
                }
            }
        }
    }

    /**
     * A column for the session table.
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
        public Column(String key, Class type, boolean tree, boolean sortable, boolean hidden) {
            super(key, type,
                  NbBundle.getMessage(Column.class, "CTL_ThreadsView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_ThreadsView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Implements the action of interrupting the selected threads and groups.
     *
     * @author  Nathan Fiedler
     */
    public static class InterruptAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (!(n instanceof ThreadNode)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(InterruptAction.class,
                    "LBL_ThreadsView_InterruptAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (n instanceof ThreadNode) {
                        ThreadReference tr = ((ThreadNode) n).getThread();
                        tr.interrupt();
                    }
                }
            }
        }
    }

    /**
     * Implements the action of setting the current thread.
     *
     * @author  Nathan Fiedler
     */
    public static class SetCurrentThreadAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                return activatedNodes[0] instanceof ThreadNode;
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(SetCurrentThreadAction.class,
                    "LBL_ThreadsView_SetCurrentThreadAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                Node n = activatedNodes[0];
                if (n instanceof ThreadNode) {
                    ThreadReference tr = ((ThreadNode) n).getThread();
                    SessionManager sm = SessionProvider.getSessionManager();
                    Session session = sm.getCurrent();
                    DebuggingContext dc = ContextProvider.getContext(session);
                    dc.setThread(tr, false);
                }
            }
        }
    }

    /**
     * Implements the action of resuming the selected threads and groups.
     *
     * @author  Nathan Fiedler
     */
    public static class ResumeAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length > 0;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(ResumeAction.class,
                    "LBL_ThreadsView_ResumeAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (n instanceof ThreadNode) {
                        ThreadReference tr = ((ThreadNode) n).getThread();
                        tr.resume();
                    } else if (n instanceof ThreadGroupNode) {
                        ThreadGroupReference tgr = ((ThreadGroupNode) n).getGroup();
                        tgr.resume();
                    }
                }
            }
        }
    }

    /**
     * Implements the action of suspending the selected threads and groups.
     *
     * @author  Nathan Fiedler
     */
    public static class SuspendAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length > 0;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(SuspendAction.class,
                    "LBL_ThreadsView_SuspendAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (n instanceof ThreadNode) {
                        ThreadReference tr = ((ThreadNode) n).getThread();
                        tr.suspend();
                    } else if (n instanceof ThreadGroupNode) {
                        ThreadGroupReference tgr = ((ThreadGroupNode) n).getGroup();
                        tgr.suspend();
                    }
                }
            }
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

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(RefreshAction.class,
                    "LBL_ThreadsView_RefreshAction");
        }

        protected void performAction(Node[] activatedNodes) {
            ThreadsView.findInstance().buildTree();
        }
    }
}
