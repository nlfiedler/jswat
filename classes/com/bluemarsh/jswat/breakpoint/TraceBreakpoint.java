/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: TraceBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.BasicBreakpointUI;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Threads;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.Iterator;
import java.util.List;

/**
 * Class TraceBreakpoint implements the Breakpoint interface. It shows
 * each time any method is entered or exited during the execution of the
 * debuggee program.
 *
 * @author  Nathan Fiedler
 */
public class TraceBreakpoint extends AbstractBreakpoint
    implements SessionListener {
    /** Method entry event request. */
    private MethodEntryRequest entryRequest;
    /** Method exit event request. */
    private MethodExitRequest exitRequest;

    /**
     * Default constructor for deserialization.
     */
    TraceBreakpoint() {
    } // TraceBreakpoint

    /**
     * Creates a TraceBreakpoint event with the given class and
     * thread filters.
     *
     * @param  classes  comma-separated list of class filters,
     *                  or null if none are given.
     * @param  threads  comma-separated list of thread filters,
     *                  or null if none are given.
     */
    public TraceBreakpoint(String classes, String threads) {
        // By default we do not suspend the VM.
        super.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        if (classes != null) {
            super.setClassFilters(classes);
        }
        if (threads != null) {
            super.setThreadFilters(threads);
        }
    } // TraceBreakpoint

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
     * Create the method entry and exit event requests.
     */
    protected void createRequests() {
        Session session = getBreakpointGroup().getSession();
        if (!session.isActive()) {
            // Nothing we can do right now.
            return;
        }
        VirtualMachine vm = session.getConnection().getVM();
        EventRequestManager erm = vm.eventRequestManager();

        // Delete the old requests, if any.
        deleteRequests();

        // Create the new requests.
        entryRequest = erm.createMethodEntryRequest();
        entryRequest.putProperty("breakpoint", this);
        entryRequest.setSuspendPolicy(getSuspendPolicy());

        exitRequest = erm.createMethodExitRequest();
        exitRequest.putProperty("breakpoint", this);
        exitRequest.setSuspendPolicy(getSuspendPolicy());

        // Apply thread filters.
        String filtersStr = getThreadFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = Strings.stringToList(filtersStr);
            for (int ii = 0; ii < filters.size(); ii++) {
                String tid = (String) filters.get(ii);
                ThreadReference thread = Threads.getThreadByID(vm, tid);
                if (thread != null) {
                    entryRequest.addThreadFilter(thread);
                    exitRequest.addThreadFilter(thread);
                } else {
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING,
                        Bundle.getString("noSuchThreadFilter") + ' ' + tid);
                }
            }
        }

        // Apply class filters.
        filtersStr = getClassFilters();
        if (filtersStr != null && filtersStr.length() > 0) {
            List filters = Strings.stringToList(filtersStr);
            for (int ii = 0; ii < filters.size(); ii++) {
                String filter = (String) filters.get(ii);
                entryRequest.addClassFilter(filter);
                exitRequest.addClassFilter(filter);
            }
        }

        // Enable the requests so they work.
        entryRequest.setEnabled(isEnabled());
        exitRequest.setEnabled(isEnabled());
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
     * Delete the method entry and exit event requests.
     */
    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (entryRequest != null) {
                VirtualMachine vm = entryRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(entryRequest);
            }
            if (exitRequest != null) {
                VirtualMachine vm = exitRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(exitRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        }
        entryRequest = null;
        exitRequest = null;
    } // deleteRequests

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        super.destroy();
        deleteRequests();

        // We need to stop listening for events.
        Session session = getBreakpointGroup().getSession();
        session.removeListener(this);
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.removeListener(MethodEntryEvent.class, this);
        vmeman.removeListener(MethodExitEvent.class, this);
    } // destroy

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        BasicBreakpointUI bbui = new BasicBreakpointUI(this);
        bbui.addClassFilter();
        bbui.addThreadFilter();
        return bbui;
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        // We need to listen for breakpoint events.
        Session session = getBreakpointGroup().getSession();
        session.addListener(this);
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(MethodEntryEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
        vmeman.addListener(MethodExitEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
    } // init

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  always returns true.
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
     * This breakpoint has caused the debuggee VM to stop. Execute all
     * monitors associated with this breakpoint. If the breakpoint is
     * locatable, perform the usual operations that go along with a
     * locatable event.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        // Show the trace information.
        Session session = getBreakpointGroup().getSession();
        StringBuffer buf = new StringBuffer(80);
        if (e instanceof MethodEntryEvent) {
            buf.append(Bundle.getString("traceEntered"));
        } else if (e instanceof MethodExitEvent) {
            buf.append(Bundle.getString("traceExited"));
        } else {
            buf.append("unexpected event: ");
            buf.append(e);
        }
        buf.append(' ');
        buf.append(showEventLocation((LocatableEvent) e));
        session.getUIAdapter().showMessage(
            UIAdapter.MESSAGE_NOTICE, buf.toString());

        // Get the monitor list iterator.
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        Iterator iter = monitorList.iterator();
        while (iter.hasNext()) {
            Monitor monitor = (Monitor) iter.next();
            monitor.perform(session);
        }

        // We always resume since we are 'trace'.
        return true;
    } // performStop

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
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants
     * for suspending threads. The breakpoint must be disabled
     * before calling this method.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    public void setSuspendPolicy(int policy) {
        super.setSuspendPolicy(policy);
        if (entryRequest != null) {
            entryRequest.setSuspendPolicy(getSuspendPolicy());
        }
        if (exitRequest != null) {
            exitRequest.setSuspendPolicy(getSuspendPolicy());
        }
    } // setSuspendPolicy

    /**
     * Return a String describing the location of the given event.
     *
     * @param  le  locatable event.
     * @return  String descriptor.
     */
    protected String showEventLocation(LocatableEvent le) {
        StringBuffer buf = new StringBuffer(80);
        Location loc = le.location();
        Method method = loc.method();

        // Show the method return type.
        buf.append(method.returnTypeName());
        buf.append(' ');

        // Show the class type and method name.
        String tname = loc.declaringType().name();
        tname = Names.justTheName(tname);
        buf.append(tname);
        buf.append('.');
        buf.append(method.name());

        // Show the method arguments.
        buf.append('(');
        List args = method.argumentTypeNames();
        buf.append(Strings.listToString(args));
        buf.append(')');

        // Show the current thread name.
        buf.append(" in thread [");
        try {
            buf.append(le.thread().name());
        } catch (VMDisconnectedException vmde) {
            // This will happen for the last method exit when the events
            // do not suspend the debuggee.
            buf.append(le.thread().uniqueID());
        }
        buf.append(']');
        return buf.toString();
    } // showEventLocation

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
        buf.append("trace");
        String filters = getClassFilters();
        if (filters != null && filters.length() > 0) {
            buf.append(", class ");
            buf.append(filters);
        }
        filters = getThreadFilters();
        if (filters != null && filters.length() > 0) {
            buf.append(", thread ");
            buf.append(filters);
        }

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
} // TraceBreakpoint
