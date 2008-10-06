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
 * $Id: ActionsPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.BeepMonitor;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.ExpressionMonitor;
import com.bluemarsh.jswat.core.breakpoint.Monitor;
import com.bluemarsh.jswat.core.breakpoint.StackTraceMonitor;
import com.bluemarsh.jswat.core.util.NameValuePair;
import com.sun.jdi.request.EventRequest;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 * ActionsPanel edits the common breakpoint actions.
 *
 * @author  Nathan Fiedler
 */
public class ActionsPanel extends AbstractAdapter
        implements FocusListener, ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Suspend policy of breakpoint. */
    private static enum Policy { ALL, EVENT, NONE };
    /** The suspend combo model. */
    private DefaultComboBoxModel suspendModel;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form ActionsPanel.
     */
    public ActionsPanel() {
        initComponents();
        expressionCheckBox.addItemListener(this);
        expressionTextField.setEnabled(false);
        // Populate the suspend combobox.
        suspendModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(getClass(), "CTL_Actions_Suspend_All");
        NameValuePair<Policy> pair = new NameValuePair<Policy>(label, Policy.ALL);
        suspendModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Actions_Suspend_Event");
        pair = new NameValuePair<Policy>(label, Policy.EVENT);
        suspendModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Actions_Suspend_None");
        pair = new NameValuePair<Policy>(label, Policy.NONE);
        suspendModel.addElement(pair);
        suspendComboBox.setModel(suspendModel);
    }

    public boolean canCreateBreakpoint() {
        return false;
    }

    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        return null;
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null && breakpoint != null) {
            saveParameters(breakpoint);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == expressionCheckBox) {
            expressionTextField.setEnabled(e.getStateChange() == e.SELECTED);
        }
        String msg = validateInput();
        fireInputPropertyChange(msg);
        if (msg == null && breakpoint != null) {
            saveParameters(breakpoint);
        }
    }

    public void loadParameters(Breakpoint bp) {
        int suspendPolicy = bp.getSuspendPolicy();
        Policy policy;
        switch (suspendPolicy) {
            case EventRequest.SUSPEND_ALL :
                policy = Policy.ALL;
                break;
            case EventRequest.SUSPEND_EVENT_THREAD :
                policy = Policy.EVENT;
                break;
            case EventRequest.SUSPEND_NONE :
                policy = Policy.NONE;
                break;
            default :
                // This is unexpected, but treat it as an 'all' case.
                policy = Policy.ALL;
                break;
        }
        for (int ii = suspendModel.getSize() - 1; ii >= 0; ii--) {
            NameValuePair<?> pair =
                    (NameValuePair<?>) suspendModel.getElementAt(ii);
            Policy sp = (Policy) pair.getValue();
            if (sp.equals(policy)) {
                suspendModel.setSelectedItem(pair);
            }
        }

        // Find any monitors and prepare the interface appropriately.
        Iterator<Monitor> miter = bp.monitors();
        while (miter.hasNext()) {
            Monitor mon = miter.next();
            if (mon instanceof BeepMonitor) {
                beepCheckBox.setSelected(true);
            } else if (mon instanceof StackTraceMonitor) {
                stackCheckBox.setSelected(true);
            } else if (mon instanceof ExpressionMonitor) {
                ExpressionMonitor em = (ExpressionMonitor) mon;
                expressionCheckBox.setSelected(true);
                expressionTextField.setEnabled(true);
                expressionTextField.setText(em.getExpression());
            }
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            suspendComboBox.addItemListener(this);
            beepCheckBox.addItemListener(this);
            stackCheckBox.addItemListener(this);
            expressionTextField.addFocusListener(this);
        }
        breakpoint = bp;
    }

    public void saveParameters(Breakpoint bp) {
        NameValuePair<?> pair =
                (NameValuePair<?>) suspendComboBox.getSelectedItem();
        Policy policy = (Policy) pair.getValue();
        int sp = 0;
        switch (policy) {
            case ALL :
                sp = EventRequest.SUSPEND_ALL;
                break;
            case EVENT :
                sp = EventRequest.SUSPEND_EVENT_THREAD;
                break;
            case NONE :
                sp = EventRequest.SUSPEND_NONE;
                break;
        }
        bp.setSuspendPolicy(sp);

        // Update the monitors (may have been added or removed).
        BeepMonitor bm = null;
        StackTraceMonitor stm = null;
        ExpressionMonitor em = null;
        Iterator<Monitor> miter = bp.monitors();
        while (miter.hasNext()) {
            Monitor mon = miter.next();
            if (mon instanceof BeepMonitor) {
                bm = (BeepMonitor) mon;
            } else if (mon instanceof StackTraceMonitor) {
                stm = (StackTraceMonitor) mon;
            } else if (mon instanceof ExpressionMonitor) {
                em = (ExpressionMonitor) mon;
            }
        }

        if (beepCheckBox.isSelected()) {
            if (bm == null) {
                bp.addMonitor(BeepMonitor.getInstance());
            }
        } else if (bm != null) {
            bp.removeMonitor(bm);
        }

        if (stackCheckBox.isSelected()) {
            if (stm == null) {
                bp.addMonitor(StackTraceMonitor.getInstance());
            }
        } else if (stm != null) {
            bp.removeMonitor(stm);
        }

        if (expressionCheckBox.isSelected()) {
            if (em == null) {
                em = new ExpressionMonitor();
                bp.addMonitor(em);
            }
            em.setExpression(expressionTextField.getText());
        } else if (em != null) {
            bp.removeMonitor(em);
        }
    }

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
        suspendLabel = new javax.swing.JLabel();
        suspendComboBox = new javax.swing.JComboBox();
        beepCheckBox = new javax.swing.JCheckBox();
        stackCheckBox = new javax.swing.JCheckBox();
        expressionCheckBox = new javax.swing.JCheckBox();
        expressionTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Actions_Title")));
        suspendLabel.setLabelFor(suspendComboBox);
        suspendLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Actions_Suspend"));

        suspendComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Actions_Suspend"));

        beepCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Actions_Beep"));
        beepCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        beepCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        stackCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Actions_StackTrace"));
        stackCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        stackCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        expressionCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Actions_Expression"));
        expressionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        expressionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(suspendLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(suspendComboBox, 0, 151, Short.MAX_VALUE))
                    .add(beepCheckBox)
                    .add(stackCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(expressionCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(expressionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(suspendLabel)
                    .add(suspendComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(beepCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(stackCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(expressionCheckBox)
                    .add(expressionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox beepCheckBox;
    private javax.swing.JCheckBox expressionCheckBox;
    private javax.swing.JTextField expressionTextField;
    private javax.swing.JCheckBox stackCheckBox;
    private javax.swing.JComboBox suspendComboBox;
    private javax.swing.JLabel suspendLabel;
    // End of variables declaration//GEN-END:variables
}
