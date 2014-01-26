/*********************************************************************
 *
 *	Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: VMListAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the Virtual Machine list action. It creates a list of
 * the available virtual machines that are currently running.
 *
 * @author  Nathan Fiedler
 */
public class VMListAction extends JSwatAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new VMListAction object with the default action
     * command string of "vmList".
     */
    public VMListAction() {
        super("vmList");
    } // VMListAction

    /**
     * Performs the VM list action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Session session = getSession(event);
        Log out = session.getStatusLog();

	VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
	List connectors = vmm.attachingConnectors();
	int size = connectors.size();
        if (size < 1) {
            out.writeln(swat.getResourceString("noRunningVMs"));
        } else {
            StringBuffer buf = new StringBuffer
                (Bundle.getString("VMList.listOfVMs"));
            buf.append('\n');
            Iterator iter = connectors.iterator();
            while (iter.hasNext()) {
                buf.append(iter.next().toString());
                buf.append('\n');
            }
            out.writeln(buf.toString());
        }
    } // actionPerformed
} // VMListAction
