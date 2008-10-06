/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: HelpCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
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
                Command command = parser.getCommand(token);
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
