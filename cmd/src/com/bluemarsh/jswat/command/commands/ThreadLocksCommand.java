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
 * are Copyright (C) 2005. All Rights Reserved.
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
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays information concerning the thread locks.
 *
 * @author Nathan Fiedler
 */
public class ThreadLocksCommand extends AbstractCommand {

    public String getName() {
        return "threadlocks";
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
                // Show current thread lock information.
                printThreadLockInfo(current, writer);
            }
        } else {

            String token = arguments.nextToken();
            if (token.equals("all")) {
                // Show thread locks for all threads.
                List<ThreadReference> threads = vm.allThreads();
                for (ThreadReference thread : threads) {
                    printThreadLockInfo(thread, writer);
                }
            } else {
                // Show thread locks for the given thread.
                // Find the thread by the ID number.
                ThreadReference thread = Threads.findThread(vm, token);
                if (thread != null) {
                    printThreadLockInfo(thread, writer);
                } else {
                    throw new CommandException(
                        NbBundle.getMessage(getClass(), "ERR_InvalidThreadID"));
                }
            }
        }
    }

    /**
     * Print thread lock information for the given thread.
     *
     * @param  thread  thread for which to display lock info.
     * @param  writer  where thread lock information should go.
     */
    protected void printThreadLockInfo(ThreadReference thread,
            PrintWriter writer) throws CommandException {
        try {
            VirtualMachine vm = thread.virtualMachine();
            StringBuilder sb = new StringBuilder(512);
            sb.append(NbBundle.getMessage(getClass(),
                    "CTL_threadlocks_monitorInfo", thread.name()));
            sb.append('\n');
            if (vm.canGetOwnedMonitorInfo()) {
                List<ObjectReference> owned = thread.ownedMonitors();
                if (owned.size() == 0) {
                    sb.append("  ");
                    sb.append(NbBundle.getMessage(getClass(),
                            "CTL_threadlocks_noMonitors"));
                    sb.append('\n');
                } else {
                    Iterator<ObjectReference> iter = owned.iterator();
                    while (iter.hasNext()) {
                        ObjectReference monitor = iter.next();
                        sb.append("  ");
                        sb.append(NbBundle.getMessage(getClass(),
                                "CTL_threadlocks_ownedMonitor", monitor.toString()));
                        sb.append('\n');
                    }
                }
            } else {
                sb.append("  ");
                sb.append(NbBundle.getMessage(getClass(),
                        "CTL_threadlocks_cannotGetOwnedMonitors"));
                sb.append('\n');
            }
            if (vm.canGetCurrentContendedMonitor()) {
                ObjectReference waiting = thread.currentContendedMonitor();
                sb.append("  ");
                if (waiting == null) {
                    sb.append(NbBundle.getMessage(getClass(),
                            "CTL_threadlocks_notWaiting"));
                } else {
                    sb.append(NbBundle.getMessage(getClass(),
                            "CTL_threadlocks_waitingFor", waiting.toString()));
                }
                sb.append('\n');
            } else {
                sb.append("  ");
                sb.append(NbBundle.getMessage(getClass(),
                        "CTL_threadlocks_cannotGetContendedMonitor"));
                sb.append('\n');
            }
            writer.print(sb.toString());
        } catch (UnsupportedOperationException uoe) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "CTL_threadlocks_unsupported"), uoe);
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_ThreadNotSuspended"), itse);
        }
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
