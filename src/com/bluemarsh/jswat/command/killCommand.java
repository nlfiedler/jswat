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
 * FILE:        killCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/29/99        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'kill' command.
 *
 * $Id: killCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the class that handles the 'kill' command.
 *
 * @author  Nathan Fiedler
 */
public class killCommand extends JSwatCommand {

    /**
     * Perform the 'kill' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!args.hasMoreTokens()) {
            // No thread numbers means kill the entire VM.
            if (session.isActive()) {
                session.deactivate(true);
            } else {
                out.writeln(swat.getResourceString("noActiveSession"));
            }

        } else {
            try {
                while (args.hasMoreTokens()) {
                    String idToken = args.nextToken();
                    // Find the thread by the ID number.
                    ThreadReference thread = ThreadUtils.getThreadByID(
                        session, idToken);
                    if (thread != null) {
                        // Kill the current thread.
                        List classes = findClassesByPattern
                            (session, "java.lang.Exception");

                        // Create an exception object and stop the thread.
                        ObjectReference obj;
                        try {
                            obj = createObject((ReferenceType) classes.get(0),
                                               new ArrayList(), thread);
                            thread.stop(obj);
                            out.writeln(swat.getResourceString
                                        ("threadTerminated"));
                        } catch (ClassNotLoadedException cnle) {
                            out.writeln(swat.getResourceString
                                        ("classNotPrepared"));
                        } catch (InvalidTypeException ite) {
                            out.writeln(ite.toString());
                        } catch (InvocationException ie) {
                            out.writeln(ie.toString());
                        } catch (IncompatibleThreadStateException itse) {
                            out.writeln("Thread " + idToken + ": " +
                                        swat.getResourceString
                                        ("threadNotSuspended"));
                        }
                    } else {
                        out.writeln(swat.getResourceString("threadNotFound") +
                                    " " + idToken);
                    }
                }
            } catch (NotActiveException nse) {
                out.writeln(swat.getResourceString("noActiveSession"));
            }
        }
    } // perform
} // killCommand
