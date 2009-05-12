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
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.request.EventRequest;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Sets a breakpoint to stop when a certain exception is thrown.
 *
 * @author Nathan Fiedler
 */
public class CatchExceptionCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "catch";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        BreakpointManager brkman = BreakpointProvider.getBreakpointManager(session);

        // See if user provided the go or thread option.
        String peek = arguments.peek();
        int suspendPolicy = EventRequest.SUSPEND_ALL;
        if (peek.equals("go")) {
            suspendPolicy = EventRequest.SUSPEND_NONE;
            arguments.nextToken();
        } else if (peek.equals("thread")) {
            suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            arguments.nextToken();
        }

        BreakpointFactory brkfac = BreakpointProvider.getBreakpointFactory();
        String spec = arguments.rest();
        try {
            Breakpoint bp = brkfac.createExceptionBreakpoint(spec, true, true);
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            brkman.addBreakpoint(bp);
            writer.println(NbBundle.getMessage(CatchExceptionCommand.class,
                    "CTL_catch_Added"));
        } catch (MalformedClassNameException mcne) {
            throw new CommandException(
                NbBundle.getMessage(CatchExceptionCommand.class,
                "ERR_MalformedClass", spec), mcne);
        }
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }
}
