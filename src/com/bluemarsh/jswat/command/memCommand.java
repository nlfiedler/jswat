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
 * $Id: memCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import java.text.DecimalFormat;

/**
 * Defines the class that handles the 'mem' command.
 *
 * @author  Nathan Fiedler
 */
public class memCommand extends JSwatCommand {

    /**
     * Perform the 'mem' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        boolean dogc = true;
        if (args.hasMoreTokens()) {
            String arg = args.nextToken();
            if (arg.equals("+gc")) {
                dogc = true;
            } else if (arg.equals("-gc")) {
                dogc = false;
            } else {
                out.writeln("Unknown option to 'mem' command.");
                return;
            }
        }

        if (dogc) {
            out.writeln("Collecting garbage...");
            System.gc();
        }
        Runtime runtime = Runtime.getRuntime();
        DecimalFormat format = new DecimalFormat("#,##0.00");

        StringBuffer buf = new StringBuffer();
        buf.append("Total memory: ");
        double value = runtime.totalMemory() / 1048576.0;
        buf.append(format.format(value));
        buf.append(" MB");
        out.writeln(buf.toString());

        buf = new StringBuffer();
        buf.append("Free memory: ");
        value = runtime.freeMemory() / 1048576.0;
        buf.append(format.format(value));
        buf.append(" MB");
        out.writeln(buf.toString());

        buf = new StringBuffer();
        buf.append("Maximum memory: ");
        value = runtime.maxMemory() / 1048576.0;
        buf.append(format.format(value));
        buf.append(" MB");
        out.writeln(buf.toString());
    } // perform
} // memCommand
