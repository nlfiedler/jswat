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
 * FILE:        conditionCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/08/02        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'condition' command.
 *
 * $Id: conditionCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.ValueCondition;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.StringTokenizer;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Defines the class that handles the 'condition' command.
 *
 * @author  Nathan Fiedler
 */
public class conditionCommand extends JSwatCommand {

    /**
     * Perform the 'condition' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        String action = null;
        String conditionStr = null;
        String brknumStr = null;

        try {
            action = args.nextToken();
            // Discard the required white space.
            args.nextToken("\"");
            conditionStr = args.nextToken();
            // Use the default whitespace delimiters.
            args.setDelimiters(null);
            // Discard the trailing double-quote.
            args.nextToken();
            brknumStr = args.nextToken();
        } catch (NoSuchElementException nsee) {
            missingArgs(out);
            return;
        }

        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            out.writeln(Bundle.getString("condition.badbrk"));
            return;
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            out.writeln(Bundle.getString("condition.nobrk"));
            return;
        }

        int eqidx = StringUtils.indexOfUnescaped(conditionStr, '=');
        if (eqidx < 0) {
            out.writeln(Bundle.getString("condition.badexpr"));
            return;
        }

        if (action.equals("add")) {
            // Adding a new condition.
            ValueCondition condition = new ValueCondition(conditionStr);
            brk.addCondition(condition);
            out.writeln(Bundle.getString("condition.added"));

        } else if (action.equals("del")) {
            // Removing an existing condition.
            // First process the condition string.
            String var = conditionStr.substring(0, eqidx).trim();
            String val = conditionStr.substring(eqidx + 1).trim();

            ListIterator iter = brk.conditions();
            boolean found = false;
            while (iter.hasNext() && !found) {
                Condition condition = (Condition) iter.next();
                if (condition instanceof ValueCondition) {
                    // At present we only deal with value conditions.
                    ValueCondition vc = (ValueCondition) condition;
                    String vcvar = vc.getVariableName();
                    if (!vcvar.equals(var)) {
                        continue;
                    }
                    String vcval = vc.getValueString();
                    if (!vcval.equals(val)) {
                        continue;
                    }
                    // It's a match, remove that condition.
                    iter.remove();
                    found = true;
                }
            }

            if (!found) {
                out.writeln(Bundle.getString("condition.nocond"));
                return;
            } else {
                out.writeln(Bundle.getString("condition.removed"));
            }
        } else {
            out.writeln(Bundle.getString("condition.badaction"));
            return;
        }
    } // perform
} // conditionCommand
