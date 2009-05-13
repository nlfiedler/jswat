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
 * are Copyright (C) 2009. All Rights Reserved.
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
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.request.EventRequest;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Creates trace breakpoints to monitor method entry and exit events.
 *
 * @author Nathan Fiedler
 */
public class MethodTraceCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "trace";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();

        // Look for any optional arguments.
        int suspendPolicy = EventRequest.SUSPEND_ALL;
        boolean enter = true;
        boolean exit = true;
        while (arguments.hasMoreTokens()) {
            String peek = arguments.peek();
            if (peek.equals("go")) {
                suspendPolicy = EventRequest.SUSPEND_NONE;
            } else if (peek.equals("thread")) {
                suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            } else if (peek.equals("enter")) {
                exit = false;
            } else if (peek.equals("exit")) {
                enter = false;
            } else {
                break;
            }
            // Remove the argument we just processed.
            arguments.nextToken();
        }

        String cfilter = null;
        String tfilter = null;
        while (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            if (token.startsWith("c:")) {
                cfilter = token.substring(2);
            } else if (token.startsWith("t:")) {
                tfilter = token.substring(2);
            } else {
                throw new CommandException(NbBundle.getMessage(
                        MethodTraceCommand.class, "ERR_trace_UnknownFilter", token));
            }
        }

        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        Breakpoint bp = bf.createTraceBreakpoint(cfilter, tfilter, enter, exit);
        bp.setEnabled(false);
        bp.setSuspendPolicy(suspendPolicy);
        bp.setEnabled(true);
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.addBreakpoint(bp);
        writer.println(NbBundle.getMessage(MethodTraceCommand.class,
                "CTL_trace_Added"));
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }
}
