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

import com.bluemarsh.jswat.nodes.variables.VariableNode;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class EvaluatorView shows the evaluation input field and the results of that
 * evaluation in tree form.
 * <p/>
 * @author Nathan Fiedler
 */
public class EvaluatorView extends AbstractView implements ExplorerManager.Provider {

    /**
     * silence the compiler warnings
     */
    private static final long serialVersionUID = 1L;
    /**
     * Panel which provides the interface for expression evaluation.
     */
    private EvaluatorPanel evaluatorPanel;
    /**
     * Our explorer manager.
     */
    private ExplorerManager explorerManager;
    /**
     * Component showing our nodes.
     */
    private PersistentOutlineView nodeView;

    /**
     * Creates a new instance of EvaluatorView.
     */
    public EvaluatorView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the nodes view.
        String columnLabel = NbBundle.getMessage(
                BreakpointsView.class, "CTL_EvaluatorView_Column_Name_"
                + VariableNode.PROP_NAME);
        nodeView = new PersistentOutlineView(columnLabel);
        nodeView.getOutline().setRootVisible(false);
        addColumn(nodeView, VariableNode.PROP_TYPE);
        addColumn(nodeView, VariableNode.PROP_VALUE);
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
                BreakpointsView.class, "CTL_EvaluatorView_Column_Name_" + name);
        String description = NbBundle.getMessage(
                BreakpointsView.class, "CTL_EvaluatorView_Column_Desc_" + name);
        view.addPropertyColumn(name, displayName, description);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     * <p/>
     * @param kids root node's children, or Children.LEAF if none.
     */
    void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                EvaluatorView.class, "CTL_EvaluatorView_Column_Name_"
                + VariableNode.PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                EvaluatorView.class, "CTL_EvaluatorView_Column_Desc_"
                + VariableNode.PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        if (evaluatorPanel == null) {
            evaluatorPanel = new EvaluatorPanel(this);
            add(evaluatorPanel, BorderLayout.NORTH);
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EvaluatorView.class, "CTL_EvaluatorView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-evaluator-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(EvaluatorView.class, "CTL_EvaluatorView_Tooltip");
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }

    // Secret, undocumented method that NetBeans calls?
    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if (version.equals("1.0")) {
            nodeView.readSettings(p, "Evaluator");
        }
    }

    // Secret, undocumented method that NetBeans calls?
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        nodeView.writeSettings(p, "Evaluator");
    }
}
