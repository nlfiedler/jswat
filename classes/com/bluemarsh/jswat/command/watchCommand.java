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
 * FILE:        watchCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/09/02        Initial version
 *      nf      07/29/02        Fixed bug 600
 *      nf      12/27/02        Implemented RFE 529
 *
 * $Id: watchCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.breakpoint.WatchBreakpoint;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.request.EventRequest;

/**
 * Defines the class that handles the 'watch' command.
 *
 * @author  Nathan Fiedler
 */
public class watchCommand extends JSwatCommand {

    /**
     * Perform the 'watch' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Default to suspending all of the threads.
        int suspendPolicy = EventRequest.SUSPEND_ALL;

        // See if user provided the go or thread option.
        String peek = args.peek();
        if (peek.equals("go")) {
            suspendPolicy = EventRequest.SUSPEND_NONE;
            // Remove the option flag.
            args.nextToken();
        } else if (peek.equals("thread")) {
            suspendPolicy = EventRequest.SUSPEND_EVENT_THREAD;
            // Remove the option flag.
            args.nextToken();
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Get the variable name.
        String varname = args.nextToken();

        // The object instance filter, if any.
        String objexpr = null;

        // See which type of breakpoint it shall be.
        boolean onAccess = true;
        boolean onModify = true;
        if (args.hasMoreTokens()) {
            String type = args.peek();
            if (type.equals("access")) {
                onModify = false;
                args.nextToken();
            } else if (type.equals("modify")) {
                onAccess = false;
                args.nextToken();
            }
        }
        if (args.hasMoreTokens()) {
            if (objexpr == null) {
                args.returnAsIs(true);
                objexpr = args.rest();
            } else {
                throw new CommandException(
                    Bundle.getString("watch.unexpectedArg"));
            }
        }

        ObjectReference objref = null;
        if (objexpr != null) {
            // Get the current thread.
            ContextManager ctxtman = (ContextManager)
                session.getManager(ContextManager.class);
            ThreadReference thread = ctxtman.getCurrentThread();
            int frame = ctxtman.getCurrentFrame();
            Evaluator eval = new Evaluator(objexpr);
            try {
                Object o = eval.evaluate(thread, frame);
                if (o instanceof ObjectReference) {
                    objref = (ObjectReference) o;
                } else {
                    throw new CommandException(
                        Bundle.getString("watch.exprNotObject"));
                }
            } catch (EvaluationException ee) {
                throw new CommandException(
                    Bundle.getString("evalError") + ' ' + ee.getMessage(), ee);
            } catch (Exception e) {
                throw new CommandException(e.getMessage(), e);
            }
        }

        // Add the watch breakpoint unconditionally.
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        WatchBreakpoint bp = new WatchBreakpoint(varname, onAccess, onModify,
                                                 objref);
        try {
            brkman.addNewBreakpoint(bp);
            // Set the suspend policy on the breakpoint
            bp.setEnabled(false);
            bp.setSuspendPolicy(suspendPolicy);
            bp.setEnabled(true);
        } catch (ResolveException re) {
            // this can't happen
            throw new CommandException(re);
        }

        // We know that watch breakpoints resolve immediately
        // when the session is active.
        if (!session.isActive() || !bp.isResolved()) {
            out.writeln(Bundle.getString("watch.disabledForNow1"));
            out.writeln(Bundle.getString("watch.disabledForNow2"));
        } else {
            out.writeln(Bundle.getString("watch.watchAdded"));
        }
    } // perform
} // watchCommand
