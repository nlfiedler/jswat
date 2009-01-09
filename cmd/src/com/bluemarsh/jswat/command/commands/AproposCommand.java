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
