/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        DefaultBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      08/21/01        Removed errorMessageFor()
 *      nf      09/26/01        Fixed bug #248
 *
 * DESCRIPTION:
 *      Defines the default Breakpoint base class.
 *
 * $Id: ResolvableBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Class ResolvableBreakpoint represents a breakpoint that requires
 * resolution against a class in the debuggee VM. Such breakpoints
 * include the location breakpoint, exception breakpoint, and
 * watchpoint breakpoint.
 *
 * @author  Nathan Fiedler
 */
public abstract class ResolvableBreakpoint extends DefaultBreakpoint {
    /** serial version */
    static final long serialVersionUID = -5671015427599789946L;
    /** Specification for the class this breakpoint is meant for. */
    protected ReferenceTypeSpec referenceSpec;
    /** Class prepare request used to resolve the breakpoint. */
    protected transient ClassPrepareRequest prepareRequest;
    /** Resolved event request if any, or null if not yet resolved. */
    protected transient EventRequest eventRequest;

    /**
     * Constructs a ResolvableBreakpoint using the given class
     * identifier.
     *
     * @param  classId  class name pattern with optional wildcards.
     * @exception  ClassNotFoundException
     *             Thrown if classId is not a valid identifier.
     */
    ResolvableBreakpoint(String classId) throws ClassNotFoundException {
        referenceSpec = new PatternReferenceTypeSpec(classId);
    } // ResolvableBreakpoint

    /**
     * Delete the event request.
     */
    protected void deleteEventRequest() {
        if (eventRequest != null) {
            try {
                VirtualMachine vm = eventRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(eventRequest);
                if (logCategory.isEnabled()) {
                    logCategory.report("deleted event request for " + this);
                }
            } catch (VMDisconnectedException vmde) {
                // This happens all the time.
            }
            eventRequest = null;
        }
    } // deleteEventRequest

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        deleteEventRequest();
        // Don't want to know about the class prepare events.
        if (prepareRequest != null) {
            try {
                VirtualMachine vm = prepareRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(prepareRequest);
            } catch (VMDisconnectedException vmde) {
                // this will happen all the time
            }
            prepareRequest = null;
        }
        super.destroy();
    } // destroy

    /**
     * Returns the event request for this breakpoint, if the
     * breakpoint has been resolved. If this value is non-null,
     * the caller can be certain the breakpoint is resolved.
     *
     * @return  breakpoint's event request.
     * @see #isResolved
     */
    public EventRequest eventRequest() {
        return eventRequest;
    } // eventRequest

    /**
     * Returns the reference type spec of this breakpoint.
     *
     * @return  the reference type spec.
     */
    public ReferenceTypeSpec getReferenceTypeSpec() {
        return referenceSpec;
    } // getReferenceTypeSpec

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  true if this breakpoint has resolved, false otherwise.
     */
    public boolean isResolved() {
        return eventRequest() != null;
    } // isResolved

    /**
     * Reset the stopped count to zero and clear any other attributes
     * such that this breakpoint can be used again.
     * This does not change the enabled-ness of the breakpoint.
     */
    public void reset() {
        // Make this breakpoint unresolved so it will be resolved again.
        deleteEventRequest();
        // Now do the usual reset thing, including notifying listeners.
        super.reset();
    } // reset

    /**
     * Try to resolve this breakpoint against the class prepare event.
     *
     * @param  event  class prepare event
     * @return  event request, or null if it could not be resolved.
     */
    public EventRequest resolveAgainstEvent(ClassPrepareEvent event)
        throws ResolveException {
        if (prepareRequest != null &&
            prepareRequest.equals(event.request()) &&
            // Work around for JPDA bug 4331522.
            referenceSpec.matches(event.referenceType())) {
            // Remove the existing event request, if any.
            deleteEventRequest();
            // It looks like a match, let's try to resolve the request.
            eventRequest = resolveReference(event.referenceType());
        }
        return eventRequest;
    } // resolveAgainstEvent

    /**
     * Try to resolve this event request eagerly. That is, try to
     * find a matching prepared class now. If one is not found,
     * create a class prepare request so we can resolve when the
     * class is loaded.
     *
     * @param  vm  VirtualMachine
     * @return  event request, or null if it could not be resolved.
     */
    public EventRequest resolveEagerly(VirtualMachine vm)
        throws ResolveException {
        // Create class prepare request for this spec.
        prepareRequest = referenceSpec.createPrepareRequest(vm);
        // Save reference so we can determine ownership later.
        prepareRequest.putProperty("breakpoint", this);
        prepareRequest.enable();

        // Get the appropriate list of classes.
        List classes = null;
        if (referenceSpec.isExact()) {
            classes = vm.classesByName(referenceSpec.getIdentifier());
        } else {
            vm.suspend();
            classes = vm.allClasses();
            vm.resume();
        }
        // Have to create a modifiable version of the list.
        classes = new ArrayList(classes);

        // Run through the list of classes trying to find a match.
        ListIterator iter = classes.listIterator();
        while (iter.hasNext()) {
            ReferenceType reftype = (ReferenceType) iter.next();
            if (reftype.isPrepared() && referenceSpec.matches(reftype)) {
                eventRequest = resolveReference(reftype);
                // Keep going through the list.
            }
            // Do nested classes, too.
            List nestedTypes = reftype.nestedTypes();
            Iterator nestedIter = nestedTypes.iterator();
            while (nestedIter.hasNext()) {
                Object o = nestedIter.next();
                iter.add(o);
            }
        }
        return eventRequest;
    } // resolveEagerly

    /**
     * Resolve against the given ReferenceType. If successful, return
     * the new event request.
     *
     * @param  refType  ReferenceType against which to resolve.
     * @return  event request, or null if not resolved.
     */
    protected abstract EventRequest resolveReference(ReferenceType refType)
        throws ResolveException;

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will
     * remain effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        EventRequest er = eventRequest();
        if (er != null) {
            try {
                er.setEnabled(enabled);
            } catch (VMDisconnectedException vmde) {
                eventRequest = null;
                prepareRequest = null;
            }
        }
    } // setEnabled

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants
     * for suspending threads.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        // Update the existing event request suspend policy.
        if (eventRequest != null) {
            eventRequest.setSuspendPolicy(suspendPolicy);
        }
    } // setSuspendPolicy
} // ResolvableBreakpoint
