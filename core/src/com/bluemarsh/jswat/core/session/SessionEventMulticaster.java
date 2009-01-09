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
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.session.Session;

/**
 * Class SessionEventMulticaster implements a thread-safe list of
 * session listeners. It is technically a tree but it grows only
 * in one direction, which makes it more like a linked list. This
 * class behaves like a listener but it simply forwards the events
 * to the contained session listeners.
 *
 * <pre><code>
 * SessionListener sessionListener = null;
 *
 * public synchronized void addSessionListener(SessionListener l) {
 *     sessionListener = SessionEventMulticaster.add(sessionListener, l);
 * }
 * public synchronized void removeSessionListener(SessionListener l) {
 *     sessionListener = SessionEventMulticaster.remove(sessionListener, l);
 * }
 * protected void fireEvent(SessionEvent e) {
 *     SessionListener listener = sessionListener;
 *     if (listener != null) {
 *         listener.sessionEvent(e);
 *     }
 * }
 * </code></pre>
 *
 * <p>This marvelous design was originally put to code by Amy Fowler and
 * John Rose in the form of the <code>AWTEventMulticaster</code> class
 * in the <code>java.awt</code> package. This implementation is based on
 * the description given in <u>Taming Java Threads</u> by Allen Holub.</p>
 */
public class SessionEventMulticaster implements SessionListener {
    /** A session listener. */
    protected final SessionListener listener1;
    /** A session listener. */
    protected final SessionListener listener2;

    /**
     * Adds the second listener to the first listener and returns the
     * resulting multicast listener.
     *
     * @param  l1  a session listener.
     * @param  l2  the session listener being added.
     * @return  session multicast listener.
     */
    public static SessionListener add(SessionListener l1,
                                      SessionListener l2) {
        return (l1 == null) ? l2 :
               (l2 == null) ? l1 : new SessionEventMulticaster(l1, l2);
    }

    /**
     * Removes the second listener from the first listener and returns
     * the resulting multicast listener.
     *
     * @param  l1  a session listener.
     * @param  l2  the listener being removed.
     * @return  session multicast listener.
     */
    public static SessionListener remove(SessionListener l1,
                                         SessionListener l2) {
        if (l1 == l2 || l1 == null) {
            return null;
        } else if (l1 instanceof SessionEventMulticaster) {
            return ((SessionEventMulticaster) l1).remove(l2);
        } else {
            return l1;
        }
    }

    /**
     * Creates a session event multicaster instance which chains
     * listener l1 with listener l2.
     *
     * @param  l1  a session listener.
     * @param  l2  a session listener.
     */
    protected SessionEventMulticaster(SessionListener l1, SessionListener l2) {
        listener1 = l1;
        listener2 = l2;
    }

    /**
     * Removes a session listener from this multicaster and returns the
     * resulting multicast listener.
     *
     * @param  l  the listener to be removed.
     * @return  the other listener.
     */
    protected SessionListener remove(SessionListener l) {
        if (l == listener1) {
            return listener2;
        }
        if (l == listener2) {
            return listener1;
        }
        // Recursively seek out the target listener.
        SessionListener l1 = remove(listener1, l);
        SessionListener l2 = remove(listener2, l);
        return (l1 == listener1 && l2 == listener2) ? this : add(l1, l2);
    }

    /**
     * Handles the given event by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  e  the session event.
     */
    public void connected(SessionEvent e) {
        listener1.connected(e);
        listener2.connected(e);
    }

    /**
     * Handles the given event by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  e  the session event.
     */
    public void closing(SessionEvent e) {
        listener1.closing(e);
        listener2.closing(e);
    }

    /**
     * Handles the given event by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  e  the session event.
     */
    public void disconnected(SessionEvent e) {
        listener1.disconnected(e);
        listener2.disconnected(e);
    }

    /**
     * Handles the given action by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  s  the session.
     */
    public void opened(Session s) {
        listener1.opened(s);
        listener2.opened(s);
    }

    /**
     * Handles the given event by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  e  the session event.
     */
    public void resuming(SessionEvent e) {
        listener1.resuming(e);
        listener2.resuming(e);
    }

    /**
     * Handles the given event by invoking the appropriate method on
     * the chained listeners.
     *
     * @param  e  the session event.
     */
    public void suspended(SessionEvent e) {
        listener1.suspended(e);
        listener2.suspended(e);
    }
}
