/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        SessionListenerList.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/03/00        Initial version
 *      nf      04/15/01        Changed to use report.Category.
 *
 * DESCRIPTION:
 *      Contains the class that defines the list for containing
 *      SessionListener objects.
 *
 * $Id: SessionListenerList.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.util.LockException;
import com.bluemarsh.util.SimpleLock;
import java.util.Vector;

/**
 * Class SessionListenerList provides a means for holding a set
 * of SessionListener objects and performing various actions on
 * them. These actions include adding, removing, activating,
 * deactivating, and closing the listeners. This class is written
 * to ensure that the listeners are processed exactly once for
 * each operation.
 *
 * @author  Nathan Fiedler
 */
class SessionListenerList {
    /** List is doing nothing at this time. */
    public static final int LIST_NOOP = 0;
    /** List is presently closing all the listeners. */
    public static final int LIST_CLOSING = 1;
    /** List is presently activating all the listeners. */
    public static final int LIST_ACTIVATING = 2;
    /** List is presently deactivating all the listeners. */
    public static final int LIST_DEACTIVATING = 3;
    /** Trace reporting category. */
    private static Category logCategory =
        Category.instanceOf("session_listener_list");
    /** List of listeners wrapped inside Entry objects. */
    protected Vector listenerList;
    /** Current state of the list. */ 
    protected volatile int listState;
    /** Set to true if the list has been modified. Used to
     * detect modifications made while iterating the listeners. */
    protected volatile boolean listChanged;

    /**
     * Constructs an empty SessionListenerList to hold SessionListener
     * objects.
     *
     * @param  session  Owning Session object.
     */
    public SessionListenerList(Session session) {
        listenerList = new Vector();
        listState = LIST_NOOP;
    } // SessionListenerList

    /**
     * Retrieves a SessionListener from the list at the given position.
     * This will try to acquire the lock on the listener first.
     *
     * @param  index  Offset into the list.
     * @return  SessionListener if successful, null if error.
     */
    public SessionListener acquireListener(int index) {
        try {
            Entry e = (Entry) listenerList.get(index);
            return e.acquireListener();
        } catch (LockException le) {
            return null;
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }
    } // acquireListener

    /**
     * Activate all of the registered listeners. This method will
     * guarantee that all listeners are activated exactly once.
     * If a listener is added during the activation procedure, it
     * will be activated by the time this method exits.
     *
     * @param  session  activating Session.
     */
    public synchronized void activateAll(Session session) {
        listState = LIST_ACTIVATING;
        do {
            // Start by assuming the list has not changed.
            listChanged = false;
            // Process each listener, possibly activating it.
            int size = listenerList.size();
            if (logCategory.isEnabled()) {
                logCategory.report("SessionListenerList: activating " +
                                   size + " listeners");
            }
            // Walk the list backwards to avoid concurrent removal
            // leading to accessing an invalid entry.
            for (int index = size - 1; (index >= 0) && !listChanged; index--) {
                try {
                    Entry e = (Entry) listenerList.get(index);
                    // Get the lock on the listener for the next few steps.
                    SessionListener listener = e.acquireListener();
                    if (e.getState() == Entry.ENTRY_INACTIVE) {
                        // Activate if inactive.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: activating " +
                                 listener.getClass().getName());
                        }
                        listener.activate(session);
                        e.setState(Entry.ENTRY_ACTIVE);
                    }
                    // We're finished with the listener.
                    e.releaseListener();
                    if (e.shouldRemove) {
                        // This listener has been marked for removal.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: removing " +
                                 listener.getClass().getName());
                        }
                        listenerList.remove(e);
                    }
                } catch (LockException le) {
                    // We do not expect this to ever happen.
                    le.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    // List must have changed.
                    break;
                }
            }
            // Did the list change? Go through again.
        } while (listChanged);
        listState = LIST_NOOP;
        synchronized (this) {
            // Let others know we're done.
            notifyAll();
        }
    } // activateAll

    /**
     * Adds the given listener to the list. Calls the <code>init()</code>
     * method first, then calls <code>activate()</code> if the
     * <code>activate</code> parameter is true.
     *
     * @param  listener  SessionListener to add.
     * @param  session   owning Session.
     * @param  activate  True to activate listener, false otherwise.
     */
    public void add(SessionListener listener, Session session,
                    boolean activate) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        if (logCategory.isEnabled()) {
            logCategory.report("SessionListenerList: initializing " +
                               listener.getClass().getName());
        }
        listener.init(session);
        // Create the entry that will go in the list.
        Entry entry = new Entry(listener);
        if (activate) {
            // Caller wants this listener activated now.
            if (logCategory.isEnabled()) {
                logCategory.report("SessionListenerList: activating " +
                                   listener.getClass().getName());
            }
            listener.activate(session);
            try {
                entry.setState(Entry.ENTRY_ACTIVE);
            } catch (LockException le) {
                // We do not expect this to ever happen.
                le.printStackTrace();
            }
        }
        // Add the new listener to the list.
        if (logCategory.isEnabled()) {
            logCategory.report("SessionListenerList: adding " +
                               listener.getClass().getName());
        }
        listenerList.add(entry);
        // Signal that the list has changed.
        listChanged = true;
    } // add

    /**
     * Clears the entire list of listeners. This will wait until
     * the list is not being accessed by other threads.
     */
    public synchronized void clear() {
        while (listState != LIST_NOOP) {
            // Wait for the list to settle down.
            try {
                wait();
            } catch (InterruptedException ie) {
                break;
            }
        }
        if (logCategory.isEnabled()) {
            logCategory.report("SessionListenerList: clearing list ");
        }
        // Okay, this isn't really safe in that other threads
        // iterating the list will choke when the elements
        // suddenly go away. However, they are all catching
        // that case now so it's no problem.
        listenerList.clear();
        listChanged = true;
    } // clear

    /**
     * Close all of the registered listeners. This method will
     * guarantee that all listeners are closed exactly once.
     * If a listener is added during the activation procedure, it
     * will be closed by the time this method exits.
     *
     * @param  session  closing Session.
     */
    public synchronized void closeAll(Session session) {
        listState = LIST_CLOSING;
        do {
            // Start by assuming the list has not changed.
            listChanged = false;
            // Process each listener, possibly closing it.
            int size = listenerList.size();
            if (logCategory.isEnabled()) {
                logCategory.report("SessionListenerList: closing " +
                                   size + " listeners");
            }
            // Walk the list backwards to avoid concurrent removal
            // leading to accessing an invalid entry.
            for (int index = size - 1; (index >= 0) && !listChanged; index--) {
                try {
                    Entry e = (Entry) listenerList.get(index);
                    // Get the lock on the listener for the next few steps.
                    SessionListener listener = e.acquireListener();
                    int state = e.getState();
                    if (state == Entry.ENTRY_ACTIVE) {
                        // Deactivate if active.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: deactivating " +
                                 listener.getClass().getName());
                        }
                        listener.deactivate(session);
                    }
                    if (state != Entry.ENTRY_CLOSED) {
                        // Close if open.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: closing " +
                                 listener.getClass().getName());
                        }
                        listener.close(session);
                        e.setState(Entry.ENTRY_CLOSED);
                    }
                    // We're finished with the listener.
                    e.releaseListener();
                    if (e.shouldRemove) {
                        // This listener has been marked for removal.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: removing " +
                                 listener.getClass().getName());
                        }
                        listenerList.remove(e);
                    }
                } catch (LockException le) {
                    // We do not expect this to ever happen.
                    le.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    // List must have changed.
                    break;
                }
            }
            // Did the list change? Go through again.
        } while (listChanged);
        listState = LIST_NOOP;
        synchronized (this) {
            // Let others know we're done.
            notifyAll();
        }
    } // closeAll

    /**
     * Deactivate all of the registered listeners. This method will
     * guarantee that all listeners are deactivated exactly once.
     * If a listener is added during the deactivation procedure, it
     * will be deactivated by the time this method exits.
     *
     * @param  session  deactivating Session.
     */
    public synchronized void deactivateAll(Session session) {
        listState = LIST_DEACTIVATING;
        do {
            // Start by assuming the list has not changed.
            listChanged = false;
            // Process each listener, possibly deactivating it.
            int size = listenerList.size();
            if (logCategory.isEnabled()) {
                logCategory.report("SessionListenerList: deactivating " +
                                   size + " listeners");
            }
            // Walk the list backwards to avoid concurrent removal
            // leading to accessing an invalid entry.
            for (int index = size - 1; (index >= 0) && !listChanged; index--) {
                try {
                    Entry e = (Entry) listenerList.get(index);
                    // Get the lock on the listener for the next few steps.
                    SessionListener listener = e.acquireListener();
                    if (e.getState() == Entry.ENTRY_ACTIVE) {
                        // Deactivate if active.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: deactivating " +
                                 listener.getClass().getName());
                        }
                        listener.deactivate(session);
                        e.setState(Entry.ENTRY_INACTIVE);
                    } else if (logCategory.isEnabled()) {
                        logCategory.report
                            ("SessionListenerList: " +
                             listener.getClass().getName() +
                             " not active");
                    }
                    // We're finished with the listener.
                    e.releaseListener();
                    if (e.shouldRemove) {
                        // This listener has been marked for removal.
                        if (logCategory.isEnabled()) {
                            logCategory.report
                                ("SessionListenerList: removing " +
                                 listener.getClass().getName());
                        }
                        listenerList.remove(e);
                    }
                } catch (LockException le) {
                    // We do not expect this to ever happen.
                    le.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    // List must have changed.
                    break;
                }
            }
            // Did the list change? Go through again.
        } while (listChanged);
        listState = LIST_NOOP;
        synchronized (this) {
            // Let others know we're done.
            notifyAll();
        }
    } // deactivateAll

    /**
     * Searches the listener list looking for the Entry that
     * owns this SessionListener.
     *
     * @param  listener  SessionListener to find Entry for.
     * @return  Entry for the listener, or null if not found.
     */
    protected Entry findEntry(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        Entry entry = null;
        do {
            // Start by assuming the list has not changed.
            listChanged = false;
            int size = listenerList.size();
            // Search for the listener in the list.
            // Walk the list backwards to avoid concurrent removal
            // leading to accessing an invalid entry.
            for (int index = size - 1; (index >= 0) && !listChanged; index--) {
                try {
                    entry = (Entry) listenerList.get(index);
                    if (entry.ownsListener(listener)) {
                        // Found it.
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    // List must have changed.
                    break;
                }
            }
        } while (listChanged);
        return entry;
    } // findEntry

    /**
     * Releases a SessionListener back to the list.
     * This will release the lock on the listener.
     *
     * @param  listener  SessionListener to release.
     * @exception  IllegalStateException
     *             Thrown if listener has been removed.
     *             This should never happen.
     */
    public void releaseListener(SessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        Entry entry = findEntry(listener);
        if (entry == null) {
            throw new IllegalStateException("entry removed unexpectedly");
        }
        entry.releaseListener();
    } // releaseListener

    /**
     * Releases a SessionListener back to the list. Uses the given
     * position to try to find the listener. If the listener is no
     * longer at that position, the method will search for the
     * listener in the list.
     * This will release the lock on the listener.
     *
     * @param  listener  SessionListener to release.
     * @param  index     Offset into list where listener was last found.
     * @exception  IllegalStateException
     *             Thrown if listener has been removed.
     *             This should never happen.
     */
    public void releaseListener(SessionListener listener, int index) {
        try {
            Entry e = (Entry) listenerList.get(index);
            if (e.ownsListener(listener)) {
                // Found the same one, release it.
                e.releaseListener();
            } else {
                // It moved in the list, search for it.
                releaseListener(listener);
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            // List must have changed, search for it.
            releaseListener(listener);
        }
    } // releaseListener

    /**
     * Removes the given listener from the list. If the listener is
     * active, it will be deactivated. The <code>close()</code> method
     * of the listener will be called. If the listener was not in
     * the list, nothing will happen.
     *
     * @param  listener  SessionListener to remove from list.
     * @param  session   owning Session.
     */
    public void remove(SessionListener listener, Session session) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }

        Entry entry = findEntry(listener);
        if (entry == null) {
            // Listener has already been removed.
            return;
        }
        if (listState != LIST_NOOP) {
            // If the list is already enumerating, we have nothing
            // to do, except notify ourselves to remove this entry
            // from the list.
            entry.shouldRemove = true;
            // Signal that the list has in effect been changed.
            listChanged = true;
            return;
        }

        // Get the lock on the listener for the next few steps.
        try {
            entry.acquireListener();
            int state = entry.getState();
            if (state == Entry.ENTRY_ACTIVE) {
                // Deactivate if active.
                if (logCategory.isEnabled()) {
                    logCategory.report("SessionListenerList: deactivating " +
                                       listener.getClass().getName());
                }
                listener.deactivate(session);
            }
            if (state != Entry.ENTRY_CLOSED) {
                // Close if open.
                if (logCategory.isEnabled()) {
                    logCategory.report("SessionListenerList: closing " +
                                       listener.getClass().getName());
                }
                listener.close(session);
                entry.setState(Entry.ENTRY_CLOSED);
            }
            // We're finished with the listener.
            entry.releaseListener();
        } catch (LockException le) {
            // We do not expect this to ever happen.
            le.printStackTrace();
        }

        // Remove the listener from the list.
        if (logCategory.isEnabled()) {
            logCategory.report("SessionListenerList: removing " +
                               listener.getClass().getName());
        }
        listenerList.remove(entry);
        // Signal that the list has changed.
        listChanged = true;
    } // remove

    /**
     * Returns the number of elements in the list.
     */
    public int size() {
        return listenerList.size();
    } // size

    /**
     * Class Entry defines the data members for a given SessionListener
     * in the SessionListenerList. It is used to wrap around the
     * listener and control access to it.
     */
    protected class Entry {
        /** Access control lock. */
        private SimpleLock lock;
        /** SessionListener, as set in the constructor. */
        private SessionListener listener;
        /** First possible entry state value. */
        private static final int ENTRY_FIRST = 1;
        /** Listener has been closed. */
        public static final int ENTRY_CLOSED = 1;
        /** Listener has been activated. */
        public static final int ENTRY_ACTIVE = 2;
        /** Listener in inactive. This is the initial state. */
        public static final int ENTRY_INACTIVE = 3;
        /** Last possible entry state value. */
        private static final int ENTRY_LAST = 3;
        /** Current state of the listener. Set in <code>setState()</code>. */ 
        private int entryState;
        /** True if this element is marked for removal. */
        public boolean shouldRemove;

        /**
         * Constructs a new Entry with the given SessionListener.
         * The state of the entry defaults to <code>ENTRY_INACTIVE</code>.
         *
         * @param  listener  SessionListener.
         */
        public Entry(SessionListener listener) {
            this.listener = listener;
            lock = new SimpleLock();
            entryState = ENTRY_INACTIVE;
        } // Entry

        /**
         * Acquire the access control lock for this listener and
         * return a reference to the listener. Will wait until
         * the listener lock is released.
         *
         * @return  SessionListener.
         * @exception  LockException
         *             Thrown if failed to acquire lock.
         */
        public SessionListener acquireListener() throws LockException {
            // Let's timeout after 3 seconds, as that is more than
            // long enough to wait for a listener.
            lock.acquire(listener, 3000);
            return listener;
        } // acquireListener

        /**
         * Returns true if this Entry holds a reference to the same
         * listener as that given. Uses the <code>equals()</code> method
         * to compare the two listeners.
         */
        public boolean ownsListener(SessionListener listener) {
            if ((listener == null) || (this.listener == null)) {
                // Nulls are bad, always return false.
                return false;
            }
            return listener.equals(this.listener);
        } // ownsListener

        /**
         * Return the current state of the listener.
         *
         * @return  One of <code>ENTRY_CLOSED</code>,
         *          <code>ENTRY_ACTIVE</code>,
         *          or <code>ENTRY_INACTIVE</code>.
         */
        public int getState() {
            return entryState;
        } // getState

        /**
         * Release the lock for this session listener. Has no effect
         * if the lock was already released.
         */
        public void releaseListener() {
            lock.release(listener);
        } // releaseListener

        /**
         * Sets the state of the entry. This will try to acquire the
         * lock for the listener if it is not already locked. If the
         * lock had to be acquired to set the state, the lock will
         * be released after the state has been set.
         *
         * @param  state  New state for this entry.
         * @exception  IllegalArgumentException
         *             Thrown if state is an invalid value.
         * @exception  IllegalStateException
         *             Thrown if the listener has already closed.
         * @exception  LockException
         *             Thrown if failed to acquire lock.
         */
        public void setState(int state) throws LockException {
            if ((state > ENTRY_LAST) || (state < ENTRY_FIRST)) {
                throw new IllegalArgumentException("bad state parameter");
            }
            if (entryState == ENTRY_CLOSED) {
                throw new IllegalStateException("listener has closed");
            }
            boolean waslocked = false;
            if (!lock.hasKey(listener)) {
                lock.acquire(listener);
                waslocked = true;
            }
            entryState = state;
            if (waslocked) {
                lock.release(listener);
            }
        } // setState

        /**
         * Return a string representation of this Entry.
         */
        public String toString() {
            StringBuffer buff = new StringBuffer("Entry=[");
            buff.append("lock ");
            if (lock.hasKey(listener)) {
                buff.append("acquired");
            } else {
                buff.append("released");
            }
            buff.append(", listener=");
            buff.append(listener.getClass().getName());
            buff.append(", state=");
            if (entryState == ENTRY_CLOSED) {
                buff.append("closed");
            } else if (entryState == ENTRY_ACTIVE) {
                buff.append("active");
            } else if (entryState == ENTRY_INACTIVE) {
                buff.append("inactive");
            }
            buff.append(", shouldRemove=");
            buff.append(shouldRemove);
            buff.append("]");
            return buff.toString();
        } // toString
    } // Entry
} // SessionListenerList
