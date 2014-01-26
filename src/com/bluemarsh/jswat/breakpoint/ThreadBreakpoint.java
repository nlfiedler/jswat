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
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        ThreadBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/08/02        Initial version
 *      nf      07/26/02        Fixed bug 596
 *      nf      08/10/02        Fixed bug 606
 *
 * $Id: ThreadBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.ThreadBreakpointUI;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.util.prefs.Preferences;

/**
 * Class ThreadBreakpoint implements the Breakpoint interface. It stops
 * each time a thread has started or stopped during the execution of the
 * debuggee program.
 *
 * @author  Nathan Fiedler
 */
public class ThreadBreakpoint extends AbstractBreakpoint
    implements SessionListener {
    /** Thread name, or null if not given. */
    private String threadName;
    /** True to stop on start. */
    private boolean onStart;
    /** True to stop on death. */
    private boolean onDeath;
    /** Thread start event request. */
    private ThreadStartRequest startRequest;
    /** Thread death event request. */
    private ThreadDeathRequest deathRequest;

    /**
     * Default constructor for deserialization.
     */
    ThreadBreakpoint() {
    } // ThreadBreakpoint

    /**
     * Creates a ThreadBreakpoint.
     *
     * @param  thread  thread name, or null for no filter.
     * @param  start   true to stop on thread start.
     * @param  death   true to stop on thread death.
     */
    public ThreadBreakpoint(String thread, boolean start, boolean death) {
        this.threadName = thread;
        this.onStart = start;
        this.onDeath = death;
    } // ThreadBreakpoint

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
        EventRequestManager erm = session.getVM().eventRequestManager();

        // Delete the old requests, if any.
        deleteRequests();

        // Create the new requests.
        if (onStart) {
            startRequest = erm.createThreadStartRequest();
            startRequest.putProperty("breakpoint", this);
            startRequest.setSuspendPolicy(getSuspendPolicy());
            startRequest.setEnabled(isEnabled());
        }

        if (onDeath) {
            deathRequest = erm.createThreadDeathRequest();
            deathRequest.putProperty("breakpoint", this);
            deathRequest.setSuspendPolicy(getSuspendPolicy());
            deathRequest.setEnabled(isEnabled());
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
     * Delete the thread start and death event requests.
     */
    protected void deleteRequests() {
        // Delete the old requests, if any.
        try {
            if (startRequest != null) {
                VirtualMachine vm = startRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(startRequest);
            }
            if (deathRequest != null) {
                VirtualMachine vm = deathRequest.virtualMachine();
                EventRequestManager erm = vm.eventRequestManager();
                erm.deleteEventRequest(deathRequest);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens all the time.
        }
        startRequest = null;
        deathRequest = null;
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
        vmeman.removeListener(ThreadStartEvent.class, this);
        vmeman.removeListener(ThreadDeathEvent.class, this);
    } // destroy

    /**
     * Returns the stop-on-death status.
     *
     * @return  true if stopping when thread dies.
     */
    public boolean getStopOnDeath() {
        return onDeath;
    } // getStopOnDeath

    /**
     * Returns the stop-on-start status.
     *
     * @return  true if stopping when thread starts.
     */
    public boolean getStopOnStart() {
        return onStart;
    } // getStopOnStart

    /**
     * Returns the thread breakpoint's thread name.
     *
     * @return  name of the thread this breakpoint is watching.
     */
    public String getThreadName() {
        return threadName;
    } // getThreadName

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new ThreadBreakpointUI(this);
    } // getUIAdapter

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
        Session session = getBreakpointGroup().getSession();
        session.addListener(this);
        // We need to listen for thread start and death events.
        // Just listen for both of them in any case.
        VMEventManager vmeman = (VMEventManager)
            session.getManager(VMEventManager.class);
        vmeman.addListener(ThreadStartEvent.class, this,
                           VMEventListener.PRIORITY_BREAKPOINT);
        vmeman.addListener(ThreadDeathEvent.class, this,
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
        // Is this the thread we are interested in?
        ThreadReference thread = null;
        if (e instanceof ThreadStartEvent && onStart) {
            thread = ((ThreadStartEvent) e).thread();
        } else if (e instanceof ThreadDeathEvent && onDeath) {
            thread = ((ThreadDeathEvent) e).thread();
        } else {
            return true;
        }

        String name = null;
        try {
            name = thread.name();
        } catch (VMDisconnectedException vmde) {
            // Happens when event does not suspend.
            return true;
        }
        if (threadName != null && threadName.length() > 0) {
            if (!threadName.equals(name)) {
                // Not a thread we are interested in.
                return true;
            }
        }

        // Show the thread event information.
        StringBuffer buf = new StringBuffer(80);
        if (e instanceof ThreadStartEvent && onStart) {
            buf.append(Bundle.getString("threadStarted"));
        } else if (e instanceof ThreadDeathEvent && onDeath) {
            buf.append(Bundle.getString("threadDied"));
        }
        buf.append(' ');
        if (name != null && name.length() > 0) {
            buf.append('"');
            buf.append(name);
            buf.append('"');
        } else {
            buf.append(thread.uniqueID());
        }
        Session session = getBreakpointGroup().getSession();
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
        threadName = prefs.get("threadName", null);
        onStart = prefs.getBoolean("onStart", true);
        onDeath = prefs.getBoolean("onDeath", true);
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
     * Sets the stop-on-death status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when thread dies.
     */
    public void setStopOnDeath(boolean stop) {
        onDeath = stop;
    } // setStopOnDeath

    /**
     * Sets the stop-on-start status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when thread starts.
     */
    public void setStopOnStart(boolean stop) {
        onStart = stop;
    } // setStopOnStart

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
        if (startRequest != null) {
            startRequest.setSuspendPolicy(getSuspendPolicy());
        }
        if (deathRequest != null) {
            deathRequest.setSuspendPolicy(getSuspendPolicy());
        }
    } // setSuspendPolicy

    /**
     * Sets the thread breakpoint's thread name.
     *
     * @param  name  name of the thread to watch.
     */
    public void setThreadName(String name) {
        threadName = name;
    } // setThreadName

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
        buf.append("thread");
        if (threadName != null && threadName.length() > 0) {
            buf.append(' ');
            buf.append(threadName);
        }

        if (!terse) {
            if (onStart) {
                buf.append(" start");
            }
            if (onDeath) {
                buf.append(" death");
            }
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
        if (threadName == null) {
            prefs.remove("threadName");
        } else {
            prefs.put("threadName", threadName);
        }
        prefs.putBoolean("onStart", onStart);
        prefs.putBoolean("onDeath", onDeath);
        return true;
    } // writeObject
} // ThreadBreakpoint
