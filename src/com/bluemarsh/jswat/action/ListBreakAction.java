/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: ListBreakAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.breakpoint.ui.ManagerDialog;
import java.awt.event.ActionEvent;

/**
 * Class ListBreakAction is responsible for showing a dialog that
 * displays all the breakpoints that are set in the VM.
 *
 * @author  Nathan Fiedler
 */
public class ListBreakAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new ListBreakAction object with the default action
     * command string of "listBreak".
     */
    public ListBreakAction() {
        super("listBreak");
    } // ListBreakAction

    /**
     * Performs the set breakpoint action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // Show the breakpoints dialog.
        new ManagerDialog(getSession(event), getFrame(event));
    } // actionPerformed
} // ListBreakAction
