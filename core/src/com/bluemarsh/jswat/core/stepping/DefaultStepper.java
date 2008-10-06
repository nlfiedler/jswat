/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultStepper.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.event.DispatcherEvent;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;

/**
 * Class DefaultStepper provides a simple implementation of a Stepper.
 *
 * @author Nathan Fiedler
 */
public class DefaultStepper extends AbstractStepper implements DispatcherListener {
    /** The JDI event types to be expected. */
    private static List<Class> jdiEventTypes;

    static {
        jdiEventTypes = new ArrayList<Class>(1);
        jdiEventTypes.add(StepEvent.class);
    }

    /**
     * Creates a new instance of DefaultStepper.
     */
    public DefaultStepper() {
    }

    public boolean eventOccurred(DispatcherEvent e) {
        VirtualMachine vm = e.getEvent().virtualMachine();
        if (vm.canGetSyntheticAttribute()) {
            CoreSettings cs = CoreSettings.getDefault();
            // Does the user want to skip stepping into synthetic methods?
            if (cs.getSkipSynthetics()) {
                StepEvent event = (StepEvent) e.getEvent();
                if (event.location().method().isSynthetic()) {
                    // Current method is synthetic, step out of here.
                    try {
                        StepRequest req = createStep(StepRequest.STEP_LINE, StepRequest.STEP_OUT);
                        req.enable();
                        // Resume VM so step event will occur.
                        return true;
                    } catch (SteppingException se) {
                        ErrorManager.getDefault().notify(se);
                    }
                }
            }
        }
        // Otherwise we want the VM to wait for the user.
        return false;
    }

    public Iterator<Class> eventTypes() {
        return jdiEventTypes.iterator();
    }

    public StepRequest step(VirtualMachine vm, ThreadReference thread, int size, int depth) {
        try {
            // Clear any previously set step requests on this thread.
            clearPreviousStep(vm, thread);

            // Ask the event request manager to create a step request.
            EventRequestManager mgr = vm.eventRequestManager();
            StepRequest request = mgr.createStepRequest(thread, size, depth);

            // Add class exclusions set by the user.
            CoreSettings cs = CoreSettings.getDefault();
            List excludes = cs.getSteppingExcludes();
            Iterator iter = excludes.iterator();
            while (iter.hasNext()) {
                String excl = (String) iter.next();
                request.addClassExclusionFilter(excl);
            }

            // Skip the first event that is fired.
            request.addCountFilter(1);
            return request;
        } catch (VMDisconnectedException vmde) {
            return null;
        }
    }
}
