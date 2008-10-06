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
 * FILE:        brkinfoCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/09/02        Initial version
 *      nf      07/11/02        Fixed bug 568
 *
 * $Id: brkinfoCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.Monitor;
import java.util.Iterator;

/**
 * Defines the class that handles the 'brkinfo' command.
 *
 * @author  Nathan Fiedler
 */
public class brkinfoCommand extends JSwatCommand {

    /**
     * Perform the 'brkinfo' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            // Display a list of all breakpoints.
            BreakpointManager brkman = (BreakpointManager)
                session.getManager(BreakpointManager.class);
            Iterator iter = brkman.breakpoints(true);
            if (iter.hasNext()) {
                out.writeln(Bundle.getString("brkinfo.breakpointList"));
                // For each breakpoint, display a description.
                while (iter.hasNext()) {
                    Breakpoint bp = (Breakpoint) iter.next();
                    printBreakpoint(bp, out);
                }
            } else {
                out.writeln(Bundle.getString("brkinfo.noBreakpointsDefined"));
            }
            return;
        }

        String brknumStr = args.nextToken();
        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            throw new CommandException(Bundle.getString("brkinfo.badbrk"));
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            throw new CommandException(Bundle.getString("brkinfo.nobrk"));
        }

        // Breakpoint toString()
        out.writeln(brk.toString());

        // BreakpointGroup
        BreakpointGroup brkgrp = brk.getBreakpointGroup();
        out.write(Bundle.getString("brkinfo.brkgrp"));
        out.write(" ");
        out.writeln(brkgrp.getName());

        // Breakpoint expiration, skip count, etc.
        int expires = brk.getExpireCount();
        if (expires > 0) {
            out.write(Bundle.getString("brkinfo.expires"));
            out.write(" ");
            out.writeln(Integer.toString(expires));
        }
        int skips = brk.getSkipCount();
        if (skips > 0) {
            out.write(Bundle.getString("brkinfo.skips"));
            out.write(" ");
            out.writeln(Integer.toString(skips));
        }

        // Breakpoint state
        if (brk.isEnabled()) {
            out.writeln(Bundle.getString("brkinfo.enabled"));
        } else {
            out.writeln(Bundle.getString("brkinfo.disabled"));
        }
        if (brk.isResolved()) {
            out.writeln(Bundle.getString("brkinfo.resolved"));
        } else {
            out.writeln(Bundle.getString("brkinfo.unresolved"));
        }
        if (brk.hasExpired()) {
            out.writeln(Bundle.getString("brkinfo.expired"));
        }
        if (brk.isSkipping()) {
            out.writeln(Bundle.getString("brkinfo.skipping"));
        }

        // Breakpoint filters
        String filters = brk.getClassFilters();
        if (filters != null && filters.length() > 0) {
            out.write(Bundle.getString("brkinfo.classFilters"));
            out.write(" ");
            out.writeln(filters);
        }
        filters = brk.getThreadFilters();
        if (filters != null && filters.length() > 0) {
            out.write(Bundle.getString("brkinfo.threadFilters"));
            out.write(" ");
            out.writeln(filters);
        }

        // Breakpoint conditions
        Iterator conditer = brk.conditions();
        if (conditer.hasNext()) {
            out.writeln(Bundle.getString("brkinfo.conditions"));
            while (conditer.hasNext()) {
                Condition cond = (Condition) conditer.next();
                out.writeln(cond.toString());
            }
        }

        // Breakpoint monitors
        Iterator moniter = brk.monitors();
        if (moniter.hasNext()) {
            out.writeln(Bundle.getString("brkinfo.monitors"));
            while (moniter.hasNext()) {
                Monitor mon = (Monitor) moniter.next();
                out.writeln(mon.toString());
            }
        }
    } // perform

    /**
     * Prints the breakpoint specification to the Log.
     *
     * @param  bp      Breakpoint to print.
     * @param  out     Log to print to.
     */
    protected void printBreakpoint(Breakpoint bp, Log out) {
        // Print the breakpoint number.
        StringBuffer buf = new StringBuffer(80);
        buf.append(bp.getNumber());
        buf.append(". ");

        // Print the current state of the breakpoint.
        if (!bp.isResolved()) {
            buf.append(Bundle.getString("brkinfo.unresolvedInBrackets"));
        } else if (!bp.isEnabled()) {
            buf.append(Bundle.getString("brkinfo.disabledInBrackets"));
        } else if (bp.hasExpired()) {
            buf.append(Bundle.getString("brkinfo.expiredInBrackets"));
        } else if (bp.isSkipping()) {
            buf.append(Bundle.getString("brkinfo.skippingInBrackets"));
        } else {
            buf.append(Bundle.getString("brkinfo.enabledInBrackets"));
        }

        // Have the breakpoint print itself.
        buf.append(' ');
        buf.append(bp.toString());
        out.writeln(buf.toString());
    } // printBreakpoint
} // brkinfoCommand
