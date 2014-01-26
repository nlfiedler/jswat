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
 * FILE:        catchCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/99        Initial version
 *      nf      08/21/01        Use readable resolve error message
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'catch' command.
 *
 * $Id: catchCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.request.EventRequest;

/**
 * Defines the class that handles the 'catch' command.
 *
 * @author  Nathan Fiedler
 */
public class catchCommand extends JSwatCommand {

    /**
     * Perform the 'catch' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Check for correct arguments.
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        // See if user provided the go or thread option.
        String peek = args.peek();
        // Default to suspending all of the threads.
        int suspendPolicy = EventRequest.SUSPEND_ALL;
        if (peek.equals("go")) {
            suspendPolicy = EventRequest.SUSPEND_NONE;
            // Remove the option flag.
            args.nextToken();
        } else if (peek.equals("thread")) {
            suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            // Remove the option flag.
            args.nextToken();
        }

        // Get the exception name.
        String cname = args.nextToken();

        // Create an exception breakpoint.
        try {
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            Breakpoint bp = brkman.createExceptionCatch(cname);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            out.writeln(Bundle.getString("catch.added"));
        } catch (ClassNotFoundException cnfe) {
            StringBuffer buf = new StringBuffer
                (swat.getResourceString("badClassName"));
            buf.append(": ");
            buf.append(cname);
            out.writeln(buf.toString());
        } catch (ResolveException re) {
            out.writeln(re.errorMessage());
        }
    } // perform
} // catchCommand
