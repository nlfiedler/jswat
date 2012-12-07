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
 * are Copyright (C) 2004-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.nodes.sessions.CreateSessionAction;
import com.bluemarsh.jswat.nodes.sessions.FinishAllAction;
import com.bluemarsh.jswat.nodes.sessions.GetSessionCookie;
import com.bluemarsh.jswat.nodes.sessions.SessionNode;
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
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
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
    private PersistentOutlineView nodeView;
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
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");

        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the session view.
        nodeView = new PersistentOutlineView();
        nodeView.getOutline().setRootVisible(false);
        columns = new Node.Property[]{
                    new Column(SessionNode.PROP_NAME, true, true),
                    new Column(SessionNode.PROP_HOST, false, true),
                    new Column(SessionNode.PROP_STATE, false, true),
                    new Column(SessionNode.PROP_LANG, false, true)
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
                return new Action[]{
                            SystemAction.get(CreateSessionAction.class),
                            SystemAction.get(FinishAllAction.class)
                        };
            }
        };
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                SessionsView.class, "CTL_SessionsView_Column_Name_name"));
        rootNode.setShortDescription(NbBundle.getMessage(
                SessionsView.class, "CTL_SessionsView_Column_Desc_name"));
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
        NodeFactory factory = NodeFactory.getDefault();
        while (iter.hasNext()) {
            Session session = iter.next();
            SessionNode node = factory.createSessionNode(session);
            list.add(node);
        }
        Children children = new Children.Array();
        Node[] nodes = list.toArray(new Node[list.size()]);
        children.add(nodes);
        buildRoot(children);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        // Clear the tree to release resources.
        buildRoot(Children.LEAF);
        // Stop listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        buildTree();
        // Start listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
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
                    "Cannot find '" + PREFERRED_ID
                    + "' component in the window system");
            return getDefault();
        }
        if (win instanceof SessionsView) {
            return (SessionsView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '"
                + PREFERRED_ID + "' ID, this a potential source of errors");
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

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SessionsView.class, "CTL_SessionsView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-sessions-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(SessionsView.class, "CTL_SessionsView_Tooltip");
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
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
    public void sessionAdded(SessionManagerEvent e) {
        NodeFactory factory = NodeFactory.getDefault();
        Session session = e.getSession();
        SessionNode node = factory.createSessionNode(session);
        Children children = explorerManager.getRootContext().getChildren();
        Node[] nodes = {node};
        children.add(nodes);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        // Find the corresponding session node.
        Children children = explorerManager.getRootContext().getChildren();
        Node[] nodes = children.getNodes();
        Node[] remove = new Node[1];
        for (Node n : nodes) {
            GetSessionCookie gsc = n.getCookie(GetSessionCookie.class);
            if (gsc != null && gsc.getSession().equals(session)) {
                remove[0] = n;
                break;
            }
        }
        children.remove(remove);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
    }

    @Override
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
        @SuppressWarnings("unchecked")
        public Column(String key, boolean tree, boolean sortable) {
            super(key, String.class,
                    NbBundle.getMessage(Column.class, "CTL_SessionsView_Column_Name_" + key),
                    NbBundle.getMessage(Column.class, "CTL_SessionsView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
        }

        @Override
        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }
}
