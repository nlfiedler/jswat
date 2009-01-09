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
 * are Copyright (C) 2002-2006. All Rights Reserved.
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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import org.openide.util.NbBundle;

/**
 * Class DefaultThreadBreakpoint is the default implementation of a
 * ThreadBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DefaultThreadBreakpoint extends AbstractBreakpoint implements
        SessionListener, ThreadBreakpoint {
    /** True to stop on start. */
    private boolean onStart;
    /** True to stop on death. */
    private boolean onDeath;
    /** Thread start event request. */
    private ThreadStartRequest startRequest;
    /** Thread death event request. */
    private ThreadDeathRequest deathRequest;

    public boolean canFilterClass() {
        return false;
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
        EventRequestManager erm = session.getConnection()
            .getVM().eventRequestManager();

        // Create the new requests.
        if (onStart) {
            startRequest = erm.createThreadStartRequest();
            register(startRequest);
        }

        if (onDeath) {
            deathRequest = erm.createThreadDeathRequest();
            register(deathRequest);
        }
    }

    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (startRequest != null) {
                VirtualMachine vm = startRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(startRequest);
            }
            if (deathRequest != null) {
                VirtualMachine vm = deathRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(deathRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        } finally {
            startRequest = null;
            deathRequest = null;
        }
    }

    public String describe(Event e) {
        String name;
        String type;
        if (e instanceof ThreadStartEvent) {
            name = Threads.getIdentifier(((ThreadStartEvent) e).thread());
            type = NbBundle.getMessage(DefaultThreadBreakpoint.class,
                    "Thread.description.stop.start");
        } else if (e instanceof ThreadDeathEvent) {
            name = Threads.getIdentifier(((ThreadDeathEvent) e).thread());
            type = NbBundle.getMessage(DefaultThreadBreakpoint.class,
                    "Thread.description.stop.death");
        } else {
            throw new IllegalArgumentException("expected thread event, but got " +
                    e.getClass().getName());
        }
        return NbBundle.getMessage(DefaultThreadBreakpoint.class,
                "Thread.description.stop", name, type);
    }

    public void disconnected(SessionEvent sevt) {
        deleteRequests();
    }

    public String getDescription() {
        String type = "";
        if (onDeath && onStart) {
            type = NbBundle.getMessage(DefaultThreadBreakpoint.class,
                    "Thread.description.both");
        } else if (onDeath) {
            type = NbBundle.getMessage(DefaultThreadBreakpoint.class,
                    "Thread.description.death");
        } else if (onStart) {
            type = NbBundle.getMessage(DefaultThreadBreakpoint.class,
                    "Thread.description.start");
        }
        return NbBundle.getMessage(DefaultThreadBreakpoint.class,
                "Thread.description", type);
    }

    public boolean getStopOnDeath() {
        return onDeath;
    }

    public boolean getStopOnStart() {
        return onStart;
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

    public void setStopOnDeath(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onDeath;
        onDeath = stop;
        propSupport.firePropertyChange(PROP_STOPONDEATH, old, stop);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    public void setStopOnStart(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onStart;
        onStart = stop;
        propSupport.firePropertyChange(PROP_STOPONSTART, old, stop);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (startRequest != null) {
            boolean enabled = startRequest.isEnabled();
            startRequest.setEnabled(false);
            applySuspendPolicy(startRequest);
            startRequest.setEnabled(enabled);
        }
        if (deathRequest != null) {
            boolean enabled = deathRequest.isEnabled();
            deathRequest.setEnabled(false);
            applySuspendPolicy(deathRequest);
            deathRequest.setEnabled(enabled);
        }
    }

    protected boolean shouldResume(Event event) {
        // Is this the thread we are interested in?
        ThreadReference thread = null;
        if (event instanceof ThreadStartEvent && onStart) {
            thread = ((ThreadStartEvent) event).thread();
        } else if (event instanceof ThreadDeathEvent && onDeath) {
            thread = ((ThreadDeathEvent) event).thread();
        } else {
            return true;
        }
        String name = null;
        try {
            name = thread.name();
        } catch (VMDisconnectedException vmde) {
            // Happens when event does not suspend.
            return true;
        }
        String filter = getThreadFilter();
        if (filter != null && filter.length() > 0) {
            return !filter.equals(name);
        }
        // Delegate to the superclass to check any conditions.
        return super.shouldResume(event);
    }

    public void suspended(SessionEvent sevt) {
    }
}
