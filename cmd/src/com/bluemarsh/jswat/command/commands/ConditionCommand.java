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
import com.bluemarsh.jswat.core.breakpoint.Condition;
import com.bluemarsh.jswat.core.breakpoint.ExpressionCondition;
import com.bluemarsh.jswat.core.session.Session;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ListIterator;
import org.openide.util.NbBundle;

/**
 * Manages conditions on breakpoints.
 *
 * @author Nathan Fiedler
 */
public class ConditionCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "condition";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

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
                        ConditionCommand.class, "ERR_InvalidNumber", token), nfe);
            }
        } else {
            throw new MissingArgumentsException();
        }
        if (bp == null) {
            throw new CommandException(NbBundle.getMessage(
                    ConditionCommand.class, "ERR_condition_NotFound"));
        }

        if (cmd.equals("list")) {
            ListIterator<Condition> iter = bp.conditions();
            while (iter.hasNext()) {
                Condition cond = iter.next();
                if (cond.isVisible()) {
                    writer.println(cond.describe());
                }
            }
        } else if (cmd.equals("add")) {
            if (arguments.hasMoreTokens()) {
                // Add the given expression condition to the breakpoint.
                arguments.returnAsIs(true);
                String expr = arguments.rest().trim();
                Condition cond = bf.createCondition(expr);
                bp.addCondition(cond);
            } else {
                throw new MissingArgumentsException();
            }
            writer.println(NbBundle.getMessage(ConditionCommand.class,
                    "CTL_condition_Added"));
        } else if (cmd.equals("del")) {
            if (arguments.hasMoreTokens()) {
                // Find the matching condition and remove it.
                arguments.returnAsIs(true);
                String expr = arguments.rest().trim();
                ListIterator<Condition> iter = bp.conditions();
                while (iter.hasNext()) {
                    Condition cond = iter.next();
                    if (cond instanceof ExpressionCondition) {
                        ExpressionCondition ec = (ExpressionCondition) cond;
                        if (ec.getExpression().equals(expr)) {
                            iter.remove();
                            break;
                        }
                    }
                }
            } else {
                // Remove all conditions from the breakpoint.
                ListIterator<Condition> iter = bp.conditions();
                while (iter.hasNext()) {
                    iter.next();
                    iter.remove();
                }
            }
            writer.println(NbBundle.getMessage(ConditionCommand.class,
                    "CTL_condition_Removed"));
        } else {
            throw new CommandException(NbBundle.getMessage(
                    ConditionCommand.class, "ERR_condition_UnknownCmd", cmd));
        }
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }
}
