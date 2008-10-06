/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      JSwat Commands
 * FILE:        aproposCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/11/02        Initial version
 *      nf      01/21/02        Fixed bug 390
 *      nf      07/13/02        Implemented RFE 430
 *
 * $Id: aproposCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the class that handles the 'apropos' command.
 *
 * @author  Nathan Fiedler
 */
public class aproposCommand extends JSwatCommand {

    /**
     * Perform the 'apropos' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Get the list of known commands.
        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);
        String[] commands = cmdman.getCommandNames();

        // Take the rest of the arguments as a regular expression.
        String regex = args.rest();
        regex = regex.toLowerCase();

        // Grep the command descriptions for something with 'regex'.
        boolean success = false;
        Pattern patt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        for (int ii = 0; ii < commands.length; ii++) {
            JSwatCommand command = cmdman.getCommand(commands[ii]);
            String desc = command.description();
            Matcher matcher = patt.matcher(desc);
            if (matcher.find()) {
                // Found a matching description.
                out.write(command.getCommandName());
                out.write(" - ");
                out.writeln(desc);
                success = true;
            }
        }

        if (!success) {
            // Yes, treat this as an error case.
            throw new CommandException(Bundle.getString("apropos.notfound"));
        }
    } // perform
} // aproposCommand
