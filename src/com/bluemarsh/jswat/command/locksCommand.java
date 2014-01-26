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
 * FILE:        locksCommand.java
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
 * $Id: locksCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'locks' command.
 *
 * @author  Nathan Fiedler
 */
public class locksCommand extends JSwatCommand {

    /**
     * Perform the 'locks' command.
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
                ObjectReference object = (ObjectReference) o;
                StringBuffer buf = new StringBuffer(256);
                buf.append(Bundle.getString("locks.monitorInfoFor"));
                buf.append(' ');
                buf.append(object.toString());
                buf.append(':');
                ThreadReference owner = object.owningThread();
                buf.append("  ");
                if (owner == null) {
                    buf.append(Bundle.getString("locks.notOwned"));
                    buf.append('\n');
                } else {
                    buf.append(Bundle.getString("locks.ownedBy"));
                    buf.append(' ');
                    buf.append(owner.name());
                    buf.append(", ");
                    buf.append(Bundle.getString("locks.entryCount"));
                    buf.append(' ');
                    buf.append(Integer.toString(object.entryCount()));
                    buf.append('\n');
                }
                List waiters = object.waitingThreads();
                if (waiters.size() == 0) {
                    buf.append("  ");
                    buf.append(Bundle.getString("locks.noWaiters"));
                    buf.append('\n');
                } else {
                    Iterator iter = waiters.iterator();
                    while (iter.hasNext()) {
                        ThreadReference waiter = (ThreadReference) iter.next();
                        buf.append("  ");
                        buf.append(Bundle.getString("locks.waitingThread"));
                        buf.append(' ');
                        buf.append(owner.name());
                        buf.append('\n');
                    }
                }
                out.write(buf.toString());
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
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(Bundle.getString("threadNotSuspended"),
                itse);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                ioobe);
        } catch (InvalidStackFrameException isfe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                isfe);
        } catch (ObjectCollectedException oce) {
            throw new CommandException(Bundle.getString("objectCollected"),
                oce);
        } catch (UnsupportedOperationException uoe) {
            throw new CommandException(Bundle.getString("locks.uoe"), uoe);
        }
    } // perform
} // locksCommand
