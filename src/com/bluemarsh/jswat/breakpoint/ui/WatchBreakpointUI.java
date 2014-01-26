/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Breakpoint UI
 * FILE:        WatchBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/09/02        Initial version
 *
 * $Id: WatchBreakpointUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.WatchBreakpoint;
import com.sun.jdi.ObjectReference;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class WatchBreakpointUI is an adapter capable of building a user
 * interface to represent a watch breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class WatchBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    private WatchBreakpoint targetBreakpoint;
    /** Original watch name. */
    private String originalFieldName;
    /** Breakpoint's watch name text field. */
    private JTextField fieldnameTextfield;
    /** Original onAccess flag. */
    private boolean originalOnAccess;
    /** On variable access event checkbox. */
    private JCheckBox onAccessCheckbox;
    /** Original onModify flag. */
    private boolean originalOnModify;
    /** On variable modify event checkbox. */
    private JCheckBox onModifyCheckbox;

    /**
     * Create a WatchBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public WatchBreakpointUI(WatchBreakpoint bp) {
        super(bp);
        targetBreakpoint = bp;
        addClassFilter();
        addThreadFilter();

        // Create the text input field to change the watch name.
        JLabel label = new JLabel(Bundle.getString("fieldnameLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        fieldnameTextfield = new JTextField(20);
        originalFieldName = bp.getFieldName();
        fieldnameTextfield.setText(originalFieldName);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(fieldnameTextfield, gbc);
        propertiesPanel.add(fieldnameTextfield);

        ObjectReference objref = bp.getObjectFilter();
        String objId = null;
        try {
            objId = objref.referenceType().name() + ':' + objref.uniqueID();
        } catch (Exception e) {
            // silently give up
        }
        if (objId != null) {
            // Show the type and unique ID of the object filter, if any.
            label = new JLabel(Bundle.getString("objFilterLabel"));
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbl.setConstraints(label, gbc);
            propertiesPanel.add(label);
            JTextField field = new JTextField(objId);
            field.setEditable(false);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbl.setConstraints(field, gbc);
            propertiesPanel.add(field);
        }

        originalOnAccess = bp.getStopOnAccess();
        onAccessCheckbox = new JCheckBox(Bundle.getString(
            "watchStopOnAccessCheckbox"), originalOnAccess);
        gbl.setConstraints(onAccessCheckbox, gbc);
        propertiesPanel.add(onAccessCheckbox);

        originalOnModify = bp.getStopOnModify();
        onModifyCheckbox = new JCheckBox(Bundle.getString(
            "watchStopOnModifyCheckbox"), originalOnModify);
        gbl.setConstraints(onModifyCheckbox, gbc);
        propertiesPanel.add(onModifyCheckbox);
    } // WatchBreakpointUI

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
        // Must call superclass to get the default behavior.
        super.commit();
        // Save the ui component values to the breakpoint.
        boolean wasEnabled = targetBreakpoint.isEnabled();
        targetBreakpoint.setEnabled(false);
        String str = fieldnameTextfield.getText();
        if (str.length() > 0) {
            targetBreakpoint.setFieldName(str);
        }
        targetBreakpoint.setStopOnAccess(onAccessCheckbox.isSelected());
        targetBreakpoint.setStopOnModify(onModifyCheckbox.isSelected());
        targetBreakpoint.setEnabled(wasEnabled);
    } // commit

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    public void undo() {
        super.undo();
        boolean wasEnabled = targetBreakpoint.isEnabled();
        targetBreakpoint.setEnabled(false);
        targetBreakpoint.setFieldName(originalFieldName);
        targetBreakpoint.setStopOnAccess(originalOnAccess);
        targetBreakpoint.setStopOnModify(originalOnModify);
        targetBreakpoint.setEnabled(wasEnabled);
    } // undo
} // WatchBreakpointUI
