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
 * are Copyright (C) 2006-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.connect;

/**
 * Class ConnectionEventMulticaster implements a thread-safe list of
 * connection listeners. It is technically a tree but it grows only
 * in one direction, which makes it more like a linked list. This
 * class behaves like a listener but it simply forwards the events
 * to the contained connection listeners.
 *
 * <pre><code>
 * ConnectionListener connectionListener = null;
 *
 * public synchronized void addConnectionListener(ConnectionListener l) {
 *     connectionListener = ConnectionEventMulticaster.add(connectionListener, l);
 * }
 * public synchronized void removeConnectionListener(ConnectionListener l) {
 *     connectionListener = ConnectionEventMulticaster.remove(connectionListener, l);
 * }
 * protected void fireEvent(ConnectionEvent e) {
 *     ConnectionListener listener = connectionListener;
 *     if (listener != null) {
 *         listener.connectionEvent(e);
 *     }
 * }
 * </code></pre>
 *
 * <p>This marvelous design was originally put to code by Amy Fowler and
 * John Rose in the form of the <code>AWTEventMulticaster</code> class
 * in the <code>java.awt</code> package. This implementation is based on
 * the description given in <u>Taming Java Threads</u> by Allen Holub.</p>
 */
public class ConnectionEventMulticaster implements ConnectionListener {

    /** A connection listener. */
    protected final ConnectionListener listener1;
    /** A connection listener. */
    protected final ConnectionListener listener2;

    /**
     * Adds the second listener to the first listener and returns the
     * resulting multicast listener.
     *
     * @param  l1  a connection listener.
     * @param  l2  the connection listener being added.
     * @return  connection multicast listener.
     */
    public static ConnectionListener add(ConnectionListener l1,
            ConnectionListener l2) {
        return (l1 == null) ? l2
                : (l2 == null) ? l1 : new ConnectionEventMulticaster(l1, l2);
    }

    /**
     * Removes the second listener from the first listener and returns
     * the resulting multicast listener.
     *
     * @param  l1  a connection listener.
     * @param  l2  the listener being removed.
     * @return  connection multicast listener.
     */
    public static ConnectionListener remove(ConnectionListener l1,
            ConnectionListener l2) {
        if (l1 == l2 || l1 == null) {
            return null;
        } else if (l1 instanceof ConnectionEventMulticaster) {
            return ((ConnectionEventMulticaster) l1).remove(l2);
        } else {
            return l1;
        }
    }

    /**
     * Creates a connection event multicaster instance which chains
     * listener l1 with listener l2.
     *
     * @param  l1  a connection listener.
     * @param  l2  a connection listener.
     */
    protected ConnectionEventMulticaster(ConnectionListener l1, ConnectionListener l2) {
        listener1 = l1;
        listener2 = l2;
    }

    /**
     * Removes a connection listener from this multicaster and returns the
     * resulting multicast listener.
     *
     * @param  l  the listener to be removed.
     * @return  the other listener.
     */
    protected ConnectionListener remove(ConnectionListener l) {
        if (l == listener1) {
            return listener2;
        }
        if (l == listener2) {
            return listener1;
        }
        // Recursively seek out the target listener.
        ConnectionListener l1 = remove(listener1, l);
        ConnectionListener l2 = remove(listener2, l);
        return (l1 == listener1 && l2 == listener2) ? this : add(l1, l2);
    }

    @Override
    public void connected(ConnectionEvent e) {
        listener1.connected(e);
        listener2.connected(e);
    }
}
