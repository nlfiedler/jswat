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
 * are Copyright (C) 2001-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import org.openide.util.NbBundle;

/**
 * Class DefaultUncaughtExceptionBreakpoint is the default implementation
 * of an UncaughtExceptionBreakpoint.
 *
 * @author Nathan Fiedler
 */
public class DefaultUncaughtExceptionBreakpoint extends AbstractBreakpoint
        implements SessionListener, UncaughtExceptionBreakpoint {
    /** Our exception request. */
    private ExceptionRequest eventRequest;

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
        if (group == null) {
            // During deserialization, we cannot get the group or session.
            // We will have our chance when the debuggee is connected.
        }
        Session session = BreakpointProvider.getSession(group);
        if (session == null || !session.isConnected()) {
            // Nothing we can do right now.
            return;
        }
        VirtualMachine vm = session.getConnection().getVM();
        EventRequestManager erm = vm.eventRequestManager();
        eventRequest = erm.createExceptionRequest(null, false, true);
        register(eventRequest);
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
