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
 * $Id: CreatorPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class CreatorPanel assembles the necessary parts to create a new breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class CreatorPanel extends JPanel implements ActionListener, ItemListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Dialog for displaying this panel. */
    private Dialog inputDialog;
    /** True if the user has provided valid input, false otherwise. */
    private boolean okayToGo;
    /** Map of EditorPanel instances keyed by String types. */
    private Map<String, EditorPanel> panelsMap;
    /** Map of BreakpointType instances keyed by String types. */
    private Map<String, BreakpointType> typesMap;

    /**
     * Creates new form CreatorPanel.
     */
    public CreatorPanel() {
        panelsMap = new HashMap<String, EditorPanel>();
        typesMap = new HashMap<String, BreakpointType>();
        initComponents();
        // Line breakpoints cannot be created by dialog.
        BreakpointType[] typeEnums = {
            BreakpointType.CLASS,
            BreakpointType.EXCEPTION,
            BreakpointType.METHOD,
            BreakpointType.THREAD,
            BreakpointType.TRACE,
            BreakpointType.WATCH
        };
        String[] typeLabels = {
            "LBL_Creator_Class_Type",
            "LBL_Creator_Exception_Type",
            "LBL_Creator_Method_Type",
            "LBL_Creator_Thread_Type",
            "LBL_Creator_Trace_Type",
            "LBL_Creator_Variable_Type"
        };
        for (int ii = 0; ii < typeEnums.length; ii++) {
            String label = NbBundle.getMessage(CreatorPanel.class, typeLabels[ii]);
            typeComboBox.addItem(label);
            BreakpointType type = typeEnums[ii];
            EditorPanel panel = new EditorPanel(type);
            subPanel.add(panel, label);
            panelsMap.put(label, panel);
            typesMap.put(label, type);
        }
        typeComboBox.addItemListener(this);
    }

    /**
     * Invoked by the press of a button.
     *
     * @param  event  action event.
     */
    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("OK")) {
            // Validate the input before saving the data and closing.
            if (validateInput()) {
                okayToGo = true;
                inputDialog.dispose();
            }
        } else {
            inputDialog.dispose();
        }
    }

    /**
     * Create a new Breakpoint of the type specified by the user. The
     * breakpoint will only be partially initialized after this method call.
     * The caller must add the breakpoint to the breakpoint manager, then
     * call the saveParameters(Breakpoint) method of this class to complete
     * the breakpoint initialization.
     *
     * @return  new Breakpoint, or null if not possible.
     */
    public Breakpoint createBreakpoint() {
        // Get the selected editor panel and create a new breakpoint.
        String label = (String) typeComboBox.getSelectedItem();
        EditorPanel ep = panelsMap.get(label);
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        return ep.createBreakpoint(bf);
    }

    /**
     * Build and display a dialog for editing the runtimes.
     *
     * @return  true if user input is valid and ready to go, false otherwise.
     */
    public boolean display() {
        // Collect the dialog elements.
        String title = NbBundle.getMessage(getClass(), "LBL_CreatorPanel_Title");
        // Display dialog and get the user response.
        DialogDescriptor dd = new DialogDescriptor(
                this, title, true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx("jswat-create-breakpoint"), this);
        dd.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        inputDialog = DialogDisplayer.getDefault().createDialog(dd);
        okayToGo = false;
        inputDialog.setVisible(true);
        // (blocks until dialog is disposed...)
        return okayToGo;
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     *
     * @param  e  item event.
     */
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable selc = e.getItemSelectable();
        Object[] sels = selc.getSelectedObjects();
        if (sels != null && sels.length == 1) {
            String type = (String) sels[0];
            CardLayout layout = (CardLayout) subPanel.getLayout();
            layout.show(subPanel, type);
        }
    }

    /**
     * Now that the breakpoint has been created and added to the manager,
     * save the rest of the user-provided values to the breakpoint instance.
     *
     * @param  bp  breakpoint for which to complete initialization.
     */
    public void saveParameters(Breakpoint bp) {
        String label = (String) typeComboBox.getSelectedItem();
        EditorPanel ep = panelsMap.get(label);
        // Save the additional values to the breakpoint.
        ep.saveParameters(bp);
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    private boolean validateInput() {
        String label = (String) typeComboBox.getSelectedItem();
        EditorPanel ep = panelsMap.get(label);
        return ep.validateInput();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        subPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 12)));
        typeLabel.setLabelFor(typeComboBox);
        typeLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("LBL_Creator_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(typeLabel, gridBagConstraints);

        typeComboBox.setToolTipText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/ui/breakpoint/Form").getString("HINT_Creator_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(typeComboBox, gridBagConstraints);

        subPanel.setLayout(new java.awt.CardLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(subPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel subPanel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
