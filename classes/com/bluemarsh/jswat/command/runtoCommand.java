/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        runtoCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/06/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'runto' command.
 *
 * $Id: runtoCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;

/**
 * Defines the class that handles the 'runto' command.
 *
 * @author  Nathan Fiedler
 */
public class runtoCommand extends JSwatCommand {

    /**
     * Perform the 'runto' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Parse the rest of the command to get the code location
        // for the breakpoint.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            Breakpoint bp = brkman.parseBreakpointSpec(args.rest());
            bp.deleteOnExpire();
            bp.setExpireCount(1);
            // Do the "run" part.
            session.resumeVM(this, false, false);
        } catch (ClassNotFoundException cnfe) {
            throw new CommandException(Bundle.getString("invalidClassName") +
                                       '\n' + cnfe.toString());
        } catch (NumberFormatException nfe) {
            // This can only mean one thing.
            // This must come before IllegalArgumentException.
            throw new CommandException(Bundle.getString("badLineNumber") +
                                       '\n' + nfe.toString());
        } catch (IllegalArgumentException iae) {
            // User gave us something screwy.
            throw new CommandException(iae.toString());
        } catch (MalformedMemberNameException mmne) {
            throw new CommandException(Bundle.getString("invalidMethod") +
                                       '\n' + mmne.toString());
        } catch (ResolveException re) {
            throw new CommandException(re.errorMessage());
        } catch (IllegalStateException ise) {
            // we checked this already.
        }
    } // perform
} // runtoCommand
