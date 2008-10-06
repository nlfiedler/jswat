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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionsView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.actions.NewSessionAction;
import com.bluemarsh.jswat.ui.components.DebuggeeInfoPanel;
import com.bluemarsh.jswat.ui.nodes.BaseNode;
import com.bluemarsh.jswat.ui.nodes.SessionNode;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import org.openide.ErrorManager;
import org.openide.actions.CustomizeAction;
import org.openide.actions.DeleteAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class SessionsView displays the open sessions and permits actions to
 * be performed on those sessions.
 *
 * @author  Nathan Fiedler
 */
public class SessionsView extends AbstractView
        implements ExplorerManager.Provider, SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The singleton instance of this class. */
    private static SessionsView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "sessions";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Array of actions for our node. */
    private Action[] nodeActions;
    /** Array of actions for the root node. */
    private Action[] rootActions;
    /** Columns for the tree-table view. */
    private Node.Property[] columns;

    /**
     * Constructs a new instance of SessionsView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public SessionsView() {
        explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, true));
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));

        rootActions = new Action[] {
            SystemAction.get(AddSessionAction.class),
            SystemAction.get(FinishAllAction.class),
        };
        buildRoot(Children.LEAF);
        nodeActions = new Action[] {
            SystemAction.get(CustomizeAction.class),
            SystemAction.get(CopySessionAction.class),
            SystemAction.get(DeleteAction.class),
            SystemAction.get(SetCurrentSessionAction.class),
            SystemAction.get(FinishSessionAction.class),
            SystemAction.get(FinishAllAction.class),
            SystemAction.get(DescribeDebuggeeAction.class),
        };
        addSelectionListener(explorerManager);

        // Create the session view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(SessionNode.PROP_NAME, true, true),
            new Column(SessionNode.PROP_HOST, false, true),
            new Column(SessionNode.PROP_STATE, false, true),
            new Column(SessionNode.PROP_LANG, false, true),
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
        BaseNode rootNode = new BaseNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                SessionsView.class, "CTL_SessionsView_Column_Name_name"));
        rootNode.setShortDescription(NbBundle.getMessage(
                SessionsView.class, "CTL_SessionsView_Column_Desc_name"));
        rootNode.setActions(rootActions);
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the session node tree for the first time. Should be called
     * just once, when the top component is first shown.
     */
    private void buildTree() {
        // Populate the root node with SessionNode children.
        List<Node> list = new LinkedList<Node>();
        SessionManager sm = SessionProvider.getSessionManager();
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            SessionNode node = createSessionNode(session);
            list.add(node);
        }
        Children children = new Children.Array();
        Node[] nodes = list.toArray(new Node[list.size()]);
        children.add(nodes);
        buildRoot(children);
    }

    protected void componentClosed() {
        super.componentClosed();
        // Clear the tree to release resources.
        buildRoot(Children.LEAF);
        // Stop listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
    }

    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        buildTree();
        // Start listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
    }

    /**
     * Creates a new instance of SessionNode for the given Session.
     *
     * @param  session  Session for which to create node.
     * @return  new node.
     */
    private SessionNode createSessionNode(Session session) {
        SessionNode node = new SessionNode(session);
        node.setActions(nodeActions);
        node.setPreferredAction(SystemAction.get(SetCurrentSessionAction.class));
        return node;
    }

    /**
     * Obtain the window instance, first by looking for it in the window
     * system, then if not found, creating the instance.
     *
     * @return  the window instance.
     */
    public static synchronized SessionsView findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find '" + PREFERRED_ID +
                    "' component in the window system");
            return getDefault();
        }
        if (win instanceof SessionsView) {
            return (SessionsView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" +
                PREFERRED_ID + "' ID, this a potential source of errors");
        return getDefault();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * Clients should not call this method, but instead use findInstance().
     *
     * @return  instance of this class.
     */
    public static synchronized SessionsView getDefault() {
        if (theInstance == null) {
            theInstance = new SessionsView();
        }
        return theInstance;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SessionsView.class, "CTL_SessionsView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-sessions-view");
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String getToolTipText() {
        return NbBundle.getMessage(SessionsView.class, "CTL_SessionsView_Tooltip");
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        SessionNode node = createSessionNode(session);
        Children children = explorerManager.getRootContext().getChildren();
        Node[] nodes = { node };
        children.add(nodes);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        // Find the corresponding session node.
        Children children = explorerManager.getRootContext().getChildren();
        Node[] nodes = children.getNodes();
        Node[] remove = new Node[1];
        for (Node n : nodes) {
            if (n instanceof SessionNode) {
                SessionNode sn = (SessionNode) n;
                if (sn.getSession() == session) {
                    remove[0] = sn;
                    break;
                }
            }
        }
        children.remove(remove);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        Children children = explorerManager.getRootContext().getChildren();
        Node[] nodes = children.getNodes();
        for (Node n : nodes) {
            if (n instanceof SessionNode) {
                SessionNode sn = (SessionNode) n;
                sn.iconChanged();
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
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
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         */
        public Column(String key, boolean tree, boolean sortable) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_SessionsView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_SessionsView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Implements the action of creating a new session instance.
     *
     * @author  Nathan Fiedler
     */
    public static class AddSessionAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(AddSessionAction.class,
                    "LBL_AddSessionAction");
        }

        protected void performAction(Node[] activatedNodes) {
            CallableSystemAction action = (CallableSystemAction)
                    SystemAction.get(NewSessionAction.class);
            action.performAction();
        }
    }

    /**
     * Implements the action of copying the selected session.
     *
     * @author  Nathan Fiedler
     */
    public static class CopySessionAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes != null && activatedNodes.length > 0;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(CopySessionAction.class,
                    "LBL_CopySessionAction");
        }

        protected void performAction(Node[] activatedNodes) {
            SessionManager sm = SessionProvider.getSessionManager();
            for (Node n : activatedNodes) {
                if (n instanceof SessionNode) {
                    SessionNode sessionNode = (SessionNode) n;
                    Session session = sessionNode.getSession();
                    // Copy the session, giving it a generated name.
                    Session copy = sm.copy(session, null);
                    // Copy the source/class paths to the copy.
                    PathManager pm = PathProvider.getPathManager(session);
                    List<String> cpath = pm.getClassPath();
                    List<FileObject> spath = pm.getSourcePath();
                    pm = PathProvider.getPathManager(copy);
                    pm.setClassPath(cpath);
                    pm.setSourcePath(spath);
                }
            }
        }
    }

    /**
     * Implements the action of describing a debuggee.
     *
     * @author  Nathan Fiedler
     */
    public static class DescribeDebuggeeAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                for (Node n : activatedNodes) {
                    if (!(n instanceof SessionNode)) {
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
            return NbBundle.getMessage(DescribeDebuggeeAction.class,
                    "LBL_DescribeDebuggeeAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                for (Node n : activatedNodes) {
                    if (n instanceof SessionNode) {
                        SessionNode sn = (SessionNode) n;
                        DebuggeeInfoPanel dip = new DebuggeeInfoPanel();
                        dip.display(sn.getSession());
                    }
                }
            }
        }
    }

    /**
     * Implements the action of finishing a session.
     *
     * @author  Nathan Fiedler
     */
    public static class FinishSessionAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                for (Node n : activatedNodes) {
                    if (!(n instanceof SessionNode)) {
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
            return NbBundle.getMessage(FinishSessionAction.class,
                    "LBL_FinishSessionAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length > 0) {
                for (Node n : activatedNodes) {
                    if (n instanceof SessionNode) {
                        SessionNode sn = (SessionNode) n;
                        Session s = sn.getSession();
                        if (s.isConnected()) {
                            s.disconnect(!s.getConnection().isRemote());
                        }
                    }
                }
            }
        }
    }

    /**
     * Implements the action of finishing all open sessions.
     *
     * @author  Nathan Fiedler
     */
    public static class FinishAllAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(FinishAllAction.class,
                    "LBL_FinishAllAction");
        }

        protected void performAction(Node[] activatedNodes) {
            SessionManager sm = SessionProvider.getSessionManager();
            Iterator iter = sm.iterateSessions();
            while (iter.hasNext()) {
                Session s = (Session) iter.next();
                if (s.isConnected()) {
                    s.disconnect(!s.getConnection().isRemote());
                }
            }
        }
    }

    /**
     * Implements the action of changing the current session.
     *
     * @author  Nathan Fiedler
     */
    public static class SetCurrentSessionAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            // performAction() should run in event thread
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                // Make sure selected sessions are not marked as current.
                SessionManager sm = SessionProvider.getSessionManager();
                Session current = sm.getCurrent();
                Node n = activatedNodes[0];
                if (n instanceof SessionNode) {
                    SessionNode sessionNode = (SessionNode) n;
                    Session session = sessionNode.getSession();
                    return session != current;
                }
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(SetCurrentSessionAction.class,
                    "LBL_SetCurrentSessionAction");
        }

        protected void performAction(Node[] activatedNodes) {
            // Get the current session.
            SessionManager sm = SessionProvider.getSessionManager();
            Session currentSession = sm.getCurrent();

            // Find the current session node.
            SessionsView view = SessionsView.findInstance();
            Children children = view.explorerManager.getRootContext().getChildren();
            Node[] nodes = children.getNodes();
            SessionNode currentNode = null;
            for (Node n : nodes) {
                if (n instanceof SessionNode) {
                    SessionNode sn = (SessionNode) n;
                    if (sn.getSession() == currentSession) {
                        currentNode = sn;
                        break;
                    }
                }
            }

            // Make the activated session the current one.
            Node n = activatedNodes[0];
            if (n instanceof SessionNode) {
                SessionNode sn = (SessionNode) n;
                Session session = sn.getSession();
                sm.setCurrent(session);
                // Cause the new node icon to refresh.
                sn.iconChanged();
            }

            // Cause the old node icon to refresh.
            currentNode.iconChanged();
        }
    }
}
