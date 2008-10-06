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
 * $Id: threadCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;

/**
 * Defines the class that handles the 'thread' command.
 *
 * @author  Nathan Fiedler
 */
public class threadCommand extends JSwatCommand {

    /**
     * Builds up a description of the given thread. This includes the
     * thread ID value, it's full name, and the status of the thread.
     *
     * @param  buffer  StringBuffer to append description to, if null
     *                 a buffer will be allocated.
     * @param  thrd    Thread reference.
     * @return  String containing thread description and status.
     */
    protected static String buildDescriptor(StringBuffer buffer,
                                            ThreadReference thrd) {
        if (thrd == null) {
            return null;
        }
        if (buffer == null) {
            buffer = new StringBuffer();
        }
        try {
            buffer.append("id[");
            buffer.append(thrd.uniqueID());
            buffer.append("] ");
            buffer.append(thrd.name());
            buffer.append(": ");
            buffer.append(Threads.threadStatus(thrd));
        } catch (Exception e) {
            return e.toString();
        }
        return buffer.toString();
    } // buildDescriptor

    /**
     * Perform the 'thread' command.
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
        if (!args.hasMoreTokens()) {
            // Print the current thread.
            ThreadReference thread = contextManager.getCurrentThread();
            if (thread == null) {
                throw new CommandException(
                    Bundle.getString("noCurrentThread"));
            } else {
                out.writeln(Bundle.getString("thread.currentThread") + ' ' +
                            buildDescriptor(null, thread));
            }
            return;
        }

        // Find the thread by the ID number.
        VirtualMachine vm = session.getConnection().getVM();
        ThreadReference thread = Threads.getThreadByID(
            vm, args.nextToken());
        if (thread != null) {
            // Set the current thread.
            contextManager.setCurrentThread(thread);
        } else {
            throw new CommandException(Bundle.getString("invalidThreadID"));
        }
    } // perform
} // threadCommand
