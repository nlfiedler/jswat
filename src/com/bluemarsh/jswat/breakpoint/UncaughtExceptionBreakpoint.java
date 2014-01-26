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
 * FILE:        UncaughtExceptionBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/16/01        Initial version
 *      nf      10/27/02        Fixed bug 615
 *
 * DESCRIPTION:
 *      Defines the exception breakpoint class for uncaught exceptions.
 *
 * $Id: UncaughtExceptionBreakpoint.java 641 2002-10-27 22:55:56Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.report.Category;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

/**
 * Class UncaughtExceptionBreakpoint implements the Breakpoint interface.
 * It halts execution of the debuggee VM whenever an uncaught exception is
 * thrown.
 *
 * @author Nathan Fiedler
 */
class UncaughtExceptionBreakpoint extends DefaultBreakpoint {
    /** Reporting category. */
    protected static Category logCategory = Category.instanceOf("breakpoint");
    /** serial version */
    static final long serialVersionUID = -3215278515799927187L;
    /** Owning session object. We need this because we're special
     * and are not a part of any breakpoint group (which would have
     * a handy getSession() method). */
    protected Session session;
    /** Our exception request. */
    protected EventRequest eventRequest;

    /**
     * Constructs a ExceptionBreakpoint for any uncaught exception.
     * This breakpoint should be left out of the default breakpoint
     * group so as to avoid any issues with a breakpoint that the
     * user doesn't know about.
     *
     * @param  erm  needed to create exception request.
     */
    UncaughtExceptionBreakpoint(EventRequestManager erm) {
        // Catch any and all uncaught exceptions.
        eventRequest = erm.createExceptionRequest(null, false, true);
        // Save a reference to ourselves in case we need it.
        eventRequest.putProperty("breakpoint", this);
        // Have to enable the request to work.
        eventRequest.setEnabled(true);
    } // UncaughtExceptionBreakpoint

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(ExceptionEvent.class, this);
    } // destroy

    /**
     * Invoked when a VM event has occurred.
     *
     * @param  e  VM event.
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        // Does this event belong to the request we created?
        EventRequest er = e.request();
        Object o = er.getProperty("breakpoint");
        boolean shouldResume = true;
        if (o == this) {
            // Check if this is a ThreadDeath error.
            ExceptionEvent ee = (ExceptionEvent) e;
            ObjectReference exc = ee.exception();
            ReferenceType type = exc.referenceType();
            if (type.name().equals("java.lang.ThreadDeath")) {
                // In which case we do nothing.
                return true;
            }
            // Yes, this is our event.
            shouldResume = false;
            logCategory.report("handling an uncaught exception");
            // We're special, don't call the superclass.
            performStop(e);
        }
        return shouldResume;
    } // eventOccurred

    /**
     * Returns the event request for this breakpoint, if the
     * breakpoint has been resolved.
     *
     * @return  breakpoint's event request, or null if unresolved.
     */
    public EventRequest eventRequest() {
        return eventRequest;
    } // eventRequest

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return null;
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     *
     * @param  session  needed since we don't belong to a group.
     */
    public void init(Session session) {
        super.init();
        // We need to listen for exception events.
        logCategory.report("initializing uncaught exception breakpoint");
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(ExceptionEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
        this.session = session;
    } // init

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  true, this breakpoint is always resolved.
     */
    public boolean isResolved() {
        return true;
    } // isResolved

    /**
     * This breakpoint has caused the debuggee VM to stop. Increment any
     * breakpoint counters and execute all monitors associated with
     * this breakpoint.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        session.setStatus(Bundle.getString("exceptionThrown"));
        session.handleLocatableEvent((LocatableEvent) e);
        // We're special, don't call the superclass.
        ExceptionBreakpoint.showException((ExceptionEvent) e,
                                          session.getStatusLog());
        return false;
    } // performStop

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        buf.append("all uncaught exceptions");
        if (!terse) {
            buf.append(' ');
            if (suspendPolicy == EventRequest.SUSPEND_ALL) {
                buf.append(Bundle.getString("suspendAll"));
            } else if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
                buf.append(Bundle.getString("suspendThread"));
            } else if (suspendPolicy == EventRequest.SUSPEND_NONE) {
                buf.append(Bundle.getString("suspendNone"));
            }
        }
        return buf.toString();
    } // toString

    /**
     * Returns a String representation of this.
     */
    public String toString() {
        return toString(false);
    } // toString
} // UncaughtExceptionBreakpoint
