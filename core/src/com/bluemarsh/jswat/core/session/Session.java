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
 * are Copyright (C) 2003-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

/**
 * A Session is responsible for managing the debugging session, as well
 * as the debuggee connection. It provides methods for connecting,
 * disconnecting, and closing the session.
 *
 * @author  Nathan Fiedler
 */
public interface Session {

    /** Name of the session name property. */
    String PROP_SESSION_NAME = "sessionName";
    /** Name of the runtime identifier property. */
    String PROP_RUNTIME_ID = "RuntimeId";
    /** Name of the Java runtime parameters property. */
    String PROP_JAVA_PARAMS = "JavaParams";
    /** Name of the main class name property. */
    String PROP_CLASS_NAME = "ClassName";
    /** Name of the main class parameters property. */
    String PROP_CLASS_PARAMS = "ClassParams";
    /** Name of the process identifier property. */
    String PROP_PROCESS_ID = "ProcessID";
    /** Name of the shared name property. */
    String PROP_SHARED_NAME = "SharedName";
    /** Name of the socket host property. */
    String PROP_SOCKET_HOST = "SocketHost";
    /** Name of the socket port property. */
    String PROP_SOCKET_PORT = "SocketPort";
    /** Name of the connector type property. */
    String PROP_CONNECTOR = "Connector";
    /** Property value for shared attaching connector. */
    String PREF_PROCESS = "Process";
    /** Property value for shared attaching connector. */
    String PREF_SHARED = "Shared";
    /** Property value for socket attaching connector. */
    String PREF_SOCKET = "Socket";
    /** Property value for shared listening connector. */
    String PREF_SHARED_LISTEN = "SharedListen";
    /** Property value for socket listening connector. */
    String PREF_SOCKET_LISTEN = "SocketListen";

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Adds a SessionListener to this session. This method calls the
     * <code>init()</code> method on the listener. It may also call the
     * <code>activate()</code> method if the Session is already connected.
     *
     * @param  listener  SessionListener to add to this session.
     */
    void addSessionListener(SessionListener listener);

    /**
     * Shutdown the session permanently. This will close all the
     * listeners, clear all lists and set all references to null.
     */
    void close();

    /**
     * Connects the Session to the given connection. The connection must
     * already be established with the remote debuggee.
     *
     * @param  connection  the debuggee connection.
     */
    void connect(JvmConnection connection);

    /**
     * Disconnects the session from the debuggee. If the debuggee was
     * lanched, it is terminated. If the debuggee was remote, the session
     * disconnects but leaves the remote debuggee running, unless the
     * 'forceExit' parameter is set to true.
     *
     * @param  forceExit  true to close debuggee VM forcibly in all
     *                    cases; false to leave remote debuggee running.
     */
    void disconnect(boolean forceExit);

    /**
     * Returns the debuggee address of this session instance.
     *
     * @return  debuggee address, or empty string if not known.
     */
    String getAddress();

    /**
     * Returns the JvmConnection object for this Session, if any.
     *
     * @return  JvmConnection, or null if none has been made yet.
     */
    JvmConnection getConnection();

    /**
     * Returns the unique, immutable identifier for this session. This
     * identifier is appropriate for use in filenames for persisting
     * data that is keyed off of the session instance.
     *
     * @return  unique session identifier.
     */
    String getIdentifier();

    /**
     * Retrieves the named property stored in this Session instance.
     *
     * @param  name  name of the property.
     * @return  value of the property, or <code>null</code> if not set.
     */
    String getProperty(String name);

    /**
     * Returns the state of this session instance as a brief description
     * (e.g. "running", "connected", "suspended").
     *
     * @return  state.
     */
    String getState();

    /**
     * Returns the language stratum of this session instance (e.g. "Java").
     *
     * @return  stratum, or empty string if not known.
     */
    String getStratum();

    /**
     * Returns true if this session is connected to a debuggee.
     *
     * @return  true if connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Determines if the debuggee VM has any threads that have been
     * suspended by the debugger.
     *
     * @return  true if any threads have been suspended by the debugger,
     *          false otherwise.
     */
    boolean isSuspended();

    /**
     * Returns an iterator over the set of the property names.
     *
     * @return  property key set iterator.
     */
    Iterator<String> propertyNames();

    /**
     * Remove a PropertyChangeListener from the listener list.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a Session listener from this session. This calls the
     * <code>close()</code> method of the listener.
     *
     * @param  listener  SessionListener to remove from this session.
     */
    void removeSessionListener(SessionListener listener);

    /**
     * Resume execution of the debuggee VM, from a suspended state.
     */
    void resumeVM();

    /**
     * Set the identifier for this Session. Generally only the SessionManager
     * calls this method during the construction of the Session.
     *
     * @param  id  identifier for this session.
     */
    void setIdentifier(String id);

    /**
     * Sets the value of the named property for this Session instance,
     * replacing any previously set value.
     *
     * @param  name   name of the property.
     * @param  value  value of the property, or null to remove property.
     */
    void setProperty(String name, String value);

    /**
     * Suspend execution of the debuggee VM.
     */
    void suspendVM();
}
