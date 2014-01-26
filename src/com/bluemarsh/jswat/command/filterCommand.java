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
 * FILE:        filterCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/02/02        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'filter' command.
 *
 * $Id: filterCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * Defines the class that handles the 'filter' command.
 *
 * @author  Nathan Fiedler
 */
public class filterCommand extends JSwatCommand {

    /**
     * Perform the 'filter' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        String action = null;
        String brknumStr = null;
        String type = null;
        String filter = null;

        try {
            action = args.nextToken();
            brknumStr = args.nextToken();
            type = args.nextToken();
            filter = args.nextToken();
        } catch (NoSuchElementException nsee) {
            missingArgs(out);
            return;
        }

        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            out.writeln(Bundle.getString("filter.badbrk"));
            return;
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            out.writeln(Bundle.getString("filter.nobrk"));
            return;
        }

        if (action.equals("add")) {
            // Adding a new filter.
            String filters;
            if (type.equals("thread")) {
                filters = brk.getThreadFilters();
            } else if (type.equals("class")) {
                filters = brk.getClassFilters();
            } else {
                out.writeln(Bundle.getString("filter.badtype"));
                return;
            }

            if (filters != null && filters.length() > 0) {
                filters = filters + "," + filter;
            } else {
                filters = filter;
            }

            if (type.equals("thread")) {
                brk.setThreadFilters(filters);
            } else if (type.equals("class")) {
                brk.setClassFilters(filters);
            }

            out.writeln(Bundle.getString("filter.added"));

        } else if (action.equals("del")) {
            // Removing an existing filter.
            String filters;
            if (type.equals("thread")) {
                filters = brk.getThreadFilters();
            } else if (type.equals("class")) {
                filters = brk.getClassFilters();
            } else {
                out.writeln(Bundle.getString("filter.badtype"));
                return;
            }

            int idx = filters.indexOf(filter);
            if (idx == 0) {
                // Remove from the start of the string.
                int comma = filters.indexOf(',', idx);
                if (comma > 0) {
                    filters = filters.substring(comma + 1);
                } else {
                    // Filter list only has one entry.
                    filters = "";
                }
            } else if (idx + filter.length() == filters.length()) {
                // Remove from the end of the string.
                int comma = filters.lastIndexOf(',', idx);
                filters = filters.substring(0, comma);
            } else if (idx > 0) {
                // Remove from the middle of the string.
                int comma = filters.lastIndexOf(',', idx);
                filters = filters.substring(0, comma) +
                    filters.substring(idx + filter.length());
            } else {
                out.writeln(Bundle.getString("filter.nofilter"));
                return;
            }

            if (type.equals("thread")) {
                brk.setThreadFilters(filters);
            } else if (type.equals("class")) {
                brk.setClassFilters(filters);
            }

            out.writeln(Bundle.getString("filter.removed"));
        } else {
            out.writeln(Bundle.getString("filter.badaction"));
            return;
        }
    } // perform
} // filterCommand
