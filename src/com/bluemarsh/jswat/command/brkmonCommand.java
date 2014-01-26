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
 * FILE:        brkmonCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/08/02        Initial version
 *      nf      06/15/02        Renamed to brkmon
 *
 * $Id: brkmonCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.CommandMonitor;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Defines the class that handles the 'brkmon' command.
 *
 * @author  Nathan Fiedler
 */
public class brkmonCommand extends JSwatCommand {

    /**
     * Perform the 'brkmon' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        String action = null;
        String monitorStr = null;
        String brknumStr = null;

        try {
            action = args.nextToken();
            monitorStr = args.nextToken();
            brknumStr = args.nextToken();
        } catch (NoSuchElementException nsee) {
            throw new MissingArgumentsException();
        }

        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            throw new CommandException(Bundle.getString("brkmon.badbrk"));
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            throw new CommandException(Bundle.getString("brkmon.nobrk"));
        }

        if (action.equals("add")) {
            // Adding a new monitor.
            CommandMonitor monitor = new CommandMonitor(monitorStr);
            brk.addMonitor(monitor);
            out.writeln(Bundle.getString("brkmon.added"));

        } else if (action.equals("del")) {
            // Removing an existing monitor.
            ListIterator iter = brk.monitors();
            boolean found = false;
            while (iter.hasNext() && !found) {
                Monitor monitor = (Monitor) iter.next();
                if (monitor instanceof CommandMonitor) {
                    // At present we only deal with command monitors.
                    CommandMonitor cm = (CommandMonitor) monitor;
                    String cmnd = cm.getCommand();
                    if (!cmnd.equals(monitorStr)) {
                        continue;
                    }
                    // It's a match, remove that monitor.
                    iter.remove();
                    found = true;
                }
            }

            if (!found) {
                throw new CommandException(Bundle.getString("brkmon.nomon"));
            } else {
                out.writeln(Bundle.getString("brkmon.removed"));
            }
        } else {
            throw new CommandException(Bundle.getString("brkmon.badaction"));
        }
    } // perform
} // brkmonCommand
