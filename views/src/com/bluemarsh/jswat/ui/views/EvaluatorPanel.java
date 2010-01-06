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

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.MessageNode;
import com.bluemarsh.jswat.nodes.variables.VariableFactory;
import com.bluemarsh.jswat.nodes.variables.VariableNode;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Class EvaluatorPanel constructs the panel which contains the widgets for
 * the expression evaluator interface.
 *
 * @author  Nathan Fiedler
 */
public class EvaluatorPanel extends javax.swing.JPanel implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Where the evaluation result is displayed. */
    private EvaluatorView resultView;
    /** JComboBox changes the action command when firing events so it is
     * impossible to distinquish between a "changed" and an "edited" event.
     * So, save the original command so we can detect which is which. */
    private final String comboActionCmd;

    /**
     * Creates new form EvaluatorPanel.
     *
     * @param  view  the view where result is displayed.
     */
    public EvaluatorPanel(EvaluatorView view) {
        initComponents();
        evaluateButton.addActionListener(this);
        expressionComboBox.addActionListener(this);
        comboActionCmd = expressionComboBox.getActionCommand();
        resultView = view;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (!cmd.equals(comboActionCmd)) {
            // The combobox was edited or the evaluate button was invoked.
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            int frame = dc.getFrame();

            String expr = (String) expressionComboBox.getEditor().getItem();
            String orgexpr = expr;
            int comma = expr.indexOf(",");
            String mods = comma >= 0 ? expr.substring(comma + 1).trim() : null;
            expr = comma > 0 ? expr.substring(0, comma) : (comma == 0 ? "" : expr);
            String msg = null;
            Value result = null;
            if (expr.length() == 0) {
                msg = "";
            } else {
                Evaluator eval = new Evaluator(expr);
                try {
                    Object o = eval.evaluate(thread, frame);
                    if (o instanceof Value) {
                        // From the debuggee, build out the object tree.
                        result = (Value) o;
                    } else {
                        // Not from the debuggee, just convert to a string.
                        msg = o == null ? "null" : o.toString();
                    }
                } catch (EvaluationException ee) {
                    msg = ee.getLocalizedMessage();
                    if (msg == null || msg.length() == 0) {
                        msg = NbBundle.getMessage(EvaluatorPanel.class,
                                "ERR_Evaluation_error", ee);
                    }
                    // Notify so I can get the stack trace.
                    ErrorManager.getDefault().notify(ee);
                } catch (Exception e) {
                    msg = e.getLocalizedMessage();
                    if (msg == null || msg.length() == 0) {
                        msg = NbBundle.getMessage(EvaluatorPanel.class,
                                "ERR_Evaluation_error", e);
                    }
                    // Notify so I can get the stack trace.
                    ErrorManager.getDefault().notify(e);
                }
            }

            Node node = null;
            if (msg != null) {
                node = new MessageNode(msg);
            } else if (result != null) {
                if (result instanceof VoidValue) {
                    node = new MessageNode("void");
                } else {
                    VariableFactory vf = VariableFactory.getDefault();
                    if (mods != null && mods.length() == 0) {
                        mods = null;
                    }
                    node = vf.create(orgexpr, result.type().name(), result,
                            VariableNode.Kind.LOCAL, mods);
                }
            }
            if (node != null) {
                Node[] nodes = new Node[] { node };
                Children children = new Children.Array();
                children.add(nodes);
                resultView.buildRoot(children);
            }

            DefaultComboBoxModel model =
                    (DefaultComboBoxModel) expressionComboBox.getModel();
            if (model.getIndexOf(expr) == -1) {
                model.insertElementAt(expr, 0);
            }
        }
        // Else, we ignore the combobox changed event.
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        inputLabel = new javax.swing.JLabel();
        expressionComboBox = new javax.swing.JComboBox();
        evaluateButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/views/Forms").getString("LBL_EvaluatorInput"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(inputLabel, gridBagConstraints);

        expressionComboBox.setEditable(true);
        expressionComboBox.setFont(new java.awt.Font("DialogInput", 0, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.75;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(expressionComboBox, gridBagConstraints);

        evaluateButton.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/views/Forms").getString("LBL_EvaluateButton"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(evaluateButton, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton evaluateButton;
    private javax.swing.JComboBox expressionComboBox;
    private javax.swing.JLabel inputLabel;
    // End of variables declaration//GEN-END:variables
}
