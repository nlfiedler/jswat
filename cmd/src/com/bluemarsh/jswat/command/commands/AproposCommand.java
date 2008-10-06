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
 * $Id: AproposCommand.java 15 2007-06-03 00:01:17Z nfiedler $
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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds commands that have descriptions containing matching an expression.
 *
 * @author Nathan Fiedler
 */
public class AproposCommand extends AbstractCommand {

    public String getName() {
        return "apropos";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        // Get the list of known commands.
        CommandParser parser = context.getParser();
        PrintWriter writer = context.getWriter();
        Iterator<Command> commands = parser.getCommands();

        // Take the rest of the arguments as a regular expression.
        String regex = arguments.rest();
        regex = regex.toLowerCase();

        // Grep the command descriptions for something with 'regex'.
        Pattern patt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        while (commands.hasNext()) {
            Command command = commands.next();
            String desc = command.getDescription();
            Matcher matcher = patt.matcher(desc);
            if (matcher.find()) {
                writer.print(command.getName());
                writer.print(" - ");
                writer.println(desc);
            }
        }
    }

    public boolean requiresArguments() {
        return true;
    }
}
