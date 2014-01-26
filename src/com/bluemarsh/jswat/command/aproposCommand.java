/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'apropos' command.
 *
 * $Id: aproposCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.StringTokenizer;

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
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        // Get the list of known commands.
        String commandList = com.bluemarsh.jswat.Bundle.getString("commands");
        if (commandList == null || commandList.equals("")) {
            out.writeln(Bundle.getString("apropos.errorCommandList"));
            return;
        }
        String[] commands = StringUtils.tokenize(commandList);

        // Take the rest of the arguments as a single phrase.
        String phrase = args.restTrim();
        phrase = phrase.toLowerCase();

        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);

        // Grep the command descriptions for something with 'phrase'.
        boolean success = false;
        for (int ii = 0; ii < commands.length; ii++) {
            JSwatCommand command = cmdman.getCommand(commands[ii]);
            String desc = command.description();
            desc = desc.toLowerCase();
            if (desc.indexOf(phrase) > -1) {
                // Found a matching description.
                out.write(command.getCommandName());
                out.write(" - ");
                out.writeln(desc);
                success = true;
            }
        }

        if (!success) {
            out.writeln(Bundle.getString("apropos.notfound"));
        }
    } // perform
} // aproposCommand
