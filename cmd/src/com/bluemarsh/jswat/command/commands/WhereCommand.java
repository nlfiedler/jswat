/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WhereCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays the call stack for one or all threads.
 *
 * @author Nathan Fiedler
 */
public class WhereCommand extends AbstractCommand {

    public String getName() {
        return "where";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        // Get the current thread.
        DebuggingContext dc = context.getDebuggingContext();
        ThreadReference current = dc.getThread();
        if (!arguments.hasMoreTokens()) {
            // No arguments, try to use the current thread.
            if (current == null) {
                throw new CommandException(
                    NbBundle.getMessage(getClass(), "ERR_NoThread"));
            } else {
                printStack(current, writer, dc);
            }
        } else {

            String token = arguments.nextToken();
            if (token.equals("all")) {
                // Show thread locks for all threads.
                List<ThreadReference> threads = vm.allThreads();
                for (ThreadReference thread : threads) {
                    printStack(thread, writer, dc);
                    writer.println();
                }
            } else {
                // Show thread locks for the given thread.
                // Find the thread by the ID number.
                ThreadReference thread = Threads.findThread(vm, token);
                if (thread != null) {
                    printStack(thread, writer, dc);
                } else {
                    throw new CommandException(
                        NbBundle.getMessage(getClass(), "ERR_InvalidThreadID"));
                }
            }
        }
    }

    /**
     * Display the stack frames of the given thread.
     *
     * @param  thread  ThreadReference whose stack is to be printed.
     * @param  writer  writer to print stack to.
     * @param  dc      debugging context.
     * @throws  CommandException
     *          if something goes wrong.
     */
    protected void printStack(ThreadReference thread, PrintWriter writer,
                              DebuggingContext dc) throws CommandException {
        List stack = null;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_ThreadNotSuspended"));
        } catch (ObjectCollectedException oce) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_ObjectCollected"));
        }
        if (stack == null) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_IncompatibleThread"));
        }
        boolean threadIsCurrent = false;
        ThreadReference currThrd = dc.getThread();
        if (currThrd != null && currThrd.equals(thread)) {
            threadIsCurrent = true;
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append(NbBundle.getMessage(getClass(), "CTL_where_header",
                thread.name()));
        sb.append('\n');
        int nFrames = stack.size();
        if (nFrames == 0) {
            sb.append(NbBundle.getMessage(getClass(), "CTL_where_emptyStack"));
            sb.append('\n');
        }
        for (int i = 0; i < nFrames; i++) {
            StackFrame frame = (StackFrame) stack.get(i);
            Location loc = frame.location();
            Method method = loc.method();
            if (threadIsCurrent) {
                if (dc.getFrame() == i) {
                    sb.append("* [");
                } else {
                    sb.append("  [");
                }
            } else {
                sb.append("  [");
            }
            sb.append(i + 1);
            sb.append("] ");
            sb.append(method.declaringType().name());
            sb.append('.');
            sb.append(method.name());
            sb.append(" (");
            if (method.isNative()) {
                sb.append(NbBundle.getMessage(getClass(), "CTL_where_native"));
            } else if (loc.lineNumber() != -1) {
                try {
                    sb.append(loc.sourceName());
                } catch (AbsentInformationException e) {
                    sb.append(NbBundle.getMessage(getClass(),
                            "CTL_where_absentInfo"));
                }
                sb.append(':');
                sb.append(loc.lineNumber());
            }
            sb.append(')');
            long pc = loc.codeIndex();
            if (pc != -1) {
                sb.append(", pc = ");
                sb.append(pc);
            }
            sb.append('\n');
        }
        writer.print(sb.toString());
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
