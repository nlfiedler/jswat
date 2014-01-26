/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * FILE:        MethodBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/30/01        Initial version
 *      nf      03/20/02        Made it do something
 *      nf      03/29/02        Added thread filters
 *
 * DESCRIPTION:
 *      Defines the method breakpoint ui adapter.
 *
 * $Id: MethodBreakpointUI.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.MethodBreakpoint;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.util.StringUtils;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class MethodBreakpointUI is an adapter capable of building a user
 * interface to represent a method breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class MethodBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    protected MethodBreakpoint targetBreakpoint;
    /** Original method name. */
    protected String originalMethodName;
    /** Original method arguments. */
    protected List originalMethodArgs;
    /** Breakpoint's method name text field. */
    protected JTextField methodNameTextfield;
    /** Breakpoint's method arguments text field. */
    protected JTextField methodArgsTextfield;

    /**
     * Create a MethodBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public MethodBreakpointUI(MethodBreakpoint bp) {
        super(bp);
        addThreadFilter();
        targetBreakpoint = bp;

        // Create the text input field to change the method name.
        JLabel label = new JLabel(Bundle.getString("methodNameLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        methodNameTextfield = new JTextField(20);
        originalMethodName = bp.getMethodName();
        methodNameTextfield.setText(originalMethodName);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(methodNameTextfield, gbc);
        propertiesPanel.add(methodNameTextfield);

        label = new JLabel(Bundle.getString("methodArgsLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        methodArgsTextfield = new JTextField(20);
        originalMethodArgs = bp.getMethodArgs();
        methodArgsTextfield.setText(
            StringUtils.listToString(originalMethodArgs));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(methodArgsTextfield, gbc);
        propertiesPanel.add(methodArgsTextfield);
    } // MethodBreakpointUI

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
        // Must call superclass to get the default behavior.
        super.commit();
        // Save the ui component values to the breakpoint.
        String str = methodNameTextfield.getText();
        if (!str.equals(targetBreakpoint.getMethodName())) {
            // This will reset the breakpoint and make it re-resolve.
            targetBreakpoint.setMethodName(str);
        }
        str = methodArgsTextfield.getText();
        List list = StringUtils.stringToList(str);
        if (!list.equals(targetBreakpoint.getMethodArgs())) {
            // This will reset the breakpoint and make it re-resolve.
            targetBreakpoint.setMethodArgs(list);
        }
    } // commit

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    public void undo() {
        super.undo();
        targetBreakpoint.setMethodName(originalMethodName);
        targetBreakpoint.setMethodArgs(originalMethodArgs);
    } // undo
} // MethodBreakpointUI
