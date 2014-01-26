/*********************************************************************
 *
 *      Copyright (C) 1999 Nathan Fiedler
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
 * FILE:        interruptCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      9/5/99          Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'interrupt' command.
 *
 * $Id: interruptCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ThreadReference;

/**
 * Defines the class that handles the 'interrupt' command.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/5/99
 */
public class interruptCommand extends JSwatCommand {

    /**
     * Perform the 'interrupt' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Check for active session.
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }
        // Check for missing arguments.
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }
        // Read the next token as the thread ID.
        String idStr = args.nextToken();

        // Find the thread by the ID number.
        try {
            ThreadReference thread = ThreadUtils.getThreadByID(session, idStr);
            if (thread != null) {
                // Interrupt the thread.
                thread.interrupt();
                StringBuffer buf = new StringBuffer(80);
                buf.append("Thread ");
                String name = thread.name();
                if ((name == null) || (name.equals(""))) {
                    buf.append(idStr);
                } else {
                    buf.append(name);
                }
                buf.append(" interrupted.");
                out.writeln(buf.toString());
            } else {
                out.writeln(Bundle.getString("invalidThreadID"));
            }
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform
} // interruptCommand
