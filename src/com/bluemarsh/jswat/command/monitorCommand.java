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
 * FILE:        monitorCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/15/02        Initial version
 *
 * $Id: monitorCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MonitorManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.CommandMonitor;
import java.util.Iterator;

/**
 * Defines the class that handles the 'monitor' command.
 *
 * @author  Nathan Fiedler
 */
public class monitorCommand extends JSwatCommand {

    /**
     * Perform the 'monitor' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        MonitorManager monman = (MonitorManager)
            session.getManager(MonitorManager.class);
        if (args.hasMoreTokens()) {
            // Grab rest of string as monitor command.
            // Be sure to preserve the quotes and escapes.
            args.returnAsIs(true);
            String monitorStr = args.rest();
            CommandMonitor monitor = new CommandMonitor(monitorStr);
            monman.addMonitor(monitor);
            out.writeln(Bundle.getString("monitor.added"));
        } else {
            Iterator iter = monman.monitors();
            if (iter.hasNext()) {
                StringBuffer buf = new StringBuffer();
                buf.append(Bundle.getString("monitor.list"));
                buf.append('\n');
                while (iter.hasNext()) {
                    Monitor mon = (Monitor) iter.next();
                    int num = mon.getNumber();
                    buf.append(num);
                    buf.append(": ");
                    buf.append(mon.description());
                    buf.append('\n');
                }
                int len = buf.length();
                buf.delete(len - 1, len);
                out.writeln(buf.toString());
            } else {
                out.writeln(Bundle.getString("monitor.none"));
            }
        }
    } // perform
} // monitorCommand
