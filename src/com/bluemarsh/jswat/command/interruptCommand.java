/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * $Id: interruptCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

/**
 * Defines the class that handles the 'interrupt' command.
 *
 * @author  Nathan Fiedler
 */
public class interruptCommand extends JSwatCommand {

    /**
     * Perform the 'interrupt' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Check for active session.
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        // Check for missing arguments.
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }
        // Read the next token as the thread ID.
        String idStr = args.nextToken();

        // Find the thread by the ID number.
        VirtualMachine vm = session.getConnection().getVM();
        ThreadReference thread = Threads.getThreadByID(vm, idStr);
        if (thread != null) {
            // Interrupt the thread.
            thread.interrupt();
            StringBuffer buf = new StringBuffer(80);
            buf.append("Thread ");
            String name = thread.name();
            if (name == null || name.length() == 0) {
                buf.append(idStr);
            } else {
                buf.append(name);
            }
            buf.append(" interrupted.");
            out.writeln(buf.toString());
        } else {
            throw new CommandException(Bundle.getString("invalidThreadID"));
        }
    } // perform
} // interruptCommand
