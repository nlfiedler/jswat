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
 * are Copyright (C) 2001-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.util.Names;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.InvalidRequestStateException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class ResolvableBreakpoint represents a breakpoint that requires
 * resolution against a class in the debuggee VM. Such breakpoints
 * include the location breakpoint, exception breakpoint, and watchpoint
 * breakpoint.
 *
 * @author  Nathan Fiedler
 */
public abstract class DefaultResolvableBreakpoint extends AbstractBreakpoint
        implements ResolvableBreakpoint, SessionListener {
    /** Specification for the class this breakpoint is meant for;
     * may be wild-carded with a leading or trailing asterisk. */
    private String className;
    /** Name of the class specified, without wildcards. */
    private String tameName;
    /** True if class specification is suffixed with a wildcard. */
    private boolean postWild;
    /** True if class specification is prefixed with a wildcard. */
    private boolean preWild;
    /** Class prepare request used to resolve the breakpoint. */
    private ClassPrepareRequest prepareRequest;
    /** Resolved event requests, if breakpoint has resolved. */
    private List<EventRequest> eventRequests;

    /**
     * Creates a new instance of ResolvableBreakpoint.
     */
    protected DefaultResolvableBreakpoint() {
        eventRequests = new LinkedList<EventRequest>();
    }

    @Override
    public void connected(SessionEvent sevt) {
        BreakpointGroup group = getBreakpointGroup();
        Session session = BreakpointProvider.getSession(group);
        VirtualMachine vm = session.getConnection().getVM();
        if (vm != null) {
            try {
                resolveEagerly(vm);
                if (isResolved()) {
                    propSupport.firePropertyChange(PROP_RESOLVED, false, true);
                }
            } catch (ResolveException re) {
                fireError(re);
            }
        }
    }

    /**
     * Create the class prepare request for this breakpoint.
     *
     * @param  vm  virtual machine in which to create request.
     * @return  class prepare request, for subclasses to modify if desired.
     */
    protected ClassPrepareRequest createPrepareRequest(VirtualMachine vm) {
        prepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        // Test tameName for length since that indicates whether there is
        // anything to the className other than a wildcard.
        if (tameName.length() > 0) {
            // Use the className with the wilcard as the event filter.
            prepareRequest.addClassFilter(className);
        }
        register(prepareRequest);
        return prepareRequest;
    }

    /**
     * Delete the class prepare request so we can resolve all over again.
     */
    private void deletePrepareRequest() {
        if (prepareRequest != null) {
            try {
                VirtualMachine vm = prepareRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(prepareRequest);
            } catch (VMDisconnectedException vmde) {
                // This happens all the time.
            } finally {
                prepareRequest = null;
            }
        }
    }

    @Override
    protected void deleteRequests() {
        // Delete only the non-class-prepare event requests here.
        if (!eventRequests.isEmpty()) {
            try {
                for (EventRequest er : eventRequests) {
                    VirtualMachine vm = er.virtualMachine();
                    EventRequestManager erm = vm.eventRequestManager();
                    erm.deleteEventRequest(er);
                }
            } catch (VMDisconnectedException vmde) {
                // This happens all the time.
            } finally {
                eventRequests.clear();
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        deletePrepareRequest();
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        // Like the other breakpoint types, delete the requests. This
        // covers the case where the breakpoint is being removed (as a
        // session listener) and may actually be connected to a session,
        // in which case we need to remove the event requests from the
        // debuggee event request manager.
        deletePrepareRequest();
        deleteRequests();
    }

    @Override
    public boolean eventOccurred(Event event) {
        if (event instanceof ClassPrepareEvent) {
            try {
                if (isEnabled()) {
                    ClassPrepareEvent cpe = (ClassPrepareEvent) event;
                    ReferenceType clazz = cpe.referenceType();
                    if (matches(clazz) &&
                            resolveReference(clazz, eventRequests)) {
                        propSupport.firePropertyChange(
                                PROP_RESOLVED, false, true);
                    }
                }
            } catch (ResolveException re) {
                fireError(re);
                return false;
            }
            return true;
        } else {
            return super.eventOccurred(event);
        }
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean isResolved() {
        return !eventRequests.isEmpty();
    }

    /**
     * Compare the loaded class with the identifier that this breakpoint
     * is defined against to determine if they match.
     *
     * @param  clazz  class to match against.
     * @return  true if class is a match, false otherwise.
     */
    protected boolean matches(ReferenceType clazz) {
        String cname = clazz.name();
        if (preWild) {
            return cname.endsWith(tameName);
        } else if (postWild) {
            return cname.startsWith(tameName);
        } else {
            return cname.equals(tameName);
        }
    }

    @Override
    public void reset() {
        super.reset();
        propSupport.firePropertyChange(PROP_RESOLVED, true, false);
    }

    /**
     * Try to resolve this event request eagerly. That is, try to find a
     * matching prepared class now. If one is not found, create a class
     * prepare request so we can resolve when the class is loaded.
     *
     * @param  vm  VirtualMachine
     * @throws  ResolveException
     *          if the resolution fails in a bad way.
     */
    protected void resolveEagerly(VirtualMachine vm) throws ResolveException {
        // Clear out the old prepare request to make room for the new one.
        deletePrepareRequest();
        // Create class prepare request for this breakpoint.
        createPrepareRequest(vm);

        // Get the appropriate list of classes.
        List<ReferenceType> classes = null;
        if (preWild || postWild) {
            classes = vm.allClasses();
        } else {
            classes = vm.classesByName(className);
        }

        // We handle exceptions specially because a class may be loaded by
        // more than one class loader, and one of those instances may have
        // the location requested, while the others may not. If none of the
        // classes resolve successfully, then we throw the exception.
        ResolveException originalExc = null;
        boolean resolved = false;

        // Run through the list of classes trying to find a match.
        Iterator<ReferenceType> iter = classes.iterator();
        while (iter.hasNext()) {
            ReferenceType clazz = iter.next();
            try {
                // NJPL support requires matching against the class
                // itself, not the name of the class.
                if (clazz.isPrepared() && matches(clazz)) {
                    resolved = resolveReference(clazz, eventRequests);
                    // Keep going through the list so we get all of the
                    // matching classes, in the event that a single
                    // class has been loaded by multiple classloaders.
                }

            } catch (ResolveException re) {
                // See if we should keep looking for another instance of the
                // class with this location before throwing the exception.
                if (re.getCause() instanceof NoSuchMethodException) {
                    originalExc = re;
                } else {
                    throw re;
                }
            }
        }

        if (!resolved && originalExc != null) {
            // With an exception and no successful resolution...
            throw originalExc;
        }
    }

    /**
     * Called by subclasses to re-resolve the breakpoint in the event that
     * it had been disabled and just recently re-enabled. Also possible that
     * the class was hotswapped and the breakpoint needs to resolve again.
     *
     * <p>This method will fire a change event if the resolution is
     * successful, and fires an error event if a resolve exception occurs.</p>
     */
    protected void resolveEagerlyWithEvents() {
        BreakpointGroup group = getBreakpointGroup();
        if (group == null) {
            return;
        }
        Session session = BreakpointProvider.getSession(group);
        if (session != null && session.isConnected()) {
            VirtualMachine vm = session.getConnection().getVM();
            if (vm != null) {
                try {
                    resolveEagerly(vm);
                    if (isResolved()) {
                        propSupport.firePropertyChange(
                                PROP_RESOLVED, false, true);
                    }
                } catch (ResolveException re) {
                    fireError(re);
                }
            }
        }
    }

    /**
     * Resolve against the given ReferenceType. If successful, add the
     * new event requests to the provided list.
     *
     * @param  refType   ReferenceType against which to resolve.
     * @param  requests  list to which new event requests should be added.
     * @return  true if resolved, false otherwise.
     * @throws  ResolveException
     *          if the resolution fails in a bad way.
     */
    protected abstract boolean resolveReference(ReferenceType refType,
            List<EventRequest> requests) throws ResolveException;

    @Override
    public void setClassName(String name) throws MalformedClassNameException {
        // Do strict checking of class name validity because if the
        // name is invalid, it will never match a future loaded class.
        if (!Names.isValidClassname(name, true)) {
            String msg = NbBundle.getMessage(DefaultResolvableBreakpoint.class,
                    "Resolve.invalidName", name);
            throw new MalformedClassNameException(msg);
        }

        // Determine how the name is wildcarded, if at all.
        String old = className;
        className = name;
        preWild = name.startsWith("*");
        postWild = name.endsWith("*");
        if (preWild) {
            tameName = name.substring(1);
        } else if (postWild) {
            tameName = name.substring(0, name.length() - 1);
        } else {
            tameName = name;
        }

        // Reset ourselves so we get resolved all over again.
        deleteRequests();
        propSupport.firePropertyChange(PROP_CLASSNAME, old, name);
        if (isEnabled()) {
            resolveEagerlyWithEvents();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!eventRequests.isEmpty()) {
            try {
                for (EventRequest er : eventRequests) {
                    er.setEnabled(isEnabled());
                }
            } catch (InvalidRequestStateException irse) {
                // This seems to happen when the code has been redefined.
                deleteRequests();
                fireError(irse);
            } catch (VMDisconnectedException vmde) {
                // We will clean up in disconnected().
            }
        }
    }

    @Override
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        // Update the existing event request suspend policy.
        if (!eventRequests.isEmpty()) {
            for (EventRequest er : eventRequests) {
                boolean enabled = er.isEnabled();
                er.setEnabled(false);
                applySuspendPolicy(er);
                er.setEnabled(enabled);
            }
        }
    }
}
