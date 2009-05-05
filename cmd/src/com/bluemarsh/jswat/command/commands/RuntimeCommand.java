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
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import java.io.PrintWriter;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Provides means for managing the Java runtime environments.
 *
 * @author Nathan Fiedler
 */
public class RuntimeCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "runtime";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session session = context.getSession();
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();

        if (arguments.hasMoreTokens()) {
            String cmd = arguments.nextToken();
            if (cmd.equals("add")) {
                if (arguments.hasMoreTokens()) {
                    String path = arguments.nextToken();
                    RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
                    String id = rm.generateIdentifier();
                    try {
                        JavaRuntime jr = rf.createRuntime(path, id);
                        if (arguments.hasMoreTokens()) {
                            String exec = arguments.nextToken();
                            jr.setExec(exec);
                        }
                        rm.add(jr);
                    } catch (IllegalArgumentException iae) {
                        throw new CommandException(NbBundle.getMessage(
                                RuntimeCommand.class, "ERR_runtime_Invalid"),
                                iae);
                    }
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("del")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    JavaRuntime jr = rm.findById(id);
                    if (jr == null) {
                        throw new CommandException(NbBundle.getMessage(
                                RuntimeCommand.class, "ERR_runtime_Unknown", id));
                    }
                    rm.remove(jr);
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("use")) {
                if (arguments.hasMoreTokens()) {
                    String id = arguments.nextToken();
                    JavaRuntime jr = rm.findById(id);
                    if (jr == null) {
                        throw new CommandException(NbBundle.getMessage(
                                RuntimeCommand.class, "ERR_runtime_Unknown", id));
                    }
                    session.setProperty(Session.PROP_RUNTIME_ID, id);
                } else {
                    throw new MissingArgumentsException();
                }
            } else if (cmd.equals("opt")) {
                String opts = "";
                if (arguments.hasMoreTokens()) {
                    arguments.returnAsIs(true);
                    opts = arguments.rest();
                }
                session.setProperty(Session.PROP_JAVA_PARAMS, opts);
            } else {
                throw new CommandException(NbBundle.getMessage(
                        RuntimeCommand.class, "ERR_runtime_Subcommand", cmd));
            }
        } else {
            String currentId = session.getProperty(Session.PROP_RUNTIME_ID);
            Iterator<JavaRuntime> iter = rm.iterateRuntimes();
            StringBuilder sb = new StringBuilder();
            // Current and non-current runtime string formats.
            String formatC = "* [%s] %s (%s, %s)\n";
            String formatNC = "  [%s] %s (%s, %s)\n";
            while (iter.hasNext()) {
                JavaRuntime jr = iter.next();
                String id = jr.getIdentifier();
                String format;
                if (id.equals(currentId)) {
                    format = formatC;
                } else {
                    format = formatNC;
                }
                sb.append(String.format(format, id, jr.getName(), jr.getBase(),
                        jr.getExec()));
            }
            writer.print(sb.toString());
        }
    }
}
