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
 * $Id: DefaultWatchBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class DefaultWatchBreakpoint is a WatchBreakpoint that resolves against
 * a class and watches a single field in all instances of that class.
 *
 * @author  Nathan Fiedler
 */
public class DefaultWatchBreakpoint extends DefaultResolvableBreakpoint
        implements ResolvableWatchBreakpoint {
    /** Name of the field we are watching. */
    private String fieldName;
    /** True to stop on field access. */
    private boolean onAccess;
    /** True to stop on field modification. */
    private boolean onModify;

    /**
     * Creates a new instance of WatchBreakpoint.
     */
    public DefaultWatchBreakpoint() {
        addJdiEventType(AccessWatchpointEvent.class);
        addJdiEventType(ModificationWatchpointEvent.class);
    }

    public boolean canFilterClass() {
        return true;
    }

    public boolean canFilterThread() {
        return true;
    }

    public void closing(SessionEvent sevt) {
    }

    public String describe(Event e) {
        String result = null;
        if (e instanceof WatchpointEvent) {
            result = Utilities.describeWatch((WatchpointEvent) e);
        }
        if (result == null) {
            throw new IllegalArgumentException(
                    "expected watchpoint event, but got " +
                    e.getClass().getName());
        }
        return result;
    }

    public String getDescription() {
        StringBuilder buf = new StringBuilder(80);
        String type = "";
        if (onAccess && onModify) {
            type = NbBundle.getMessage(DefaultWatchBreakpoint.class,
                    "Watch.description.both");
        } else if (onAccess) {
            type = NbBundle.getMessage(DefaultWatchBreakpoint.class,
                    "Watch.description.access");
        } else if (onModify) {
            type = NbBundle.getMessage(DefaultWatchBreakpoint.class,
                    "Watch.description.modify");
        }
        buf.append(NbBundle.getMessage(DefaultWatchBreakpoint.class,
                "Watch.description", fieldName, type));

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

    public String getFieldName() {
        return fieldName;
    }

    public boolean getStopOnAccess() {
        return onAccess;
    }

    public boolean getStopOnModify() {
        return onModify;
    }

    public void opened(Session session) {
    }

    protected boolean resolveReference(ReferenceType refType,
            List<EventRequest> requests) throws ResolveException {

        int oldsize = requests.size();
        // Resolve the name to an actual field.
        Field field = null;
        try {
            field = refType.fieldByName(fieldName);
        } catch (ClassNotPreparedException cnpe) {
            throw new ResolveException(cnpe);
        }
        // Create the watch requests.
        if (field != null) {
            VirtualMachine vm = field.virtualMachine();
            EventRequestManager erm = vm.eventRequestManager();

            // Create the new requests.
            if (onAccess) {
                if (vm.canWatchFieldAccess()) {
                    AccessWatchpointRequest accessRequest =
                            erm.createAccessWatchpointRequest(field);
                    accessRequest.putProperty("breakpoint", this);
                    applySuspendPolicy(accessRequest);
                    String filter = getClassFilter();
                    if (filter != null) {
                        accessRequest.addClassFilter(filter);
                    }
                    accessRequest.setEnabled(isEnabled());
                    requests.add(accessRequest);
                } else {
                    throw new ResolveException(NbBundle.getMessage(
                            DefaultWatchBreakpoint.class,
                            "Watch.cannotWatchAccess"),
                            new UnsupportedOperationException());
                }
            }

            if (onModify) {
                if (vm.canWatchFieldModification()) {
                    ModificationWatchpointRequest modifyRequest =
                            erm.createModificationWatchpointRequest(field);
                    modifyRequest.putProperty("breakpoint", this);
                    applySuspendPolicy(modifyRequest);
                    String filter = getClassFilter();
                    if (filter != null) {
                        modifyRequest.addClassFilter(filter);
                    }
                    modifyRequest.setEnabled(isEnabled());
                    requests.add(modifyRequest);
                } else {
                    throw new ResolveException(NbBundle.getMessage(
                            DefaultWatchBreakpoint.class,
                            "Watch.cannotWatchModify"),
                            new UnsupportedOperationException());
                }
            }

            // We were successful if there are more requests now than before.
            return requests.size() > oldsize;
        } else {
            // May be that the class name filter matched incorrectly.
            return false;
        }
    }

    public void resuming(SessionEvent sevt) {
    }

    public void setClassFilter(String filter) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setClassFilter(filter);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void setEnabled(boolean enabled) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        super.setEnabled(enabled);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void setFieldName(String name) throws MalformedMemberNameException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name must be non-empty");
        }
        if (!Names.isJavaIdentifier(name)) {
            throw new MalformedMemberNameException(name);
        }
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        String old = fieldName;
        fieldName = name;
        propSupport.firePropertyChange(PROP_FIELDNAME, old, name);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void setStopOnAccess(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onAccess;
        onAccess = stop;
        propSupport.firePropertyChange(PROP_STOPONACCESS, old, stop);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void setStopOnModify(boolean stop) {
        // Delete so we can recreate them using changed settings.
        deleteRequests();
        boolean old = onModify;
        onModify = stop;
        propSupport.firePropertyChange(PROP_STOPONMODIFY, old, stop);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    public void suspended(SessionEvent sevt) {
    }
}
