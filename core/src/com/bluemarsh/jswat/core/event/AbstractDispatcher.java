/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractDispatcher.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.stepping.Stepper;
import com.sun.jdi.event.Event;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * AbstractDispatcher prioritizes the registered listeners according to their
 * class -- breakpoints get events first, then Sessions, and lastly all other
 * classes. This is the preferred base class for concrete
 * <code>Dispatcher</code> implementations.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractDispatcher implements Dispatcher {
    /** The list of event class-listener pairs.  */
    private List<Entry> listenerList;

    /**
     * Constructs a new instance of AbstractDispatcher.
     */
    protected AbstractDispatcher() {
        listenerList = new LinkedList<Entry>();
    }

    public void addListener(DispatcherListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        // Add the listener to the appropriate list entry.
        synchronized (listenerList) {
            Iterator<Class> events = listener.eventTypes();
            while (events.hasNext()) {
                Class type = events.next();
                Entry entry = findEntry(type);
                if (entry == null) {
                    entry = new Entry(type);
                    listenerList.add(entry);
                }
                entry.add(listener);
            }
        }
    }

    /**
     * Finds an Entry in the listener list that matches the given type.
     *
     * @param  type  an event class.
     * @return  matching Entry, or null if none found.
     */
    protected Entry findEntry(Class type) {
        synchronized (listenerList) {
            for (Entry entry : listenerList) {
                if (entry.getType().equals(type)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * Finds an Entry in the listener list that matches the given event.
     *
     * @param  event  JDI event for which to find matching entry.
     * @return  matching Entry, or null if none found.
     */
    protected Entry findEntry(Event event) {
        synchronized (listenerList) {
            for (Entry entry : listenerList) {
                if (entry.getType().isInstance(event)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public void removeListener(DispatcherListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        synchronized (listenerList) {
            Iterator<Class> events = listener.eventTypes();
            while (events.hasNext()) {
                Class type = events.next();
                Entry entry = findEntry(type);
                if (entry != null) {
                    entry.remove(listener);
                }
            }
        }
    }

    /**
     * Represents an entry in the event listener list. Each entry has a
     * JDI event class and a prioritized list of listeners for that
     * type of event. This class acts as a listener itself, passing on
     * the events to the real listeners in the priority order.
     *
     * @author  Nathan Fiedler
     */
    protected static class Entry implements DispatcherListener {
        /** The class of events this entry corresponds to. */
        private Class eventClass;
        /** List of listeners of type Stepper. */
        private DispatcherListener stepperList;
        /** List of listeners of type Breakpoint. */
        private DispatcherListener breakpointList;
        /** List of listeners of type Session. */
        private DispatcherListener sessionList;
        /** List of listeners of that are not breakpoints or sessions. */
        private DispatcherListener normalList;

        /**
         * Constructs an entry for a given event type.
         *
         * @param  type  event class.
         */
        public Entry(Class type) {
            eventClass = type;
        }

        /**
         * Adds the listener to this event type.
         *
         * @param  listener  listener to add to this event class.
         */
        public void add(DispatcherListener listener) {
            if (listener instanceof Stepper) {
                stepperList = DispatcherEventMulticaster.add(stepperList, listener);
            } else if (listener instanceof Breakpoint) {
                breakpointList = DispatcherEventMulticaster.add(breakpointList, listener);
            } else if (listener instanceof Session) {
                sessionList = DispatcherEventMulticaster.add(sessionList, listener);
            } else {
                normalList = DispatcherEventMulticaster.add(normalList, listener);
            }
        }

        public boolean equals(Object o) {
            if (o instanceof Entry) {
                Entry entry = (Entry) o;
                return eventClass.equals(entry.eventClass);
            } else {
                return false;
            }
        }

        public boolean eventOccurred(DispatcherEvent e) {
            boolean shouldResume = true;
            // Notify the listeners in the priority order.
            DispatcherListener listener = stepperList;
            if (listener != null) {
                shouldResume &= listener.eventOccurred(e);
            }
            listener = breakpointList;
            if (listener != null) {
                shouldResume &= listener.eventOccurred(e);
            }
            listener = sessionList;
            if (listener != null) {
                shouldResume &= listener.eventOccurred(e);
            }
            listener = normalList;
            if (listener != null) {
                shouldResume &= listener.eventOccurred(e);
            }
            return shouldResume;
        }

        public Iterator<Class> eventTypes() {
            // This will never be called by the event handler.
            return null;
        }

        /**
         * Returns the event type this entry is listening to.
         *
         * @return  event class.
         */
        public Class getType() {
            return eventClass;
        }

        public int hashCode() {
            return eventClass.hashCode();
        }

        /**
         * Removes the listener from this event type.
         *
         * @param  listener  listener to remove from this event class.
         */
        public void remove(DispatcherListener listener) {
            if (listener instanceof Stepper) {
                stepperList = DispatcherEventMulticaster.remove(stepperList, listener);
            } else if (listener instanceof Breakpoint) {
                breakpointList = DispatcherEventMulticaster.remove(breakpointList, listener);
            } else if (listener instanceof Session) {
                sessionList = DispatcherEventMulticaster.remove(sessionList, listener);
            } else {
                normalList = DispatcherEventMulticaster.remove(normalList, listener);
            }
        }
    }
}
