/*********************************************************************
 *
 *      Copyright (C) 1999 Nathan Fiedler
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
 * FILE:        suspendCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      5/31/99         Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'suspend' command.
 *
 * $Id: suspendCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ThreadReference;

/**
 * Defines the class that handles the 'suspend' command.
 *
 * @author  Nathan Fiedler
 */
public class suspendCommand extends JSwatCommand {

    /**
     * Perform the 'suspend' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        try {
            if (args.hasMoreTokens()) {
                String token = args.nextToken();
                // Check if user wants to suspend 'all' threads.
                if (!token.equals("all")) {

                    // User is suspended individual threads.
                    suspendThread(session, token, out);
                    while (args.hasMoreTokens()) {
                        suspendThread(session, args.nextToken(), out);
                    }
                    return;
                }
            }
            // User wants to suspend the entire VM.
            session.suspendVM();
            out.writeln(swat.getResourceString("vmSuspended"));
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform

    /**
     * Suspend the thread given by the ID token string.
     *
     * @param  session  Debugging session.
     * @param  idToken  Thread ID as a string.
     * @param  out      Output to write to.
     * @exception  NotActiveException
     *             Thrown if there is no active session.
     */
    protected void suspendThread(Session session, String idToken, Log out)
        throws NotActiveException {
        // Find the thread by the ID number.
        ThreadReference thread = ThreadUtils.getThreadByID(session, idToken);
        if (thread != null) {
            // Suspend the thread.
            thread.suspend();
            out.writeln(swat.getResourceString("threadSuspended"));
        } else {
            out.writeln(swat.getResourceString("threadNotFound") + " " +
                        idToken);
        }
    } // suspendThread
} // suspendCommand
