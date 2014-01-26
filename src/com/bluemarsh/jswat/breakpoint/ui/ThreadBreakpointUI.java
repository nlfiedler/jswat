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
 * FILE:        ThreadBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/08/02        Initial version
 *
 * $Id: ThreadBreakpointUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.ThreadBreakpoint;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class ThreadBreakpointUI is an adapter capable of building a user
 * interface to represent a thread breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class ThreadBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    private ThreadBreakpoint targetBreakpoint;
    /** Original thread name. */
    private String originalThreadName;
    /** Breakpoint's thread name text field. */
    private JTextField threadnameTextfield;
    /** Original onStart flag. */
    private boolean originalOnStart;
    /** On thread start event checkbox. */
    private JCheckBox onStartCheckbox;
    /** Original onDeath flag. */
    private boolean originalOnDeath;
    /** On thread death event checkbox. */
    private JCheckBox onDeathCheckbox;

    /**
     * Create a ThreadBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public ThreadBreakpointUI(ThreadBreakpoint bp) {
        super(bp);
        targetBreakpoint = bp;

        // Create the text input field to change the thread name.
        JLabel label = new JLabel(Bundle.getString("threadnameLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);

        threadnameTextfield = new JTextField(20);
        originalThreadName = bp.getThreadName();
        threadnameTextfield.setText(originalThreadName);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(threadnameTextfield, gbc);
        propertiesPanel.add(threadnameTextfield);

        originalOnStart = bp.getStopOnStart();
        onStartCheckbox = new JCheckBox(Bundle.getString(
            "threadStopOnStartCheckbox"), originalOnStart);
        gbl.setConstraints(onStartCheckbox, gbc);
        propertiesPanel.add(onStartCheckbox);

        originalOnDeath = bp.getStopOnDeath();
        onDeathCheckbox = new JCheckBox(Bundle.getString(
            "threadStopOnDeathCheckbox"), originalOnDeath);
        gbl.setConstraints(onDeathCheckbox, gbc);
        propertiesPanel.add(onDeathCheckbox);
    } // ThreadBreakpointUI

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
        String str = threadnameTextfield.getText();
        if (!str.equals(targetBreakpoint.getThreadName())) {
            targetBreakpoint.setThreadName(str);
        }
        targetBreakpoint.setStopOnStart(onStartCheckbox.isSelected());
        targetBreakpoint.setStopOnDeath(onDeathCheckbox.isSelected());
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
        targetBreakpoint.setThreadName(originalThreadName);
        targetBreakpoint.setStopOnStart(originalOnStart);
        targetBreakpoint.setStopOnDeath(originalOnDeath);
        targetBreakpoint.setEnabled(wasEnabled);
    } // undo
} // ThreadBreakpointUI
