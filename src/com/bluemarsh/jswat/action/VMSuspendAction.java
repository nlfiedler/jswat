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
 * $Id: VMSuspendAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import java.awt.event.ActionEvent;

/**
 * Implements the Virtual Machine suspend action. It doens't do
 * much except contact the running VM and suspend it.
 *
 * @author  Nathan Fiedler
 */
public class VMSuspendAction extends JSwatAction implements SessionAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMSuspendAction object with the default action
     * command string of "vmSuspend".
     */
    public VMSuspendAction() {
        super("vmSuspend");
    } // VMSuspendAction

    /**
     * Performs the virtual machine suspend action. Finds the
     * appropriate VM and suspends it.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        // ask the user for the class name
        Session session = getSession(event);
        try {
            session.suspendVM();
        } catch (NotActiveException nse) {
            Log out = session.getStatusLog();
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // actionPerformed
} // VMSuspendAction
