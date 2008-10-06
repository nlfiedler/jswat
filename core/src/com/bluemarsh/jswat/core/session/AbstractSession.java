/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractSession.java 15 2007-06-03 00:01:17Z nfiedler $
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
 * Class AbstractSession provides a few of the necessary methods that
 * all Session implementations will need.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractSession implements Session {
    /** Unique identifier for this instance. */
    private String sessionIdentifier;
    /** List of SessionListener objects. */
    private SessionListener listenerList;
    /** Set of session properties. */
    private Map<String, String> sessionProperties;
    /** Handles property change listeners and sending events. */
    private PropertyChangeSupport propSupport;

    /**
     * Creates a new instance of AbstractSession.
     */
    public AbstractSession() {
        sessionProperties = new HashMap<String, String>();
        propSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    public void addSessionListener(SessionListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            listenerList = SessionEventMulticaster.add(listenerList, listener);
        }
        listener.opened(this);
        if (isConnected()) {
            SessionEvent se = new SessionEvent(this, SessionEvent.Type.CONNECTED);
            se.getType().fireEvent(se, listener);
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  o  the reference object with which to compare.
     */
    public boolean equals(Object o) {
        Session s = (Session) o;
        return s.getIdentifier().equals(getIdentifier());
    }

    /**
     * Fires the event to all of the registered listeners.
     *
     * @param  se  session event.
     */
    protected void fireEvent(SessionEvent se) {
        SessionListener sl;
        synchronized (this) {
            sl = listenerList;
        }
        if (sl != null) {
            se.getType().fireEvent(se, sl);
        }
    }

    public String getAddress() {
        JvmConnection conn = getConnection();
        if (conn != null) {
            return conn.getAddress();
        } else {
            return "";
        }
    }

    public String getIdentifier() {
        return sessionIdentifier;
    }

    public String getProperty(String name) {
        return sessionProperties.get(name);
    }

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

    /**
     * Returns a hash code value for the object.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    public Iterator<String> propertyNames() {
        return sessionProperties.keySet().iterator();
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    public void removeSessionListener(SessionListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (this) {
            listenerList = SessionEventMulticaster.remove(listenerList, listener);
        }
        if (isConnected()) {
            SessionEvent se = new SessionEvent(this, SessionEvent.Type.DISCONNECTED);
            se.getType().fireEvent(se, listener);
        }
        SessionEvent se = new SessionEvent(this, SessionEvent.Type.CLOSING);
        se.getType().fireEvent(se, listener);
    }

    public void setIdentifier(String id) {
        sessionIdentifier = id;
    }

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
