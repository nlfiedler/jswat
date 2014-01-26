/*
 *      Copyright (C) 1999-2014 Nathan Fiedler
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
 */
package com.bluemarsh.jswat.event;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Manager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.PriorityList;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for maintaining a list of all the objects
 * interested in events sent from the back-end of the JPDA debugger. Listeners
 * registered for VM events are listed according to the event they are
 * interested in. Within each of these lists the listeners are sorted in
 * priority order. Those with a higher priority will be notified of the event
 * before those of a lower priority.
 * <p>
 * @author Nathan Fiedler
 */
public class VMEventManager implements Manager, Runnable {

    /**
     * A null array to be shared by all empty listener lists.
     */
    private static final Object[] NULL_ARRAY = new Object[0];
    /**
     * Logger.
     */
    private static final Logger logger;
    /**
     * VM event queue.
     */
    private EventQueue eventQueue;
    /**
     * True if we are connected to the debuggee VM.
     */
    private boolean vmConnected;
    /**
     * Owning session.
     */
    private Session owningSession;
    /**
     * The list of event class-listener pairs.
     */
    private Object[] listenerList;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.event");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Creates a new VMEventManager object.
     */
    public VMEventManager() {
        listenerList = NULL_ARRAY;
    } // VMEventManager

    /**
     * Called when the Session has activated. This occurs when the debuggee has
     * launched or has been attached to the debugger.
     * <p>
     * @param sevt session event.
     */
    public void activated(SessionEvent sevt) {
        // Start the event handling thread. Continuously monitors
        // the VM for new events.
        eventQueue = sevt.getSession().getVM().eventQueue();
        vmConnected = true;
        new Thread(this, "event handler").start();
        logger.info("event listening thread started");
    } // activated

    /**
     * Adds the given listener as a listener for events of the given type. When
     * an event of type <code>event</code> occurs, all registered listeners for
     * that type will be notified.
     * <p>
     * @param event    VM event to listen for.
     * @param listener Listener to add for event.
     * @param priority Priority for this listener (1-255), where higher values
     *                 give higher priority.
     */
    public synchronized void addListener(Class event, VMEventListener listener,
            int priority) {
        // Do the usual arguments checking.
        if ((priority > VMEventListener.PRIORITY_HIGHEST)
                || (priority < VMEventListener.PRIORITY_LOWEST)) {
            throw new IllegalArgumentException("priority out of range");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        // Handle the special-case priorities.
        if ((priority == VMEventListener.PRIORITY_BREAKPOINT)
                && !(listener instanceof Breakpoint)) {
            throw new IllegalArgumentException(
                    "priority only for breakpoints");
        }
        if ((priority == VMEventListener.PRIORITY_SESSION)
                && !(listener instanceof Session)) {
            throw new IllegalArgumentException("priority only for Session");
        }

        PriorityList list = null;
        if (listenerList == NULL_ARRAY) {
            // If this is the first listener added, initialize the lists.
            list = new PriorityList();
            listenerList = new Object[]{event, list};
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
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "added {0} listener: {1}",
                    new Object[]{Names.justTheName(event.getName()),
                        Names.justTheName(listener.getClass().getName())});
        }
    } // addListener

    /**
     * Called when the Session is about to be closed.
     * <p>
     * @param sevt session event.
     */
    public void closing(SessionEvent sevt) {
        owningSession = null;
        eventQueue = null;
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no longer
     * connected to the Session.
     * <p>
     * @param sevt session event.
     */
    public void deactivated(SessionEvent sevt) {
    } // deactivated

    /**
     * Get the priority list matching the given event. Checks the event's class
     * and whether it "is an instance of" any of the events in our list.
     * <p>
     * @param event VM event to find in list.
     * @return PriorityList if found, or null.
     */
    protected synchronized PriorityList getList(Object event) {
        for (int i = listenerList.length - 2; i >= 0; i -= 2) {
            if (((Class) listenerList[i]).isInstance(event)) {
                return (PriorityList) listenerList[i + 1];
            }
        }
        return null;
    } // getList

    /**
     * Called after the Session has added this listener to the Session listener
     * list.
     * <p>
     * @param session the Session.
     */
    public void opened(Session session) {
        owningSession = session;
    } // opened

    /**
     * Send the given event to the listeners on the list. The listeners are
     * notified in order of the priority enforced by the list.
     * <p>
     * @param event     event to process.
     * @param listeners list of listeners to handle event.
     * @return true to resume VM, false to suspend VM.
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
     * <p>
     * @param event    VM event to listen for.
     * @param listener Listener to remove from list.
     */
    public synchronized void removeListener(Class event,
            VMEventListener listener) {
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
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "removed {0} listener: {1}",
                        new Object[]{Names.justTheName(event.getName()),
                            Names.justTheName(listener.getClass().getName())});
            }
        }
        // Note that we never remove the priority list from our array.
        // Why bother when we may just have to re-add it anyway?
    } // removeListener

    /**
     * Called when the debuggee is about to be resumed.
     * <p>
     * @param sevt session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Start waiting for events from the back-end of the JPDA debugger. When
     * events occur the notifications will be sent out to all of the registered
     * listeners. This thread dies automatically when the debuggee VM
     * disconnects from the debugger.
     * <p>
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
                    if (logger.isLoggable(Level.INFO)) {
                        logger.log(Level.INFO, "received event: {0}", event);
                    }

                    // Notify the appropriate listeners of the event.
                    PriorityList list = getList(event);
                    if (list != null) {
                        // processEvent() returns true if we should resume.
                        shouldResume &= processEvent(event, list);
                        if (logger.isLoggable(Level.INFO)) {
                            logger.log(Level.INFO, "processed event: {0}", event);
                        }
                    }
                }
                if (shouldResume) {
                    // Resume only if everyone said it was okay to go.
                    set.resume();
                    logger.info("resuming VM through event set");
                }
            } catch (InterruptedException ie) {
                // Nothing left to do but leave.
                break;
            } catch (VMDisconnectedException vmde) {
                // Notify the session and break out.
                owningSession.disconnected(this);
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

    /**
     * Called when the debuggee has been suspended.
     * <p>
     * @param sevt session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // VMEventManager
