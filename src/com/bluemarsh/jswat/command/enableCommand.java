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
 * FILE:        enableCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/06/99        Initial version
 *      nf      06/11/01        Changed to use new breakpoints code
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'enable' command.
 *
 * $Id: enableCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.util.StringTokenizer;
import java.util.Iterator;

/**
 * Defines the class that handles the 'enable' command.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/6/99
 */
public class enableCommand extends stopCommand {
    /** Set to true to enable breakpoints, false to disable. */
    protected boolean enabledValue = true;

    /**
     * Perform the 'enable' command.
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

        // Enable the breakpoints using breakpoint manager.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);

        // Check the command arguments.
        String option = args.peek();
        if (option.equals("all")) {
            // Process all of the existing breakpoints.
            Iterator iter = brkman.breakpoints(true);
            while (iter.hasNext()) {
                Breakpoint bp = (Breakpoint) iter.next();
                if (enabledValue) {
                    brkman.enableBreakpoint(bp);
                } else {
                    brkman.disableBreakpoint(bp);
                }
            }

            // Now process the breakpoint groups.
            iter = brkman.groups(true);
            while (iter.hasNext()) {
                BreakpointGroup group = (BreakpointGroup) iter.next();
                group.setEnabled(enabledValue);
            }
        } else {

            while (args.hasMoreTokens()) {
                int n = -1;
                try {
                    // Try to parse the option as an integer.
                    n = Integer.parseInt(args.nextToken());
                } catch (NumberFormatException nfe) {
                    out.writeln(Bundle.getString("invalidBreakpoint"));
                    return;
                }

                // Process the nth breakpoint.
                Breakpoint bp = brkman.getBreakpoint(n);
                if (bp == null) {
                    out.writeln(Bundle.getString("invalidBreakpoint"));
                    return;
                } else {
                    if (enabledValue) {
                        brkman.enableBreakpoint(bp);
                    } else {
                        brkman.disableBreakpoint(bp);
                    }
                }
            }
        }

        if (enabledValue) {
            out.writeln(Bundle.getString("enable.breakpointsEnabled"));
        } else {
            out.writeln(Bundle.getString("enable.breakpointsDisabled"));
        }
    } // perform
} // enableCommand
