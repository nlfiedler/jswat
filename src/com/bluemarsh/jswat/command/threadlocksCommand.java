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
 * FILE:        threadlocksCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/05/99        Initial version
 *      nf      09/03/01        Improved error handling
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'threadlocks' command.
 *
 * $Id: threadlocksCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'threadlocks' command.
 *
 * @author  Nathan Fiedler
 */
public class threadlocksCommand extends JSwatCommand {

    /**
     * Perform the 'threadlocks' command.
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

        // Get the current thread.
        ThreadReference current = session.getCurrentThread();
        if (!args.hasMoreTokens()) {
            // No arguments, try to use the current thread.
            if (current == null) {
                out.writeln(Bundle.getString("noCurrentThread"));
            } else {
                // Show current thread lock information.
                printThreadLockInfo(current, out);
            }
        } else {

            String token = args.nextToken();
            if (token.equals("all")) {
                // Show thread locks for all threads.
                List threadsList = session.getVM().allThreads();
                for (int i = 0; i < threadsList.size(); i++) {
                    printThreadLockInfo((ThreadReference)
                                        threadsList.get(i), out);
                }
            } else {

                // Show thread locks for the given thread.
                // Find the thread by the ID number.
                try {
                    ThreadReference thread = ThreadUtils.getThreadByID(
                        session, token);
                    if (thread != null) {
                        printThreadLockInfo(thread, out);
                    } else {
                        out.writeln(Bundle.getString("invalidThreadID"));
                    }
                } catch (NotActiveException nse) {
                    out.writeln(swat.getResourceString("noActiveSession"));
                }
            }
        }
    } // perform

    /**
     * Display thread lock information for the given thread.
     *
     * @param  thread  Thread of which to display lock info.
     * @param  out     Output to write to.
     */
    protected void printThreadLockInfo(ThreadReference thread, Log out) {
        try {
            StringBuffer buf = new StringBuffer(256);
            buf.append(swat.getResourceString("threadlocksMonitorInfo"));
            buf.append(' ');
            buf.append(thread.name());
            buf.append(':');
            buf.append('\n');
            List owned = thread.ownedMonitors();
            if (owned.size() == 0) {
                buf.append("  ");
                buf.append(swat.getResourceString("threadlocksNoMonitors"));
                buf.append('\n');
            } else {
                Iterator iter = owned.iterator();
                while (iter.hasNext()) {
                    ObjectReference monitor = (ObjectReference) iter.next();
                    buf.append("  ");
                    buf.append(swat.getResourceString
                               ("threadlocksOwnedMonitor"));
                    buf.append(' ');
                    buf.append(monitor.toString());
                    buf.append('\n');
                }
            }
            ObjectReference waiting = thread.currentContendedMonitor();
            buf.append("  ");
            if (waiting == null) {
                buf.append(swat.getResourceString("threadlocksNotWaiting"));
            } else {
                buf.append(swat.getResourceString("threadlocksWaitingFor"));
                buf.append(' ');
                buf.append(waiting.toString());
            }
            buf.append('\n');
            out.write(buf.toString());
        } catch (UnsupportedOperationException uoe) {
            out.writeln(Bundle.getString("threadlocks.uoe"));
        } catch (IncompatibleThreadStateException e) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
        }
    } // printThreadLockInfo
} // threadlocksCommand
