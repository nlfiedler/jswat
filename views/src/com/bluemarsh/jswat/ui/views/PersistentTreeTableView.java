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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.views;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.ErrorManager;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;

/**
 * A TreeTableView subclass that persists the various settings, and
 * restores them as needed.
 *
 * @author  Nathan Fiedler
 */
public class PersistentTreeTableView extends TreeTableView {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Restore the column width values from the input stream (performed
     * on the AWT event thread since this affects Swing components).
     *
     * @param  int  the stream to deserialize from.
     */
    public void restoreColumnWidths(ObjectInput in) {
        // Must read from the stream immediately and not on another
        // thread, lest it be closed by the time that thread is run.
        TableColumnModel tcm = treeTable.getColumnModel();
        int count = tcm.getColumnCount();
        final int[] widths = new int[count];
        try {
            for (int index = 0; index < count; index++) {
                widths[index] = in.readInt();
            }
        }  catch (IOException ioe) {
            // Could be reading an old instance which is missing data.
            // In any case, ignore this as there is no use in reporting it
            // (and return immediately so as not to invoke the runnable).
            return;
        }

        // Changing Swing widgets must be done on the AWT event thread.
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // TreeTableView prohibits moving the tree
                // column, so it is always offset zero.
                setTreePreferredWidth(widths[0]);
                for (int index = 1; index < widths.length; index++) {
                    setTableColumnPreferredWidth(index - 1, widths[index]);
                }
            }
            });
    }

    /**
     * Save the column width values to the output stream.
     *
     * @param  out  the stream to serialize to.
     */
    public void saveColumnWidths(ObjectOutput out) {
        try {
            TableColumnModel tcm = treeTable.getColumnModel();
            int count = tcm.getColumnCount();
            for (int index = 0; index < count; index++) {
                TableColumn tc = tcm.getColumn(index);
                int width = tc.getWidth();
                out.writeInt(width);
            }
        }  catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
    }

    /**
     * Select the given node in the tree, scrolling as needed to make the
     * node visible, as well as expanding the path to the node.
     *
     * @param  node  node to be selected.
     */
    public void scrollAndSelectNode(Node node) {
// XXX: this is not working at all!
        // It is basically guaranteed that the model is a NodeTreeModel.
        NodeTreeModel model = (NodeTreeModel) tree.getModel();
        TreeNode tn = Visualizer.findVisualizer(node);
        TreeNode[] tnp = model.getPathToRoot(tn);
        TreePath path = new TreePath(tnp);
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }
}
