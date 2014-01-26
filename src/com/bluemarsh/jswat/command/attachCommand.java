/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * FILE:        attachCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/29/99        Initial version
 *      nf      03/11/01        Use session settings
 *      nf      05/02/01        Fix bug #107, add a wait cursor
 *      nf      08/06/01        Undoing last change, it's not GUI-less
 *      nf      04/04/02        Implemented RFE #404, shmem transport
 *
 * $Id: attachCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NoAttachingConnectorException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;

/**
 * Defines the class that handles the 'attach' command.
 *
 * @author  Nathan Fiedler
 */
public class attachCommand extends JSwatCommand {

    /**
     * Perform the 'attach' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (session.isActive()) {
            // Deactivate current session.
            session.deactivate(false, this);
        }

        String remoteHost = session.getProperty("remoteHost");
        String remotePort = session.getProperty("remotePort");
        boolean useShare = Boolean.valueOf(
            session.getProperty("useShare")).booleanValue();
        String shareName = session.getProperty("shareName");

        // Check for arguments.
        if (args.hasMoreTokens()) {
            // Get the argument and see what it is.
            String address = args.nextToken();
            int colon = address.indexOf(':');
            if (colon >= 0) {
                // Argument is of the form "host:port".
                remoteHost = address.substring(0, colon);
                remotePort = address.substring(colon + 1);
            } else {
                // Argument is just the port number or its the
                // name of the shared memory space.
                try {
                    Integer.parseInt(address);
                    remoteHost = "";
                    remotePort = address;
                    useShare = false;
                } catch (NumberFormatException nfe) {
                    shareName = address;
                    useShare = true;
                }
            }
        }

        VMConnection connection = null;
        if (useShare) {
            if (shareName == null || shareName.length() == 0) {
                out.writeln(Bundle.getString("attach.missingShare"));
                return;
            }
            try {
                connection = VMConnection.buildConnection(shareName);
            } catch (NoAttachingConnectorException nace) {
                out.writeln(com.bluemarsh.jswat.Bundle.getString(
                    "noShmemVMsFound"));
                return;
            }
        } else {
            if (remotePort == null || remotePort.length() == 0) {
                out.writeln(Bundle.getString("attach.missingPort"));
                return;
            }
            try {
                connection = VMConnection.buildConnection(
                    remoteHost, remotePort);
            } catch (NoAttachingConnectorException nace) {
                out.writeln(com.bluemarsh.jswat.Bundle.getString(
                    "noSocketVMsFound"));
                return;
            }
        }

        // Try to attach to the running VM.
        if (connection != null) {

            // Display the host and port that we're going to use.
            StringBuffer buf = new StringBuffer(
                Bundle.getString("attach.attachingTo"));
            if (useShare) {
                buf.append(' ');
                buf.append(shareName);
            } else {
                buf.append(' ');
                buf.append(remoteHost);
                buf.append(':');
                buf.append(remotePort);
            }
            out.writeln(buf.toString());

            // Attach to the debuggee VM.
            if (connection.attachDebuggee(session, true)) {
                // Save the attach parameters for later reuse.
                if (useShare) {
                    session.setProperty("shareName", shareName);
                    session.setProperty("useShare", "true");
                } else {
                    session.setProperty("remoteHost", remoteHost);
                    session.setProperty("remotePort", remotePort);
                    session.setProperty("useShare", "false");
                }
            } else {
                out.writeln(Bundle.getString("attach.failed"));
            }
        }
    } // perform
} // attachCommand
