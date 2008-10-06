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
 * $Id: nextiCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Stepping;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.StepRequest;

/**
 * Defines the class that handles the 'nexti' command.
 *
 * @author  Nathan Fiedler
 */
public class nextiCommand extends JSwatCommand {

    /**
     * Perform the 'nexti' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        ContextManager contextManager = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference current = contextManager.getCurrentThread();
        if (current == null) {
            throw new CommandException(Bundle.getString("noCurrentThread"));
        }
        if (!current.isSuspended()) {
            throw new CommandException(Bundle.getString("threadNotSuspended"));
        }

        boolean onlyThread = false;
        if (args.hasMoreTokens()) {
            String token = args.peek();
            if (token.equals("thread")) {
                // Suspend only the event thread.
                onlyThread = true;
            }
        }

        // Step a single instruction, over functions.
        if (Stepping.step(
                session.getVM(), current, StepRequest.STEP_MIN,
                StepRequest.STEP_OVER, onlyThread,
                session.getProperty("excludes"))) {
            // Must use the Session to (quietly) resume the VM.
            session.resumeVM(this, true, true);
        }
    } // perform
} // nextiCommand
