/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultStepper.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Class DefaultStepper provides a simple implementation of a Stepper.
 *
 * @author Nathan Fiedler
 */
public class DefaultStepper extends AbstractStepper implements DispatcherListener {

    /**
     * Creates a new instance of DefaultStepper.
     */
    public DefaultStepper() {
    }

    public boolean eventOccurred(Event event) {
        VirtualMachine vm = event.virtualMachine();
        if (event instanceof StepEvent) {
            if (vm.canGetSyntheticAttribute()) {
                CoreSettings cs = CoreSettings.getDefault();
                // Does the user want to skip stepping into synthetic methods?
                if (cs.getSkipSynthetics()) {
                    StepEvent stepEvent = (StepEvent) event;
                    if (stepEvent.location().method().isSynthetic()) {
                        // Current method is synthetic, step out of here.
                        try {
                            StepRequest req = createStep(
                                    StepRequest.STEP_LINE, StepRequest.STEP_OUT);
                            req.enable();
                            // Resume VM so step event will occur.
                            return true;
                        } catch (SteppingException se) {
                            ErrorManager.getDefault().notify(se);
                        }
                    }
                }
            }
        } else if (event instanceof MethodExitEvent) {
            // We already know the VM supports getting return values,
            // otherwise we would not be here.
            MethodExitEvent mee = (MethodExitEvent) event;
            Value retval = mee.returnValue();
            String name = mee.method().name();
            String args = Strings.listToString(mee.method().
                    argumentTypeNames());
            OutputWriter writer = OutputProvider.getWriter();
            String msg = NbBundle.getMessage(DefaultStepper.class,
                    "CTL_DefaultStepper_Returned", name, args, retval);
            writer.printOutput(msg);
            // Have the VM resume as this event is not what we are
            // really waiting for, but just informational.
            return true;
        }
        // Otherwise we want the VM to wait for the user.
        return false;
    }

    public StepRequest step(VirtualMachine vm, ThreadReference thread,
            int size, int depth) {
        try {
            // Clear any previously set step requests on this thread.
            clearPreviousStep(vm, thread);
            EventRequestManager mgr = vm.eventRequestManager();

            if (depth == StepRequest.STEP_OUT && vm.canGetMethodReturnValues()) {
                // If the VM supports method exit return values, set up
                // a request to get the next method exit event for this
                // class on this thread so we can display it.
                MethodExitRequest mer = mgr.createMethodExitRequest();
                mer.addThreadFilter(thread);
                mer.addClassFilter(thread.frame(0).location().declaringType());
                // Make this a one-off event, so it self terminates.
                mer.addCountFilter(1);
                register(mer);
                mer.enable();
            }

            // Ask the event request manager to create a step request.
            StepRequest request = mgr.createStepRequest(thread, size, depth);

            // Add class exclusions set by the user.
            CoreSettings cs = CoreSettings.getDefault();
            List excludes = cs.getSteppingExcludes();
            Iterator iter = excludes.iterator();
            while (iter.hasNext()) {
                String excl = (String) iter.next();
                request.addClassExclusionFilter(excl);
            }

            // Make this a one-off event, so it self terminates.
            request.addCountFilter(1);
            register(request);
            return request;
        } catch (IncompatibleThreadStateException itse) {
            // This cannot be possible.
            ErrorManager.getDefault().notify(itse);
        } catch (VMDisconnectedException vmde) {
            // This is unusual and there's no sense reporting it.
        }
        return null;
    }
}
