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
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'stop' command.
 *
 * $Id: stopCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Locatable;
import com.sun.jdi.Location;
import com.sun.jdi.request.EventRequest;
import java.util.Iterator;

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
    public void perform(Session session, StringTokenizer args, Log out) {
        // Check for more arguments.
        if (!args.hasMoreTokens()) {
            // No more arguments, print a list of breakpoints.
            printBreakList(session, out);
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

        // Parse the rest of the command to get the code location
        // for the breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            Breakpoint bp = brkman.parseBreakpointSpec(args);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            out.writeln(Bundle.getString("stop.breakpointAdded"));
        } catch (ClassNotFoundException cnfe) {
            out.writeln(Bundle.getString("invalidClassName") + '\n' +
                        cnfe.toString());
        } catch (NumberFormatException nfe) {
            // This can only mean one thing.
            // This must come before IllegalArgumentException.
            out.writeln(Bundle.getString("badLineNumber") +
                        '\n' + nfe.toString());
        } catch (IllegalArgumentException iae) {
            // User gave us something screwy.
            out.writeln(iae.toString());
        } catch (MalformedMemberNameException mmne) {
            out.writeln(Bundle.getString("invalidMethod") + '\n' +
                        mmne.toString());
        } catch (ResolveException re) {
            out.writeln(re.errorMessage());
        }
    } // perform

    /**
     * Display a list of the breakpoints to the output.
     *
     * @param  session  JSwat session on which to operate.
     * @param  out      Output to write messages to.
     */
    protected void printBreakList(Session session, Log out) {
        // Display a list of all breakpoints.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Iterator iter = brkman.breakpoints(true);
        if (iter.hasNext()) {
            out.writeln(Bundle.getString("stop.breakpointList"));
            // For each breakpoint, display a description.
            while (iter.hasNext()) {
                Breakpoint bp = (Breakpoint) iter.next();
                printBreakpoint(brkman, bp, out);
            }
        } else {
            out.writeln(Bundle.getString("stop.noBreakpointsDefined"));
        }
    } // printBreakList

    /**
     * Prints the breakpoint specification to the Log.
     *
     * @param  brkman  Breakpoint manager.
     * @param  bp      Breakpoint to print.
     * @param  out     Log to print to.
     */
    protected void printBreakpoint(BreakpointManager brkman,
                                   Breakpoint bp, Log out) {
        // Print the breakpoint number.
        StringBuffer buf = new StringBuffer(80);
        buf.append(brkman.getBreakpointNumber(bp));
        buf.append(". ");

        // Print the current state of the breakpoint.
        if (!bp.isResolved()) {
            buf.append(Bundle.getString("stop.unresolvedInBrackets"));
        } else if (!bp.isEnabled()) {
            buf.append(Bundle.getString("stop.disabledInBrackets"));
        } else if (bp.hasExpired()) {
            buf.append(Bundle.getString("stop.expiredInBrackets"));
        } else if (bp.isSkipping()) {
            buf.append(Bundle.getString("stop.skippingInBrackets"));
        } else {
            buf.append(Bundle.getString("stop.enabledInBrackets"));
        }

        // Have the breakpoint print itself.
        buf.append(' ');
        buf.append(bp.toString());
        out.writeln(buf.toString());
    } // printBreakpoint
} // stopCommand
