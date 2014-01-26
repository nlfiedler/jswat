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
 * FILE:        unmonitorCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/15/02        Initial version
 *
 * $Id: unmonitorCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MonitorManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Monitor;
import java.util.ListIterator;

/**
 * Defines the class that handles the 'unmonitor' command.
 *
 * @author  Nathan Fiedler
 */
public class unmonitorCommand extends JSwatCommand {

    /**
     * Perform the 'unmonitor' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        MonitorManager monman = (MonitorManager)
            session.getManager(MonitorManager.class);
        if (args.peek().equals("all")) {
            args.nextToken();
            ListIterator liter = monman.monitors();
            while (liter.hasNext()) {
                liter.next();
                liter.remove();
            }

        } else {
            while (args.hasMoreTokens()) {
                String arg = args.nextToken();
                int monnum = -1;
                try {
                    monnum = Integer.parseInt(arg);
                } catch (NumberFormatException nfe) {
                    throw new CommandException(
                        Bundle.getString("unmonitor.badnum") + ' ' + arg);
                }

                if (monnum <= 0) {
                    throw new CommandException(
                        Bundle.getString("unmonitor.badnum"));
                } else {
                    Monitor monitor = monman.getMonitor(monnum);
                    if (monitor == null) {
                        throw new CommandException(
                            Bundle.getString("unmonitor.nomon") + ' ' + arg);
                    } else {
                        monman.removeMonitor(monitor);
                    }
                }
            }
        }

        out.writeln(Bundle.getString("unmonitor.removed"));
    } // perform
} // unmonitorCommand
