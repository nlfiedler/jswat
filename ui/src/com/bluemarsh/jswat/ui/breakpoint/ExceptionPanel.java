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
import com.bluemarsh.jswat.core.breakpoint.ExceptionBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.util.NameValuePair;
import com.bluemarsh.jswat.core.util.Names;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Class ExceptionPanel is the specific editor for a ExceptionBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class ExceptionPanel extends AbstractAdapter
        implements FocusListener, ItemListener {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /** Type of exception breakpoint. */
    private static enum Type {

        CAUGHT, BOTH, UNCAUGHT
    };
    /** The stop-on combo model. */
    private DefaultComboBoxModel stopOnModel;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form ExceptionPanel.
     */
    public ExceptionPanel() {
        initComponents();
        // Populate the stop-on combobox.
        stopOnModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(getClass(), "CTL_Exception_StopOn_Caught");
        NameValuePair<Type> pair = new NameValuePair<Type>(label, Type.CAUGHT);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Exception_StopOn_Uncaught");
        pair = new NameValuePair<Type>(label, Type.UNCAUGHT);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Exception_StopOn_Both");
        pair = new NameValuePair<Type>(label, Type.BOTH);
        stopOnModel.addElement(pair);
        stopOnComboBox.setModel(stopOnModel);
    }

    @Override
    public boolean canCreateBreakpoint() {
        return true;
    }

    @Override
    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        NameValuePair<?> pair =
                (NameValuePair<?>) stopOnComboBox.getSelectedItem();
        Type type = (Type) pair.getValue();
        boolean caught = true;
        boolean uncaught = true;
        switch (type) {
            case CAUGHT:
                uncaught = false;
                break;
            case UNCAUGHT:
                caught = false;
                break;
        }
        Breakpoint bp = null;
        String pkg = packageTextField.getText();
        String cls = classTextField.getText();
        String cname = pkg != null && pkg.length() > 0 ? pkg + '.' + cls : cls;
        try {
            bp = factory.createExceptionBreakpoint(cname, caught, uncaught);
        } catch (MalformedClassNameException mcne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mcne);
        }
        return bp;
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
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String msg = validateInput();
            fireInputPropertyChange(msg);
            if (msg == null) {
                saveParameters(breakpoint);
            }
        }
    }

    @Override
    public void loadParameters(Breakpoint bp) {
        ExceptionBreakpoint eb = (ExceptionBreakpoint) bp;
        boolean caught = eb.getStopOnCaught();
        boolean uncaught = eb.getStopOnUncaught();
        Type type;
        if (caught && uncaught) {
            type = Type.BOTH;
        } else if (caught) {
            type = Type.CAUGHT;
        } else {
            type = Type.UNCAUGHT;
        }
        for (int ii = stopOnModel.getSize() - 1; ii >= 0; ii--) {
            NameValuePair<?> pair =
                    (NameValuePair<?>) stopOnModel.getElementAt(ii);
            Type pt = (Type) pair.getValue();
            if (pt.equals(type)) {
                stopOnModel.setSelectedItem(pair);
            }
        }
        String fullname = eb.getClassName();
        String cname = Names.getShortClassName(fullname);
        classTextField.setText(cname);
        String pname = Names.getPackageName(fullname);
        packageTextField.setText(pname);

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            classTextField.addFocusListener(this);
            packageTextField.addFocusListener(this);
            stopOnComboBox.addItemListener(this);
        }
        breakpoint = bp;
    }

    @Override
    public void saveParameters(Breakpoint bp) {
        NameValuePair<?> pair =
                (NameValuePair<?>) stopOnComboBox.getSelectedItem();
        Type type = (Type) pair.getValue();
        boolean caught = true;
        boolean uncaught = true;
        switch (type) {
            case CAUGHT:
                uncaught = false;
                break;
            case UNCAUGHT:
                caught = false;
                break;
        }
        ExceptionBreakpoint eb = (ExceptionBreakpoint) bp;
        eb.setStopOnCaught(caught);
        eb.setStopOnUncaught(uncaught);
        String pname = packageTextField.getText();
        String cname = classTextField.getText();
        String fullname;
        if (pname != null && pname.length() > 0) {
            fullname = pname + '.' + cname;
        } else {
            fullname = cname;
        }
        try {
            eb.setClassName(fullname);
        } catch (MalformedClassNameException mcne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mcne);
        }
    }

    @Override
    public String validateInput() {
        String pkg = packageTextField.getText();
        String cls = classTextField.getText();
        String cname = pkg != null && pkg.length() > 0 ? pkg + '.' + cls : cls;
        if (!Names.isValidClassname(cname, true)) {
            return NbBundle.getMessage(getClass(), "ERR_Location_Invalid_Class", cname);
        }
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

        packageLabel = new javax.swing.JLabel();
        packageTextField = new javax.swing.JTextField();
        classLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        stopOnLabel = new javax.swing.JLabel();
        stopOnComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Exception_Border")));
        packageLabel.setLabelFor(packageTextField);
        packageLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Exception_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(packageLabel, gridBagConstraints);

        packageTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Exception_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(packageTextField, gridBagConstraints);

        classLabel.setLabelFor(classTextField);
        classLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Exception_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(classLabel, gridBagConstraints);

        classTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Exception_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(classTextField, gridBagConstraints);

        stopOnLabel.setLabelFor(stopOnComboBox);
        stopOnLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Exception_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(stopOnLabel, gridBagConstraints);

        stopOnComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Exception_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(stopOnComboBox, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageTextField;
    private javax.swing.JComboBox stopOnComboBox;
    private javax.swing.JLabel stopOnLabel;
    // End of variables declaration//GEN-END:variables
}
