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
 * $Id: WatchPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.InstanceBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.breakpoint.ResolvableBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.WatchBreakpoint;
import com.bluemarsh.jswat.core.util.NameValuePair;
import com.bluemarsh.jswat.core.util.Names;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Class WatchPanel is the specific editor for a WatchBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class WatchPanel extends AbstractAdapter
        implements FocusListener, ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Type of watch breakpoint. */
    private static enum Type { ACCESS, BOTH, MODIFY };
    /** The stop-on combo model. */
    private DefaultComboBoxModel stopOnModel;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form WatchPanel.
     */
    public WatchPanel() {
        initComponents();
        // Populate the stop-on combobox.
        stopOnModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(getClass(), "CTL_Watch_StopOn_Access");
        NameValuePair<Type> pair = new NameValuePair<Type>(label, Type.ACCESS);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Watch_StopOn_Modify");
        pair = new NameValuePair<Type>(label, Type.MODIFY);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Watch_StopOn_Both");
        pair = new NameValuePair<Type>(label, Type.BOTH);
        stopOnModel.addElement(pair);
        stopOnComboBox.setModel(stopOnModel);
    }

    public boolean canCreateBreakpoint() {
        return true;
    }

    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        NameValuePair<?> pair =
                (NameValuePair<?>) stopOnComboBox.getSelectedItem();
        Type type = (Type) pair.getValue();
        boolean access = true;
        boolean modify = true;
        switch (type) {
            case ACCESS :
                modify = false;
                break;
            case MODIFY :
                access = false;
                break;
        }
        Breakpoint bp = null;
        String pkg = packageTextField.getText();
        String cls = classTextField.getText();
        String cname = pkg != null && pkg.length() > 0 ? pkg + '.' + cls : cls;
        String field = fieldTextField.getText();
        try {
            bp = factory.createWatchBreakpoint(cname, field, access, modify);
        } catch (MalformedClassNameException mcne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mcne);
        } catch (MalformedMemberNameException mmne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mmne);
        }
        return bp;
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
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String msg = validateInput();
            fireInputPropertyChange(msg);
            if (msg == null) {
                saveParameters(breakpoint);
            }
        }
    }

    public void loadParameters(Breakpoint bp) {
        WatchBreakpoint wb = (WatchBreakpoint) bp;
        boolean access = wb.getStopOnAccess();
        boolean modify = wb.getStopOnModify();
        Type type;
        if (access && modify) {
            type = Type.BOTH;
        } else if (access) {
            type = Type.ACCESS;
        } else {
            type = Type.MODIFY;
        }
        for (int ii = stopOnModel.getSize() - 1; ii >= 0; ii--) {
            NameValuePair<?> pair =
                    (NameValuePair<?>) stopOnModel.getElementAt(ii);
            Type pt = (Type) pair.getValue();
            if (pt.equals(type)) {
                stopOnModel.setSelectedItem(pair);
            }
        }
        if (bp instanceof ResolvableBreakpoint) {
            ResolvableBreakpoint rb = (ResolvableBreakpoint) bp;
            String fullname = rb.getClassName();
            String cname = Names.getShortClassName(fullname);
            classTextField.setText(cname);
            String pname = Names.getPackageName(fullname);
            packageTextField.setText(pname);
        } else {
            if (bp instanceof InstanceBreakpoint) {
                InstanceBreakpoint ib = (InstanceBreakpoint) bp;
                ObjectReference obj = ib.getObjectReference();
                ReferenceType clazz = obj.referenceType();
                String pname = Names.getPackageName(clazz.name());
                packageTextField.setText(pname);
                String cname = Names.getShortClassName(clazz.name());
                classTextField.setText(cname);
            }
            packageTextField.setEnabled(false);
            classTextField.setEnabled(false);
            // If not resolvable, the field name cannot change either.
            fieldTextField.setEnabled(false);
        }
        fieldTextField.setText(wb.getFieldName());

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            classTextField.addFocusListener(this);
            fieldTextField.addFocusListener(this);
            packageTextField.addFocusListener(this);
            stopOnComboBox.addItemListener(this);
        }
        breakpoint = bp;
    }

    public void saveParameters(Breakpoint bp) {
        NameValuePair<?> pair =
                (NameValuePair<?>) stopOnComboBox.getSelectedItem();
        Type type = (Type) pair.getValue();
        boolean access = true;
        boolean modify = true;
        switch (type) {
            case ACCESS :
                modify = false;
                break;
            case MODIFY :
                access = false;
                break;
        }
        WatchBreakpoint wb = (WatchBreakpoint) bp;
        wb.setStopOnAccess(access);
        wb.setStopOnModify(modify);
        if (bp instanceof ResolvableBreakpoint) {
            ResolvableBreakpoint rb = (ResolvableBreakpoint) bp;
            String pname = packageTextField.getText();
            String cname = classTextField.getText();
            String fullname;
            if (pname != null && pname.length() > 0) {
                fullname = pname + '.' + cname;
            } else {
                fullname = cname;
            }
            try {
                rb.setClassName(fullname);
            } catch (MalformedClassNameException mcne) {
                // Input was already validated, this should not happen.
                ErrorManager.getDefault().notify(mcne);
            }
        }
        try {
            wb.setFieldName(fieldTextField.getText());
        } catch (MalformedMemberNameException mmne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mmne);
        }
    }

    public String validateInput() {
        String field = fieldTextField.getText();
        if (!Names.isJavaIdentifier(field)) {
            return NbBundle.getMessage(getClass(), "ERR_Watch_Invalid_Field", field);
        }
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
        fieldLabel = new javax.swing.JLabel();
        fieldTextField = new javax.swing.JTextField();
        stopOnLabel = new javax.swing.JLabel();
        stopOnComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Location_Border_Title")));
        packageLabel.setLabelFor(packageTextField);
        packageLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Watch_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(packageLabel, gridBagConstraints);

        packageTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Watch_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(packageTextField, gridBagConstraints);

        classLabel.setLabelFor(classTextField);
        classLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Watch_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(classLabel, gridBagConstraints);

        classTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Watch_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(classTextField, gridBagConstraints);

        fieldLabel.setLabelFor(fieldTextField);
        fieldLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Watch_Field"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(fieldLabel, gridBagConstraints);

        fieldTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Watch_Field"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(fieldTextField, gridBagConstraints);

        stopOnLabel.setLabelFor(stopOnComboBox);
        stopOnLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Watch_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(stopOnLabel, gridBagConstraints);

        stopOnComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Watch_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(stopOnComboBox, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel fieldLabel;
    private javax.swing.JTextField fieldTextField;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageTextField;
    private javax.swing.JComboBox stopOnComboBox;
    private javax.swing.JLabel stopOnLabel;
    // End of variables declaration//GEN-END:variables
}
