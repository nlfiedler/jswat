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
 * are Copyright (C) 2004-2013. All Rights Reserved.
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
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class SessionsView displays the open sessions and permits actions to be
 * performed on those sessions.
 * <p/>
 * @author Nathan Fiedler
 */
public class SessionsView extends AbstractView
        implements ExplorerManager.Provider, SessionManagerListener {

    /**
     * silence the compiler warnings
     */
    private static final long serialVersionUID = 1L;
    /**
     * The singleton instance of this class.
     */
    private static SessionsView theInstance;
    /**
     * Preferred window system identifier for this window.
     */
    public static final String PREFERRED_ID = "sessions";
    /**
     * Our explorer manager.
     */
    private ExplorerManager explorerManager;
    /**
     * Component showing our nodes.
     */
    private PersistentOutlineView nodeView;

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
        String columnLabel = NbBundle.getMessage(
                BreakpointsView.class, "CTL_SessionsView_Column_Name_"
                + SessionNode.PROP_NAME);
        nodeView = new PersistentOutlineView(columnLabel);
        nodeView.getOutline().setRootVisible(false);
        addColumn(nodeView, SessionNode.PROP_HOST);
        addColumn(nodeView, SessionNode.PROP_STATE);
        addColumn(nodeView, SessionNode.PROP_LANG);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Adds a column to the outline view, with attributes extracted from the
     * properties associated with the given name.
     * <p/>
     * @param view the outline view to modify.
     * @param name the name of the property column to add.
     */
    private void addColumn(OutlineView view, String name) {
        String displayName = NbBundle.getMessage(
                BreakpointsView.class, "CTL_SessionsView_Column_Name_" + name);
        String description = NbBundle.getMessage(
                BreakpointsView.class, "CTL_SessionsView_Column_Desc_" + name);
        view.addPropertyColumn(name, displayName, description);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     * <p/>
     * @param kids root node's children, or Children.LEAF if none.
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
     * Builds the session node tree for the first time. Should be called just
     * once, when the top component is first shown.
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
     * Obtain the window instance, first by looking for it in the window system,
     * then if not found, creating the instance.
     * <p/>
     * @return the window instance.
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
     * <p/>
     * @return instance of this class.
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

    // Secret, undocumented method that NetBeans calls?
    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if (version.equals("1.0")) {
            nodeView.readSettings(p, "Sessions");
        }
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
            GetSessionCookie gsc = n.getLookup().lookup(GetSessionCookie.class);
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

    // Secret, undocumented method that NetBeans calls?
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        nodeView.writeSettings(p, "Sessions");
    }
}
