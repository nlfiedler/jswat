/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        enablegcCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/05/99        Initial version
 *      nf      07/09/01        Corrected the absent info message.
 *      nf      12/23/02        Implemented RFE 559
 *
 * $Id: enablegcCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;

/**
 * Defines the class that handles the 'enablegc' command.
 *
 * @author  Nathan Fiedler
 */
public class enablegcCommand extends JSwatCommand {

    /**
     * Perform the 'enablegc' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            throw new CommandException(Bundle.getString("noCurrentThread"));
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // We do our own parsing, thank you very much.
        args.returnAsIs(true);
        String expr = args.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, ctxtman.getCurrentFrame());
            if (o instanceof ObjectReference) {
                ObjectReference obj = (ObjectReference) o;
                obj.enableCollection();
                out.writeln(Bundle.getString("enablegc.collectionEnabled"));
            } else {
                throw new CommandException(Bundle.getString("fieldNotObject"));
            }
        } catch (ClassNotPreparedException cnpe) {
            throw new CommandException(Bundle.getString("classNotPrepared"),
                cnpe);
        } catch (EvaluationException ee) {
            throw new CommandException(
                Bundle.getString("evalError") + ' ' + ee.getMessage(), ee);
        } catch (IllegalThreadStateException itse) {
            throw new CommandException(
                Bundle.getString("threadNotRunning") + '\n' + itse.toString(),
                itse);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                ioobe);
        } catch (InvalidStackFrameException isfe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                isfe);
        } catch (NativeMethodException nme) {
            throw new CommandException(Bundle.getString("nativeMethod"), nme);
        } catch (ObjectCollectedException oce) {
            throw new CommandException(Bundle.getString("objectCollected"),
                oce);
        }
    } // perform
} // enablegcCommand
