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
 * are Copyright (C) 2006-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.openide.util.NbBundle;

/**
 * Class DefaultLocationBreakpoint is a default implementation of a
 * LocationBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultLocationBreakpoint extends AbstractBreakpoint
        implements LocationBreakpoint, SessionListener {

    /** The Location that we are set to stop upon. */
    private Location location;
    /** Resolved event request, if breakpoint has resolved. */
    private EventRequest eventRequest;

    /**
     * Creates a new instance of DefaultLineBreakpoint.
     */
    public DefaultLocationBreakpoint() {
    }

    @Override
    public boolean canFilterClass() {
        return false;
    }

    @Override
    public boolean canFilterThread() {
        return true;
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void connected(SessionEvent sevt) {
    }

    /**
     * Create the breakpoint request.
     */
    private void createRequests() {
        VirtualMachine vm = location.virtualMachine();
        EventRequestManager erm = vm.eventRequestManager();
        eventRequest = erm.createBreakpointRequest(location);
        register(eventRequest);
        // We are always resolved, but nothing wrong with firing this anyway.
        propSupport.firePropertyChange(PROP_RESOLVED, false, true);
    }

    @Override
    protected void deleteRequests() {
        if (eventRequest != null) {
            try {
                VirtualMachine vm = eventRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(eventRequest);
            } catch (VMDisconnectedException vmde) {
                // This happens all the time.
            } finally {
                eventRequest = null;
            }
        }
    }

    @Override
    public String describe(Event e) {
        LocatableEvent le = (LocatableEvent) e;
        String[] params = new String[]{
            location.declaringType().name(),
            location.method().name(),
            location.method().signature(),
            String.valueOf(location.codeIndex()),
            Threads.getIdentifier(le.thread())
        };
        return NbBundle.getMessage(DefaultLocationBreakpoint.class,
                "Location.description.stop", params);
    }

    @Override
    public void destroy() {
        deleteRequests();
        super.destroy();
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        deleteRequests();
        // Delete ourselves since our location will be stale,
        // thus rendering us completely useless.
        fireEvent(new BreakpointEvent(this, BreakpointEventType.REMOVED, null));
    }

    @Override
    public String getDescription() {
        String[] params = new String[]{
            location.declaringType().name(),
            location.method().name(),
            location.method().signature(),
            String.valueOf(location.codeIndex())
        };
        return NbBundle.getMessage(DefaultLocationBreakpoint.class,
                "Location.description", params);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            createRequests();
        }
    }

    @Override
    public void setLocation(Location location) {
        Location old = this.location;
        this.location = location;
        // Reset ourselves so we get resolved all over again.
        deleteRequests();
        propSupport.firePropertyChange(PROP_LOCATION, old, location);
        if (isEnabled()) {
            createRequests();
        }
    }

    @Override
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (eventRequest != null) {
            boolean enabled = eventRequest.isEnabled();
            eventRequest.setEnabled(false);
            applySuspendPolicy(eventRequest);
            eventRequest.setEnabled(enabled);
        }
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
