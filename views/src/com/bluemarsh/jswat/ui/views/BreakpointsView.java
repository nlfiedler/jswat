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

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroupEvent;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroupListener;
import com.bluemarsh.jswat.core.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.nodes.breakpoints.BreakpointGroupNode;
import com.bluemarsh.jswat.nodes.breakpoints.BreakpointNode;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Class BreakpointsView displays the breakpoints for the current session and
 * permits actions to be performed on those breakpoints.
 *
 * @author Nathan Fiedler
 */
public class BreakpointsView extends AbstractView
        implements BreakpointListener, BreakpointGroupListener,
        ExplorerManager.Provider, SessionManagerListener {

    /**
     * silence the compiler warnings
     */
    private static final long serialVersionUID = 1L;
    /**
     * Our explorer manager.
     */
    private ExplorerManager explorerManager;
    /**
     * Component showing our nodes.
     */
    private PersistentOutlineView nodeView;

    /**
     * Creates a new instance of BreakpointsView.
     */
    public BreakpointsView() {
        explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");

        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the view.
        String columnLabel = NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Name_"
                + Breakpoint.PROP_DESCRIPTION);
        nodeView = new PersistentOutlineView(columnLabel);
        nodeView.getOutline().setRootVisible(false);
        nodeView.setPropertyColumnDescription(columnLabel, NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Desc_"
                + Breakpoint.PROP_DESCRIPTION));
        addColumn(nodeView, Breakpoint.PROP_ENABLED);
        addColumn(nodeView, Breakpoint.PROP_RESOLVED);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Adds a column to the outline view, with attributes extracted from the
     * properties associated with the given name.
     *
     * @param view the outline view to modify.
     * @param name the name of the property column to add.
     */
    private void addColumn(OutlineView view, String name) {
        String displayName = NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Name_" + name);
        String description = NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Desc_" + name);
        view.addPropertyColumn(name, displayName, description);
    }

    @Override
    public void breakpointAdded(BreakpointEvent event) {
        // Find the group node and add a new breakpoint node to it.
        Breakpoint bp = event.getBreakpoint();
        BreakpointGroup bg = bp.getBreakpointGroup();
        BreakpointGroupNode gn = findGroupNode(bg);
        if (gn != null) {
            Children children = gn.getChildren();
            NodeFactory factory = NodeFactory.getDefault();
            BreakpointNode bn = factory.createBreakpointNode(bp);
            children.add(new Node[]{bn});
        }
        // Else, this is an event for a different session.
    }

    @Override
    public void breakpointRemoved(BreakpointEvent event) {
        // Find the breakpoint node and remove it from the parent node.
        Breakpoint bp = event.getBreakpoint();
        BreakpointNode bn = findBreakpointNode(bp);
        if (bn != null) {
            BreakpointGroupNode gn = (BreakpointGroupNode) bn.getParentNode();
            Children children = gn.getChildren();
            children.remove(new Node[]{bn});
        }
        // Else, this is an event for a different session.
    }

    @Override
    public void breakpointStopped(BreakpointEvent event) {
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     *
     * @param kids root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Name_description"));
        rootNode.setShortDescription(NbBundle.getMessage(
                BreakpointsView.class, "CTL_BreakpointsView_Column_Desc_description"));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Constructs the node tree to reflect the current breakpoints and groups.
     *
     * @param session session from which to retrieve breakpoints.
     */
    protected void buildTree(Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        NodeFactory factory = NodeFactory.getDefault();
        BreakpointGroupNode groupNode = factory.createBreakpointGroupNode(
                bm.getDefaultGroup());
        Children children = new Children.Array();
        children.add(new Node[]{groupNode});
        buildRoot(children);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                expandAll(nodeView);
            }
        });
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
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            bm.removeBreakpointListener(this);
            bm.removeBreakpointGroupListener(this);
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        SessionManager sm = SessionProvider.getSessionManager();
        buildTree(sm.getCurrent());
        // Start listening to everything that affects our tree.
        sm.addSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            bm.addBreakpointListener(this);
            bm.addBreakpointGroupListener(this);
        }
    }

    @Override
    public void errorOccurred(BreakpointEvent event) {
    }

    @Override
    public void errorOccurred(BreakpointGroupEvent event) {
    }

    /**
     * Finds the node for the given breakpoint.
     *
     * @param bp breakpoint for which to find node.
     * @return corresponding breakpoint node, or null if none found.
     */
    private BreakpointNode findBreakpointNode(Breakpoint bp) {
        Queue<Node> queue = new LinkedList<Node>();
        queue.offer(explorerManager.getRootContext());
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node instanceof BreakpointNode) {
                BreakpointNode bn = (BreakpointNode) node;
                Breakpoint bnb = bn.getBreakpoint();
                if (bnb.equals(bp)) {
                    return bn;
                }
            }
            Children children = node.getChildren();
            Enumeration<Node> enm = children.nodes();
            while (enm.hasMoreElements()) {
                Node child = enm.nextElement();
                queue.offer(child);
            }
        }
        return null;
    }

    /**
     * Finds the node for the given breakpoint group.
     *
     * @param bp breakpoint group for which to find node.
     * @return corresponding group node, or null if none found.
     */
    private BreakpointGroupNode findGroupNode(BreakpointGroup bg) {
        Queue<Node> queue = new LinkedList<Node>();
        queue.offer(explorerManager.getRootContext());
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node instanceof BreakpointGroupNode) {
                BreakpointGroupNode gn = (BreakpointGroupNode) node;
                BreakpointGroup gng = gn.getBreakpointGroup();
                if (gng.equals(bg)) {
                    return gn;
                }
            }
            Children children = node.getChildren();
            Enumeration<Node> enm = children.nodes();
            while (enm.hasMoreElements()) {
                Node child = enm.nextElement();
                queue.offer(child);
            }
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(BreakpointsView.class, "CTL_BreakpointsView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(BreakpointsView.class, "CTL_BreakpointsView_Tooltip");
    }

    @Override
    public void groupAdded(BreakpointGroupEvent event) {
        // Find the group's parent and add a new group node to it.
        BreakpointGroup bg = event.getBreakpointGroup();
        BreakpointGroup parent = bg.getParent();
        if (parent != null) {
            BreakpointGroupNode gn = findGroupNode(parent);
            if (gn != null) {
                NodeFactory factory = NodeFactory.getDefault();
                BreakpointGroupNode child = factory.createBreakpointGroupNode(bg);
                gn.getChildren().add(new Node[]{child});
            }
            // Else, this is an event for a different session.
        }
        // Else, there is something wrong if there was no parent.
    }

    @Override
    public void groupRemoved(BreakpointGroupEvent event) {
        // Find the group's parent and remove the group node.
        BreakpointGroup bg = event.getBreakpointGroup();
        BreakpointGroupNode gn = findGroupNode(bg);
        if (gn != null) {
            BreakpointGroupNode parent = (BreakpointGroupNode) gn.getParentNode();
            Children children = parent.getChildren();
            children.remove(new Node[]{gn});
        }
        // Else, this is an event for a different session.
    }

    @Override
    protected String preferredID() {
        return BreakpointsView.class.getName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // In general the nodes listen for property changes to the
        // breakpoints and groups, but for the parent changes, we
        // are the ones that need to update the node hierarchy.
        String pname = event.getPropertyName();
        Object src = event.getSource();
        if (pname.equals(Breakpoint.PROP_BREAKPOINTGROUP)
                && src instanceof Breakpoint) {
            // Find the breakpoint node and move it to the new parent node.
            Breakpoint bp = (Breakpoint) src;
            BreakpointNode bn = findBreakpointNode(bp);
            if (bn != null) {
                BreakpointGroupNode gn = (BreakpointGroupNode) bn.getParentNode();
                Children children = gn.getChildren();
                children.remove(new Node[]{bn});
                BreakpointGroup bg = bp.getBreakpointGroup();
                gn = findGroupNode(bg);
                if (gn != null) {
                    children = gn.getChildren();
                    children.add(new Node[]{bn});
                }
            }
        } else if (pname.equals(BreakpointGroup.PROP_PARENT)
                && src instanceof BreakpointGroup) {
            // Find the group node and move it to the new parent node.
            BreakpointGroup bg = (BreakpointGroup) src;
            BreakpointGroupNode gn = findGroupNode(bg);
            if (gn != null) {
                BreakpointGroupNode parentNode = (BreakpointGroupNode) gn.getParentNode();
                Children children = parentNode.getChildren();
                children.remove(new Node[]{gn});
                BreakpointGroup parentGroup = bg.getParent();
                parentNode = findGroupNode(parentGroup);
                if (parentNode != null) {
                    children = parentNode.getChildren();
                    children.add(new Node[]{gn});
                }
            }
        }
    }

    // Secret, undocumented method that NetBeans calls?
    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if (version.equals("1.0")) {
            nodeView.readSettings(p, "Breakpoints");
        }
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.addBreakpointListener(this);
        bm.addBreakpointGroupListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.removeBreakpointListener(this);
        bm.removeBreakpointGroupListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        Session session = e.getSession();
        buildTree(session);
    }

    // Secret, undocumented method that NetBeans calls?
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        nodeView.writeSettings(p, "Breakpoints");
    }
}
