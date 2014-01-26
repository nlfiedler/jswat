/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: conditionCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.ExprCondition;
import com.bluemarsh.jswat.util.Strings;
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
    public void perform(Session session, CommandArguments args, Log out) {
        String action = null;
        String conditionStr = null;
        String brknumStr = null;

        try {
            action = args.nextToken();
            conditionStr = args.nextToken();
            brknumStr = args.nextToken();
        } catch (NoSuchElementException nsee) {
            throw new MissingArgumentsException();
        }

        int brknum = -1;
        try {
            brknum = Integer.parseInt(brknumStr);
        } catch (NumberFormatException nfe) {
            throw new CommandException(Bundle.getString("condition.badbrk"));
        }

        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        Breakpoint brk = brkman.getBreakpoint(brknum);
        if (brk == null) {
            throw new CommandException(Bundle.getString("condition.nobrk"));
        }

        if (action.equals("add")) {
            // Adding a new condition.
            ExprCondition condition = new ExprCondition(conditionStr);
            brk.addCondition(condition);
            out.writeln(Bundle.getString("condition.added"));

        } else if (action.equals("del")) {
            // Removing an existing condition.
            ListIterator iter = brk.conditions();
            boolean found = false;
            while (iter.hasNext() && !found) {
                Condition condition = (Condition) iter.next();
                if (condition instanceof ExprCondition) {
                    // At present we only deal with expr conditions.
                    ExprCondition vc = (ExprCondition) condition;
                    String expr = vc.getExprString();
                    if (!expr.equals(conditionStr)) {
                        continue;
                    }
                    // It's a match, remove that condition.
                    iter.remove();
                    found = true;
                }
            }

            if (!found) {
                throw new CommandException(
                    Bundle.getString("condition.nocond"));
            } else {
                out.writeln(Bundle.getString("condition.removed"));
            }
        } else {
            throw new CommandException(
                Bundle.getString("condition.badaction"));
        }
    } // perform
} // conditionCommand
