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
 * $Id: EditorPanel.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Class EditorPanel assembles the necessary parts to edit a breakpoint
 * of a particular type. This may also be used to create a new breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class EditorPanel extends JPanel implements PropertyChangeListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Panel for editing type-specific breakpoint attributes. */
    private BreakpointAdapter specificPanel;
    /** Panel for editing class and thread filters. */
    private BreakpointAdapter filtersPanel;
    /** Panel for editing common breakpoint attributes. */
    private BreakpointAdapter commonPanel;
    /** Panel for editing actions. */
    private BreakpointAdapter actionsPanel;

    /**
     * Creates new form EditorPanel.
     *
     * @param  type  type of breakpoint to be edited.
     */
    public EditorPanel(BreakpointType type) {
        initComponents();
        // Set up the constraints for all of the panels.
        GridBagLayout gbl = (GridBagLayout) subPanel.getLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        // Create the panels according to the breakpoint type.
        switch (type) {
            case CLASS :
                specificPanel = new ClassPanel();
                break;
            case EXCEPTION :
                specificPanel = new ExceptionPanel();
                break;
            case LINE :
                specificPanel = new LinePanel();
                break;
            case LOCATION :
                // Do not need a specific editor for this one.
                break;
            case METHOD :
                specificPanel = new MethodPanel();
                break;
            case THREAD :
                specificPanel = new ThreadPanel();
                break;
            case TRACE :
                specificPanel = new TracePanel();
                break;
            case UNCAUGHT :
                // Do not need a specific editor for this one.
                break;
            case WATCH :
                specificPanel = new WatchPanel();
                break;
        }

        // Add the specific panel first.
        if (specificPanel != null) {
            specificPanel.addPropertyChangeListener(this);
            gbl.setConstraints((JComponent) specificPanel, gbc);
            subPanel.add((JComponent) specificPanel);
        }

        // Add the filters panel.
        filtersPanel = new FiltersPanel(type);
        filtersPanel.addPropertyChangeListener(this);
        gbl.setConstraints((JComponent) filtersPanel, gbc);
        subPanel.add((JComponent) filtersPanel);

        // Create and add the common panel.
        commonPanel = new CommonPanel();
        commonPanel.addPropertyChangeListener(this);
        gbl.setConstraints((JComponent) commonPanel, gbc);
        subPanel.add((JComponent) commonPanel);

        // Create and add the actions panel, but not for trace breakpoints,
        // for which we never want to control its actions since it has a
        // very specific behavior.
        if (type != BreakpointType.TRACE) {
            actionsPanel = new ActionsPanel();
            actionsPanel.addPropertyChangeListener(this);
            gbl.setConstraints((JComponent) actionsPanel, gbc);
            subPanel.add((JComponent) actionsPanel);
        }
    }

    public void addNotify() {
        super.addNotify();
        // Ensure dialog is not overly narrow.
        Dimension size = getPreferredSize();
        size.width = 450;
        setPreferredSize(size);
    }

    /**
     * Create a Breakpoint instance that encapsulates the information
     * provided by the user.
     *
     * @param  factory  breakpoint factory to construct breakpoint.
     * @return  new Breakpoint, or null if creation not supported.
     */
    public Breakpoint createBreakpoint(BreakpointFactory factory) {
        Breakpoint bp = null;
        if (specificPanel != null) {
            if (specificPanel.canCreateBreakpoint()) {
                // First create the breakpoint.
                bp = specificPanel.createBreakpoint(factory);
                // Next have the other panels modify it.
                if (commonPanel != null) {
                    commonPanel.saveParameters(bp);
                }
                if (filtersPanel != null) {
                    filtersPanel.saveParameters(bp);
                }
                if (actionsPanel != null) {
                    actionsPanel.saveParameters(bp);
                }
            }
        }
        return bp;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(BreakpointAdapter.PROP_INPUTVALID)) {
            String msg = (String) evt.getNewValue();
            if (msg != null) {
                // There was a validation problem, show the message.
                messageLabel.setText(msg);
            } else {
                // Validate everything again just to make sure.
                validateInput();
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
        if (commonPanel != null) {
            commonPanel.loadParameters(bp);
        }
        if (specificPanel != null) {
            specificPanel.loadParameters(bp);
        }
        if (filtersPanel != null) {
            filtersPanel.loadParameters(bp);
        }
        if (actionsPanel != null) {
            actionsPanel.loadParameters(bp);
        }
    }

    /**
     * Saves the values from the fields of this editor to the given Breakpoint.
     *
     * @param  bp  Breakpoint to modify.
     */
    public void saveParameters(Breakpoint bp) {
        if (commonPanel != null) {
            commonPanel.saveParameters(bp);
        }
        if (specificPanel != null) {
            specificPanel.saveParameters(bp);
        }
        if (filtersPanel != null) {
            filtersPanel.saveParameters(bp);
        }
        if (actionsPanel != null) {
            actionsPanel.saveParameters(bp);
        }
    }

    /**
     * Validates the input for this dialog. If invalid, a message will be
     * displayed at the bottom of the dialog.
     *
     * @return  true if input valid, false if invalid.
     */
    boolean validateInput() {
        BreakpointAdapter[] adapters = {
            commonPanel, specificPanel, filtersPanel, actionsPanel
        };
        for (BreakpointAdapter adapter : adapters) {
            if (adapter != null) {
                String msg = adapter.validateInput();
                if (msg != null) {
                    messageLabel.setText(msg);
                    return false;
                }
            }
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        subPanel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        subPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(subPanel, gridBagConstraints);

        messageLabel.setForeground(new java.awt.Color(255, 0, 0));
        messageLabel.setText("   ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(messageLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel subPanel;
    // End of variables declaration//GEN-END:variables
}
