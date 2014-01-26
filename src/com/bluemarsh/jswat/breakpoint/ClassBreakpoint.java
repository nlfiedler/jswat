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
 * $Id: ClassBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.ClassBreakpointUI;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.util.Strings;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class ClassBreakpoint implements the Breakpoint interface. It stops
 * each time a particular class is loaded or unloaded during the
 * execution of the debuggee program.
 *
 * @author  Nathan Fiedler
 */
public class ClassBreakpoint extends AbstractBreakpoint
    implements SessionListener {
    /** True to stop on prepare. */
    private boolean onPrepare;
    /** True to stop on unload. */
    private boolean onUnload;
    /** Class prepare event request. */
    private ClassPrepareRequest prepareRequest;
    /** Class unload event request. */
    private ClassUnloadRequest unloadRequest;

    /**
     * Default constructor for deserialization.
     */
    ClassBreakpoint() {
    } // ClassBreakpoint

    /**
     * Creates a ClassBreakpoint event with the given class filters.
     *
     * @param  classes  comma-separated list of class filters,
     *                  or null if none are given.
     * @param  prepare  true to stop on class prepare.
     * @param  unload   true to stop on class unload.
     */
    public ClassBreakpoint(String classes, boolean prepare, boolean unload) {
        if (classes != null) {
            super.setClassFilters(classes);
        }
        this.onPrepare = prepare;
        this.onUnload = unload;
    } // ClassBreakpoint

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
     * Create the class prepare and unload event requests.
     */
    protected void createRequests() {
        Session session = getBreakpointGroup().getSession();
        if (!session.isActive()) {
            // Nothing we can do right now.
            return;
        }
        EventRequestManager erm = session.getVM().eventRequestManager();

        // Delete the old requests, if any.
        deleteRequests();

        String filtersStr = getClassFilters();
        List filters = null;
        if (filtersStr != null && filtersStr.length() > 0) {
            filters = Strings.stringToList(filtersStr);
        }

        // Create the new requests.
        if (onPrepare) {
            prepareRequest = erm.createClassPrepareRequest();
            prepareRequest.putProperty("breakpoint", this);
            prepareRequest.setSuspendPolicy(getSuspendPolicy());
            if (filters != null) {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String filter = (String) filters.get(ii);
                    prepareRequest.addClassFilter(filter);
                }
            }
            prepareRequest.setEnabled(isEnabled());
        }

        if (onUnload) {
            unloadRequest = erm.createClassUnloadRequest();
            unloadRequest.putProperty("breakpoint", this);
            unloadRequest.setSuspendPolicy(getSuspendPolicy());
            if (filters != null) {
                for (int ii = 0; ii < filters.size(); ii++) {
                    String filter = (String) filters.get(ii);
                    unloadRequest.addClassFilter(filter);
                }
            }
            unloadRequest.setEnabled(isEnabled());
        }
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
     * Delete the class prepare and unload event requests.
     */
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
        }
        prepareRequest = null;
        unloadRequest = null;
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
        vmeman.removeListener(ClassPrepareEvent.class, this);
        vmeman.removeListener(ClassUnloadEvent.class, this);
    } // destroy

    /**
     * Returns the stop-on-prepare status.
     *
     * @return  true if stopping when class prepares.
     */
    public boolean getStopOnPrepare() {
        return onPrepare;
    } // getStopOnPrepare

    /**
     * Returns the stop-on-unload status.
     *
     * @return  true if stopping when class unloads.
     */
    public boolean getStopOnUnload() {
        return onUnload;
    } // getStopOnUnload

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new ClassBreakpointUI(this);
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
        vmeman.addListener(ClassPrepareEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
        vmeman.addListener(ClassUnloadEvent.class, this,
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
        String cname = null;
        if (e instanceof ClassPrepareEvent) {
            buf.append(Bundle.getString("classPrepared"));
            cname = ((ClassPrepareEvent) e).referenceType().name();
        } else if (e instanceof ClassUnloadEvent) {
            buf.append(Bundle.getString("classUnloaded"));
            cname = ((ClassUnloadEvent) e).className();
        } else {
            return true;
        }
        buf.append(' ');
        buf.append(cname);
        return performStop(e, buf.toString());
    } // performStop

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        onPrepare = prefs.getBoolean("onPrepare", true);
        onUnload = prefs.getBoolean("onUnload", true);
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
     * Sets the stop-on-prepare status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when class prepares.
     */
    public void setStopOnPrepare(boolean stop) {
        onPrepare = stop;
    } // setStopOnPrepare

    /**
     * Sets the stop-on-unload status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when class unloads.
     */
    public void setStopOnUnload(boolean stop) {
        onUnload = stop;
    } // setStopOnUnload

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
        if (prepareRequest != null) {
            prepareRequest.setSuspendPolicy(getSuspendPolicy());
        }
        if (unloadRequest != null) {
            unloadRequest.setSuspendPolicy(getSuspendPolicy());
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
        buf.append("class");
        String filters = getClassFilters();
        if (filters != null && filters.length() > 0) {
            buf.append(' ');
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
        prefs.putBoolean("onPrepare", onPrepare);
        prefs.putBoolean("onUnload", onUnload);
        return true;
    } // writeObject
} // ClassBreakpoint
