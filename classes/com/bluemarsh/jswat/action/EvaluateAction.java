/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: EvaluateAction.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.DefaultComboBoxModel;

/**
 * Implements the evaluate action used to evaluate Java expressions.
 *
 * @author  Nathan Fiedler
 */
public class EvaluateAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new EvaluateAction object with the default action
     * command string of "evaluate".
     */
    public EvaluateAction() {
        super("evaluate");
    } // EvaluateAction

    /**
     * Performs the evaluate action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Frame topFrame = getFrame(event);
        JDialog dialog = new EvaluateDialog(topFrame);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(topFrame);
        dialog.setVisible(true);
    } // actionPerformed
} // EvaluateAction

/**
 * Class EvaluateDialog is a dialog for evaluating expressions.
 *
 * @author  Nathan Fiedler
 */
class EvaluateDialog extends JDialog implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Text field to receive expression. */
    private JComboBox comboBox;
    /** Text area to show evaluation results. */
    private JTextArea textArea;
    /** Button to close the dialog. */
    private JButton closeButton;
    /** Model for the combobox. */
    private static DefaultComboBoxModel evalHistory =
        new DefaultComboBoxModel();

    /**
     * Constructs a EvaluateDialog with the given parent frame.
     *
     * @param  topFrame  parent frame.
     */
    public EvaluateDialog(Frame topFrame) {
        super(topFrame, Bundle.getString("Evaluate.title"));
        Container pane = getContentPane();
        GridBagLayout gbl = new GridBagLayout();
        pane.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);

        JLabel label = new JLabel(Bundle.getString("Evaluate.enterExpr"));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(label, gbc);
        pane.add(label);

        comboBox = new JComboBox(evalHistory);
        comboBox.addActionListener(this);
        comboBox.setEditable(true);
        Component editor = comboBox.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            ((JTextField) editor).setColumns(30);
        }
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(comboBox, gbc);
        pane.add(comboBox);

        JButton button = new JButton(Bundle.getString("Evaluate.evalLabel"));
        button.addActionListener(this);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        label = new JLabel(Bundle.getString("Evaluate.result"));
        gbl.setConstraints(label, gbc);
        pane.add(label);

        textArea = new JTextArea(10, 30);
        textArea.setEditable(false);
        JScrollPane scroller = new JScrollPane(textArea);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        closeButton = new JButton(Bundle.getString("closeButton"));
        closeButton.addActionListener(this);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbl.setConstraints(closeButton, gbc);
        pane.add(closeButton);
    } // EvaluateDialog

    /**
     * Performs the evaluate action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == closeButton) {
            dispose();
            return;
        }

        String cmd = event.getActionCommand();
        if (!cmd.equals("comboBoxChanged")) {
            // The combobox was edited or the evaluate button was invoked.
            Session session = JSwatAction.getSession(event);
            ContextManager ctxtman = (ContextManager)
                session.getManager(ContextManager.class);
            ThreadReference thread = ctxtman.getCurrentThread();
            int frame = ctxtman.getCurrentFrame();

            String expr = (String) comboBox.getEditor().getItem();
            if (expr.length() == 0) {
                textArea.setText("");
                return;
            }

            Evaluator eval = new Evaluator(expr);
            String s;
            try {
                Object o = eval.evaluate(thread, frame);
                if (o instanceof Value) {
                    Value v = (Value) o;
                    s = Variables.printValue(v, thread, "\n");
                } else {
                    if (o == null) {
                        s = "null";
                    } else {
                        s = o.toString();
                    }
                }
            } catch (EvaluationException ee) {
                s = Bundle.getString("Evaluate.error") + ' '
                    + Strings.exceptionToString(ee);
            } catch (Exception e) {
                s = Bundle.getString("Evaluate.error") + ' '
                    + Strings.exceptionToString(e);
            }

            textArea.setText(s);

            int index = evalHistory.getIndexOf(expr);
            if (index == -1) {
                evalHistory.insertElementAt(expr, 0);
            }
        }
        // Else, we ignore the combobox changed event.
    } // actionPerformed

    /**
     * Creates the root pane for the dialog.
     *
     * @return root pane.
     */
    protected JRootPane createRootPane() {
        // From: http://www.javaworld.com/javaworld/javatips/jw-javatip72.html
        // Handle the Esc key to dismiss the dialog.
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    } // createRootPane
} // EvaluateDialog
