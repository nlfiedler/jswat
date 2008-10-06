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
 * $Id: catchCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ExceptionBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
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
    public void perform(Session session, CommandArguments args, Log out) {
        // Check for correct arguments.
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Get the exception name.
        String cname = args.nextToken();

        // See if user provided the go or thread option.
        // Default to suspending all of the threads.
        int suspendPolicy = EventRequest.SUSPEND_ALL;
        if (cname.equals("go")) {
            suspendPolicy = EventRequest.SUSPEND_NONE;
            cname = null;
        } else if (cname.equals("thread")) {
            suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            cname = null;
        }

        if (cname == null) {
            if (!args.hasMoreTokens()) {
                throw new MissingArgumentsException();
            }
            cname = args.nextToken();
        }

        // For now we always stop on caught and uncaught.
        boolean caught = true;
        boolean uncaught = true;

        // Create an exception breakpoint.
        try {
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            Breakpoint bp = new ExceptionBreakpoint(cname, caught, uncaught);
            brkman.addNewBreakpoint(bp);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            out.writeln(Bundle.getString("catch.added"));
        } catch (ClassNotFoundException cnfe) {
            StringBuffer buf = new StringBuffer(
                Bundle.getString("catch.badClassName"));
            buf.append(": ");
            buf.append(cname);
            throw new CommandException(buf.toString());
        } catch (ResolveException re) {
            throw new CommandException(re);
        }
    } // perform
} // catchCommand
