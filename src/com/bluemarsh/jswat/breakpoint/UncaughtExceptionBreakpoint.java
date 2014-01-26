/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 *      nf      09/18/02        Fixed bug 615
 *      nf      12/01/02        Fixed bug 680
 *
 * $Id: UncaughtExceptionBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.UncaughtExceptionBreakpointUI;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import java.util.logging.Level;
import java.util.prefs.Preferences;

/**
 * Class UncaughtExceptionBreakpoint implements the Breakpoint
 * interface. It halts execution of the debuggee VM whenever an uncaught
 * exception is thrown.
 *
 * @author Nathan Fiedler
 */
public class UncaughtExceptionBreakpoint extends AbstractBreakpoint
    implements SessionListener {
    /** True to stop when the exception is caught. */
    private boolean onCaught;
    /** True to stop when the exception is not caught. */
    private boolean onUncaught;
    /** Our exception request. */
    private ExceptionRequest eventRequest;

    /**
     * Constructs a ExceptionBreakpoint for any uncaught exception.
     */
    UncaughtExceptionBreakpoint() {
        // By default we only stop for uncaught exceptions.
        onUncaught = true;
    } // UncaughtExceptionBreakpoint

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        createRequests();
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Create the uncaught exception event requests.
     */
    protected void createRequests() {
        Session session = getBreakpointGroup().getSession();
        if (!session.isActive()) {
            // Nothing we can do right now.
            return;
        }
        // Delete the old requests, if any.
        deleteRequests();
        VirtualMachine vm = session.getVM();
        EventRequestManager erm = vm.eventRequestManager();
        eventRequest = erm.createExceptionRequest(null, onCaught, onUncaught);
        ExceptionBreakpoint.prepareRequest(vm, this, eventRequest);
    } // createRequests

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public void deactivated(SessionEvent sevt) {
    } // deactivated

    /**
     * Delete the exception request.
     */
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
        }
        eventRequest = null;
    } // deleteRequests

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        deleteRequests();

        Session session = getBreakpointGroup().getSession();
        session.removeListener(this);
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

            // Check if this is a ThreadDeath error. Because of this
            // extra check, we do not call the superclass method.
            ExceptionEvent ee = (ExceptionEvent) e;
            ObjectReference exc = ee.exception();
            ReferenceType type = exc.referenceType();
            if (type.name().equals("java.lang.ThreadDeath")) {
                // In which case we do nothing.
                return true;
            }

            // Yes, this is our event.
            if (logger.isLoggable(Level.INFO)) {
                logger.info("evaluating " + this);
            }

            // Need to increment this for things to work.
            incrementStoppedCount();
            shouldResume = shouldResume(e);
            if (!shouldResume) {
                // Seems we should execute the stop code.
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("performing stop for uncaught exception");
                }
                String desc = ExceptionBreakpoint.describeException(ee);
                desc = Bundle.getString("exceptionThrown") + ' ' + desc;
                shouldResume = performStop(e, desc);
            }
        }
        return shouldResume;
    } // eventOccurred

    /**
     * Returns the stop-on-caught status.
     *
     * @return  true if stopping when caught exceptions are thrown.
     */
    public boolean getStopOnCaught() {
        return onCaught;
    } // getStopOnCaught

    /**
     * Returns the stop-on-uncaught status.
     *
     * @return  true if stopping when uncaught exceptions are thrown.
     */
    public boolean getStopOnUncaught() {
        return onUncaught;
    } // getStopOnUncaught

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new UncaughtExceptionBreakpointUI(this);
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        super.init();
        logger.info("initializing uncaught exception breakpoint");
        Session session = getBreakpointGroup().getSession();
        session.addListener(this);
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        // We need to listen for exception events.
        vmeman.addListener(ExceptionEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
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
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        onCaught = prefs.getBoolean("onCaught", false);
        onUncaught = prefs.getBoolean("onUncaught", true);
        return super.readObject(prefs);
    } // readObject

    /**
     * Reset the stopped count to zero and clear any other attributes
     * such that this breakpoint can be used again for a new session.
     * This does not change the enabled-ness of the breakpoint.
     */
    public void reset() {
        super.reset();
        deleteRequests();
    } // reset

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpoint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will
     * remain effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            // Re-create the requests using the latest settings.
            createRequests();
        } else {
            deleteRequests();
        }
    } // setEnabled

    /**
     * Sets the stop-on-caught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when caught exceptions are thrown.
     */
    public void setStopOnCaught(boolean stop) {
        onCaught = stop;
    } // setStopOnCaught

    /**
     * Sets the stop-on-uncaught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when uncaught exceptions are thrown.
     */
    public void setStopOnUncaught(boolean stop) {
        onUncaught = stop;
    } // setStopOnUncaught

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants
     * for suspending threads. The breakpoint must be disabled
     * before calling this method.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (eventRequest != null) {
            eventRequest.setSuspendPolicy(getSuspendPolicy());
        }
    } // setSuspendPolicy

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended

    /**
     * Returns a String representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        return toString(false);
    } // toString

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string of this.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        if (onCaught && !onUncaught) {
            buf.append("all caught");
        } else if (!onCaught && onUncaught) {
            buf.append("all uncaught");
        } else if (!onCaught && !onUncaught) {
            buf.append("no");
        } else {
            buf.append("all");
        }
        buf.append(" exceptions");
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
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        prefs.putBoolean("onCaught", onCaught);
        prefs.putBoolean("onUncaught", onUncaught);
        return true;
    } // writeObject
} // UncaughtExceptionBreakpoint
