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
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.windows.TopComponent;

/**
 * Overrides
 * <code>TopComponent</code> to provide a few convenience methods for the views
 * in the JSwat UI module.
 * <p/>
 * @author Nathan Fiedler
 */
public abstract class AbstractView extends TopComponent {

    /**
     * Creates a new instance of AbstractView.
     */
    public AbstractView() {
    }

    /**
     * Creates a listener to track the selected nodes in the given explorer
     * manager, settingn those nodes as activated.
     * <p/>
     * @param em explorer manager to listen to.
     */
    protected void addSelectionListener(ExplorerManager em) {
        em.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // As nodes are selected, make them the activated nodes for
                // the owning top component.
                ExplorerManager manager = (ExplorerManager) evt.getSource();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    setActivatedNodes(manager.getSelectedNodes());
                }
            }
        });
    }

    /**
     * Expands the set of node paths in the view.
     * <p/>
     * @param paths list of node path names to expand.
     * @param view  tree view to traverse.
     * @param root  root of the node tree.
     */
    protected static void expandPaths(List<String[]> paths, OutlineView view, Node root) {
        for (String[] path : paths) {
            try {
                Node node = NodeOp.findPath(root, path);
                // Note that this expansion works because we are expanding
                // the parent nodes before their children.
                view.expandNode(node);
            } catch (NodeNotFoundException nnfe) {
                // Oh well, that is to be expected, since things can
                // very well change after a tree has been rebuilt.
            }
        }
    }

    /**
     * Determines the list of paths of expanded nodes. Use this to take a
     * snapshot of the expanded state of the view, then later re-expand the
     * nodes with the same names after the tree has been rebuilt.
     * <p/>
     * @param view tree view to traverse.
     * @param root root of the node tree.
     * @return list of expanded paths.
     */
    protected static List<String[]> getExpanded(OutlineView view, Node root) {
//        Stack<Node> stack = new Stack<Node>();
//        stack.push(root);
        Set<Node> expanded = new HashSet<Node>();
// TODO: the OutlineView.isExpanded() call is blocking(?) and makes views freeze
//        while (!stack.empty()) {
//            Node node = stack.pop();
//            if (view.isExpanded(node)) {
//                Node[] kids = node.getChildren().getNodes();
//                for (Node kid : kids) {
//                    stack.push(kid);
//                }
//            } else {
//                Node parent = node.getParentNode();
//                if (parent != null) {
//                    expanded.add(parent);
//                }
//            }
//        }
        List<String[]> paths = new LinkedList<String[]>();
        for (Node node : expanded) {
            String[] path = NodeOp.createPath(node, root);
            paths.add(path);
        }
        return paths;
    }

    /**
     * Expands all paths.
     */
    public void expandAll(OutlineView view) {
        // TODO: OutlineView.expandPath() doesn't seem to work
        // Borrowed from TreeView source in openide.explorer module...
        Outline outline = view.getOutline();
        TreeNode root = (TreeNode) outline.getOutlineModel().getRoot();
        expandOrCollapseAll(outline, new TreePath(root), true);
    }

    /**
     * Expand or collapse a tree path and its descendants.
     * <p/>
     * @param tree   outline on which to operate.
     * @param parent parent tree path.
     * @param expand true to expand, false to collapse.
     */
    @SuppressWarnings("unchecked")
    private void expandOrCollapseAll(Outline tree, TreePath parent, boolean expand) {
        // Borrowed from TreeView source in openide.explorer module...
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
                TreeNode n = e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandOrCollapseAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    /**
     * Checks if the given Session is the current session or not.
     * <p/>
     * @param session session to compare.
     * @return true if current session, false otherwise.
     */
    protected static boolean isCurrent(Session session) {
        SessionManager sm = SessionProvider.getSessionManager();
        Session current = sm.getCurrent();
        return current.equals(session);
    }

    /**
     * Checks if the given event is for the current session or not.
     * <p/>
     * @param sevt session event.
     * @return true if current session, false otherwise.
     */
    protected static boolean isCurrent(SessionEvent sevt) {
        return isCurrent(sevt.getSession());
    }
}
