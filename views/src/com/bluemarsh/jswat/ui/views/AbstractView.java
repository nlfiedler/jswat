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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeView;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.windows.TopComponent;

/**
 * Overrides <code>TopComponent</code> to provide a few convenience methods
 * for the views in the JSwat UI module.
 *
 * @author  Nathan Fiedler
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
     *
     * @param  em  explorer manager to listen to.
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
     *
     * @param  paths  list of node path names to expand.
     * @param  view   tree view to traverse.
     * @param  root   root of the node tree.
     */
    protected static void expandPaths(List<String[]> paths, TreeView view, Node root) {
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
     * snapshot of the expanded state of the view, then later re-expand
     * the nodes with the same names after the tree has been rebuilt.
     *
     * @param  view  tree view to traverse.
     * @param  root  root of the node tree.
     * @return  list of expanded paths.
     */
    protected static List<String[]> getExpanded(TreeView view, Node root) {
        List<String[]> paths = new LinkedList<String[]>();
        Stack<Node> stack = new Stack<Node>();
        stack.push(root);
        Set<Node> expanded = new HashSet<Node>();
        while (!stack.empty()) {
            Node node = stack.pop();
            if (view.isExpanded(node)) {
                Node[] kids = node.getChildren().getNodes();
                for (Node kid : kids) {
                    stack.push(kid);
                }
            } else {
                Node parent = node.getParentNode();
                if (parent != null) {
                    expanded.add(parent);
                }
            }
        }
        for (Node node : expanded) {
            String[] path = NodeOp.createPath(node, root);
            paths.add(path);
        }
        return paths;
    }

    /**
     * Checks if the given Session is the current session or not.
     *
     * @param  session  session to compare.
     * @return  true if current session, false otherwise.
     */
    protected static boolean isCurrent(Session session) {
        SessionManager sm = SessionProvider.getSessionManager();
        Session current = sm.getCurrent();
        return current.equals(session);
    }

    /**
     * Checks if the given event is for the current session or not.
     *
     * @param  sevt  session event.
     * @return  true if current session, false otherwise.
     */
    protected static boolean isCurrent(SessionEvent sevt) {
        return isCurrent(sevt.getSession());
    }

    /**
     * Restore the column settings from the input stream.
     *
     * @param  in       the stream to deserialize from.
     * @param  columns  the columns to be restored.
     */
    protected static void restoreColumns(ObjectInput in, Node.Property[] columns) {
        try {
            int count = in.readInt();
            for (int ii = 0; ii < count; ii++) {
                boolean b = in.readBoolean();
                columns[ii].setValue("InvisibleInTreeTableView", Boolean.valueOf(b));
                int i = in.readInt();
                columns[ii].setValue("OrderNumberTTV", Integer.valueOf(i));
                b = in.readBoolean();
                columns[ii].setValue("SortingColumnTTV", Boolean.valueOf(b));
                b = in.readBoolean();
                columns[ii].setValue("DescendingOrderTTV", Boolean.valueOf(b));
            }
        } catch (Exception e) {
            // Could be reading an old instance which is missing data.
            // In any case, ignore this as there is no use in reporting it.
        }
    }

    /**
     * Save the column settings to the output stream.
     *
     * @param  out      the stream to serialize to.
     * @param  columns  the columns to be saved.
     */
    protected static void saveColumns(ObjectOutput out, Node.Property[] columns)
            throws IOException {
        out.writeInt(columns.length);
        for (int ii = 0; ii < columns.length; ii++) {
            Boolean b = (Boolean) columns[ii].getValue("InvisibleInTreeTableView");
            if (b == null) {
                b = Boolean.FALSE;
            }
            out.writeBoolean(b.booleanValue());
            Integer i = (Integer) columns[ii].getValue("OrderNumberTTV");
            if (i == null) {
                i = new Integer(ii);
            }
            out.writeInt(i.intValue());
            b = (Boolean) columns[ii].getValue("SortingColumnTTV");
            if (b == null) {
                b = Boolean.FALSE;
            }
            out.writeBoolean(b.booleanValue());
            b = (Boolean) columns[ii].getValue("DescendingOrderTTV");
            if (b == null) {
                b = Boolean.FALSE;
            }
            out.writeBoolean(b.booleanValue());
        }
    }
}
