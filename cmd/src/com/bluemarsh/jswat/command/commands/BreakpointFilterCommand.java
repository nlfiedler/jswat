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
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import java.io.PrintWriter;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Manages class and thread filters on breakpoints.
 *
 * @author Nathan Fiedler
 */
public class BreakpointFilterCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);

        String cmd = arguments.nextToken();
        Breakpoint bp = null;
        if (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            try {
                int n = Integer.parseInt(token);
                Iterator<Breakpoint> iter = bm.getDefaultGroup().breakpoints(true);
                while (iter.hasNext()) {
                    Breakpoint b = iter.next();
                    Integer bn = (Integer) b.getProperty(Breakpoint.PROP_NUMBER);
                    if (bn == n) {
                        bp = b;
                        break;
                    }
                }
            } catch (NumberFormatException nfe) {
                throw new CommandException(NbBundle.getMessage(
                        BreakpointFilterCommand.class, "ERR_InvalidNumber", token), nfe);
            }
        } else {
            throw new MissingArgumentsException();
        }
        if (bp == null) {
            throw new CommandException(NbBundle.getMessage(
                    BreakpointFilterCommand.class, "ERR_filter_NotFound"));
        }

        if (cmd.equals("list")) {
            String cl = bp.getClassFilter();
            if (cl == null) {
                cl = "<>";
            }
            String th = bp.getThreadFilter();
            if (th == null) {
                th = "<>";
            }
            String msg = NbBundle.getMessage(BreakpointFilterCommand.class,
                    "CTL_filter_Filters", cl, th);
            writer.println(msg);
        } else if (cmd.equals("add")) {
            if (arguments.hasMoreTokens()) {
                // Add the given expression condition to the breakpoint.
                String expr = arguments.nextToken();
                if (expr.startsWith("c:")) {
                    if (bp.canFilterClass()) {
                        expr = expr.substring(2);
                        bp.setClassFilter(expr);
                    } else {
                        throw new CommandException(NbBundle.getMessage(
                                BreakpointFilterCommand.class, "ERR_filter_NoFilter"));
                    }
                } else if (expr.startsWith("t:")) {
                    if (bp.canFilterThread()) {
                        expr = expr.substring(2);
                        bp.setThreadFilter(expr);
                    } else {
                        throw new CommandException(NbBundle.getMessage(
                                BreakpointFilterCommand.class, "ERR_filter_NoFilter"));
                    }
                } else {
                    throw new CommandException(NbBundle.getMessage(
                            BreakpointFilterCommand.class, "ERR_filter_InvalidType", expr));
                }
            } else {
                throw new MissingArgumentsException();
            }
            writer.println(NbBundle.getMessage(BreakpointFilterCommand.class,
                    "CTL_filter_Added"));
        } else if (cmd.equals("del")) {
            if (arguments.hasMoreTokens()) {
                // Remove the filter of the corresponding type.
                String type = arguments.nextToken();
                if (type.equals("class")) {
                    if (bp.canFilterClass()) {
                        bp.setClassFilter(null);
                    }
                } else if (type.startsWith("thread")) {
                    if (bp.canFilterThread()) {
                        bp.setThreadFilter(null);
                    }
                } else {
                    throw new CommandException(NbBundle.getMessage(
                            BreakpointFilterCommand.class, "ERR_filter_InvalidType", type));
                }
            } else {
                // Remove all filters from the breakpoint.
                if (bp.canFilterClass()) {
                    bp.setClassFilter(null);
                }
                if (bp.canFilterThread()) {
                    bp.setThreadFilter(null);
                }
            }
            writer.println(NbBundle.getMessage(BreakpointFilterCommand.class,
                    "CTL_filter_Removed"));
        } else {
            throw new CommandException(NbBundle.getMessage(
                    BreakpointFilterCommand.class, "ERR_filter_UnknownCmd", cmd));
        }
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }
}
