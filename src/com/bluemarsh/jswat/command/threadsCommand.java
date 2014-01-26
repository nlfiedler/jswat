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
 * FILE:        threadsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/31/99        Initial version
 *      nf      09/10/01        Implemented request 219
 *      nf      01/12/02        Implemented request 20
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'threads' command.
 *
 * $Id: threadsCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadGroupIterator;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'threads' command.
 *
 * @author  Nathan Fiedler
 */
public class threadsCommand extends threadCommand {

    /**
     * Perform the 'threads' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Get the list of all threads.
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        List threadsList = null;
        if (args.hasMoreTokens()) {
            long tid = -1;
            String name = args.nextToken();
            // Is it a thread name or unique ID?
            try {
                tid = Long.parseLong(name);
            } catch (NumberFormatException nfe) {
            }
            // Look for a thread group with this name or id.
            Iterator iter = new ThreadGroupIterator(
                session.getVM().topLevelThreadGroups());
            while (iter.hasNext()) {
                ThreadGroupReference group = (ThreadGroupReference)
                    iter.next();
                if (tid > -1) {
                    if (group.uniqueID() == tid) {
                        threadsList = group.threads();
                        break;
                    }
                } else {
                    if (group.name().equals(name)) {
                        threadsList = group.threads();
                        break;
                    }
                }
            }
        } else {
            threadsList = session.getVM().allThreads();
        }

        if (threadsList == null) {
            out.writeln(Bundle.getString("invalidThreadGroupID"));
        } else if (threadsList.size() > 0) {
            ContextManager conman = (ContextManager)
                session.getManager(ContextManager.class);
            ThreadReference current = conman.getCurrentThread();

            // For each thread in the list, get its status and
            // name and print those out the display.
            StringBuffer buff = new StringBuffer(64);
            StringBuffer outbuf = new StringBuffer(256);
            Iterator iter = threadsList.iterator();
            while (iter.hasNext()) {
                ThreadReference thrd = (ThreadReference) iter.next();
                if (thrd.equals(current)) {
                    outbuf.append("* ");
                }
                outbuf.append(buildDescriptor(buff, thrd));
                outbuf.append('\n');
                buff = new StringBuffer(64);
            }
            out.write(outbuf.toString());
        }
    } // perform
} // threadsCommand
