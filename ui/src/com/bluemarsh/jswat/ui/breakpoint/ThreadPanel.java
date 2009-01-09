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
import com.bluemarsh.jswat.core.breakpoint.ThreadBreakpoint;
import com.bluemarsh.jswat.core.util.NameValuePair;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.openide.util.NbBundle;

/**
 * Class ThreadPanel is the specific editor for a ThreadBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class ThreadPanel extends AbstractAdapter implements ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Type of thread breakpoint. */
    private static enum Type { BOTH, DEATH, START };
    /** The stop-on combo model. */
    private DefaultComboBoxModel stopOnModel;
    /** Breakpoint to update. */
    private Breakpoint breakpoint;

    /**
     * Creates new form ThreadPanel.
     */
    public ThreadPanel() {
        initComponents();
        // Populate the stop-on combobox.
        stopOnModel = new DefaultComboBoxModel();
        String label = NbBundle.getMessage(getClass(), "CTL_Thread_StopOn_Start");
        NameValuePair<Type> pair = new NameValuePair<Type>(label, Type.START);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Thread_StopOn_Death");
        pair = new NameValuePair<Type>(label, Type.DEATH);
        stopOnModel.addElement(pair);
        label = NbBundle.getMessage(getClass(), "CTL_Thread_StopOn_Both");
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
        boolean start = true;
        boolean death = true;
        switch (type) {
            case START :
                death = false;
                break;
            case DEATH :
                start = false;
                break;
        }
        // We do not have access to the filter field, let the filter
        // panel handle that appropriately.
        return factory.createThreadBreakpoint(null, start, death);
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
        ThreadBreakpoint tb = (ThreadBreakpoint) bp;
        boolean start = tb.getStopOnStart();
        boolean death = tb.getStopOnDeath();
        Type type;
        if (start && death) {
            type = Type.BOTH;
        } else if (start) {
            type = Type.START;
        } else {
            type = Type.DEATH;
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
        boolean start = true;
        boolean death = true;
        switch (type) {
            case START :
                death = false;
                break;
            case DEATH :
                start = false;
                break;
        }
        ThreadBreakpoint tb = (ThreadBreakpoint) bp;
        tb.setStopOnStart(start);
        tb.setStopOnDeath(death);
    }

    public String validateInput() {
        // nothing for us to validate
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

        useFiltersLabel = new javax.swing.JLabel();
        stopOnLabel = new javax.swing.JLabel();
        stopOnComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Thread_Border")));
        useFiltersLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Thread_UseFilters"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(useFiltersLabel, gridBagConstraints);

        stopOnLabel.setLabelFor(stopOnComboBox);
        stopOnLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Thread_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 12);
        add(stopOnLabel, gridBagConstraints);

        stopOnComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Thread_StopOn"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(stopOnComboBox, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox stopOnComboBox;
    private javax.swing.JLabel stopOnLabel;
    private javax.swing.JLabel useFiltersLabel;
    // End of variables declaration//GEN-END:variables
}
