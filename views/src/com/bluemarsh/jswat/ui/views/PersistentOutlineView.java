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

import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;

/**
 * An OutlineView subclass that persists the various settings, and restores them
 * as needed.
 *
 * @author Nathan Fiedler
 */
public class PersistentOutlineView extends OutlineView {

    /**
     * silence the compiler warnings
     */
    private static final long serialVersionUID = 1L;

    protected PersistentOutlineView() {
        super();
    }

    protected PersistentOutlineView(String nodesColumnLabel) {
        super(nodesColumnLabel);
    }

    /**
     * Restore the column width values from the input stream (performed on the
     * AWT event thread since this affects Swing components).
     *
     * @param int the stream to deserialize from.
     */
    public void restoreColumnWidths(ObjectInput in) {
// TODO: replace with call to OutlineView.readSettings()
    }

    /**
     * Save the column width values to the output stream.
     *
     * @param out the stream to serialize to.
     */
    public void saveColumnWidths(ObjectOutput out) {
// TODO: replace with call to OutlineView.writeSettings()
    }

    /**
     * Select the given node in the tree, scrolling as needed to make the node
     * visible, as well as expanding the path to the node.
     *
     * @param node node to be selected.
     */
    public void scrollAndSelectNode(Node node) {
// TODO: get OutlineView column scroll/select working
        // It is basically guaranteed that the model is a NodeTreeModel.
//        NodeTreeModel model = (NodeTreeModel) tree.getModel();
//        TreeNode tn = Visualizer.findVisualizer(node);
//        TreeNode[] tnp = model.getPathToRoot(tn);
//        TreePath path = new TreePath(tnp);
//        tree.setSelectionPath(path);
//        tree.scrollPathToVisible(path);
    }
}
