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
 * FILE:        stdinCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/12/01        Initial version
 *      nf      09/03/01        Accept no args and test remote better
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'stdin' command.
 *
 * $Id: stdinCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines the class that handles the 'stdin' command.
 *
 * @author  Nathan Fiedler
 */
public class stdinCommand extends JSwatCommand {

    /**
     * Perform the 'stdin' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        String input = null;
        if (args.hasMoreTokens()) {
            input = args.restTrim();
        }
        if (session.isActive()) {
            VirtualMachine vm = session.getVM();
            if (vm.process() == null) {
                out.writeln(Bundle.getString("stdin.remoteDebuggee"));
            } else {
                OutputStream os = vm.process().getOutputStream();
                try {
                    if (input != null) {
                        os.write(input.getBytes());
                    }
                    // Need to send line feed and flush stream.
                    os.write('\n');
                    os.flush();
                } catch (IOException ioe) {
                    out.writeln(ioe.toString());
                }
            }
        } else {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform
} // stdinCommand
