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
 * $Id: BreakpointsView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroupEvent;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroupListener;
import com.bluemarsh.jswat.core.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.nodes.BaseNode;
import com.bluemarsh.jswat.ui.nodes.BreakpointGroupNode;
import com.bluemarsh.jswat.ui.nodes.BreakpointNode;
import com.bluemarsh.jswat.ui.nodes.ShowSourceAction;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
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
import javax.swing.ActionMap;
import javax.swing.JScrollPane;
import org.openide.actions.CustomizeAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Class BreakpointsView displays the breakpoints for the current session and
 * permits actions to be performed on those breakpoints.
 *
 * @author Nathan Fiedler
 */
public class BreakpointsView extends AbstractView
        implements BreakpointListener,
                   BreakpointGroupListener,
                   ExplorerManager.Provider,
                   SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Array of actions for view nodes. */
    private Action[] nodeActions;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Creates a new instance of BreakpointsView.
     */
    public BreakpointsView() {
        explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));

        buildRoot(Children.LEAF);
        nodeActions = new Action[] {
            SystemAction.get(CustomizeAction.class),
            SystemAction.get(NewAction.class),
            SystemAction.get(DeleteAction.class),
            SystemAction.get(EnableAction.class),
            SystemAction.get(DisableAction.class),
        };
        addSelectionListener(explorerManager);

        // Create the view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(Breakpoint.PROP_DESCRIPTION, String.class, true, true, false),
            new Column(Breakpoint.PROP_ENABLED, Boolean.TYPE, false, true, false),
            new Column(Breakpoint.PROP_RESOLVED, Boolean.TYPE, false, true, true),
            new Column(Breakpoint.PROP_SKIPPING, Boolean.TYPE, false, true, true),
            new Column(Breakpoint.PROP_EXPIRED, Boolean.TYPE, false, true, true),
        };
        nodeView.setProperties(columns);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    public void breakpointAdded(BreakpointEvent event) {
        // Find the group node and add a new breakpoint node to it.
        Breakpoint bp = event.getBreakpoint();
        BreakpointGroup bg = bp.getBreakpointGroup();
        BreakpointGroupNode gn = findGroupNode(bg);
        if (gn != null) {
            Children children = gn.getChildren();
            BreakpointNode bn = createBreakpointNode(bp);
            children.add(new Node[] { bn });
        }
        // Else, this is an event for a different session.
    }

    public void breakpointRemoved(BreakpointEvent event) {
        // Find the breakpoint node and remove it from the parent node.
        Breakpoint bp = event.getBreakpoint();
        BreakpointNode bn = findBreakpointNode(bp);
        if (bn != null) {
            BreakpointGroupNode gn = (BreakpointGroupNode) bn.getParentNode();
            Children children = gn.getChildren();
            children.remove(new Node[] { bn });
        }
        // Else, this is an event for a different session.
    }

    public void breakpointStopped(BreakpointEvent event) {
    }

    /**
     * Builds out the children for the given breakpoint group.
     * Recursively builds each child group.
     *
     * @param  group   breakpoint group to build.
     * @return  new Children instance.
     */
    protected Children buildGroup(BreakpointGroup group) {
        // Iterate over the breakpoint groups.
        Iterator<BreakpointGroup> groups = group.groups(false);
        List<Node> newKids = new LinkedList<Node>();
        while (groups.hasNext()) {
            BreakpointGroup subgroup = groups.next();
            Children kids = buildGroup(subgroup);
            BreakpointGroupNode subnode = createBreakpointGroupNode(kids, subgroup);
            newKids.add(subnode);
        }

        // Iterate over the breakpoints.
        Iterator<Breakpoint> brks = group.breakpoints(false);
        while (brks.hasNext()) {
            Breakpoint bp = brks.next();
            BreakpointNode subnode = createBreakpointNode(bp);
            newKids.add(subnode);
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
        Node rootNode = new BaseNode(kids);
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
     * @param  session  session from which to retrieve breakpoints.
     */
    protected void buildTree(Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup group = bm.getDefaultGroup();
        // Recursively build out the rest of the tree.
        Children kids = buildGroup(group);
        BreakpointGroupNode groupNode = createBreakpointGroupNode(kids, group);
        // Set the root node's children.
        Children children = new Children.Array();
        Node[] nodes = { groupNode };
        children.add(nodes);
        buildRoot(children);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                nodeView.expandAll();
            }
        });
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
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            bm.removeBreakpointListener(this);
            bm.removeBreakpointGroupListener(this);
        }
    }

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

    /**
     * Create a node to represent the breakpoint.
     *
     * @param  bp  breakpoint.
     * @return  node.
     */
    private BreakpointNode createBreakpointNode(Breakpoint bp) {
        BreakpointNode node = new BreakpointNode(bp);
        node.setActions(nodeActions);
        if (bp instanceof LineBreakpoint) {
            node.setPreferredAction(SystemAction.get(ShowSourceAction.class));
        } else {
            node.setPreferredAction(SystemAction.get(CustomizeAction.class));
        }
        return node;
    }

    /**
     * Create a node to represent the breakpoint group.
     *
     * @param  children  the children for the new node.
     * @param  group     breakpoint group.
     * @return  node.
     */
    private BreakpointGroupNode createBreakpointGroupNode(Children children,
            BreakpointGroup group) {
        BreakpointGroupNode node = new BreakpointGroupNode(children, group);
        node.setActions(nodeActions);
        return node;
    }

    public void errorOccurred(BreakpointEvent event) {
    }

    public void errorOccurred(BreakpointGroupEvent event) {
    }

    /**
     * Finds the node for the given breakpoint.
     *
     * @param  bp  breakpoint for which to find node.
     * @return  corresponding breakpoint node, or null if none found.
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
            Enumeration enm = children.nodes();
            while (enm.hasMoreElements()) {
                Node child = (Node) enm.nextElement();
                queue.offer(child);
            }
        }
        return null;
    }

    /**
     * Finds the node for the given breakpoint group.
     *
     * @param  bp  breakpoint group for which to find node.
     * @return  corresponding group node, or null if none found.
     */
    private BreakpointGroupNode findGroupNode(BreakpointGroup bg) {
        Queue<Node> queue = new LinkedList<Node>();
        queue.offer(explorerManager.getRootContext());
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (node instanceof BreakpointGroupNode) {
                BreakpointGroupNode gn = (BreakpointGroupNode) node;
                BreakpointGroup gng = gn.getGroup();
                if (gng.equals(bg)) {
                    return gn;
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

    public String getDisplayName() {
        return NbBundle.getMessage(BreakpointsView.class, "CTL_BreakpointsView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String getToolTipText() {
        return NbBundle.getMessage(BreakpointsView.class, "CTL_BreakpointsView_Tooltip");
    }

    public void groupAdded(BreakpointGroupEvent event) {
        // Find the group's parent and add a new group node to it.
        BreakpointGroup bg = event.getBreakpointGroup();
        BreakpointGroup parent = bg.getParent();
        if (parent != null) {
            BreakpointGroupNode gn = findGroupNode(parent);
            if (gn != null) {
                Children children = gn.getChildren();
                Children kids = buildGroup(bg);
                BreakpointGroupNode child = createBreakpointGroupNode(kids, bg);
                children.add(new Node[] { child });
            }
            // Else, this is an event for a different session.
        }
        // Else, there is something wrong if there was no parent.
    }

    public void groupRemoved(BreakpointGroupEvent event) {
        // Find the group's parent and remove the group node.
        BreakpointGroup bg = event.getBreakpointGroup();
        BreakpointGroupNode gn = findGroupNode(bg);
        if (gn != null) {
            BreakpointGroupNode parent = (BreakpointGroupNode) gn.getParentNode();
            Children children = parent.getChildren();
            children.remove(new Node[] { gn });
        }
        // Else, this is an event for a different session.
    }

    protected String preferredID() {
        return BreakpointsView.class.getName();
    }

    public void propertyChange(PropertyChangeEvent event) {
        // In general the nodes listen for property changes to the
        // breakpoints and groups, but for the parent changes, we
        // are the ones that need to update the node hierarchy.
        String pname = event.getPropertyName();
        Object src = event.getSource();
        if (pname.equals(Breakpoint.PROP_BREAKPOINTGROUP) &&
                src instanceof Breakpoint) {
            // Find the breakpoint node and move it to the new parent node.
            Breakpoint bp = (Breakpoint) src;
            BreakpointNode bn = findBreakpointNode(bp);
            if (bn != null) {
                BreakpointGroupNode gn = (BreakpointGroupNode) bn.getParentNode();
                Children children = gn.getChildren();
                children.remove(new Node[] { bn });
                BreakpointGroup bg = bp.getBreakpointGroup();
                gn = findGroupNode(bg);
                if (gn != null) {
                    children = gn.getChildren();
                    children.add(new Node[] { bn });
                }
            }
        } else if (pname.equals(BreakpointGroup.PROP_PARENT) &&
                src instanceof BreakpointGroup) {
            // Find the group node and move it to the new parent node.
            BreakpointGroup bg = (BreakpointGroup) src;
            BreakpointGroupNode gn = findGroupNode(bg);
            if (gn != null) {
                BreakpointGroupNode parentNode = (BreakpointGroupNode) gn.getParentNode();
                Children children = parentNode.getChildren();
                children.remove(new Node[] { gn });
                BreakpointGroup parentGroup = bg.getParent();
                parentNode = findGroupNode(parentGroup);
                if (parentNode != null) {
                    children = parentNode.getChildren();
                    children.add(new Node[] { gn });
                }
            }
        }
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
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.addBreakpointListener(this);
        bm.addBreakpointGroupListener(this);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.removeBreakpointListener(this);
        bm.removeBreakpointGroupListener(this);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        Session session = e.getSession();
        buildTree(session);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
    }

    /**
     * A column for the breakpoints table.
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
         * @param  type      type of column data.
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         * @param  hidden    true to hide this column initially.
         */
        public Column(String key, Class type, boolean tree, boolean sortable, boolean hidden) {
            super(key, type,
                  NbBundle.getMessage(Column.class, "CTL_BreakpointsView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_BreakpointsView_Column_Desc_" + key));
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
     * Implements the action of enabling a breakpoint.
     *
     * @author  Nathan Fiedler
     */
    public static class EnableAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null) {
                if (activatedNodes.length == 1) {
                    Node n = activatedNodes[0];
                    if (n instanceof BreakpointNode) {
                        Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                        return !bp.isEnabled();
                    } else if (n instanceof BreakpointGroupNode) {
                        BreakpointGroup bg = ((BreakpointGroupNode) n).getGroup();
                        return !bg.isEnabled();
                    }
                } else if (activatedNodes.length > 1) {
                    // For multiple selections, always enable.
                    return true;
                }
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(EnableAction.class,
                    "LBL_BreakpointsView_EnableAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (n instanceof BreakpointNode) {
                        Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                        if (!bp.isEnabled()) {
                            bp.setEnabled(true);
                        }
                    } else if (n instanceof BreakpointGroupNode) {
                        BreakpointGroup bg = ((BreakpointGroupNode) n).getGroup();
                        if (!bg.isEnabled()) {
                            bg.setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Implements the action of disabling a breakpoint.
     *
     * @author  Nathan Fiedler
     */
    public static class DisableAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null) {
                if (activatedNodes.length == 1) {
                    Node n = activatedNodes[0];
                    if (n instanceof BreakpointNode) {
                        Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                        return bp.isEnabled();
                    } else if (n instanceof BreakpointGroupNode) {
                        BreakpointGroup bg = ((BreakpointGroupNode) n).getGroup();
                        return bg.isEnabled();
                    }
                } else if (activatedNodes.length > 1) {
                    // For multiple selections, always enable.
                    return true;
                }
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(DisableAction.class,
                    "LBL_BreakpointsView_DisableAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null) {
                for (Node n : activatedNodes) {
                    if (n instanceof BreakpointNode) {
                        Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                        if (bp.isEnabled()) {
                            bp.setEnabled(false);
                        }
                    } else if (n instanceof BreakpointGroupNode) {
                        BreakpointGroup bg = ((BreakpointGroupNode) n).getGroup();
                        if (bg.isEnabled()) {
                            bg.setEnabled(false);
                        }
                    }
                }
            }
        }
    }
}
