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
 * are Copyright (C) 2006-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Editor interface for the breakpoint group.
 *
 * @author  Nathan Fiedler
 */
public class GroupEditorPanel extends JPanel implements
        DocumentListener, ItemListener, PropertyChangeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint group to modify. */
    private BreakpointGroup group;

    /**
     * Creates new form GroupEditorPanel.
     */
    public GroupEditorPanel() {
        initComponents();
        nameTextField.getDocument().addDocumentListener(this);
        parentComboBox.addItemListener(this);
        addPropertyChangeListener(this);
    }

    public void addNotify() {
        super.addNotify();
        // Ensure dialog is not overly narrow.
        Dimension size = getPreferredSize();
        size.width = 450;
        setPreferredSize(size);
        validateInput();
    }

    public void changedUpdate(DocumentEvent event) {
    }

    public void itemStateChanged(ItemEvent event) {
        validateInput();
    }

    public void insertUpdate(DocumentEvent event) {
        // This fires an event only if the value has changed.
        putClientProperty(NotifyDescriptor.PROP_VALID,
                Boolean.valueOf(event.getDocument().getLength() > 0));
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

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(NotifyDescriptor.PROP_VALID)) {
            // The input validity has changed in some way.
            validateInput();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Boolean b = (Boolean) getClientProperty(NotifyDescriptor.PROP_VALID);
        if (b.booleanValue()) {
            saveParameters(group);
        }
    }

    public void removeUpdate(DocumentEvent event) {
        // This fires an event only if the value has changed.
        putClientProperty(NotifyDescriptor.PROP_VALID,
                Boolean.valueOf(event.getDocument().getLength() > 0));
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
     * Verify the user input and display an appropriate message.
     */
    private void validateInput() {
        boolean valid = true;
        String name = nameTextField.getText();
        if (name == null || name.length() == 0) {
            validationTextField.setText(NbBundle.getMessage(
                    GroupEditorPanel.class, "ERR_GroupEditorPanel_MissingName"));
            valid = false;
        }
        if (valid) {
            validationTextField.setText(null);
        }
        // This fires an event only if the value changes.
        putClientProperty(NotifyDescriptor.PROP_VALID, Boolean.valueOf(valid));
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
        validationTextField = new javax.swing.JTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form"); // NOI18N
        enabledCheckBox.setText(bundle.getString("LBL_GroupEditor_Enabled")); // NOI18N
        enabledCheckBox.setToolTipText(bundle.getString("HINT_GroupEditor_Enabled")); // NOI18N
        enabledCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enabledCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(bundle.getString("LBL_GroupEditor_Name")); // NOI18N

        nameTextField.setToolTipText(bundle.getString("HINT_GroupEditor_Name")); // NOI18N

        parentLabel.setLabelFor(parentComboBox);
        parentLabel.setText(bundle.getString("LBL_GroupEditor_Parent")); // NOI18N

        parentComboBox.setToolTipText(bundle.getString("HINT_GroupEditor_Parent")); // NOI18N

        conditionLabel.setLabelFor(conditionTextField);
        conditionLabel.setText(bundle.getString("LBL_Common_Condition")); // NOI18N

        conditionTextField.setToolTipText(bundle.getString("HINT_GroupEditor_Condition")); // NOI18N

        validationTextField.setEditable(false);
        validationTextField.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        validationTextField.setBorder(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(validationTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(parentLabel)
                            .addComponent(nameLabel)
                            .addComponent(conditionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(parentComboBox, 0, 66, Short.MAX_VALUE)
                            .addComponent(conditionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                            .addComponent(enabledCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parentLabel)
                    .addComponent(parentComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conditionLabel)
                    .addComponent(conditionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
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
    private javax.swing.JTextField validationTextField;
    // End of variables declaration//GEN-END:variables
}
