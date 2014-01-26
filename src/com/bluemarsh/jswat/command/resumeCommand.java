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
 * FILE:        resumeCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/29/99        Initial version
 *      nf      08/21/01        Fixed bug 165
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'resume' command.
 *
 * $Id: resumeCommand.java 629 2002-10-26 23:03:26Z nfiedler $
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
 * Defines the class that handles the 'resume' command.
 *
 * @author  Nathan Fiedler
 */
public class resumeCommand extends JSwatCommand {

    /**
     * Perform the 'resume' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        try {
            if (args.hasMoreTokens()) {
                String token = args.nextToken();
                // Check if user wants to resume 'all' threads.
                if (!token.equals("all")) {

                    // User is suspended individual threads.
                    resumeThread(session, token, out);
                    while (args.hasMoreTokens()) {
                        resumeThread(session, args.nextToken(), out);
                    }
                    return;
                }
            }
            // User wants to resume the entire VM.
            out.writeln(swat.getResourceString("vmResuming"));
            session.resumeVM();
            logCategory.report("resume: VM has resumed");
        } catch (NotActiveException nse) {
            // Have to catch this even though we already checked.
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform

    /**
     * Resume the thread given by the ID token string.
     *
     * @param  session  Debugging session.
     * @param  idToken  Thread ID as a string.
     * @param  out      Output to write to.
     */
    protected void resumeThread(Session session, String idToken, Log out)
        throws NotActiveException {
        // Find the thread by the ID number.
        ThreadReference thread = ThreadUtils.getThreadByID(session, idToken);
        if (thread != null) {
            // Resume the thread.
            logCategory.report("resume: resuming thread " + idToken);
            thread.resume();
            out.writeln(swat.getResourceString("threadResumed"));
        } else {
            out.writeln(swat.getResourceString("threadNotFound") + " " +
                        idToken);
        }
    } // resumeThread
} // resumeCommand
