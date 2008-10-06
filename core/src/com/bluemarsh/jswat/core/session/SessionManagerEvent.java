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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionManagerEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import java.util.EventObject;

/**
 * Class SessionManagerEvent encapsulates information about the Session
 * that was just added or removed from the session manager.
 *
 * @author  Nathan Fiedler
 */
public class SessionManagerEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The Session related to this event. */
    private transient Session session;
    /** The type of session manager change. */
    private Type type;

    /**
     * Type of session manager event.
     */
    public static enum Type {
        ADDED {
            public void fireEvent(SessionManagerEvent e, SessionManagerListener l) {
                l.sessionAdded(e);
            }
        },
        CURRENT {
            public void fireEvent(SessionManagerEvent e, SessionManagerListener l) {
                l.sessionSetCurrent(e);
            }
        },
        REMOVED {
            public void fireEvent(SessionManagerEvent e, SessionManagerListener l) {
                l.sessionRemoved(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(SessionManagerEvent e, SessionManagerListener l);
    }

    /**
     * Constructs a new SessionManagerEvent.
     *
     * @param  source   source of this event.
     * @param  session  Session related to this event.
     * @param  type     type of the event.
     */
    public SessionManagerEvent(Object source, Session session, Type type) {
        super(source);
        this.session = session;
        this.type = type;
    }

    /**
     * Returns the Session relating to this event.
     *
     * @return  Session for this event.
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Get the session manager event type.
     *
     * @return  session manager event type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns a String representation of this SessionManagerEvent.
     *
     * @return  a String representation of this.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("SessionManagerEvent=[session=");
        buf.append(this.session);
        buf.append(", source=");
        buf.append(getSource());
        buf.append(']');
        return buf.toString();
    }
}
