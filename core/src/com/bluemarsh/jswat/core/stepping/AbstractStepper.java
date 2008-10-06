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
 * $Id: AbstractStepper.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.event.Dispatcher;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.bluemarsh.jswat.core.event.DispatcherProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class AbstractStepper provides an abstract implementation of Stepper
 * concrete implementations to subclass. It maintains provides sensible
 * default implementations of some of the methods.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractStepper implements SessionListener, Stepper {
    /** The Session instance we belong to. */
    private Session owningSession;
    
    /**
     * Creates a new instance of AbstractStepper.
     */
    public AbstractStepper() {
    }

    /**
     * Clear any step requests that may still be associated with the thread.
     * Step requests that have not yet completed must be removed before any
     * additional step requests are created.
     *
     * @param  vm      virtual machine in which to clear requests.
     * @param  thread  thread on which to remove step requests.
     */
    protected static void clearPreviousStep(VirtualMachine vm, ThreadReference thread) {
        EventRequestManager erm = vm.eventRequestManager();
        List<EventRequest> requests = new ArrayList<EventRequest>(1);
        List<StepRequest> steps = erm.stepRequests();
        for (StepRequest request : steps) {
            if (request.thread().equals(thread)) {
                requests.add(request);
            }
        }
        erm.deleteEventRequests(requests);
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
    }

    /**
     * Creates a step request for the Session associated with this instance.
     * The request must be enanbled and the Session resumed.
     *
     * @param  size   how much to step (one of the StepRequest constants).
     * @param  depth  how to step (one of the StepRequest constants).
     * @return  the step request.
     * @throws  SteppingException
     *          if there is a problem in creating the step request.
     */
    protected StepRequest createStep(int size, int depth) throws SteppingException {
        DebuggingContext dc = ContextProvider.getContext(owningSession);
        ThreadReference thread = dc.getThread();
        if (thread != null) {
            VirtualMachine vm = owningSession.getConnection().getVM();
            StepRequest req = step(vm, thread, size, depth);
            return req;
        } else {
            String msg = NbBundle.getMessage(getClass(), "CTL_Stepping_NoThread");
            throw new SteppingException(msg);
        }
    }

    public void disconnected(SessionEvent sevt) {
    }

    public void opened(Session session) {
        owningSession = session;
    }

    /**
     * Registers this stepper as a DispatchListener for the given request.
     * In general such requests are set to fire just once, using the
     * <code>EventRequest.addCountFilter()</code> method, so there is no
     * corresponding "unregister" method.
     *
     * @param  request  the event request.
     */
    protected void register(EventRequest request) {
        if (this instanceof DispatcherListener) {
            Dispatcher dispatcher = DispatcherProvider.getDispatcher(owningSession);
            dispatcher.register((DispatcherListener) this, request);
        }
    }

    public void resuming(SessionEvent sevt) {
    }

    public void step(int size, int depth) throws SteppingException {
        StepRequest req = createStep(size, depth);
        req.enable();
        owningSession.resumeVM();
    }

    public void stepInto() throws SteppingException {
        step(StepRequest.STEP_LINE, StepRequest.STEP_INTO);
    }

    public void stepOut() throws SteppingException {
        step(StepRequest.STEP_LINE, StepRequest.STEP_OUT);
    }

    public void stepOver() throws SteppingException {
        step(StepRequest.STEP_LINE, StepRequest.STEP_OVER);
    }

    public void suspended(SessionEvent sevt) {
    }
}
