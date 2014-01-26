/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * FILE:        threadbrkCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/08/02        Initial version
 *
 * $Id: threadbrkCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.ThreadBreakpoint;
import com.sun.jdi.request.EventRequest;

/**
 * Defines the class that handles the 'threadbrk' command.
 *
 * @author  Nathan Fiedler
 */
public class threadbrkCommand extends JSwatCommand {

    /**
     * Perform the 'threadbrk' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Default to suspending all of the threads.
        int suspendPolicy = EventRequest.SUSPEND_ALL;
        // Default to no thread and stop on any event.
        String threadName = null;
        boolean onStart = true;
        boolean onDeath = true;

        // Process the arguments in no particular order.
        while (args.hasMoreTokens()) {
            String arg = args.nextToken();
            if (arg.equals("start")) {
                onDeath = false;
            } else if (arg.equals("death")) {
                onStart = false;
            } else if (arg.equals("go")) {
                suspendPolicy = EventRequest.SUSPEND_NONE;
            } else if (arg.equals("thread")) {
                suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            } else {
                threadName = arg;
            }
        }

        // Create the breakpoint and add it to the list.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new ThreadBreakpoint(threadName, onStart, onDeath);
        try {
            brkman.addNewBreakpoint(bp);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            out.writeln(Bundle.getString("threadbrk.breakpointAdded"));
        } catch (ResolveException re) {
            // This cannot happen for this type of breakpoint.
            throw new CommandException(re);
        }
    } // perform
} // threadbrkCommand
