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
 * are Copyright (C) 2004-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.session;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Class SessionEventMulticaster implements a thread-safe list of session
 * listeners. In addition, it acts as a listener such that events can be sent to
 * the multicaster and it will dispatch the events to all registered listeners.
 *
 * @author Nathan Fiedler
 */
public class SessionEventMulticaster implements SessionListener {

    /**
     * A set of unique session listeners.
     */
    private final Set<SessionListener> listeners;

    /**
     * Creates a new instance of SessionEventMulticaster.
     */
    public SessionEventMulticaster() {
        // Use CopyOnWriteArraySet so that our listeners are unique,
        // that the average case of iterating the list is kept fast
        // and efficient, and that the unusual case of adding/removing
        // from the list is still thread-safe (albeit via copying).
        listeners = new CopyOnWriteArraySet<SessionListener>();
    }

    /**
     * Adds the given listener to the set of listeners.
     *
     * @param l a session listener.
     */
    public void add(SessionListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    /**
     * Removes the given listener from the set of listeners.
     *
     * @param l a session listener.
     */
    public void remove(SessionListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    @Override
    public void connected(SessionEvent e) {
        for (SessionListener l : listeners) {
            l.connected(e);
        }
    }

    @Override
    public void closing(SessionEvent e) {
        for (SessionListener l : listeners) {
            l.closing(e);
        }
    }

    @Override
    public void disconnected(SessionEvent e) {
        for (SessionListener l : listeners) {
            l.disconnected(e);
        }
    }

    @Override
    public void opened(Session s) {
        for (SessionListener l : listeners) {
            l.opened(s);
        }
    }

    @Override
    public void resuming(SessionEvent e) {
        for (SessionListener l : listeners) {
            l.resuming(e);
        }
    }

    @Override
    public void suspended(SessionEvent e) {
        for (SessionListener l : listeners) {
            l.suspended(e);
        }
    }
}
