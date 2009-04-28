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
import com.bluemarsh.jswat.core.breakpoint.AmbiguousClassSpecException;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.AmbiguousMethodException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.request.EventRequest;
import java.io.PrintWriter;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Sets a breakpoint at the specified location.
 *
 * @author Nathan Fiedler
 */
public class BreakpointSetCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "break";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        DebuggingContext dbgctx = context.getDebuggingContext();
        PrintWriter writer = context.getWriter();
        BreakpointManager brkman = BreakpointProvider.getBreakpointManager(session);

        if (!arguments.hasMoreTokens()) {
            Iterator<Breakpoint> iter = brkman.getDefaultGroup().breakpoints(true);
            while (iter.hasNext()) {
                Breakpoint bp = iter.next();
                Integer n = (Integer) bp.getProperty(Breakpoint.PROP_NUMBER);
                if (n == null) {
                    n = -1;
                }
                writer.format("[%d] %s\n", n, bp.getDescription());
            }
            return;
        }

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
            Breakpoint bp = brkfac.createBreakpoint(spec, dbgctx);
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
            brkman.addBreakpoint(bp);
            writer.println(NbBundle.getMessage(BreakpointSetCommand.class,
                    "CTL_Break_Added"));
        } catch (AbsentInformationException aie) {
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_AbsentInformation", spec), aie);
        } catch (AmbiguousClassSpecException acse) {
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_AmbiguousClass", spec), acse);
        } catch (AmbiguousMethodException ame) {
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_AmbiguousMethod", spec), ame);
        } catch (MalformedClassNameException mcne) {
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_MalformedClass", spec), mcne);
        } catch (NumberFormatException nfe) {
            // This must come before IllegalArgumentException.
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_InvalidNumber", spec), nfe);
        } catch (IllegalArgumentException iae) {
            // User gave us something screwy.
            throw new CommandException(iae.toString(), iae);
        } catch (MalformedMemberNameException mmne) {
            throw new CommandException(
                NbBundle.getMessage(BreakpointSetCommand.class,
                "ERR_MalformedMethod", spec), mmne);
        }
    }
}
