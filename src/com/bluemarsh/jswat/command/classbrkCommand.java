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
 * FILE:        classbrkCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/29/02        Initial version
 *      nf      03/31/02        Added arguments
 *
 * $Id: classbrkCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ClassBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.sun.jdi.request.EventRequest;

/**
 * Defines the class that handles the 'classbrk' command.
 *
 * @author  Nathan Fiedler
 */
public class classbrkCommand extends JSwatCommand {

    /**
     * Perform the 'classbrk' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Default to suspending all of the threads.
        int suspendPolicy = EventRequest.SUSPEND_ALL;

        // See if user provided the go or thread option.
        String peek = args.peek();
        if (peek.equals("go")) {
            suspendPolicy = EventRequest.SUSPEND_NONE;
            // Remove the option flag.
            args.nextToken();
        } else if (peek.equals("thread")) {
            suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            // Remove the option flag.
            args.nextToken();
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Process the filter arguments.
        String classes = args.nextToken();
        if (classes.equals("all")) {
            // This keyword is just a placeholder.
            classes = null;
        }

        // For now we always stop on prepare and unload.
        boolean onPrepare = true;
        boolean onUnload = true;

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new ClassBreakpoint(classes, onPrepare, onUnload);
        try {
            brkman.addNewBreakpoint(bp);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
        } catch (ResolveException re) {
            // this can't happen
            throw new CommandException(re);
        }
        out.writeln(Bundle.getString("classbrk.breakpointAdded"));
    } // perform
} // classbrkCommand
