/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        aliasCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/31/99        Initial version
 *      nf      09/03/01        Fixing 220 among other problems
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'alias' command.
 *
 * $Id: aliasCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;

/**
 * Defines the class that handles the 'alias' command.
 *
 * @author  Nathan Fiedler
 */
public class aliasCommand extends JSwatCommand {

    /**
     * Perform the 'alias' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);
        if (args.hasMoreTokens()) {
            String aliasName = args.nextToken();
            if (args.hasMoreTokens()) {
                // Grab rest of string as alias value.
                String alias = args.restTrim();
                // Add the command alias to the list.
                cmdman.createAlias(aliasName, alias);
                out.writeln(Bundle.getString("alias.defined") + ' ' +
                            aliasName);
            } else {
                // One argument, show the alias definition.
                String alias = cmdman.getAlias(aliasName);
                if (alias == null) {
                    out.writeln(Bundle.getString("alias.undefined") + ' ' +
                                aliasName);
                } else {
                    out.writeln("alias " + aliasName + " " + alias);
                }
            }
        } else {
            // No arguments, show the defined aliases.
            cmdman.listAliases();
        }
    } // perform
} // aliasCommand
