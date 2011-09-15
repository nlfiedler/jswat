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
import com.bluemarsh.jswat.command.AmbiguousMatchException;
import com.bluemarsh.jswat.command.Command;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays help concerning the available commands.
 *
 * @author Nathan Fiedler
 */
public class HelpCommand extends AbstractCommand {

    public String getName() {
        return "help";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        if (!arguments.hasMoreTokens()) {
            writer.println(NbBundle.getMessage(getClass(), "help_Help"));
        } else {
            CommandParser parser = context.getParser();
            String token = arguments.nextToken();
            if (token.equals("commands")) {
                StringBuilder sb = new StringBuilder();
                Iterator<Command> commands = parser.getCommands();
                List<String> names = new ArrayList<String>(20);
                // We assume there will be some commands, of course.
                while (commands.hasNext()) {
                    Command command = commands.next();
                    names.add(command.getName());
                }
                Collections.sort(names);
                Iterator<String> nameIter = names.iterator();
                while (nameIter.hasNext()) {
                    String name = nameIter.next();
                    sb.append(name);
                    if (name.length() >= 8) {
                        sb.append('\t');
                    } else {
                        sb.append("\t\t");
                    }
                    Command command = parser.getCommand(name);
                    sb.append(command.getDescription());
                    sb.append('\n');
                }
                writer.print(sb.toString());
            } else {
                Command command = null;
                try {
                     command = parser.findCommand(token);
                } catch (AmbiguousMatchException ame) {
                    throw new CommandException(ame.getMessage());
                }
                if (command != null) {
                    writer.println(command.getHelp());
                } else {
                    writer.println(NbBundle.getMessage(getClass(),
                            "ERR_Help_CommandUndefined", token));
                }
            }
        }
    }
}
