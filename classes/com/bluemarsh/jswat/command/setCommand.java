/*********************************************************************
 *
 *      Copyright (C) 2000-2004 Bill Smith
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
 * $Id: setCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * Defines the class that handles the 'set' command.
 *
 * @author  Bill Smith
 */
public class setCommand extends JSwatCommand {

    /**
     * Perform the 'set' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        // Get the current thread.
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            throw new CommandException(Bundle.getString("noCurrentThread"));
        }
        int frame = ctxtman.getCurrentFrame();
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // We do our own parsing, thank you very much.
        args.returnAsIs(true);
        String expr = args.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, frame);
            String s;
            if (o instanceof Value) {
                Value v = (Value) o;
                s = Variables.printValue(v, thread, "\n");
            } else {
                if (o == null) {
                    s = "null";
                } else {
                    s = o.toString();
                }
            }
            out.writeln(s);
        } catch (EvaluationException ee) {
            throw new CommandException(
                Bundle.getString("evalError") + ' ' + ee.getMessage(), ee);
        } catch (Exception e) {
            throw new CommandException(e.getMessage(), e);
        }
        UIAdapter uiadapter = session.getUIAdapter();
        uiadapter.refreshDisplay();
    } // perform
} // setCommand
