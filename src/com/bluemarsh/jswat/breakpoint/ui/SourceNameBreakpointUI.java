/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * FILE:        SourceNameBreakpointUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/18/03        Initial version
 *
 * $Id: SourceNameBreakpointUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.SourceNameBreakpoint;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class SourceNameBreakpointUI is an adapter capable of building a user
 * interface to represent a source name breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class SourceNameBreakpointUI extends LineBreakpointUI {
    /** Breakpoint that we are working against. */
    protected SourceNameBreakpoint targetBreakpoint;

    /**
     * Create a SourceNameBreakpointUI that will operate on the given
     * breakpoint.
     *
     * @param  bp  breakpoint to be edited.
     */
    public SourceNameBreakpointUI(SourceNameBreakpoint bp) {
        super(bp);
        targetBreakpoint = bp;

        // Display the package name.
        JLabel label = new JLabel(Bundle.getString("srcPackageLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);

        JTextField tf = new JTextField(bp.getPackageName(), 20);
        tf.setEditable(false);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(tf, gbc);
        propertiesPanel.add(tf);

        // Display the source file name.
        label = new JLabel(Bundle.getString("srcSourceLabel"));
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(label, gbc);
        propertiesPanel.add(label);

        tf = new JTextField(bp.getSourceName(), 20);
        tf.setEditable(false);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(tf, gbc);
        propertiesPanel.add(tf);
    } // SourceNameBreakpointUI
} // SourceNameBreakpointUI
