/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        optionsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/12/01        Initial version
 *      nf      12/15/01        Fixed bug 229
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'options' command.
 *
 * $Id: optionsCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import java.io.IOException;

/**
 * Defines the class that handles the 'options' command.
 *
 * @author  Nathan Fiedler
 */
public class optionsCommand extends JSwatCommand {

    /**
     * Perform the 'options' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (args.hasMoreTokens()) {
            // User is likely trying to set an option
            if (args.countTokens() != 2) {
                missingArgs(out);
            } else {
                // Get the option name and new value.
                String optname = args.nextToken();
                String optval = args.nextToken();
                // Set the option's new value.
                JConfigure config = swat.getJConfigure();
                if (config.getProperty(optname) == null) {
                    out.writeln(Bundle.getString("options.unknownOption"));
                    return;
                }
                config.setProperty(optname, optval);
                // Save the preferences back to disk.
                try {
                    config.storeSettings(config.getFilename());
                } catch (IOException ioe) {
                    out.writeln(Bundle.getString("options.failedSave"));
                    out.writeln(ioe.toString());
                }
                out.writeln(Bundle.getString("options.set"));
            }
        } else {
            // Show the user the list of options.
            JConfigure config = swat.getJConfigure();
            String options = config.listProperties(" = ", " ; ", "\n");
            out.writeln(Bundle.getString("options.listOfOptions") +
                        '\n' + options);
        }
    } // perform
} // optionsCommand
