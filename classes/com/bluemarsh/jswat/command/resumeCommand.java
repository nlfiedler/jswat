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
 * $Id: resumeCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

/**
 * Defines the class that handles the 'resume' command.
 *
 * @author  Nathan Fiedler
 */
public class resumeCommand extends JSwatCommand {

    /**
     * Perform the 'resume' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        if (args.hasMoreTokens() && !args.peek().equals("all")) {
            // User is resuming individual threads.
            String token = args.nextToken();
            VirtualMachine vm = session.getConnection().getVM();
            resumeThread(vm, token, out);
            while (args.hasMoreTokens()) {
                resumeThread(vm, args.nextToken(), out);
            }
        } else {
            // User wants to resume the entire VM.
            session.resumeVM(this, false, false);
        }
    } // perform

    /**
     * Resume the thread given by the ID token string.
     *
     * @param  vm   debuggee virtual machine.
     * @param  tid  thread ID as a string.
     * @param  out  output to write to.
     */
    protected void resumeThread(VirtualMachine vm, String tid, Log out) {
        // Find the thread by the ID number.
        ThreadReference thread = Threads.getThreadByID(vm, tid);
        if (thread != null) {
            // Resume the thread.
            thread.resume();
            out.writeln(Bundle.getString("resume.threadResumed"));
        } else {
            throw new CommandException(
                Bundle.getString("threadNotFound") + ' ' + tid);
        }
    } // resumeThread
} // resumeCommand
