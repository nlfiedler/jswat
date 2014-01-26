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
 * FILE:        whereCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/14/99        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'where' command.
 *
 * $Id: whereCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'where' command.
 *
 * @author  Nathan Fiedler
 */
public class whereCommand extends JSwatCommand {
    /** Set to true to display program counter information in
     * thread stack dump. */
    protected boolean showPC = false;

    /**
     * Perform the 'where' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Make sure there's an active session.
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        // Get the current thread.
        ThreadReference current = session.getCurrentThread();
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);

        if (!args.hasMoreTokens()) {
            // No arguments, try to print the current thread.
            if (current == null) {
                out.writeln(Bundle.getString("noCurrentThread"));
            } else {
                // Show current thread's stack frame.
                printStack(current, out, ctxtMgr);
            }
        } else {

            String token = args.nextToken();
            if (token.toLowerCase().equals("all")) {
                // User wants to see all thread stack frames.
                Iterator iter = session.getVM().allThreads().iterator();
                while (iter.hasNext()) {
                    ThreadReference thread = (ThreadReference) iter.next();
                    out.writeln(thread.name() + ": ");
                    printStack(thread, out, ctxtMgr);
                }
            } else {
                // Token is a thread ID value, use that to display
                // the associated stack frames.
                try {
                    ThreadReference thread = ThreadUtils.getThreadByID(
                        session, token);
                    if (thread != null) {
                        printStack(thread, out, ctxtMgr);
                    } else {
                        out.writeln(Bundle.getString("invalidThreadID"));
                    }
                } catch (NotActiveException nse) {
                    out.writeln(swat.getResourceString("noActiveSession"));
                } catch (NumberFormatException nfe) {
                    out.writeln(Bundle.getString("invalidThreadID"));
                }
            }
        }
    } // perform

    /**
     * Display the stack frames of the given thread, possibly with
     * program counter information included.
     *
     * @param  thread   ThreadReference whose stack is to be printed.
     * @param  out      Output to print stack to.
     * @param  ctxtMgr  Context manager.
     */
    protected void printStack(ThreadReference thread, Log out,
                              ContextManager ctxtMgr) {
        List stack = null;
        // Check for possible error conditions.
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
            return;
        } catch (ObjectCollectedException oce) {
            out.writeln(swat.getResourceString("objectCollected"));
            return;
        }
        if (stack == null) {
            out.writeln(swat.getResourceString("threadNotRunning"));
            return;
        }
        int nFrames = stack.size();
        if (nFrames == 0) {
            out.writeln(swat.getResourceString("emptyStack"));
            return;
        }
        boolean threadIsCurrent = false;
        ThreadReference currThrd = ctxtMgr.getCurrentThread();
        if ((currThrd != null) && currThrd.equals(thread)) {
            threadIsCurrent = true;
        }

        // For each stack frame, display its information.
        StringBuffer buf = new StringBuffer(256);
        for (int i = 0; i < nFrames; i++) {
            StackFrame frame = (StackFrame) stack.get(i);
            Location loc = frame.location();
            Method method = loc.method();
            // Show the frame number.
            if (threadIsCurrent) {
                if (ctxtMgr.getCurrentFrame() == i) {
                    // Shows that this frame is the current frame.
                    buf.append("* [");
                } else {
                    buf.append("  [");
                }
            } else {
                buf.append("  [");
            }
            buf.append(i + 1);
            buf.append("] ");
            // Show the method class/interface type.
            buf.append(method.declaringType().name());
            buf.append('.');
            // Show the method name.
            buf.append(method.name());
            buf.append(" (");
            if (method.isNative()) {
                // Method is native.
                buf.append("native method");
            } else if (loc.lineNumber() != -1) {
                // Write the source code file name.
                try {
                    buf.append(loc.sourceName());
                } catch (AbsentInformationException e) {
                    buf.append("<unknown>");
                }
                // Write the source code line number.
                buf.append(':');
                buf.append(loc.lineNumber());
            }
            buf.append(')');
            if (showPC) {
                // Show the program counter, if desired.
                long pc = loc.codeIndex();
                if (pc != -1) {
                    buf.append(", pc = ");
                    buf.append(pc);
                }
            }
            buf.append('\n');
        }
        out.write(buf.toString());
    } // printStack
} // whereCommand
