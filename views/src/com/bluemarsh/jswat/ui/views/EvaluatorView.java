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
 * are Copyright (C) 2004-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EvaluatorView.java 30 2008-06-30 01:12:15Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.nodes.variables.VariableNode;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class EvaluatorView shows the evaluation input field and the results
 * of that evaluation in tree form.
 *
 * @author  Nathan Fiedler
 */
public class EvaluatorView extends AbstractView implements ExplorerManager.Provider {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Panel which provides the interface for expression evaluation. */
    private EvaluatorPanel evaluatorPanel;
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Creates a new instance of EvaluatorView.
     */
    public EvaluatorView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the nodes view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(VariableNode.PROP_NAME, true, true, false),
            new Column(VariableNode.PROP_TYPE, false, true, false),
            new Column(VariableNode.PROP_VALUE, false, true, false),
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
    void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                EvaluatorView.class, "CTL_EvaluatorView_Column_Name_" +
                VariableNode.PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                EvaluatorView.class, "CTL_EvaluatorView_Column_Desc_" +
                VariableNode.PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    protected void componentOpened() {
        super.componentOpened();
        if (evaluatorPanel == null) {
            evaluatorPanel = new EvaluatorPanel(this);
            add(evaluatorPanel, BorderLayout.NORTH);
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EvaluatorView.class, "CTL_EvaluatorView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-evaluator-view");
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String getToolTipText() {
        return NbBundle.getMessage(EvaluatorView.class, "CTL_EvaluatorView_Tooltip");
    }

    protected String preferredID() {
        return getClass().getName();
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
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
    private class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key       keyword for this column.
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         * @param  hidden    true to hide this column initially.
         */
        @SuppressWarnings("unchecked")
        public Column(String key, boolean tree, boolean sortable, boolean hidden) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_EvaluatorView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_EvaluatorView_Column_Desc_" + key));
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
}
