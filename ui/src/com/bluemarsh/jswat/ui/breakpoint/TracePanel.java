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
 * $Id: TracePanel.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.TraceBreakpoint;
import com.bluemarsh.jswat.core.util.NameValuePair;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 * Class TracePanel is the specific editor for a TraceBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class TracePanel extends AbstractAdapter implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Type of trace breakpoint. */
    private static enum Type { BOTH, ENTER, EXIT };
    /** The stop-on combo model. */
    private DefaultComboBoxModel stopOnModel;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form TracePanel.
     */
    public TracePanel() {
        initComponents();
        // Populate the stop-on combobox.
        stopOnModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(getClass(), "CTL_Trace_StopOn_Enter");
        NameValuePair<Type> pair = new NameValuePair<Type>(label, Type.ENTER);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Trace_StopOn_Exit");
        pair = new NameValuePair<Type>(label, Type.EXIT);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Trace_StopOn_Both");
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
        boolean enter = true;
        boolean exit = true;
        switch (type) {
            case ENTER :
                exit = false;
                break;
            case EXIT :
                enter = false;
                break;
        }
        // Let the filter panel handle the filters.
        return factory.createTraceBreakpoint(null, null, enter, exit);
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
        TraceBreakpoint tb = (TraceBreakpoint) bp;
        boolean enter = tb.getStopOnEnter();
        boolean exit = tb.getStopOnExit();
        Type type;
        if (enter && exit) {
            type = Type.BOTH;
        } else if (enter) {
            type = Type.ENTER;
        } else {
            type = Type.EXIT;
        }
        for (int ii = stopOnModel.getSize() - 1; ii >= 0; ii--) {
            NameValuePair<?> pair =
                    (NameValuePair<?>) stopOnModel.getElementAt(ii);
            Type pt = (Type) pair.getValue();
            if (pt.equals(type)) {
                stopOnModel.setSelectedItem(pair);
            }
        }

        // Listen to the components after they are initialized.
        if (breakpoint == null) {
            stopOnComboBox.addItemListener(this);
        }
        breakpoint = bp;
    }

    public void saveParameters(Breakpoint bp) {
        NameValuePair<?> pair =
                (NameValuePair<?>) stopOnComboBox.getSelectedItem();
        Type type = (Type) pair.getValue();
        boolean enter = true;
        boolean exit = true;
        switch (type) {
            case ENTER :
                exit = false;
                break;
            case EXIT :
                enter = false;
                break;
        }
        TraceBreakpoint tb = (TraceBreakpoint) bp;
        tb.setStopOnEnter(enter);
        tb.setStopOnExit(exit);
    }

    public String validateInput() {
        // nothing to do
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

        stopOnLabel = new javax.swing.JLabel();
        stopOnComboBox = new javax.swing.JComboBox();
        useFiltersLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Trace_Border")));
        stopOnLabel.setLabelFor(stopOnComboBox);
        stopOnLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Trace_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 12);
        add(stopOnLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(stopOnComboBox, gridBagConstraints);

        useFiltersLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Trace_UseFilters"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(useFiltersLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox stopOnComboBox;
    private javax.swing.JLabel stopOnLabel;
    private javax.swing.JLabel useFiltersLabel;
    // End of variables declaration//GEN-END:variables
}
