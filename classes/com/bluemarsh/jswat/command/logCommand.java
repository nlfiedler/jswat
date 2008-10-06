/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: logCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import java.util.logging.Logger;

/**
 * Defines the class that handles the 'log' command.
 *
 * @author  Nathan Fiedler
 */
public class logCommand extends JSwatCommand {
    
    /**
     * Perform the 'log' command.
     *
     * @param  session  debugging session on which to operate.
     * @param  args     tokenized string of command arguments.
     * @param  out      output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (args.countTokens() < 2) {
            throw new MissingArgumentsException();
        }

        // Syntax: log <category> <msg text>
        String keyword = args.nextToken();
        for (int ii = 0; ii < loggingCommand.CATEGORIES.length; ii += 2) {
            String category = loggingCommand.CATEGORIES[ii + 1];
            if (keyword.equals(loggingCommand.CATEGORIES[ii])) {
                Logger.getLogger(category).info(args.rest());
                return;
            }
        }
        throw new CommandException(Bundle.getString(
            "logging.error.cat.unknown") + keyword);
    } // perform
} // logCommand
