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
 * are Copyright (C) 2001-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultUncaughtExceptionBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class DefaultUncaughtExceptionBreakpoint is the default implementation
 * of an UncaughtExceptionBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultUncaughtExceptionBreakpoint extends AbstractBreakpoint
        implements DispatcherListener, SessionListener,
        UncaughtExceptionBreakpoint {
    /** Our exception request. */
    private ExceptionRequest eventRequest;
    /** JDI event types we listen for. */
    private static List<Class> eventTypes;

    static {
        eventTypes = new ArrayList<Class>();
        eventTypes.add(ExceptionEvent.class);
    }

    public boolean canFilterClass() {
        return false;
    }

    public boolean canFilterThread() {
        return false;
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
        createRequests();
    }

    /**
     * Create the uncaught exception event requests.
     */
    protected void createRequests() {
        BreakpointGroup group = getBreakpointGroup();
        Session session = BreakpointProvider.getSession(group);
        if (!session.isConnected()) {
            // Nothing we can do right now.
            return;
        }
        VirtualMachine vm = session.getConnection().getVM();
        EventRequestManager erm = vm.eventRequestManager();
        eventRequest = erm.createExceptionRequest(null, false, true);
        // Save a reference to ourselves in case we need it.
        eventRequest.putProperty("breakpoint", this);
        applySuspendPolicy(eventRequest);
        // Have to enable the request to work.
        eventRequest.setEnabled(isEnabled());
    }

    protected void deleteRequests() {
        // Delete the old request, if any.
        try {
            if (eventRequest != null) {
                VirtualMachine vm = eventRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(eventRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        } finally {
            eventRequest = null;
        }
    }

    public String describe(Event e) {
        if (e instanceof ExceptionEvent) {
            return Utilities.describeException((ExceptionEvent) e);
        } else {
            throw new IllegalArgumentException("expecting an exception");
        }
    }

    public void disconnected(SessionEvent sevt) {
        deleteRequests();
    }

    public Iterator<Class> eventTypes() {
        return eventTypes.iterator();
    }

    public String getDescription() {
        return NbBundle.getMessage(DefaultUncaughtExceptionBreakpoint.class,
                "UncaughtException.description");
    }

    public boolean isResolved() {
        return true;
    }

    public void opened(Session session) {
    }

    public void resuming(SessionEvent sevt) {
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
        super.setSuspendPolicy(policy);
        if (eventRequest != null) {
            boolean enabled = eventRequest.isEnabled();
            eventRequest.setEnabled(false);
            applySuspendPolicy(eventRequest);
            eventRequest.setEnabled(enabled);
        }
    }

    protected boolean shouldResume(Event event) {
        // We do not call the superclass because the user does not set
        // counts or conditions on us.
        if (event instanceof ExceptionEvent) {
            // Check if this is a ThreadDeath error; we have to ignore those.
            ExceptionEvent ee = (ExceptionEvent) event;
            ObjectReference exc = ee.exception();
            ReferenceType type = exc.referenceType();
            if (type.name().equals("java.lang.ThreadDeath")) {
                return true;
            }
        }
        return false;
    }

    public void suspended(SessionEvent sevt) {
    }
}
