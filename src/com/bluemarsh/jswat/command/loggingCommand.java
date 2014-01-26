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
 * $Id: loggingCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.logging.Logging;

/**
 * Defines the class that handles the 'logging' command.
 *
 * @author  Nathan Fiedler
 */
public class loggingCommand extends JSwatCommand {
    protected static final String[] CATEGORIES = new String[] {
        "breakpoint", "com.bluemarsh.jswat.breakpoint",
        "event", "com.bluemarsh.jswat.event",
        "monitor", "com.bluemarsh.jswat.monitor",
        "session", "com.bluemarsh.jswat.Session",
        "sesslist", "com.bluemarsh.jswat.SessionListenerList",
        "view", "com.bluemarsh.jswat.view",
        "help", "com.bluemarsh.jswat.ui.viewer"
    };
    
    /**
     * Perform the 'logging' command.
     *
     * @param  session  debugging session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (args.hasMoreTokens()) {
           while (args.hasMoreTokens()) {
               String keyword = args.nextToken();
               // Does it have a + or - prefix?
               boolean disable = false;
               boolean enable = false;
               if (keyword.startsWith("+")) {
                   enable = true;
                   keyword = keyword.substring(1);
               } else if (keyword.startsWith("-")) {
                   disable = true;
                   keyword = keyword.substring(1);
               }
               boolean foundMatch = false;
               for (int ii = 0; ii < CATEGORIES.length; ii += 2) {
                   String category = CATEGORIES[ii + 1];
                   if (keyword.equals(CATEGORIES[ii])) {
                       foundMatch = true;
                       if (enable) {
                           // Explicitly enable the category.
                           Logging.enable(category);
                           out.writeln(Bundle.getString("logging.enabled"));
                       } else if (disable) {
                           // Explicitly disable the category.
                           Logging.disable(category);
                           out.writeln(Bundle.getString("logging.disabled"));
                       } else {
                           // Toggle the enabled state.
                           if (Logging.isEnabled(category)) {
                               Logging.disable(category);
                               out.writeln(Bundle.getString(
                                   "logging.disabled"));
                           } else {
                               Logging.enable(category);
                               out.writeln(Bundle.getString(
                                   "logging.enabled"));
                           }
                       }
                       break;
                   }
               }
               if (!foundMatch) {
                   throw new CommandException(Bundle.getString(
                       "logging.error.cat.unknown") + keyword);
               }
           }
        } else {
            // Display the list of logging categories and their state.
            for (int ii = 0; ii < CATEGORIES.length; ii += 2) {
                String keyword = CATEGORIES[ii];
                String category = CATEGORIES[ii + 1];
                StringBuffer buf = new StringBuffer();
                buf.append(Bundle.getString("logging.cat." + keyword));
                buf.append("  ");
                if (Logging.isEnabled(category)) {
                    buf.append(Bundle.getString("logging.on"));
                } else {
                    buf.append(Bundle.getString("logging.off"));
                }
                out.writeln(buf.toString());
            }
        }
    } // perform
} // loggingCommand
