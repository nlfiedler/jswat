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
    private final JvmConnection connection;
    /** The type of connection change. */
    private final ConnectionEventType type;

    /**
     * Creates a new instance of ConnectionEvent.
     *
     * @param  connection  the JVM connection.
     * @param  type        type type of the event.
     */
    public ConnectionEvent(JvmConnection connection, ConnectionEventType type) {
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
    public ConnectionEventType getType() {
        return type;
    }
}
