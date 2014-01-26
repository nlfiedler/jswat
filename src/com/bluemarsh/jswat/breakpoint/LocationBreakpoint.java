/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * FILE:        LocationBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/01/02        Initial version
 *
 * DESCRIPTION:
 *      Defines the location breakpoint class.
 *
 * $Id: LocationBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.jswat.util.ThreadUtils;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import java.util.List;

/**
 * Class LocationBreakpoint is the base class for all breakpoints that
 * are based on a particular location in code. This includes line
 * breakpoints and method breakpoints.
 *
 * @author Nathan Fiedler
 */
public abstract class LocationBreakpoint extends ResolvableBreakpoint implements LocatableBreakpoint {
    /** serial version */
    static final long serialVersionUID = 3337359746629452881L;
    /** Line number of breakpoint. */
    protected int lineNumber;

    /**
     * Constructs a LocationBreakpoint using the given class identifier.
     *
     * @param  classId  class name pattern with optional wildcards.
     * @exception  ClassNotFoundException
     *             Thrown if classId is not a valid identifier.
     */
    LocationBreakpoint(String classId) throws ClassNotFoundException {
        super(classId);
    } // LocationBreakpoint

    /**
     * Create the breakpoint event request against the given location.
     *
     * @param  location  location at which to stop.
     * @return  event request.
     */
    protected EventRequest createEventRequest(Location location) {
        EventRequestManager erm =
            location.virtualMachine().eventRequestManager();
        BreakpointRequest er = erm.createBreakpointRequest(location);
        // Save a reference to ourselves in case we need it.
        er.putProperty("breakpoint", this);
        er.setSuspendPolicy(suspendPolicy);

        // Apply thread filters.
        Session session = getBreakpointGroup().getSession();
        String filtersStr = getThreadFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = StringUtils.stringToList(filtersStr);
            try {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String thdtoken = (String) filters.get(ii);
                    ThreadReference thread = ThreadUtils.getThreadByID(
                        session, thdtoken);
                    if (thread != null) {
                        er.addThreadFilter(thread);
                    } else {
                        session.getStatusLog().writeln(
                            Bundle.getString("noSuchThreadFilter") + " " +
                            thdtoken);
                    }
                }
            } catch (NotActiveException nae) { }
        }

        // Have to enable the request to work.
        er.setEnabled(isEnabled());
        return er;
    } // createEventRequest

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(com.sun.jdi.event.BreakpointEvent.class, this);
    } // destroy

    /**
     * Return the name of the class that this breakpoint is located in.
     * This could be a fully-qualified class name or a wild-carded name
     * pattern containing a single asterisk (e.g. "*.cname").
     *
     * @return  Class name if known, null if not.
     */
    public String getClassName() {
        return referenceSpec.getIdentifier();
    } // getClassName

    /**
     * Retrieve the line number associated with this breakpoint. Not
     * all location breakpoints will have a particular line associated
     * with them (such as method breakpoints). In such cases, this
     * method may return -1.
     *
     * @return  line number of breakpoint, or -1 if unknown.
     */
    public int getLineNumber() {
        return lineNumber;
    } // getLineNumber

    /**
     * Retrieve the location associated with this breakpoint. The caller
     * may want to call <code>isResolved()</code> before calling this
     * method. An unresolved breakpoint will not have a location yet.
     *
     * @return  location of breakpoint, or null if not resolved.
     */
    public Location getLocation() {
        if (isResolved()) {
            return ((BreakpointRequest) eventRequest).location();
        } else {
            return null;
        }
    } // getLocation

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        // We need to listen for breakpoint events.
        Session session = getBreakpointGroup().getSession();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(com.sun.jdi.event.BreakpointEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
    } // init

    /**
     * Resolve against the given ReferenceType. If successful, return
     * the new event request.
     *
     * @param  refType  ReferenceType against which to resolve.
     * @return  event request, or null if not resolved.
     * @exception  ResolveException
     *             Thrown if breakpoint resolve fails.
     */
    protected EventRequest resolveReference(ReferenceType refType)
        throws ResolveException {

        // Check that the reference type is a class.
        if (!(refType instanceof ClassType)) {
            throw new ResolveException(new InvalidTypeException());
        }
        Location location = resolveLocation((ClassType) refType);
        return createEventRequest(location);
    } // resolveReference

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type. 
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     */
    protected abstract Location resolveLocation(ClassType clazz)
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
        if (enabled && eventRequest != null) {
            // Delete and recreate the event request using the
            // current breakpoint settings.
            BreakpointRequest br = (BreakpointRequest) eventRequest;
            Location loc = br.location();
            deleteEventRequest();
            eventRequest = createEventRequest(loc);
        }
    } // setEnabled

    /**
     * Returns a String representation of this.
     */
    public String toString() {
        return toString(false);
    } // toString
} // LocationBreakpoint
