/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * $Id: printCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ByteType;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharType;
import com.sun.jdi.CharValue;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;

/**
 * Defines the class that handles the 'print' command.
 *
 * @author  Nathan Fiedler
 */
public class printCommand extends JSwatCommand {

    /**
     * Perform the 'print' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Check for enough arguments.
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Get the current thread.
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        int frame = ctxtman.getCurrentFrame();

        // We do our own parsing, thank you very much.
        args.returnAsIs(true);
        String expr = args.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, frame);
            String s;
            if (o instanceof Value) {
                Value v = (Value) o;
                if (v instanceof ArrayReference) {
                    // See if this is an array we can easily convert to
                    // a string.
                    ArrayReference ar = (ArrayReference) v;
                    ArrayType at = (ArrayType) ar.referenceType();
                    Type ct = at.componentType();
                    if (ct instanceof ByteType) {
                        // We can convert this to a string.
                        int size = ar.length();
                        byte[] bytes = new byte[size];
                        StringBuffer sb = new StringBuffer();
                        for (int ii = 0; ii < size; ii++) {
                            ByteValue bv = (ByteValue) ar.getValue(ii);
                            bytes[ii] = bv.value();
                        }
                        // Use the default encoding.
                        out.writeln(new String(bytes));
                        // Skip the default behavior.
                        return;
                    } else if (ct instanceof CharType) {
                        // We can easily convert this to a string.
                        int size = ar.length();
                        StringBuffer sb = new StringBuffer();
                        for (int ii = 0; ii < size; ii++) {
                            CharValue cv = (CharValue) ar.getValue(ii);
                            sb.append(cv.value());
                        }
                        out.writeln(sb.toString());
                        // Skip the default behavior.
                        return;
                    }
                }
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
    } // perform
} // printCommand
