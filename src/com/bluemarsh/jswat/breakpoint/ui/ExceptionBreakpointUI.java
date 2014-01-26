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
 * FILE:        ExceptionBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/02        Initial version
 *
 * $Id: ExceptionBreakpointUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.ExceptionBreakpoint;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;

/**
 * Class ExceptionBreakpointUI is an adapter capable of building a user
 * interface to represent a exception breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class ExceptionBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    private ExceptionBreakpoint targetBreakpoint;
    /** Original onCaught flag. */
    private boolean originalOnCaught;
    /** On thread caught event checkbox. */
    private JCheckBox onCaughtCheckbox;
    /** Original onUncaught flag. */
    private boolean originalOnUncaught;
    /** On thread uncaught event checkbox. */
    private JCheckBox onUncaughtCheckbox;

    /**
     * Create a ExceptionBreakpointUI that will operate on the given
     * breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public ExceptionBreakpointUI(ExceptionBreakpoint bp) {
        super(bp);
        addClassFilter();
        addThreadFilter();
        targetBreakpoint = bp;

        originalOnCaught = bp.getStopOnCaught();
        onCaughtCheckbox = new JCheckBox(Bundle.getString(
            "excpStopOnCaughtCheckbox"), originalOnCaught);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(onCaughtCheckbox, gbc);
        propertiesPanel.add(onCaughtCheckbox);

        originalOnUncaught = bp.getStopOnUncaught();
        onUncaughtCheckbox = new JCheckBox(Bundle.getString(
            "excpStopOnUncaughtCheckbox"), originalOnUncaught);
        gbl.setConstraints(onUncaughtCheckbox, gbc);
        propertiesPanel.add(onUncaughtCheckbox);
    } // ExceptionBreakpointUI

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
        targetBreakpoint.setStopOnCaught(onCaughtCheckbox.isSelected());
        targetBreakpoint.setStopOnUncaught(onUncaughtCheckbox.isSelected());
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
        targetBreakpoint.setStopOnCaught(originalOnCaught);
        targetBreakpoint.setStopOnUncaught(originalOnUncaught);
        targetBreakpoint.setEnabled(wasEnabled);
    } // undo
} // ExceptionBreakpointUI
