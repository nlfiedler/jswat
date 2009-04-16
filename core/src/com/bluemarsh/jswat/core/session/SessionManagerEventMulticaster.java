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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

/**
 * Class SessionManagerEventMulticaster implements a thread-safe list of
 * session manager listeners. It is technically a tree but it grows only
 * in one direction, which makes it more like a linked list. This
 * class behaves like a listener but it simply forwards the events
 * to the contained session manager listeners.
 *
 * <pre><code>
 * SessionManagerListener sessionManagerListener = null;
 *
 * public synchronized void addSessionManagerListener(SessionManagerListener l) {
 *     sessionManagerListener = SessionManagerEventMulticaster.add(sessionManagerListener, l);
 * }
 * public synchronized void removeSessionManagerListener(SessionManagerListener l) {
 *     sessionManagerListener = SessionManagerEventMulticaster.remove(sessionManagerListener, l);
 * }
 * protected void fireEvent(SessionManagerEvent e) {
 *     SessionManagerListener listener = sessionManagerListener;
 *     if (listener != null) {
 *         listener.SessionManagerEvent(e);
 *     }
 * }
 * </code></pre>
 *
 * <p>This marvelous design was originally put to code by Amy Fowler and
 * John Rose in the form of the <code>AWTEventMulticaster</code> class
 * in the <code>java.awt</code> package. This implementation is based on
 * the description given in <u>Taming Java Threads</u> by Allen Holub.</p>
 */
public class SessionManagerEventMulticaster implements SessionManagerListener {
    /** A session manager listener. */
    private final SessionManagerListener listener1;
    /** A session manager listener. */
    private final SessionManagerListener listener2;

    /**
     * Adds the second listener to the first listener and returns the
     * resulting multicast listener.
     *
     * @param  l1  a session manager listener.
     * @param  l2  the session manager listener being added.
     * @return  session multicast listener.
     */
    public static SessionManagerListener add(SessionManagerListener l1,
                                             SessionManagerListener l2) {
        return (l1 == null) ? l2 :
               (l2 == null) ? l1 : new SessionManagerEventMulticaster(l1, l2);
    }

    /**
     * Removes the second listener from the first listener and returns
     * the resulting multicast listener.
     *
     * @param  l1  a session manager listener.
     * @param  l2  the listener being removed.
     * @return  session multicast listener.
     */
    public static SessionManagerListener remove(SessionManagerListener l1,
                                                SessionManagerListener l2) {
        if (l1 == l2 || l1 == null) {
            return null;
        } else if (l1 instanceof SessionManagerEventMulticaster) {
            return ((SessionManagerEventMulticaster) l1).remove(l2);
        } else {
            return l1;
        }
    }

    /**
     * Creates a session event multicaster instance which chains
     * listener l1 with listener l2.
     *
     * @param  l1  a session manager listener.
     * @param  l2  a session manager listener.
     */
    protected SessionManagerEventMulticaster(SessionManagerListener l1,
                                             SessionManagerListener l2) {
        listener1 = l1;
        listener2 = l2;
    }

    /**
     * Removes a session manager listener from this multicaster and returns
     * the resulting multicast listener.
     *
     * @param  l  the listener to be removed.
     * @return  the other listener.
     */
    protected SessionManagerListener remove(SessionManagerListener l) {
        if (l == listener1) {
            return listener2;
        }
        if (l == listener2) {
            return listener1;
        }
        // Recursively seek out the target listener.
        SessionManagerListener l1 = remove(listener1, l);
        SessionManagerListener l2 = remove(listener2, l);
        return (l1 == listener1 && l2 == listener2) ? this : add(l1, l2);
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        listener1.sessionAdded(e);
        listener2.sessionAdded(e);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        listener1.sessionRemoved(e);
        listener2.sessionRemoved(e);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        listener1.sessionSetCurrent(e);
        listener2.sessionSetCurrent(e);
    }
}
