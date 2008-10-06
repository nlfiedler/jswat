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
 * FILE:        traceCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/29/02        Initial version
 *      nf      03/31/02        Added arguments
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'trace' command.
 *
 * $Id: traceCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.TraceBreakpoint;

/**
 * Defines the class that handles the 'trace' command.
 *
 * @author  Nathan Fiedler
 */
public class traceCommand extends JSwatCommand {

    /**
     * Perform the 'trace' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Process the filter arguments.
        String classes = args.nextToken();
        if (classes.equals("all")) {
            // This keyword is just a placeholder.
            classes = null;
        }

        String threads = null;
        if (args.hasMoreTokens()) {
            // Thread filters have been given.
            threads = args.nextToken();
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint bp = new TraceBreakpoint(classes, threads);
        try {
            brkman.addNewBreakpoint(bp);
        } catch (ResolveException re) {
            // this can't happen
            throw new CommandException(re);
        }
        out.writeln(Bundle.getString("trace.traceAdded"));
    } // perform
} // traceCommand
