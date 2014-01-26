/*********************************************************************
 *
 *	Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: VMCloseAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.VirtualMachine;
import java.awt.event.ActionEvent;

/**
 * Implements the Virtual Machine close action. It doens't do
 * much except contact the running VM and close it.
 *
 * @author  Nathan Fiedler
 */
public class VMCloseAction extends JSwatAction implements SessionAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMCloseAction object with the default action
     * command string of "vmClose".
     */
    public VMCloseAction() {
        super("vmClose");
    } // VMCloseAction

    /**
     * Performs the virtual machine close action. Finds the
     * appropriate VM and closes it.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
	Session session = getSession(event);
	if (session.isActive()) {
            // Just disconnect with the debuggee VM.
            // If the VM was running before we connected,
            // it will remain running.
	    session.deactivate(false);
	}
    } // actionPerformed
} // VMCloseAction
