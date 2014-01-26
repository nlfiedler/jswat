/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        stepiCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/16/99        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'stepi' command.
 *
 * $Id: stepiCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.request.StepRequest;

/**
 * Defines the class that handles the 'stepi' command.
 *
 * @author  Nathan Fiedler
 */
public class stepiCommand extends stepCommand {

    /**
     * Perform the 'stepi' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
	if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        // Step a single instruction, into functions.
        step(session, args, out, StepRequest.STEP_MIN, StepRequest.STEP_INTO);
    } // perform
} // stepiCommand
