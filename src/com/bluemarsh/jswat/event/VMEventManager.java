/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: VMEventManager.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.event;

import com.bluemarsh.adt.PriorityList;
import com.bluemarsh.jswat.DefaultManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;

/**
 * This class is responsible for maintaining a list of all the
 * objects interested in events sent from the back-end of the
 * JPDA debugger. Listeners registered for VM events are listed
 * according to the event they are interested in. Within each
 * of these lists the listeners are sorted in priority order.
 * Those with a higher priority will be notified of the event
 * before those of a lower priority.
 *
 * @author  Nathan Fiedler
 */
public class VMEventManager extends DefaultManager implements Runnable {
    /** A null array to be shared by all empty listener lists. */
    protected final static Object[] NULL_ARRAY = new Object[0];
    /** Reporting category. */
    protected static Category logCategory = Category.instanceOf("event");
    /** VM event queue. */
    protected EventQueue eventQueue;
    /** True if we are connected to the debuggee VM. */
    protected boolean vmConnected;
    /** Owning session. */
    protected Session owningSession;
    /** The list of event class-listener pairs.  */
    protected Object[] listenerList;

    /**
     * Creates a new VMEventManager object.
     */
    public VMEventManager() {
        listenerList = NULL_ARRAY;
    } // VMEventManager

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     * Managers are not activated in any particular order.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        // Start the event handling thread. Continuously monitors
        // the VM for new events.
        eventQueue = session.getVM().eventQueue();
        vmConnected = true;
        new Thread(this, "event handler").start();
        logCategory.report("event listening thread started");
    } // activate

    /**
     * Adds the given listener as a listener for events of the
     * given type. When an event of type <code>event</code> occurs,
     * all registered listeners for that type will be notified.
     *
     * @param  event     VM event to listen for.
     * @param  listener  Listener to add for event.
     * @param  priority  Priority for this listener (1-255), where
     *                   higher values give higher priority.
     * @exception  IllegalArgumentException
     *             Thrown if listener is null or priority out of bounds.
     */
    public void addListener(Class event, VMEventListener listener,
                            int priority) {
        // Do the usual arguments checking.
        if ((priority > VMEventListener.PRIORITY_HIGHEST) ||
            (priority < VMEventListener.PRIORITY_LOWEST)) {
            throw new IllegalArgumentException("priority out of range");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        // Handle the special-case priorities.
        if ((priority == VMEventListener.PRIORITY_BREAKPOINT) &&
            !(listener instanceof Breakpoint)) {
            throw new IllegalArgumentException(
                "priority only for breakpoints");
        }
        if ((priority == VMEventListener.PRIORITY_SESSION) &&
            !(listener instanceof Session)) {
            throw new IllegalArgumentException("priority only for Session");
        }

        PriorityList list = null;
        if (listenerList == NULL_ARRAY) {
            // If this is the first listener added, initialize the lists.
            list = new PriorityList();
            listenerList = new Object[] { event, list };
        } else {

            // Find the event in our list, if any.
            for (int i = listenerList.length - 2; i >= 0; i -= 2) {
                if (event == listenerList[i]) {
                    list = (PriorityList) listenerList[i + 1];
                }
            }
            if (list == null) {
                // This is a new event, create a new listener list.
                list = new PriorityList();
                // Copy the array and add the new listener list.
                int i = listenerList.length;
                Object[] tmp = new Object[i + 2];
                System.arraycopy(listenerList, 0, tmp, 0, i);
                tmp[i] = event;
                tmp[i + 1] = list;
                listenerList = tmp;
            }
        }
        // Add the listener to the event's listener list.
        list.add(listener, priority);
        if (logCategory.isEnabled()) {
            logCategory.report(
                "added " + ClassUtils.justTheName(event.getName()) +
                " listener: " +
                ClassUtils.justTheName(listener.getClass().getName()));
        }
    } // addListener

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
        owningSession = null;
        eventQueue = null;
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     * Managers are not deactivated in any particular order.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
    } // deactivate

    /**
     * Get the priority list matching the given event. Checks
     * the event's class and whether it "is an instance of" any
     * of the events in our list.
     *
     * @param  event  VM event to find in list.
     * @return  PriorityList if found, or null.
     */
    protected PriorityList getList(Object event) {
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (((Class) listenerList[i]).isInstance(event)) {
                return (PriorityList) listenerList[i + 1];
            }
        }
        return null;
    } // getList

    /**
     * Called after the Session has instantiated this mananger.
     * To avoid problems with circular dependencies between managers,
     * iniitialize data members before calling
     * <code>Session.getManager()</code>.
     *
     * @param  session  Session initializing this manager.
     */
    public void init(Session session) {
        owningSession = session;
    } // init

    /**
     * Send the given event to the listeners on the list. The listeners
     * are notified in order of the priority enforced by the list.
     *
     * @param  event      event to process.
     * @param  listeners  list of listeners to handle event.
     * @return  true to resume VM, false to suspend VM.
     */
    protected boolean processEvent(Event event, PriorityList listeners) {
        // Process the listeners in priority order, as enforced
        // by the priority list object.
        boolean shouldResume = true;
        for (int i = 0; i < listeners.size(); i++) {
            VMEventListener vml = (VMEventListener) listeners.get(i);
            // Let the listener know about the event.
            try {
                // The listener will indicate if we should resume
                // the debuggee VM or not. All listeners must agree
                // to resume for the debuggee VM to run again.
                shouldResume &= vml.eventOccurred(event);
            } catch (VMDisconnectedException vmde) {
                throw vmde;
            } catch (Exception e) {
                Log out = owningSession.getStatusLog();
                out.writeStackTrace(e);
                out.writeln("Event processing continuing...");
                // Assume that we should stop after processing the listeners.
                shouldResume = false;
            }
        }
        return shouldResume;
    } // processEvent

    /**
     * Removes the given listener from the event listener list.
     *
     * @param  event     VM event to listen for.
     * @param  listener  Listener to remove from list.
     */
    public void removeListener(Class event, VMEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        // Is the event in our list?
        int index = -1;
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (listenerList[i] == event) {
                index = i;
                break;
            }
        }

        // If so, remove the listener from the list.
        if (index != -1) {
            PriorityList list = (PriorityList) listenerList[index + 1];
            list.remove(listener);
            if (logCategory.isEnabled()) {
                logCategory.report(
                    "removed " + ClassUtils.justTheName(event.getName()) +
                    " listener: " +
                    ClassUtils.justTheName(listener.getClass().getName()));
            }
        }
        // Note that we never remove the priority list from our array.
        // Why bother when we may just have to re-add it anyway?
    } // removeListener

    /**
     * Start waiting for events from the back-end of the JPDA debugger.
     * When events occur the notifications will be sent out to all of
     * the registered listeners. This thread dies automatically when
     * the debuggee VM disconnects from the debugger.
     *
     * @see #activate
     * @see #deactivate
     */
    public void run() {
        // Run until we get interrupted or the VM disconnects.
        while (true) {
            try {
                // ..wait for events to happen.
                EventSet set = eventQueue.remove();
                EventIterator iter = set.eventIterator();
                boolean shouldResume = true;
                while (iter.hasNext()) {
                    Event event = (Event) iter.next();
                    if (logCategory.isEnabled()) {
                        logCategory.report("received event: " + event);
                    }

                    // Notify the appropriate listeners of the event.
                    PriorityList list = (PriorityList) getList(event);
                    if (list != null) {
                        // processEvent() returns true if we should resume.
                        shouldResume &= processEvent(event, list);
                        if (logCategory.isEnabled()) {
                            logCategory.report("processed event: " + event);
                        }
                    }
                }
                if (shouldResume) {
                    // Resume only if everyone said it was okay to go.
                    set.resume();
                    logCategory.report("resuming VM through event set");
                }
            } catch (InterruptedException ie) {
                // Nothing left to do but leave.
                break;
            } catch (VMDisconnectedException vmde) {
                // Notify the session and break out.
                owningSession.disconnected();
                break;
            } catch (Exception e) {
                // We do not care what FindBugs says about this catch.
                // We absolutely cannot terminate this loop -- catch the
                // exception, report it, and continue reading events.
                Log out = owningSession.getStatusLog();
                out.writeStackTrace(e);
            }
        }
    } // run
} // VMEventManager
