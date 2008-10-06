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
 * FILE:        propsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/11/02        Initial version
 *
 * $Id: propsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;

/**
 * Defines the class that handles the 'props' command.
 *
 * @author  Nathan Fiedler
 */
public class propsCommand extends JSwatCommand {

    /**
     * Perform the 'props' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (args.hasMoreTokens()) {
            // User is likely trying to set a property.
            if (args.countTokens() != 2) {
                throw new MissingArgumentsException();
            } else {
                // Set the property's new value.
                String name = args.nextToken();
                String value = args.nextToken();
                if (value.equals("NULL")) {
                    session.setProperty(name, null);
                } else {
                    session.setProperty(name, value);
                }
            }

        } else {
            // Show the user the list of session properties.
            out.writeln(Bundle.getString("props.listOfProperties"));
            String[] props = session.getPropertyKeys();
            if (props == null) {
                throw new CommandException(
                    Bundle.getString("props.sessionError"));
            }
            for (int ii = 0; ii < props.length; ii++) {
                String name = props[ii];
                String value = session.getProperty(name);
                out.writeln(name + " = " + value);
            }
        }
    } // perform
} // propsCommand
