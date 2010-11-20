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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Steve Yegge, Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console.commands;

import com.sun.jdi.ThreadReference;
import java.io.PrintWriter;
import org.openide.util.NbBundle;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.sun.jdi.StackFrame;
import java.util.List;

/**
 * Displays the call stack for one or all threads.
 *
 * @author Steve Yegge
 */
public class WhereCommand
        extends com.bluemarsh.jswat.command.commands.WhereCommand {

    // Example JDB traces:
    //
    // main[8] where
    //   [1] Test.chain3 (Test.java:34)
    //   [2] Test.chain2 (Test.java:30)
    //   [3] Test.chain1 (Test.java:26)
    //   [4] Test.<init> (Test.java:10)
    //   [5] Test.main (Test.java:5)
    // main[8] up
    // main[9] where
    //   [2] Test.chain2 (Test.java:30)
    //   [3] Test.chain1 (Test.java:26)
    //   [4] Test.<init> (Test.java:10)
    //   [5] Test.main (Test.java:5)
    // main[2] where all
    // Signal Dispatcher:
    // Finalizer:
    //   [1] java.lang.Object.wait (native method)
    //   [2] java.lang.ref.ReferenceQueue.remove (ReferenceQueue.java:133)
    //   [3] java.lang.ref.ReferenceQueue.remove (ReferenceQueue.java:149)
    //   [4] java.lang.ref.Finalizer$FinalizerThread.run (Finalizer.java:177)
    // Reference Handler:
    //   [1] java.lang.Object.wait (native method)
    //   [2] java.lang.Object.wait (Object.java:502)
    //   [3] java.lang.ref.Reference$ReferenceHandler.run (Reference.java:133)
    // main:
    //   [2] Test.chain2 (Test.java:30)
    //   [3] Test.chain1 (Test.java:26)
    //   [4] Test.<init> (Test.java:10)
    //   [5] Test.main (Test.java:5)
    //
    // Notes (compared to jswat's default behavior):
    //  a) doesn't print thread name for "where" with no args
    //  b) stack frames below current frame are elided
    //  c) stack frames are 1-indexed
    //  d) no blank line separating stacks
    //  e) no message printed for empty stacks (just thread name)
    //  f) no program counter info unless the command is "wherei"
    //  g) no indicator for current frame in current thread :(
    //  h) "where <thread-id>" actually switches to that thread!
    @Override
    protected void printStack(ThreadReference thread, PrintWriter writer,
            DebuggingContext dc) throws CommandException {
        if (!com.bluemarsh.jswat.console.Main.emulateJDB()) {
            super.printStack(thread, writer, dc);
            return;
        }
        StringBuilder sb = new StringBuilder(1024);

        if (!arg.isEmpty()) {  // Note (a)
            sb.append(NbBundle.getMessage(getClass(), "CTL_where_header", thread.name()));
            sb.append('\n');
        }
        List<StackFrame> stack = getStack(thread);

        // Start at current frame -- Notes (b) and (e)
        for (int i = dc.getFrame(), nFrames = stack.size(); i < nFrames; i++) {
            sb.append("  [");  // Note (g)
            sb.append(i + 1);  // Note (c)
            sb.append("] ");
            appendFrameDescriptor(stack.get(i).location(), sb);
            // XXX:  implement Note (f)
            sb.append("\n");
        }

        // Note (d) -- remove blank line between stacks
        int len = sb.length(), end = len - 1;
        String result;
        if ("all".equals(arg) && len > 0 && sb.charAt(end) == '\n') {
            result = sb.substring(0, end);
        } else {
            result = sb.toString();
        }

        // XXX:  implement Note (h) (switch to new thread).
        writer.print(result);
    }

    /**
     * Utility shared by "up"/"down"/"frame" to emit just the
     * currently active frame of the current thread's stack trace.
     * Assumes we've already validated all the preconditions.
     */
    void displayCurrentFrame(CommandContext context) throws CommandException {
        DebuggingContext dc = ContextProvider.getContext(context.getSession());
        List<StackFrame> stack = getStack(dc.getThread());
        int i = dc.getFrame();
        StringBuilder sb = new StringBuilder(256);
        sb.append("  [").append(i).append("] ");
        appendFrameDescriptor(stack.get(i).location(), sb);
        context.getWriter().println(sb.toString());
    }
}
