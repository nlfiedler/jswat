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
 * are Copyright (C) 2003-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.session;

import com.sun.jdi.VirtualMachine;
import java.util.Map;
import org.openide.util.NbBundle;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class AbstractSession provides a few of the necessary methods that all
 * Session implementations will need.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractSession implements Session {

    /**
     * Unique identifier for this instance.
     */
    private String sessionIdentifier;
    /**
     * List of SessionListener objects.
     */
    private SessionEventMulticaster eventMulticaster;
    /**
     * Set of session properties.
     */
    private Map<String, String> sessionProperties;
    /**
     * Handles property change listeners and sending events.
     */
    private PropertyChangeSupport propSupport;

    /**
     * Creates a new instance of AbstractSession.
     */
    public AbstractSession() {
        eventMulticaster = new SessionEventMulticaster();
        sessionProperties = new HashMap<String, String>();
        propSupport = new PropertyChangeSupport(this);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void addSessionListener(SessionListener listener) {
        if (listener == null) {
            return;
        }
        eventMulticaster.add(listener);
        listener.opened(this);
        if (isConnected()) {
            SessionEvent se = new SessionEvent(this, SessionEventType.CONNECTED);
            se.getType().fireEvent(se, listener);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Session) {
            Session s = (Session) o;
            return s.getIdentifier().equals(getIdentifier());
        }
        return false;
    }

    /**
     * Fires the event to all of the registered listeners.
     *
     * @param se session event.
     */
    protected void fireEvent(SessionEvent se) {
        se.getType().fireEvent(se, eventMulticaster);
    }

    @Override
    public String getAddress() {
        JvmConnection conn = getConnection();
        if (conn != null) {
            return conn.getAddress();
        } else {
            return "";
        }
    }

    @Override
    public String getIdentifier() {
        return sessionIdentifier;
    }

    @Override
    public String getProperty(String name) {
        return sessionProperties.get(name);
    }

    @Override
    public String getState() {
        if (isConnected()) {
            if (isSuspended()) {
                return NbBundle.getMessage(getClass(), "Session.state.suspended");
            } else {
                return NbBundle.getMessage(getClass(), "Session.state.running");
            }
        } else {
            return NbBundle.getMessage(getClass(), "Session.state.disconnected");
        }
    }

    @Override
    public String getStratum() {
        JvmConnection conn = getConnection();
        String stratum = null;
        if (conn != null) {
            VirtualMachine vm = conn.getVM();
            if (vm != null) {
                stratum = vm.getDefaultStratum();
            }
        }
        return stratum == null ? "Java" : stratum;
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public Iterator<String> propertyNames() {
        return sessionProperties.keySet().iterator();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void removeSessionListener(SessionListener listener) {
        if (listener == null) {
            return;
        }
        eventMulticaster.remove(listener);
        if (isConnected()) {
            SessionEvent se = new SessionEvent(this, SessionEventType.DISCONNECTED);
            se.getType().fireEvent(se, listener);
        }
        SessionEvent se = new SessionEvent(this, SessionEventType.CLOSING);
        se.getType().fireEvent(se, listener);
    }

    @Override
    public void setIdentifier(String id) {
        sessionIdentifier = id;
    }

    @Override
    public void setProperty(String name, String value) {
        String old = sessionProperties.get(name);
        if (value == null) {
            sessionProperties.remove(name);
        } else {
            sessionProperties.put(name, value);
        }
        propSupport.firePropertyChange(name, old, value);
    }
}
