/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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

    /**
     * First argument, or empty string if no args.
     */
    protected String arg = "";  // "where"

    @Override
    public String getName() {
        return "where";
    }

    @Override
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
                throw new CommandException(getMessage("ERR_NoThread"));
            } else {
                printStack(current, writer, dc);
            }
        } else {
            // Note that like JDB, we silently ignore any further args.
            arg = arguments.nextToken();
            if (arg.equals("all")) {
                // Show thread locks for all threads.
                List<ThreadReference> threads = vm.allThreads();
                for (ThreadReference thread : threads) {
                    printStack(thread, writer, dc);
                    writer.println();
                }
            } else {
                // Show thread locks for the given thread.
                // Find the thread by the ID number.
                ThreadReference thread = Threads.findThread(vm, arg);
                if (thread != null) {
                    printStack(thread, writer, dc);
                } else {
                    throw new CommandException(getMessage("ERR_InvalidThreadID"));
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
        List<StackFrame> stack = getStack(thread);

        boolean threadIsCurrent = false;
        ThreadReference currThrd = dc.getThread();
        if (currThrd != null && currThrd.equals(thread)) {
            threadIsCurrent = true;
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append(getMessage("CTL_where_header", thread.name()));
        sb.append('\n');
        int nFrames = stack.size();
        if (nFrames == 0) {
            sb.append(getMessage("CTL_where_emptyStack"));
            sb.append('\n');
        }
        for (int i = 0; i < nFrames; i++) {
            Location loc = stack.get(i).location();
            if (threadIsCurrent) {
                if (dc.getFrame() == i) {
                    sb.append("* [");
                } else {
                    sb.append("  [");
                }
            } else {
                sb.append("  [");
            }
            // Leave the stack frame index as zero-based.
            sb.append(i);
            sb.append("] ");
            appendFrameDescriptor(loc, sb);
            long pc = loc.codeIndex();
            if (pc != -1) {
                sb.append(", pc = ");
                sb.append(pc);
            }
            sb.append('\n');
        }
        writer.print(sb.toString());
    }

    /**
     * Append a description of the current frame's location.
     * Does not print the "[i]" stack frame number.
     */
    public void appendFrameDescriptor(Location loc, StringBuilder sb) {
        Method method = loc.method();
        sb.append(method.declaringType().name());
        sb.append('.');
        sb.append(method.name());
        sb.append(" (");
        if (method.isNative()) {
            sb.append(getMessage("CTL_where_native"));
        } else if (loc.lineNumber() != -1) {
            try {
                sb.append(loc.sourceName());
            } catch (AbsentInformationException e) {
                sb.append(getMessage("CTL_where_absentInfo"));
            }
            sb.append(':');
            sb.append(loc.lineNumber());
        }
        sb.append(')');
    }

    /**
     * Return a list of {@link StackFrame} objects for passed thread.
     */
    public List<StackFrame> getStack(ThreadReference thread)
            throws CommandException {
        List<StackFrame> stack = null;
        try {
            stack = thread.frames();
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(getMessage("ERR_ThreadNotSuspended"));
        } catch (ObjectCollectedException oce) {
            throw new CommandException(getMessage("ERR_ObjectCollected"));
        }
        if (stack == null) {
            throw new CommandException(getMessage("ERR_IncompatibleThread"));
        }
        return stack;
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }

    // XXX:  move these utilities to all the main base classes,
    // and derive the appropriate class name to use.

    protected String getMessage(String key) {
        return NbBundle.getMessage(WhereCommand.class, key);
    }

    protected String getMessage(String key, String arg) {
        return NbBundle.getMessage(WhereCommand.class, key, arg);
    }
}
