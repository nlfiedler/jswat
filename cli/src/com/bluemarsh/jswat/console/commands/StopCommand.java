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

package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.command.commands.BreakpointSetCommand;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Provides {@code stop [at|in]} as an alias for the {@code break}
 * command, for jdb compatibility.
 *
 * @author Steve Yegge
 */
public class StopCommand extends AbstractCommand {

    /**
     * Match "stop [in|at]" followed by optional other args.
     * Match group 1 will be the remaining args.
     */
    private static final Pattern STOP_COMMAND_PREFIX =
            Pattern.compile("^(at|in)\\s+(.*)");

    /**
     * Match "{class}.{method}[(args)]", extracting the class qname, method name
     * and optional parenthesized arg type list as separate groups.
     */
    private static final Pattern METHOD_BREAK =
            Pattern.compile("^(.+?)\\.([a-zA-Z0-9$]+)(\\(.+\\))?");

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {
        // Discard any initial "at" or "in".
        Matcher stopMatcher = STOP_COMMAND_PREFIX.matcher(arguments.rest());
        if (!stopMatcher.matches()) {
            throw new CommandException(NbBundle.getMessage(StopCommand.class,
                    "ERR_stop_syntax"));
        }

        // Default to whatever is after the "at" keyword.
        String jswatArgString = stopMatcher.group(2);

        // JDB's method-break uses the syntax <class>.method[(arg-types)].
        // JSwat's method-break uses the syntax <class>:method[(arg-types)].
        // Convert from the former to the latter if necessary.
        if ("in".equals(stopMatcher.group(1))) {
            Matcher methodMatcher = METHOD_BREAK.matcher(jswatArgString);
            if (methodMatcher.matches()) {
                String className = methodMatcher.group(1);
                String methodName = methodMatcher.group(2);
                String argTypes = null;
                if (methodMatcher.groupCount() == 3) {
                    argTypes = methodMatcher.group(3);
                }
                if (argTypes == null) {
                    argTypes = "";
                }
                jswatArgString = className + ":" + methodName + argTypes;
            }
        }

        // Delegate to the normal "break" command handler.
        new BreakpointSetCommand().perform(context,
                new CommandArguments(jswatArgString));
    }
}
