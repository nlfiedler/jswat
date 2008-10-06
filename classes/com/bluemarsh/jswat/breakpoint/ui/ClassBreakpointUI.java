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
 * FILE:        ClassBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/08/02        Initial version
 *
 * $Id: ClassBreakpointUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.ClassBreakpoint;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;

/**
 * Class ClassBreakpointUI is an adapter capable of building a user
 * interface to represent a class breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class ClassBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    private ClassBreakpoint targetBreakpoint;
    /** Original onPrepare flag. */
    private boolean originalOnPrepare;
    /** On thread prepare event checkbox. */
    private JCheckBox onPrepareCheckbox;
    /** Original onUnload flag. */
    private boolean originalOnUnload;
    /** On thread unload event checkbox. */
    private JCheckBox onUnloadCheckbox;

    /**
     * Create a ClassBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public ClassBreakpointUI(ClassBreakpoint bp) {
        super(bp);
        addClassFilter();
        targetBreakpoint = bp;

        originalOnPrepare = bp.getStopOnPrepare();
        onPrepareCheckbox = new JCheckBox(Bundle.getString(
            "classStopOnPrepareCheckbox"), originalOnPrepare);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbl.setConstraints(onPrepareCheckbox, gbc);
        propertiesPanel.add(onPrepareCheckbox);

        originalOnUnload = bp.getStopOnUnload();
        onUnloadCheckbox = new JCheckBox(Bundle.getString(
            "classStopOnUnloadCheckbox"), originalOnUnload);
        gbl.setConstraints(onUnloadCheckbox, gbc);
        propertiesPanel.add(onUnloadCheckbox);
    } // ClassBreakpointUI

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
        targetBreakpoint.setStopOnPrepare(onPrepareCheckbox.isSelected());
        targetBreakpoint.setStopOnUnload(onUnloadCheckbox.isSelected());
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
        targetBreakpoint.setStopOnPrepare(originalOnPrepare);
        targetBreakpoint.setStopOnUnload(originalOnUnload);
        targetBreakpoint.setEnabled(wasEnabled);
    } // undo
} // ClassBreakpointUI
