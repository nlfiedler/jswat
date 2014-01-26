/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: StepAction.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.command.excludeCommand;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the step action.
 *
 * @author  Nathan Fiedler
 */
public class StepAction extends JSwatAction implements SessionAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new StepAction object with the default action
     * command string of "step".
     */
    public StepAction() {
        super("step");
    } // StepAction

    /**
     * Creates a new StepAction object with the given action
     * command string.
     *
     * @param  name  Name of the step action.
     */
    public StepAction(String name) {
        super(name);
    } // StepAction

    /**
     * Performs the step action.
     *
     * @param  event  action event
     */
    public void actionPerformed(ActionEvent event) {
        Session session = getSession(event);
        if (session.isActive()) {
            // Step a single line, into functions.
            step(session, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
        } else {
            session.getStatusLog().writeln
                (swat.getResourceString("noActiveSession"));
        }
    } // actionPerformed

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
     * Perform a general step operation.
     *
     * @param  session  JSwat session on which to operate.
     * @param  size     How much to step.
     * @param  depth    How exactly to step.
     * @return  True if successful, false if error.
     */
    protected boolean step(Session session, int size, int depth) {
        // Get the current thread.
        ThreadReference current = session.getCurrentThread();
        if (current == null) {
            session.getStatusLog().writeln
                (Bundle.getString("noCurrentThread"));
            return false;
        }

        // Clear any previously set step requests on this thread.
        VirtualMachine vm = session.getVM();

        try {
            clearPreviousStep(vm, current);

            // Ask the event request manager to create a step request.
            EventRequestManager mgr = vm.eventRequestManager();
            StepRequest request = mgr.createStepRequest(current, size, depth);

            // Add class exclusions.
            String exclude[] = excludeCommand.getExclusionSet(session);
            for (int i = exclude.length - 1; i > -1; i--) {
                request.addClassExclusionFilter(exclude[i]);
            }

            // Skip the first event that is fired.
            request.addCountFilter(1);
            request.enable();
            // Must resume VM execution now.
            vm.resume();
            return true;
        } catch (VMDisconnectedException vmde) {
            return false;
        }
    } // step
} // StepAction
