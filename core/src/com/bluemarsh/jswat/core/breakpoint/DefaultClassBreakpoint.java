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
 * are Copyright (C) 2002-2010. All Rights Reserved.
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
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import org.openide.util.NbBundle;

/**
 * Class DefaultClassBreakpoint is the default implementation of a
 * ClassBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DefaultClassBreakpoint extends AbstractBreakpoint
        implements ClassBreakpoint, SessionListener {

    /** True to stop on prepare. */
    private boolean onPrepare;
    /** True to stop on unload. */
    private boolean onUnload;
    /** Class prepare event request. */
    private ClassPrepareRequest prepareRequest;
    /** Class unload event request. */
    private ClassUnloadRequest unloadRequest;

    @Override
    public boolean canFilterClass() {
        return true;
    }

    @Override
    public boolean canFilterThread() {
        return false;
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void connected(SessionEvent sevt) {
        createRequests();
    }

    /**
     * Create the class prepare and unload event requests.
     */
    private void createRequests() {
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
        EventRequestManager erm = session.getConnection().getVM().eventRequestManager();

        String filter = getClassFilter();
        if (onPrepare) {
            prepareRequest = erm.createClassPrepareRequest();
            if (filter != null) {
                prepareRequest.addClassFilter(filter);
            }
            register(prepareRequest);
        }

        if (onUnload) {
            unloadRequest = erm.createClassUnloadRequest();
            if (filter != null) {
                unloadRequest.addClassFilter(filter);
            }
            register(unloadRequest);
        }
    }

    @Override
    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (prepareRequest != null) {
                VirtualMachine vm = prepareRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(prepareRequest);
            }
            if (unloadRequest != null) {
                VirtualMachine vm = unloadRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(unloadRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        } finally {
            prepareRequest = null;
            unloadRequest = null;
        }
    }

    @Override
    public String describe(Event e) {
        if (e instanceof ClassPrepareEvent) {
            ClassPrepareEvent cpe = (ClassPrepareEvent) e;
            String cname = cpe.referenceType().name();
            String tname = Threads.getIdentifier(cpe.thread());
            return NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.stop.load", cname, tname);
        } else if (e instanceof ClassUnloadEvent) {
            String cname = ((ClassUnloadEvent) e).className();
            return NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.stop.unload", cname);
        } else {
            throw new IllegalArgumentException("expected class event");
        }
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        deleteRequests();
    }

    @Override
    public String getDescription() {
        String cname = getClassFilter();
        if (cname == null) {
            cname = NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.any");
        }
        String type = "";
        if (onPrepare && onUnload) {
            type = NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.both");
        } else if (onPrepare) {
            type = NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.load");
        } else if (onUnload) {
            type = NbBundle.getMessage(DefaultClassBreakpoint.class,
                    "Class.description.unload");
        }
        return NbBundle.getMessage(DefaultClassBreakpoint.class,
                "Class.description", cname, type);
    }

    @Override
    public boolean getStopOnPrepare() {
        return onPrepare;
    }

    @Override
    public boolean getStopOnUnload() {
        return onUnload;
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
    public void setClassFilter(String filter) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setClassFilter(filter);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    @Override
    public void setStopOnPrepare(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onPrepare;
        onPrepare = stop;
        propSupport.firePropertyChange(PROP_STOPONPREPARE, old, stop);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    @Override
    public void setStopOnUnload(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onUnload;
        onUnload = stop;
        propSupport.firePropertyChange(PROP_STOPONUNLOAD, old, stop);
        if (isEnabled()) {
            // Re-create the requests using the latest settings.
            createRequests();
        }
    }

    @Override
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (prepareRequest != null) {
            boolean enabled = prepareRequest.isEnabled();
            prepareRequest.setEnabled(false);
            applySuspendPolicy(prepareRequest);
            prepareRequest.setEnabled(enabled);
        }
        if (unloadRequest != null) {
            boolean enabled = unloadRequest.isEnabled();
            unloadRequest.setEnabled(false);
            applySuspendPolicy(unloadRequest);
            unloadRequest.setEnabled(enabled);
        }
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
