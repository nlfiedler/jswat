/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      JSwat Commands
 * FILE:        vmlistCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/12/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'vmlist' command.
 *
 * $Id: vmlistCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import java.util.*;

/**
 * Defines the class that handles the 'vmlist' command.
 *
 * @author  Nathan Fiedler
 */
public class vmlistCommand extends JSwatCommand {

    /**
     * Perform the 'vmlist' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
	VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
	List connectors = vmm.attachingConnectors();
	int size = connectors.size();
        if (size < 1) {
            out.writeln(swat.getResourceString("noRunningVMs"));
        } else {
            StringBuffer buf = new StringBuffer
                (Bundle.getString("vmlist.listOfVMs"));
            buf.append('\n');
            Iterator iter = connectors.iterator();
            while (iter.hasNext()) {
                buf.append(iter.next().toString());
                buf.append('\n');
            }
            out.writeln(buf.toString());
        }
    } // perform
} // vmlistCommand
