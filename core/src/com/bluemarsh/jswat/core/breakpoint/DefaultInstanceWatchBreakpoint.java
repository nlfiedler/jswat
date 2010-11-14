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
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import org.openide.util.NbBundle;

/**
 * Class DefaultInstanceWatchBreakpoint is a WatchBreakpoint that watches
 * a particular field in a particular object reference. The field name
 * property is unused, and this breakpoint does not need to resolve against
 * any class, since it has the field and object already.
 *
 * @author  Nathan Fiedler
 */
public class DefaultInstanceWatchBreakpoint extends AbstractBreakpoint
        implements InstanceWatchBreakpoint, SessionListener {

    /** The object reference filter. */
    private ObjectReference objectRef;
    /** Field we are watching. */
    private Field watchField;
    /** True to stop on field access. */
    private boolean onAccess;
    /** True to stop on field modification. */
    private boolean onModify;
    /** The access watchpoint event request. */
    private AccessWatchpointRequest accessRequest;
    /** The modification watchpoint event request. */
    private ModificationWatchpointRequest modifyRequest;

    /**
     * Creates a new instance of DefaultInstanceWatchBreakpoint.
     */
    public DefaultInstanceWatchBreakpoint() {
    }

    @Override
    public boolean canFilterClass() {
        return true;
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
        if (isEnabled()) {
            createRequests();
        }
    }

    /**
     * Create the access and/or modification watchpoint requests.
     */
    protected void createRequests() {
        VirtualMachine vm = watchField.virtualMachine();
        EventRequestManager erm = vm.eventRequestManager();

        if (onAccess) {
            if (vm.canWatchFieldAccess()) {
                accessRequest = erm.createAccessWatchpointRequest(watchField);
                String filter = getClassFilter();
                if (filter != null) {
                    accessRequest.addClassFilter(filter);
                }
                accessRequest.addInstanceFilter(objectRef);
                register(accessRequest);
            } else {
                fireError(new ResolveException(NbBundle.getMessage(
                        DefaultInstanceWatchBreakpoint.class,
                        "Watch.cannotWatchAccess"),
                        new UnsupportedOperationException()));
            }
        }

        if (onModify) {
            if (vm.canWatchFieldModification()) {
                modifyRequest = erm.createModificationWatchpointRequest(watchField);
                String filter = getClassFilter();
                if (filter != null) {
                    modifyRequest.addClassFilter(filter);
                }
                modifyRequest.addInstanceFilter(objectRef);
                register(modifyRequest);
            } else {
                fireError(new ResolveException(NbBundle.getMessage(
                        DefaultInstanceWatchBreakpoint.class,
                        "Watch.cannotWatchModify"),
                        new UnsupportedOperationException()));
            }
        }
    }

    @Override
    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (accessRequest != null) {
                VirtualMachine vm = accessRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(accessRequest);
            }
            if (modifyRequest != null) {
                VirtualMachine vm = modifyRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(modifyRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        } finally {
            accessRequest = null;
            modifyRequest = null;
        }
    }

    @Override
    public String describe(Event e) {
        String result = null;
        if (e instanceof WatchpointEvent) {
            result = Utilities.describeWatch((WatchpointEvent) e);
        }
        if (result == null) {
            throw new IllegalArgumentException(
                    "expected watchpoint event, but got "
                    + e.getClass().getName());
        }
        return result;
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        deleteRequests();
        // Delete ourselves since our object reference will be stale,
        // thus rendering us completely useless.
        fireEvent(new BreakpointEvent(this, BreakpointEventType.REMOVED, null));
    }

    @Override
    public String getClassName() {
        return objectRef.referenceType().name();
    }

    @Override
    public String getDescription() {
        StringBuilder buf = new StringBuilder(80);
        String type = "";
        if (onAccess && onModify) {
            type = NbBundle.getMessage(DefaultInstanceWatchBreakpoint.class,
                    "Watch.description.both");
        } else if (onAccess) {
            type = NbBundle.getMessage(DefaultInstanceWatchBreakpoint.class,
                    "Watch.description.access");
        } else if (onModify) {
            type = NbBundle.getMessage(DefaultInstanceWatchBreakpoint.class,
                    "Watch.description.modify");
        }
        buf.append(NbBundle.getMessage(DefaultInstanceWatchBreakpoint.class,
                "Watch.description", watchField.name(), type));

        String filter = getClassFilter();
        if (filter != null) {
            buf.append(", class ");
            buf.append(filter);
        }
        filter = getThreadFilter();
        if (filter != null) {
            buf.append(", thread ");
            buf.append(filter);
        }
        return buf.toString();
    }

    @Override
    public Field getField() {
        return watchField;
    }

    @Override
    public String getFieldName() {
        return watchField.name();
    }

    @Override
    public ObjectReference getObjectReference() {
        return objectRef;
    }

    @Override
    public boolean getStopOnAccess() {
        return onAccess;
    }

    @Override
    public boolean getStopOnModify() {
        return onModify;
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
            createRequests();
        }
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
    public void setFieldName(String name) throws MalformedMemberNameException {
        // Do nothing since we already have the field.
    }

    @Override
    public void setObjectReference(ObjectReference obj) {
        objectRef = obj;
    }

    @Override
    public void setStopOnAccess(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onAccess;
        onAccess = stop;
        propSupport.firePropertyChange(PROP_STOPONACCESS, old, stop);
        if (isEnabled()) {
            createRequests();
        }
    }

    @Override
    public void setStopOnModify(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onModify;
        onModify = stop;
        propSupport.firePropertyChange(PROP_STOPONMODIFY, old, stop);
        if (isEnabled()) {
            createRequests();
        }
    }

    @Override
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (accessRequest != null) {
            boolean enabled = accessRequest.isEnabled();
            accessRequest.setEnabled(false);
            applySuspendPolicy(accessRequest);
            accessRequest.setEnabled(enabled);
        }
        if (modifyRequest != null) {
            boolean enabled = modifyRequest.isEnabled();
            modifyRequest.setEnabled(false);
            applySuspendPolicy(modifyRequest);
            modifyRequest.setEnabled(enabled);
        }
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }

    @Override
    public void setField(Field field) {
        watchField = field;
    }
}
