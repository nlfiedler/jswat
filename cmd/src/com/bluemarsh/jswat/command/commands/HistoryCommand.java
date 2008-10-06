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
 * $Id: HistoryCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Displays the command history, and permits setting the history size.
 *
 * @author Nathan Fiedler
 */
public class HistoryCommand extends AbstractCommand {

    public String getName() {
        return "history";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        CommandParser parser = context.getParser();
        if (arguments.hasMoreTokens()) {
            String token = arguments.nextToken();
            try {
                int size = Integer.parseInt(token);
                parser.setHistorySize(size);
            } catch (NumberFormatException nfe) {
                throw new CommandException(
                    NbBundle.getMessage(getClass(), "ERR_History_InvalidSize"));
            } catch (IllegalArgumentException iae) {
                throw new CommandException(
                    NbBundle.getMessage(getClass(), "ERR_History_InvalidSize"));
            }
        } else {
            Iterator<String> history = parser.getHistory(false);
            StringBuilder sb = new StringBuilder(512);
            int position = 1;
            while (history.hasNext()) {
                String entry = history.next();
                sb.append(position);
                sb.append(". ");
                sb.append(entry);
                sb.append('\n');
                position++;
            }
            context.getWriter().print(sb.toString());
        }
    }
}
