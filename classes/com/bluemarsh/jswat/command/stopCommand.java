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
 * FILE:        stopCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/01/99        Initial version
 *      nf      06/03/01        Rewrote to use new breakpoint code
 *      nf      08/21/01        Use readable resolve error message
 *      nf      07/11/02        Fixed bug 568
 *
 * $Id: stopCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.sun.jdi.request.EventRequest;

/**
 * Defines the class that handles the 'stop' command.
 *
 * @author  Nathan Fiedler
 */
public class stopCommand extends JSwatCommand {

    /**
     * Perform the 'stop' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Check for more arguments.
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
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

        // Parse the rest of the command to get the code location
        // for the breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            Breakpoint bp = brkman.parseBreakpointSpec(args.rest());
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            out.writeln(Bundle.getString("stop.breakpointAdded"));
        } catch (ClassNotFoundException cnfe) {
            throw new CommandException(
                Bundle.getString("invalidClassName") + '\n'
                + cnfe.toString(), cnfe);
        } catch (NumberFormatException nfe) {
            // This can only mean one thing.
            // This must come before IllegalArgumentException.
            throw new CommandException(
                Bundle.getString("badLineNumber") + '\n'
                + nfe.toString(), nfe);
        } catch (IllegalArgumentException iae) {
            // User gave us something screwy.
            throw new CommandException(iae.toString(), iae);
        } catch (MalformedMemberNameException mmne) {
            throw new CommandException(
                Bundle.getString("invalidMethod") + '\n'
                + mmne.toString(), mmne);
        } catch (ResolveException re) {
            throw new CommandException(re.errorMessage(), re);
        }
    } // perform
} // stopCommand
