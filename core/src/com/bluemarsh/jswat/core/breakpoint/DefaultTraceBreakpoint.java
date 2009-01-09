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
 * are Copyright (C) 2002-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import org.openide.util.NbBundle;

/**
 * Class DefaultTraceBreakpoint is a default implementation of a
 * TraceBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DefaultTraceBreakpoint extends AbstractBreakpoint implements
        SessionListener, TraceBreakpoint {
    /** Method entry event request. */
    private MethodEntryRequest entryRequest;
    /** Method exit event request. */
    private MethodExitRequest exitRequest;
    /** True to stop when method is entered. */
    private boolean stopOnEnter;
    /** True to stop when method is exited. */
    private boolean stopOnExit;

    /**
     * Creates a new instance of TraceBreakpoint.
     */
    public DefaultTraceBreakpoint() {
        // We do not suspend the VM because we are just tracing.
        super.setSuspendPolicy(EventRequest.SUSPEND_NONE);
    }

    public boolean canFilterClass() {
        return true;
    }

    public boolean canFilterThread() {
        return true;
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
        createRequests();
    }

    /**
     * Create the method entry and exit event requests.
     */
    protected void createRequests() {
        BreakpointGroup group = getBreakpointGroup();
        if (group == null) {
            // Nothing we can do right now.
            return;
        }
        Session session = BreakpointProvider.getSession(group);
        if (session == null || !session.isConnected()) {
            // Nothing we can do right now.
            return;
        }
        VirtualMachine vm = session.getConnection().getVM();
        EventRequestManager erm = vm.eventRequestManager();

        // Create the new requests.
        String cfilter = getClassFilter();
        if (stopOnEnter) {
            entryRequest = erm.createMethodEntryRequest();
            if (cfilter != null) {
                entryRequest.addClassFilter(cfilter);
            }
            register(entryRequest);
        }

        if (stopOnExit) {
            exitRequest = erm.createMethodExitRequest();
            if (cfilter != null) {
                exitRequest.addClassFilter(cfilter);
            }
            register(exitRequest);
        }
    }

    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (entryRequest != null) {
                VirtualMachine vm = entryRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(entryRequest);
            }
            if (exitRequest != null) {
                VirtualMachine vm = exitRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(exitRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        } finally {
            entryRequest = null;
            exitRequest = null;
        }
    }

    public String describe(Event e) {
        String type;
        Value returnValue = null;
        boolean hasReturnValue = false;
        if (e instanceof MethodEntryEvent) {
            type = NbBundle.getMessage(DefaultTraceBreakpoint.class,
                    "Trace.description.stop.enter");
        } else if (e instanceof MethodExitEvent) {
            VirtualMachine vm = e.virtualMachine();
            if (vm.canGetMethodReturnValues()) {
                MethodExitEvent mee = (MethodExitEvent) e;
                returnValue = mee.returnValue();
                hasReturnValue = true;
            }
            type = NbBundle.getMessage(DefaultTraceBreakpoint.class,
                    "Trace.description.stop.exit");
        } else {
            throw new IllegalArgumentException("expected method event, not " +
                    e.getClass().getName());
        }
        LocatableEvent le = (LocatableEvent) e;
        Location loc = le.location();
        Method method = loc.method();
        String cname = loc.declaringType().name();
        String args = Strings.listToString(method.argumentTypeNames());
        String tname = Threads.getIdentifier(le.thread());
        String[] params = new String[] { cname, method.name(), args, type, tname };
        String msg = NbBundle.getMessage(DefaultTraceBreakpoint.class,
                "Trace.description.stop", params);
        if (hasReturnValue) {
            return msg + ' ' + NbBundle.getMessage(DefaultTraceBreakpoint.class,
                    "Trace.description.stop.return", returnValue);
        }
        return msg;
    }

    public void disconnected(SessionEvent sevt) {
        deleteRequests();
    }

    public String getDescription() {
        // The use of asterisk below means we will match anything.
        String cfilter = getClassFilter();
        if (cfilter == null) {
            cfilter = "*";
        }
        String tfilter = getThreadFilter();
        if (tfilter == null) {
            tfilter = "*";
        }
        return NbBundle.getMessage(DefaultTraceBreakpoint.class,
                "Trace.description", cfilter, tfilter);
    }

    public boolean getStopOnEnter() {
        return stopOnEnter;
    }

    public boolean getStopOnExit() {
        return stopOnExit;
    }

    public boolean isResolved() {
        return true;
    }

    public void opened(Session session) {
    }

    public void resuming(SessionEvent sevt) {
    }

    public void setClassFilter(String filter) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setClassFilter(filter);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    public void setSuspendPolicy(int policy) {
        // Do nothing because we simply trace, we never stop.
    }

    public void setStopOnEnter(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = stopOnEnter;
        stopOnEnter = stop;
        propSupport.firePropertyChange(PROP_STOPONENTER, old, stop);
        if (isEnabled()) {
            createRequests();
        }
    }

    public void setStopOnExit(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = stopOnExit;
        stopOnExit = stop;
        propSupport.firePropertyChange(PROP_STOPONEXIT, old, stop);
        if (isEnabled()) {
            createRequests();
        }
    }

    protected boolean shouldResume(Event event) {
        // Now is our only chance to display our message and run monitors.
        // Note we pretend to be stopped in order for a description to be
        // displayed appropriately.
        BreakpointEvent be = new BreakpointEvent(this,
                BreakpointEvent.Type.STOPPED, event);
        fireEvent(be);
        runMonitors(be);
        // We always resume because we only trace.
        return true;
    }

    public void suspended(SessionEvent sevt) {
    }
}
