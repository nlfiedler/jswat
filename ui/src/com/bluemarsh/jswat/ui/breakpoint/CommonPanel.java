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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.Condition;
import com.bluemarsh.jswat.core.breakpoint.ExpressionCondition;
import com.bluemarsh.jswat.core.breakpoint.HitCountCondition;
import com.bluemarsh.jswat.core.breakpoint.HitCountConditionType;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.NameValuePair;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.NbBundle;

/**
 * CommonPanel edits the common breakpoint attributes.
 *
 * @author  Nathan Fiedler
 */
public class CommonPanel extends AbstractAdapter
        implements ChangeListener, FocusListener, ItemListener {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;
    /** Model for hit count combo box. */
    private DefaultComboBoxModel hitCountModel;

    /**
     * Creates new form CommonPanel.
     */
    public CommonPanel() {
        initComponents();
        enabledCheckBox.setSelected(true);
        Session session = SessionProvider.getCurrentSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup group = bm.getDefaultGroup();
        Breakpoints.buildGroupList(group, groupComboBox);
        // Populate the hit count combobox.
        hitCountModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(CommonPanel.class, "CTL_Common_HitCount_Equal");
        NameValuePair<HitCountConditionType> pair =
                new NameValuePair<HitCountConditionType>(label, HitCountConditionType.EQUAL);
        hitCountModel.addElement(pair);
        label = NbBundle.getMessage(CommonPanel.class, "CTL_Common_HitCount_Greater");
        pair = new NameValuePair<HitCountConditionType>(label, HitCountConditionType.GREATER);
        hitCountModel.addElement(pair);
        label = NbBundle.getMessage(CommonPanel.class, "CTL_Common_HitCount_Multiple");
        pair = new NameValuePair<HitCountConditionType>(label, HitCountConditionType.MULTIPLE);
        hitCountModel.addElement(pair);
        hitCountComboBox.setModel(hitCountModel);
        // Set hit count spinner minimum value.
        SpinnerNumberModel snm = (SpinnerNumberModel) hitCountSpinner.getModel();
        snm.setMinimum(new Integer(0));
    }

    @Override
    public boolean canCreateBreakpoint() {
        return false;
    }

    @Override
    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        return null;
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == hitCountCheckBox) {
            boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
            hitCountSpinner.setEnabled(enabled);
            hitCountComboBox.setEnabled(enabled);
        }
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    @Override
    public void loadParameters(Breakpoint bp) {
        enabledCheckBox.setSelected(bp.isEnabled());
        Breakpoints.findAndSelectGroup(groupComboBox, bp);
        // Process breakpoint conditions, if any.
        hitCountCheckBox.setSelected(false);
        hitCountComboBox.setEnabled(false);
        hitCountSpinner.setEnabled(false);
        Iterator<Condition> citer = bp.conditions();
        while (citer.hasNext()) {
            Condition cond = citer.next();
            if (cond instanceof ExpressionCondition) {
                ExpressionCondition ec = (ExpressionCondition) cond;
                conditionTextField.setText(ec.getExpression());
                break;
            } else if (cond instanceof HitCountCondition) {
                HitCountCondition hcc = (HitCountCondition) cond;
                hitCountCheckBox.setSelected(true);
                hitCountComboBox.setEnabled(true);
                hitCountSpinner.setEnabled(true);
                hitCountSpinner.setValue(hcc.getCount());
                HitCountConditionType hct = hcc.getType();
                for (int ii = hitCountModel.getSize() - 1; ii >= 0; ii--) {
                    NameValuePair<?> pair =
                            (NameValuePair<?>) hitCountModel.getElementAt(ii);
                    HitCountConditionType hcp = (HitCountConditionType) pair.getValue();
                    if (hcp.equals(hct)) {
                        hitCountModel.setSelectedItem(pair);
                    }
                }
            }
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            conditionTextField.addFocusListener(this);
            enabledCheckBox.addItemListener(this);
            groupComboBox.addItemListener(this);
            hitCountCheckBox.addItemListener(this);
            hitCountComboBox.addItemListener(this);
            hitCountSpinner.addChangeListener(this);
        }
        breakpoint = bp;
    }

    @Override
    public void saveParameters(Breakpoint bp) {
        // Update enabled state, expire count, and skip count.
        bp.setEnabled(enabledCheckBox.isSelected());

        // Update parent breakpoint group, if changed.
        BreakpointGroup newGroup = Breakpoints.getSelectedGroup(groupComboBox);
        BreakpointGroup oldGroup = bp.getBreakpointGroup();
        if (newGroup != oldGroup) {
            if (oldGroup != null) {
                oldGroup.removeBreakpoint(bp);
            }
            newGroup.addBreakpoint(bp);
        }

        // Find the breakpoint conditions that we support.
        ExpressionCondition ec = null;
        HitCountCondition hcc = null;
        Iterator<Condition> citer = bp.conditions();
        while (citer.hasNext()) {
            Condition cond = citer.next();
            if (cond instanceof ExpressionCondition) {
                ec = (ExpressionCondition) cond;
            } else if (cond instanceof HitCountCondition) {
                hcc = (HitCountCondition) cond;
            }
        }

        // Handle addition/removal of expression condition.
        String condition = conditionTextField.getText();
        if (condition.length() > 0) {
            if (ec == null) {
                ec = new ExpressionCondition();
                bp.addCondition(ec);
            }
            ec.setExpression(condition);
        } else if (ec != null) {
            bp.removeCondition(ec);
        }

        // Handle addition/removal of hit count condition.
        if (hitCountCheckBox.isSelected()) {
            if (hcc == null) {
                hcc = new HitCountCondition();
                bp.addCondition(hcc);
            }
            NameValuePair<?> pair =
                    (NameValuePair<?>) hitCountComboBox.getSelectedItem();
            HitCountConditionType hct = (HitCountConditionType) pair.getValue();
            hcc.setType(hct);
            Integer count = (Integer) hitCountSpinner.getValue();
            hcc.setCount(count);
        } else if (hcc != null) {
            bp.removeCondition(hcc);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    @Override
    public String validateInput() {
        // We have nothing to validate.
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enabledCheckBox = new javax.swing.JCheckBox();
        groupLabel = new javax.swing.JLabel();
        groupComboBox = new javax.swing.JComboBox();
        conditionLabel = new javax.swing.JLabel();
        conditionTextField = new javax.swing.JTextField();
        hitCountCheckBox = new javax.swing.JCheckBox();
        hitCountComboBox = new javax.swing.JComboBox();
        hitCountSpinner = new javax.swing.JSpinner();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LBL_Common_Border_Title"))); // NOI18N

        enabledCheckBox.setText(bundle.getString("LBL_CommonEnabled")); // NOI18N
        enabledCheckBox.setToolTipText(bundle.getString("HINT_Common_Enabled")); // NOI18N

        groupLabel.setLabelFor(groupComboBox);
        groupLabel.setText(bundle.getString("LBL_CommonGroup")); // NOI18N

        groupComboBox.setToolTipText(bundle.getString("HINT_Common_Group")); // NOI18N

        conditionLabel.setLabelFor(conditionTextField);
        conditionLabel.setText(bundle.getString("LBL_Common_Condition")); // NOI18N

        conditionTextField.setColumns(20);
        conditionTextField.setToolTipText(bundle.getString("HINT_Common_Condition")); // NOI18N

        hitCountCheckBox.setText(bundle.getString("LBL_Common_HitCountLabel")); // NOI18N

        hitCountComboBox.setToolTipText(bundle.getString("HINT_Common_HitCountType")); // NOI18N

        hitCountSpinner.setToolTipText(bundle.getString("HINT_Common_HitCountValue")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(enabledCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(conditionLabel)
                            .addComponent(groupLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(groupComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(conditionTextField)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hitCountCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hitCountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(hitCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupLabel)
                    .addComponent(groupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(conditionLabel)
                    .addComponent(conditionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hitCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hitCountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hitCountCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JTextField conditionTextField;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JComboBox groupComboBox;
    private javax.swing.JLabel groupLabel;
    private javax.swing.JCheckBox hitCountCheckBox;
    private javax.swing.JComboBox hitCountComboBox;
    private javax.swing.JSpinner hitCountSpinner;
    // End of variables declaration//GEN-END:variables
}
