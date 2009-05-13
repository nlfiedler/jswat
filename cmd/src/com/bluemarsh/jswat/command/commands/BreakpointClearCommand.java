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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Removes the specified breakpoints.
 *
 * @author Nathan Fiedler
 */
public class BreakpointClearCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
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

        String spec = arguments.peek();
        if (spec.equals("all")) {
            int removed = 0;
            // Collect the matching breakpoints in a list, otherwise
            // we risk a concurrent modification exception.
            List<Breakpoint> killring = new ArrayList<Breakpoint>();
            Iterator<Breakpoint> iter = brkman.getDefaultGroup().breakpoints(true);
            while (iter.hasNext()) {
                killring.add(iter.next());
            }
            for (Breakpoint bp : killring) {
                brkman.removeBreakpoint(bp);
                removed++;
            }
            writer.println(NbBundle.getMessage(BreakpointClearCommand.class,
                    "CTL_Clear_Removed", removed));
        } else {

            try {
                // Collect the matching breakpoints in a list, otherwise
                // we risk a concurrent modification exception.
                List<Breakpoint> killring = new ArrayList<Breakpoint>();
                while (arguments.hasMoreTokens()) {
                    spec = arguments.nextToken();
                    int n = Integer.parseInt(spec);
                    Iterator<Breakpoint> iter = brkman.getDefaultGroup().breakpoints(true);
                    while (iter.hasNext()) {
                        Breakpoint bp = iter.next();
                        Integer bn = (Integer) bp.getProperty(Breakpoint.PROP_NUMBER);
                        if (bn == n) {
                            killring.add(bp);
                        }
                    }
                }
                if (killring.size() > 0) {
                    for (Breakpoint bp : killring) {
                        brkman.removeBreakpoint(bp);
                    }
                    writer.println(NbBundle.getMessage(BreakpointClearCommand.class,
                            "CTL_Clear_Removed", killring.size()));
                } else {
                    throw new CommandException(NbBundle.getMessage(
                            BreakpointClearCommand.class, "ERR_Clear_NotFound"));
                }
            } catch (NumberFormatException nfe) {
                // This must come before IllegalArgumentException.
                throw new CommandException(
                        NbBundle.getMessage(BreakpointClearCommand.class,
                        "ERR_InvalidNumber", spec), nfe);
            }
        }
    }
}
