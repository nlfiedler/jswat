/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: GroupEditorPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.Condition;
import com.bluemarsh.jswat.core.breakpoint.ExpressionCondition;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Editor interface for the breakpoint group.
 *
 * @author  Nathan Fiedler
 */
public class GroupEditorPanel extends JPanel implements ActionListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** Breakpoint group to modify. */
    private BreakpointGroup group;

    /**
     * Creates new form GroupEditorPanel.
     */
    public GroupEditorPanel() {
        initComponents();
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("OK")) {
            // Validate the input before saving the data and closing.
            if (validateInput()) {
                saveParameters(group);
                inputDialog.dispose();
            }
        } else {
            inputDialog.dispose();
        }
    }

    /**
     * Build a dialog for editing the breakpoint group.
     *
     * @return  the breakpoint group editor dialog.
     */
    public Dialog createDialog() {
        // Collect the dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_GroupEditorPanel_Title");
        // Display dialog and get the user response.
        DialogDescriptor dd = new DialogDescriptor(
                this, title, true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx("jswat-edit-bpgroup"), this);
        dd.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        inputDialog = DialogDisplayer.getDefault().createDialog(dd);
        return inputDialog;
    }

    /**
     * Read the values from the given group to populate the fields
     * of this editor.
     *
     * @param  group  BreakpointGroup to edit.
     */
    public void loadParameters(BreakpointGroup group) {
        // Get name and enabled state.
        nameTextField.setText(group.getName());
        enabledCheckBox.setSelected(group.isEnabled());

        // Build the parent group selector.
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup root = bm.getDefaultGroup();
        Breakpoints.buildGroupList(root, parentComboBox, group);
        BreakpointGroup parent = group.getParent();
        if (parent != null) {
            Breakpoints.findAndSelectGroup(parentComboBox, parent);
        } else {
            // Default group has no parent.
            parentComboBox.setEnabled(false);
        }

        // Find expression condition, if any, and get its value.
        Iterator<Condition> citer = group.conditions();
        while (citer.hasNext()) {
            Condition cond = citer.next();
            if (cond instanceof ExpressionCondition) {
                ExpressionCondition ec = (ExpressionCondition) cond;
                conditionTextField.setText(ec.getExpression());
                break;
            }
        }
        this.group = group;
    }

    /**
     * Saves the values from the fields of this editor to the given group.
     *
     * @param  group  BreakpointGroup to modify.
     */
    public void saveParameters(BreakpointGroup group) {
        // Update name and enabled state.
        group.setName(nameTextField.getText());
        group.setEnabled(enabledCheckBox.isSelected());

        // Update parent breakpoint group, if changed.
        BreakpointGroup newGroup = Breakpoints.getSelectedGroup(parentComboBox);
        BreakpointGroup oldGroup = group.getParent();
        // Check if existing parent is null, which indicates that this
        // group is the default group, which cannot have a parent.
        if (oldGroup != null && newGroup != oldGroup && newGroup != group) {
            oldGroup.removeBreakpointGroup(group);
            newGroup.addBreakpointGroup(group);
        }

        // Add, update, or remove the expression condition.
        ExpressionCondition ec = null;
        String condition = conditionTextField.getText();
        Iterator<Condition> citer = group.conditions();
        while (citer.hasNext()) {
            Condition cond = citer.next();
            if (cond instanceof ExpressionCondition) {
                ec = (ExpressionCondition) cond;
                break;
            }
        }
        if (condition.length() > 0) {
            if (ec == null) {
                ec = new ExpressionCondition();
                group.addCondition(ec);
            }
            ec.setExpression(condition);
        } else if (ec != null) {
            group.removeCondition(ec);
        }
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    private boolean validateInput() {
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        enabledCheckBox = new javax.swing.JCheckBox();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        parentLabel = new javax.swing.JLabel();
        parentComboBox = new javax.swing.JComboBox();
        conditionLabel = new javax.swing.JLabel();
        conditionTextField = new javax.swing.JTextField();

        enabledCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_GroupEditor_Enabled"));
        enabledCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_GroupEditor_Enabled"));
        enabledCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enabledCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_GroupEditor_Name"));

        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_GroupEditor_Name"));

        parentLabel.setLabelFor(parentComboBox);
        parentLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_GroupEditor_Parent"));

        parentComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_GroupEditor_Parent"));

        conditionLabel.setLabelFor(conditionTextField);
        conditionLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Common_Condition"));

        conditionTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_GroupEditor_Condition"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(parentLabel)
                    .add(nameLabel)
                    .add(conditionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(enabledCheckBox)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .add(parentComboBox, 0, 280, Short.MAX_VALUE)
                    .add(conditionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enabledCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(parentLabel)
                    .add(parentComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(conditionLabel)
                    .add(conditionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(193, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JTextField conditionTextField;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JComboBox parentComboBox;
    private javax.swing.JLabel parentLabel;
    // End of variables declaration//GEN-END:variables
}
