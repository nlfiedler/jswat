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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommonPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.Condition;
import com.bluemarsh.jswat.core.breakpoint.ExpressionCondition;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

    /**
     * Creates new form CommonPanel.
     */
    public CommonPanel() {
        initComponents();
        enabledCheckBox.setSelected(true);
        SpinnerNumberModel snm = (SpinnerNumberModel) expireSpinner.getModel();
        snm.setMinimum(new Integer(0));
        snm = (SpinnerNumberModel) skipSpinner.getModel();
        snm.setMinimum(new Integer(0));
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointGroup group = bm.getDefaultGroup();
        Breakpoints.buildGroupList(group, groupComboBox);
    }

    /**
     * Indicates if this adapter is the sort that can construct a new
     * Breakpoint instance from the user-provided information.
     *
     * @return  true if breakpoint creation is possible, false otherwise.
     */
    public boolean canCreateBreakpoint() {
        return false;
    }

    /**
     * Create a Breakpoint instance that encapsulates the information
     * provided by the user. This may not be entirely complete since
     * some of the information is contained in other adapters. The caller
     * is responsible for invoking <code>saveParameters(Breakpoint)</code>
     * on the other adapters to make the Breakpoint instance complete.
     *
     * @param  factory  breakpoint factory to construct breakpoint.
     * @return  new Breakpoint, or null if creation not supported.
     */
    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        return null;
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    /**
     * Read the values from the given Breakpoint to populate the fields
     * of this editor.
     *
     * @param  bp  Breakpoint to edit.
     */
    public void loadParameters(Breakpoint bp) {
        enabledCheckBox.setSelected(bp.isEnabled());
        expireSpinner.setValue(new Integer(bp.getExpireCount()));
        skipSpinner.setValue(new Integer(bp.getSkipCount()));
        Breakpoints.findAndSelectGroup(groupComboBox, bp);
        // Find expression condition, if any, and get its value.
        Iterator<Condition> citer = bp.conditions();
        while (citer.hasNext()) {
            Condition cond = citer.next();
            if (cond instanceof ExpressionCondition) {
                ExpressionCondition ec = (ExpressionCondition) cond;
                conditionTextField.setText(ec.getExpression());
                break;
            }
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            conditionTextField.addFocusListener(this);
            enabledCheckBox.addItemListener(this);
            groupComboBox.addItemListener(this);
            expireSpinner.addChangeListener(this);
            skipSpinner.addChangeListener(this);
        }
        breakpoint = bp;
    }

    /**
     * Saves the values from the fields of this editor to the given Breakpoint.
     *
     * @param  bp  Breakpoint to modify.
     */
    public void saveParameters(Breakpoint bp) {
        // Update enabled state, expire count, and skip count.
        bp.setEnabled(enabledCheckBox.isSelected());
        Integer i = (Integer) expireSpinner.getValue();
        bp.setExpireCount(i.intValue());
        i = (Integer) skipSpinner.getValue();
        bp.setSkipCount(i.intValue());

        // Update parent breakpoint group, if changed.
        BreakpointGroup newGroup = Breakpoints.getSelectedGroup(groupComboBox);
        BreakpointGroup oldGroup = bp.getBreakpointGroup();
        if (newGroup != oldGroup) {
            if (oldGroup != null) {
                oldGroup.removeBreakpoint(bp);
            }
            newGroup.addBreakpoint(bp);
        }

        // Update the expression condition, if any (may be added or removed).
        ExpressionCondition ec = null;
        String condition = conditionTextField.getText();
        Iterator<Condition> citer = bp.conditions();
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
                bp.addCondition(ec);
            }
            ec.setExpression(condition);
        } else if (ec != null) {
            bp.removeCondition(ec);
        }
    }

    public void stateChanged(ChangeEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    /**
     * Validate the user-provided input.
     *
     * @return  error message if input invalid, null if valid.
     */
    public String validateInput() {
        // We have nothing to validate.
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        enabledCheckBox = new javax.swing.JCheckBox();
        groupLabel = new javax.swing.JLabel();
        groupComboBox = new javax.swing.JComboBox();
        skipLabel = new javax.swing.JLabel();
        skipSpinner = new javax.swing.JSpinner();
        expireLabel = new javax.swing.JLabel();
        expireSpinner = new javax.swing.JSpinner();
        conditionLabel = new javax.swing.JLabel();
        conditionTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Common_Border_Title")));
        enabledCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_CommonEnabled"));
        enabledCheckBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Common_Enabled"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(enabledCheckBox, gridBagConstraints);

        groupLabel.setLabelFor(groupComboBox);
        groupLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_CommonGroup"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(groupLabel, gridBagConstraints);

        groupComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Common_Group"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(groupComboBox, gridBagConstraints);

        skipLabel.setLabelFor(skipSpinner);
        skipLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_CommonSkip"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(skipLabel, gridBagConstraints);

        skipSpinner.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Common_Skip"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(skipSpinner, gridBagConstraints);

        expireLabel.setLabelFor(expireSpinner);
        expireLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_CommonExpire"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(expireLabel, gridBagConstraints);

        expireSpinner.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Common_Expire"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(expireSpinner, gridBagConstraints);

        conditionLabel.setLabelFor(conditionTextField);
        conditionLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Common_Condition"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(conditionLabel, gridBagConstraints);

        conditionTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Common_Condition"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(conditionTextField, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel conditionLabel;
    private javax.swing.JTextField conditionTextField;
    private javax.swing.JCheckBox enabledCheckBox;
    private javax.swing.JLabel expireLabel;
    private javax.swing.JSpinner expireSpinner;
    private javax.swing.JComboBox groupComboBox;
    private javax.swing.JLabel groupLabel;
    private javax.swing.JLabel skipLabel;
    private javax.swing.JSpinner skipSpinner;
    // End of variables declaration//GEN-END:variables
}
