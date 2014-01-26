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
 * FILE:        clearCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/15/99        Initial version
 *      nf      06/11/01        Use new breakpoints code
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'clear' command.
 *
 * $Id: clearCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Defines the class that handles the 'clear' command.
 *
 * @author  Nathan Fiedler
 */
public class clearCommand extends stopCommand {

    /**
     * Perform the 'clear' command.
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

        // Clear the breakpoints using breakpoint manager.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);

        // Check the command arguments.
        String option = args.peek();
        if (option.equals("all")) {
            // Clear all the existing breakpoints.
            // First have to get all the breakpoints into a list of our
            // own to avoid concurrent modification.
            ArrayList allbps = new ArrayList();
            Iterator iter = brkman.breakpoints(true);
            while (iter.hasNext()) {
                allbps.add(iter.next());
            }

            for (int i = allbps.size() - 1; i >= 0; i--) {
                brkman.removeBreakpoint((Breakpoint) allbps.get(i));
            }
            allbps.clear();
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

                // Clear the nth breakpoint.
                Breakpoint bp = brkman.getBreakpoint(n);
                if (bp == null) {
                    out.writeln(Bundle.getString("invalidBreakpoint"));
                    return;
                }
                brkman.removeBreakpoint(bp);
            }
        }

        out.writeln(Bundle.getString("clear.breakpointsDeleted"));
    } // perform
} // clearCommand
