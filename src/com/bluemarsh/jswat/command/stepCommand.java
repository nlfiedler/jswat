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
 * FILE:        stepCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/15/99        Initial version
 *      tr      12/08/01        Add exclusion filter for class patterns.
 *      nf      01/13/02        Fixed bug 309: added 'thread' option
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'step' command.
 *
 * $Id: stepCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import java.util.*;

/**
 * Defines the class that handles the 'step' command.
 *
 * @author  Nathan Fiedler
 */
public class stepCommand extends JSwatCommand {

    /**
     * Clear any step requests that may still be associated with
     * the given thread.
     *
     * @param  vm      Virtual Machine
     * @param  thread  Thread on which to remove step requests.
     */
    protected void clearPreviousStep(VirtualMachine vm,
                                     ThreadReference thread) {
        // A previous step may not have completed on this
        // thread, in which case we need to delete it.
        EventRequestManager mgr = vm.eventRequestManager();
        List requests = mgr.stepRequests();
        Iterator iter = requests.iterator();
        while (iter.hasNext()) {
            StepRequest request = (StepRequest)iter.next();
            if (request.thread().equals(thread)) {
                mgr.deleteEventRequest(request);
                break;
            }
        }
    } // clearPreviousStep

    /**
     * Perform the 'step' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
	if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        if (args.hasMoreTokens()) {
            String token = args.peek();
            if (token.equals("out") || token.equals("up")) {
                // Eat the token.
                args.nextToken();
                // Finish the current frame.
                step(session, args, out, StepRequest.STEP_LINE,
                     StepRequest.STEP_OUT);
                return;
            }
        }

        // Step a single line, into functions.
        step(session, args, out, StepRequest.STEP_LINE,
             StepRequest.STEP_INTO);
    } // perform

    /**
     * Perform a general step operation.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     * @param  size     How much to step.
     * @param  depth    How exactly to step.
     * @return  True if successful, false if error.
     */
    protected boolean step(Session session, StringTokenizer args, Log out,
                           int size, int depth) {
        // Get the current thread.
        ThreadReference current = session.getCurrentThread();
        if (current == null) {
            out.writeln(Bundle.getString("noCurrentThread"));
            return false;
        }
        if (!current.isSuspended()) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
        }

        // Clear any previously set step requests on this thread.
        VirtualMachine vm = session.getVM();
        clearPreviousStep(vm, current);

        // Ask the event request manager to create a step request.
        EventRequestManager mgr = vm.eventRequestManager();
        StepRequest request = mgr.createStepRequest(current, size, depth);

        // Add class exclusions.
        String exclude[] = excludeCommand.getExclusionSet(session);
        for (int i = exclude.length - 1; i > -1; i--) {
            request.addClassExclusionFilter(exclude[i]);
        }

        if (args.hasMoreTokens()) {
            String token = args.nextToken();
            if (token.equals("thread")) {
                // User wants to suspend this thread only.
                request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            } else if (token.equals("all")) {
                // User wants to suspend all threads.
                request.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            }
        }

        // Skip the first event that is fired.
        request.addCountFilter(1);
        request.enable();
        // Must resume VM execution now.
        vm.resume();
        return true;
    } // step
} // stepCommand
