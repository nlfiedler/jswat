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
