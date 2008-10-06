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
 * $Id: rmsessionCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.SessionSettings;
import java.util.prefs.BackingStoreException;

/**
 * Defines the class that handles the 'rmsession' command.
 *
 * @author  Nathan Fiedler
 */
public class rmsessionCommand extends JSwatCommand {

    /**
     * Perform the 'rmsession' command.
     *
     * @param  session  debugging session on which to operate.
     * @param  args     tokenized string of command arguments.
     * @param  out      output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (args.hasMoreTokens()) {
            try {
                SessionSettings.deleteSettings(args.nextToken());
            } catch (BackingStoreException bse) {
                throw new CommandException(bse);
            }
        } else {
            String[] names = null;
            String current = SessionSettings.currentSettings();
            try {
                names = SessionSettings.getAvailableSettings();
            } catch (BackingStoreException bse) {
                throw new CommandException(bse);
            }
            for (int ii = 0; ii < names.length; ii++) {
                if (current.equals(names[ii])) {
                    out.write("* ");
                } else {
                    out.write("  ");
                }
                out.writeln(names[ii]);
            }
        }
    } // perform
} // rmsessionCommand
