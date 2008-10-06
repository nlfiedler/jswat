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
 * $Id: ConnectionEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import java.util.EventObject;

/**
 * Represents a change in a JvmConnection.
 *
 * @author Nathan Fiedler
 */
public class ConnectionEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The source of this event. */
    private JvmConnection connection;
    /** The type of connection change. */
    private Type type;

    /**
     * Type of connection event.
     */
    public static enum Type {
        CONNECTED {
            public void fireEvent(ConnectionEvent e, ConnectionListener l) {
                l.connected(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(ConnectionEvent e, ConnectionListener l);
    }

    /**
     * Creates a new instance of ConnectionEvent.
     *
     * @param  connection  the JVM connection.
     * @param  type        type type of the event.
     */
    public ConnectionEvent(JvmConnection connection, Type type) {
        super(connection);
        this.connection = connection;
        this.type = type;
    }

    /**
     * Returns the JvmConnection relating to this event.
     *
     * @return  JvmConnection for this event.
     */
    public JvmConnection getConnection() {
        return connection;
    }

    /**
     * Get the connection event type.
     *
     * @return  connection event type.
     */
    public Type getType() {
        return type;
    }
}