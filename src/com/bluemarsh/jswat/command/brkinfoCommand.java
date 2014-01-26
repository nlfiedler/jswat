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
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'brkinfo' command.
 *
 * $Id: brkinfoCommand.java 629 2002-10-26 23:03:26Z nfiedler $
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
import com.bluemarsh.util.StringTokenizer;
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
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        String brknumStr = args.nextToken();
        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            out.writeln(Bundle.getString("brkinfo.badbrk"));
            return;
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            out.writeln(Bundle.getString("brkinfo.nobrk"));
            return;
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
} // brkinfoCommand
