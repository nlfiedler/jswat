/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        historyCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/05/99        Initial version
 *      nf      05/24/02        Implemented RFE 486
 *
 * $Id: historyCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;

/**
 * Defines the class that handles the 'history' command.
 *
 * @author  Nathan Fiedler
 */
public class historyCommand extends JSwatCommand {

    /**
     * Perform the 'history' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);

        if (args.hasMoreTokens()) {
            String arg = args.nextToken();
            try {
                int size = Integer.parseInt(arg);
                cmdman.setHistorySize(size);
                out.writeln(Bundle.getString("history.sizeSet"));
            } catch (NumberFormatException nfe) {
                throw new CommandException(
                    Bundle.getString("history.invalidSize"));
            } catch (IllegalArgumentException iae) {
                throw new CommandException(
                    Bundle.getString("history.invalidRange"));
            }
        } else {
            cmdman.displayHistory();
        }
    } // perform
} // historyCommand
