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
 * $Id: EvaluatorPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.nodes.MessageNode;
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

    /**
     * Creates new form EvaluatorPanel.
     *
     * @param  view  the view where result is displayed.
     */
    public EvaluatorPanel(EvaluatorView view) {
        initComponents();
        evaluateButton.addActionListener(this);
        expressionComboBox.addActionListener(this);
        resultView = view;
    }

    /**
     * Performs the evaluate action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (!cmd.equals(expressionComboBox.getActionCommand())) {
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
                    VariableFactory vf = VariableFactory.getInstance();
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

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
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

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton evaluateButton;
    private javax.swing.JComboBox expressionComboBox;
    private javax.swing.JLabel inputLabel;
    // End of variables declaration//GEN-END:variables
}
