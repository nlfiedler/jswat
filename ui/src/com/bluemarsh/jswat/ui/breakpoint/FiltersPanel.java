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
 * $Id: FiltersPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import org.openide.util.NbBundle;

/**
 * Class FiltersPanel provides a means of setting the class and thread
 * filters for a breakpoint, either during creation or subsequent
 * modification.
 *
 * @author  Nathan Fiedler
 */
public class FiltersPanel extends AbstractAdapter implements FocusListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form ThreadFilterPanel.
     *
     * @param  type  type of breakpoint to be edited.
     */
    public FiltersPanel(BreakpointType type) {
        initComponents();
        String msg = NbBundle.getMessage(getClass(),
                "CTL_Filters_Filter_Unsupported");
        switch (type) {
            case CLASS :
                threadFilterTextField.setEnabled(false);
                threadFilterTextField.setText(msg);
                break;
            case EXCEPTION :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                threadFilterTextField.setEnabled(false);
                threadFilterTextField.setText(msg);
                break;
            case LINE :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                break;
            case LOCATION :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                break;
            case METHOD :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                threadFilterTextField.setEnabled(false);
                threadFilterTextField.setText(msg);
                break;
            case THREAD :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                break;
            case UNCAUGHT :
                classFilterTextField.setEnabled(false);
                classFilterTextField.setText(msg);
                threadFilterTextField.setEnabled(false);
                threadFilterTextField.setText(msg);
                break;
        }
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
        if (msg == null) {
            saveParameters(breakpoint);
        }
    }

    public void loadParameters(Breakpoint bp) {
        if (bp.canFilterClass()) {
            String filter = bp.getClassFilter();
            if (filter != null) {
                classFilterTextField.setText(filter);
            }
        } else {
            classFilterTextField.setEnabled(false);
        }
        if (bp.canFilterThread()) {
            String filter = bp.getThreadFilter();
            if (filter != null) {
                threadFilterTextField.setText(filter);
            }
        } else {
            threadFilterTextField.setEnabled(false);
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            classFilterTextField.addFocusListener(this);
            threadFilterTextField.addFocusListener(this);
        }
        breakpoint = bp;
    }

    public void saveParameters(Breakpoint bp) {
        if (bp.canFilterClass()) {
            String filter = classFilterTextField.getText();
            if (filter.length() == 0) {
                filter = null;
            }
            bp.setClassFilter(filter);
        }
        if (bp.canFilterThread()) {
            String filter = threadFilterTextField.getText();
            if (filter.length() == 0) {
                filter = null;
            }
            bp.setThreadFilter(filter);
        }
    }

    public String validateInput() {
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

        classFilterLabel = new javax.swing.JLabel();
        classFilterTextField = new javax.swing.JTextField();
        threadFilterLabel = new javax.swing.JLabel();
        threadFilterTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Filters_BorderTitle")));
        classFilterLabel.setLabelFor(classFilterTextField);
        classFilterLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_ClassFilter_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(classFilterLabel, gridBagConstraints);

        classFilterTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_ClassFilter_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(classFilterTextField, gridBagConstraints);

        threadFilterLabel.setLabelFor(threadFilterTextField);
        threadFilterLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_ThreadFilter_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(threadFilterLabel, gridBagConstraints);

        threadFilterTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_ThreadFilter_Filter"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(threadFilterTextField, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classFilterLabel;
    private javax.swing.JTextField classFilterTextField;
    private javax.swing.JLabel threadFilterLabel;
    private javax.swing.JTextField threadFilterTextField;
    // End of variables declaration//GEN-END:variables
}
