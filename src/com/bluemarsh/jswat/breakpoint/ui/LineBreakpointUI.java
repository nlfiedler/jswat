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
 * FILE:        LineBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/24/01        Initial version
 *      nf      03/29/02        Added thread filters
 *
 * DESCRIPTION:
 *      Defines the line breakpoint ui adapter.
 *
 * $Id: LineBreakpointUI.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class LineBreakpointUI is an adapter capable of building a user
 * interface to represent a line breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class LineBreakpointUI extends BasicBreakpointUI {
    /** Breakpoint that we are working against. */
    protected LineBreakpoint targetBreakpoint;
    /** Original line number. */
    protected int originalLinenumber;
    /** Breakpoint's line number text field. */
    protected JTextField linenumberTextfield;

    /**
     * Create a LineBreakpointUI that will operate on the given breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public LineBreakpointUI(LineBreakpoint bp) {
        super(bp);
        addThreadFilter();
        targetBreakpoint = bp;

        // Create the text input field to change the line number.
        JLabel label = new JLabel(Bundle.getString("linenumberLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);
        linenumberTextfield = new JTextField(5);
        linenumberTextfield.setDocument(new NumericDocument());
        originalLinenumber = bp.getLineNumber();
        linenumberTextfield.setText(String.valueOf(originalLinenumber));
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(linenumberTextfield, gbc);
        propertiesPanel.add(linenumberTextfield);
    } // LineBreakpointUI

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
        // Must call superclass to get the default behavior.
        super.commit();
        // Save the ui component values to the breakpoint.
        String str = linenumberTextfield.getText();
        int n = Integer.parseInt(str);
        if (n != targetBreakpoint.getLineNumber()) {
            // This will reset the breakpoint and make it re-resolve.
            targetBreakpoint.setLineNumber(n);
        }
    } // commit

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    public void undo() {
        super.undo();
        targetBreakpoint.setLineNumber(originalLinenumber);
    } // undo
} // LineBreakpointUI
