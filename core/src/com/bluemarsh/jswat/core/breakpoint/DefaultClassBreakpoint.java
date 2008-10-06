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
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultClassBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.event.DispatcherListener;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class DefaultClassBreakpoint is the default implementation of a
 * ClassBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DefaultClassBreakpoint extends AbstractBreakpoint
        implements ClassBreakpoint, DispatcherListener, SessionListener {
    /** True to stop on prepare. */
    private boolean onPrepare;
    /** True to stop on unload. */
    private boolean onUnload;
    /** Class prepare event request. */
    private ClassPrepareRequest prepareRequest;
    /** Class unload event request. */
    private ClassUnloadRequest unloadRequest;
    /** JDI event types we listen for. */
    private static List<Class> eventTypes;

    static {
        eventTypes = new ArrayList<Class>();
        eventTypes.add(ClassPrepareEvent.class);
        eventTypes.add(ClassUnloadEvent.class);
    }

    public boolean canFilterClass() {
        return true;
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
     * Create the class prepare and unload event requests.
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

        String filter = getClassFilter();
        if (onPrepare) {
            prepareRequest = erm.createClassPrepareRequest();
            prepareRequest.putProperty("breakpoint", this);
            applySuspendPolicy(prepareRequest);
            if (filter != null) {
                prepareRequest.addClassFilter(filter);
            }
            prepareRequest.setEnabled(isEnabled());
        }

        if (onUnload) {
            unloadRequest = erm.createClassUnloadRequest();
            unloadRequest.putProperty("breakpoint", this);
            applySuspendPolicy(unloadRequest);
            if (filter != null) {
                unloadRequest.addClassFilter(filter);
            }
            unloadRequest.setEnabled(isEnabled());
        }
    }

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

    public void disconnected(SessionEvent sevt) {
        deleteRequests();
    }

    public Iterator<Class> eventTypes() {
        return eventTypes.iterator();
    }

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

    public boolean getStopOnPrepare() {
        return onPrepare;
    }

    public boolean getStopOnUnload() {
        return onUnload;
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

    public void suspended(SessionEvent sevt) {
    }
}
