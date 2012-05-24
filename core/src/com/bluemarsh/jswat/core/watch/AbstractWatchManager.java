/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;

/**
 * AbstractWatchManager provides an abstract WatchManager implementation for
 * concrete implementations to subclass. This class implements the
 * SessionListener interface so that the watches are loaded and saved at the
 * appropriate times.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractWatchManager implements WatchManager, SessionListener {

    /**
     * List of watch listeners.
     */
    private WatchEventMulticaster eventMulticaster;

    /**
     * Creates a new instance of AbstractWatchManager.
     */
    protected AbstractWatchManager() {
        eventMulticaster = new WatchEventMulticaster();
    }

    @Override
    public void addWatchListener(WatchListener listener) {
        if (listener != null) {
            eventMulticaster.add(listener);
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
        saveWatches(sevt.getSession());
    }

    @Override
    public void connected(SessionEvent sevt) {
    }

    @Override
    public void disconnected(SessionEvent sevt) {
    }

    /**
     * Sends the given event to all of the registered listeners.
     *
     * @param e event to be dispatched.
     */
    protected void fireEvent(WatchEvent e) {
        e.getType().fireEvent(e, eventMulticaster);
    }

    /**
     * Load the persisted watches from storage.
     *
     * @param session associated Session instance.
     */
    protected abstract void loadWatches(Session session);

    @Override
    public void opened(Session session) {
        loadWatches(session);
    }

    @Override
    public void removeWatchListener(WatchListener listener) {
        if (listener != null) {
            eventMulticaster.remove(listener);
        }
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    /**
     * Save the watches to persistent storage.
     *
     * @param session associated Session instance.
     */
    protected abstract void saveWatches(Session session);

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
