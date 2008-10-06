/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractWatchManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;

/**
 * AbstractWatchManager provides an abstract WatchManager implementation
 * for concrete implementations to subclass. This class implements the
 * SessionListener interface so that the watches are loaded and saved
 * at the appropriate times.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractWatchManager implements WatchManager, SessionListener {
    /** List of watch listeners. */
    private WatchListener watchListeners;

    /**
     * Creates a new instance of AbstractWatchManager.
     */
    protected AbstractWatchManager() {
    }

    public void addWatchListener(WatchListener listener) {
        if (listener != null) {
            synchronized (this) {
                watchListeners = WatchEventMulticaster.add(
                        watchListeners, listener);
            }
        }
    }

    public void closing(SessionEvent sevt) {
        saveWatches(sevt.getSession());
    }

    public void connected(SessionEvent sevt) {
    }

    public void disconnected(SessionEvent sevt) {
    }

    /**
     * Sends the given event to all of the registered listeners.
     *
     * @param  e  event to be dispatched.
     */
    protected void fireEvent(WatchEvent e) {
        WatchListener listeners;
        synchronized (this) {
            listeners = watchListeners;
        }
        if (listeners != null) {
            e.getType().fireEvent(e, listeners);
        }
    }

    /**
     * Load the persisted watches from storage.
     *
     * @param  session  associated Session instance.
     */
    protected abstract void loadWatches(Session session);

    public void opened(Session session) {
        loadWatches(session);
    }

    public void removeWatchListener(WatchListener listener) {
        if (listener != null) {
            synchronized (this) {
                watchListeners = WatchEventMulticaster.remove(
                        watchListeners, listener);
            }
        }
    }

    public void resuming(SessionEvent sevt) {
    }

    /**
     * Save the watches to persistent storage.
     *
     * @param  session  associated Session instance.
     */
    protected abstract void saveWatches(Session session);

    public void suspended(SessionEvent sevt) {
    }
}
