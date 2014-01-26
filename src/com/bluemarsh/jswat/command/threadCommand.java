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
 * FILE:        threadCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/15/99        Initial version
 *      nf      08/07/01        Moved getThreadByID method from Session
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'thread' command.
 *
 * $Id: threadCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ThreadReference;

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
            String statusStr;
            int statusInt = thrd.status();
            if (statusInt == ThreadReference.THREAD_STATUS_MONITOR) {
                statusStr = swat.getResourceString("threadStatusMonitor");
            } else if (statusInt == ThreadReference.THREAD_STATUS_RUNNING) {
                statusStr = swat.getResourceString("threadStatusRunning");
            } else if (statusInt == ThreadReference.THREAD_STATUS_SLEEPING) {
                statusStr = swat.getResourceString("threadStatusSleeping");
            } else if (statusInt == ThreadReference.THREAD_STATUS_WAIT) {
                statusStr = swat.getResourceString("threadStatusWait");
            } else if (statusInt == ThreadReference.THREAD_STATUS_ZOMBIE) {
                statusStr = swat.getResourceString("threadStatusZombie");
            } else if (statusInt ==
                       ThreadReference.THREAD_STATUS_NOT_STARTED) {
                statusStr = swat.getResourceString("threadStatusNotStarted");
            } else {
                statusStr = swat.getResourceString("threadStatusUnknown");
            }
            buffer.append(statusStr);
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
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        if (!args.hasMoreTokens()) {
            // Print the current thread.
            ThreadReference thread = session.getCurrentThread();
            if (thread == null) {
                out.writeln(Bundle.getString("noCurrentThread"));
            } else {
                out.writeln(swat.getResourceString("currentThread") + ' ' +
                            buildDescriptor(null, thread));
            }
            return;
        }

        try {
            // Find the thread by the ID number.
            ThreadReference thread = ThreadUtils.getThreadByID(
                session, args.nextToken());
            if (thread != null) {
                // Set the current thread.
                ContextManager contextManager = (ContextManager)
                    session.getManager(ContextManager.class);
                contextManager.setCurrentThread(thread);
            } else {
                out.writeln(Bundle.getString("invalidThreadID"));
            }
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform
} // threadCommand
