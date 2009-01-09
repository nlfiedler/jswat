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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.breakpoint.MethodBreakpoint;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Class MethodPanel is the specific editor for a MethodBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class MethodPanel extends AbstractAdapter
        implements FocusListener, ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form LinePanel.
     */
    public MethodPanel() {
        initComponents();
        allMethodsCheckBox.addItemListener(this);
    }

    /**
     * Indicates if this adapter is the sort that can construct a new
     * Breakpoint instance from the user-provided information.
     *
     * @return  true if breakpoint creation is possible, false otherwise.
     */
    public boolean canCreateBreakpoint() {
        return true;
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
        String pkg = packageTextField.getText();
        String cls = classTextField.getText();
        String cname = pkg != null && pkg.length() > 0 ? pkg + '.' + cls : cls;
        String method = methodTextField.getText();
        Breakpoint bp = null;
        List<String> empty = Collections.emptyList();
        try {
            bp = factory.createMethodBreakpoint(cname, method, empty);
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
        if (e.getSource() == allMethodsCheckBox) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                methodTextField.setEnabled(false);
                methodTextField.setText("");
                parametersTextField.setEditable(false);
                parametersTextField.setText("");
            } else {
                methodTextField.setEnabled(true);
                parametersTextField.setEditable(true);
            }
            if (breakpoint != null) {
                String msg = validateInput();
                fireInputPropertyChange(msg);
                if (msg == null) {
                    saveParameters(breakpoint);
                }
            }
        }
    }

    /**
     * Read the values from the given Breakpoint to populate the fields
     * of this editor.
     * 
     * @param  bp  Breakpoint to edit.
     */
    public void loadParameters(Breakpoint bp) {
        MethodBreakpoint mb = (MethodBreakpoint) bp;
        String fullname = mb.getClassName();
        String cname = Names.getShortClassName(fullname);
        classTextField.setText(cname);
        String pname = Names.getPackageName(fullname);
        packageTextField.setText(pname);
        String mname = mb.getMethodName();
        methodTextField.setText(mname);
        List<String> params = mb.getMethodParameters();
        String prms = Strings.listToString(params, ", ");
        parametersTextField.setText(prms);
        if (mname == null || mname.length() == 0) {
            // If no method name, must be set to all methods.
            allMethodsCheckBox.setSelected(true);
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            classTextField.addFocusListener(this);
            methodTextField.addFocusListener(this);
            packageTextField.addFocusListener(this);
            parametersTextField.addFocusListener(this);
        }
        breakpoint = bp;
    }

    /**
     * Saves the values from the fields of this editor to the given Breakpoint.
     * 
     * @param  bp  Breakpoint to modify.
     */
    public void saveParameters(Breakpoint bp) {
        MethodBreakpoint mb = (MethodBreakpoint) bp;
        String pname = packageTextField.getText();
        String cname = classTextField.getText();
        String fullname;
        if (pname != null && pname.length() > 0) {
            fullname = pname + '.' + cname;
        } else {
            fullname = cname;
        }
        try {
            mb.setClassName(fullname);
        } catch (MalformedClassNameException mcne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mcne);
        }
        try {
            mb.setMethodName(methodTextField.getText());
        } catch (MalformedMemberNameException mmne) {
            // Input was already validated, this should not happen.
            ErrorManager.getDefault().notify(mmne);
        }
        String prms = parametersTextField.getText();
        List<String> params = Strings.stringToList(prms, ",");
        mb.setMethodParameters(params);
    }

    /**
     * Validate the user-provided input.
     * 
     * @return  error message if input invalid, null if valid.
     */
    public String validateInput() {
        String method = methodTextField.getText();
        // Allow empty method names, but non-empty names must be valid.
        if (method.length() > 0 && !Names.isMethodIdentifier(method)) {
            return NbBundle.getMessage(getClass(), "ERR_Method_Invalid_Method", method);
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
        methodLabel = new javax.swing.JLabel();
        methodTextField = new javax.swing.JTextField();
        allMethodsCheckBox = new javax.swing.JCheckBox();
        parametersLabel = new javax.swing.JLabel();
        parametersTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Location_Border_Title")));
        packageLabel.setLabelFor(packageTextField);
        packageLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Method_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(packageLabel, gridBagConstraints);

        packageTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Method_Package"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(packageTextField, gridBagConstraints);

        classLabel.setLabelFor(classTextField);
        classLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Method_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(classLabel, gridBagConstraints);

        classTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Method_Class"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(classTextField, gridBagConstraints);

        methodLabel.setLabelFor(methodTextField);
        methodLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Method_Method"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 12);
        add(methodLabel, gridBagConstraints);

        methodTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Method_Method"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        add(methodTextField, gridBagConstraints);

        allMethodsCheckBox.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Method_AllMethods"));
        allMethodsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allMethodsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(allMethodsCheckBox, gridBagConstraints);

        parametersLabel.setLabelFor(parametersTextField);
        parametersLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Method_Parameters"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(parametersLabel, gridBagConstraints);

        parametersTextField.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Method_Parameters"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(parametersTextField, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allMethodsCheckBox;
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JTextField methodTextField;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageTextField;
    private javax.swing.JLabel parametersLabel;
    private javax.swing.JTextField parametersTextField;
    // End of variables declaration//GEN-END:variables
}
