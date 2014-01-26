/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        versionCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      5/29/99         Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'version' command.
 *
 * $Id: versionCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.AppVersion;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachineManager;

/**
 * Defines the class that handles the 'version' command.
 *
 * @author  Nathan Fiedler
 */
public class versionCommand extends JSwatCommand {

    /**
     * Perform the 'version' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        StringBuffer buf = new StringBuffer(80);
        String version = AppVersion.getVersion();
	buf.append("JSwat ");
        buf.append(version);
        buf.append('\n');
        buf.append("JSwat JVM ");
        buf.append(System.getProperty("java.vm.version"));
        buf.append('\n');
        buf.append("JSwat JRE ");
        buf.append(System.getProperty("java.version"));
        buf.append('\n');
	if (session.isActive()) {
	    buf.append("Debuggee VM version and information: ");
	    buf.append(session.getVM().version());
            buf.append('\n');
            // This prints out several lines of info.
	    buf.append(session.getVM().description());
            buf.append('\n');
	} else {
            // If there's no session, get the version manually.
            buf.append("JDI ");
            VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
            buf.append(vmm.majorInterfaceVersion());
            buf.append('.');
            buf.append(vmm.minorInterfaceVersion());
            buf.append('\n');
        }
        out.write(buf.toString());
    } // perform
} // versionCommand
