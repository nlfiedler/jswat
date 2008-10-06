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
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import com.sun.jdi.event.Event;
import java.util.EventObject;

/**
 * Class SessionEvent encapsulates information about the Session and its
 * current state, as well as information about the event that is taking
 * place.
 *
 * @author  Nathan Fiedler
 */
public class SessionEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The Session related to this event. */
    private transient Session session;
    /** The JDI event that caused this event. */
    private transient Event event;
    /** The type of session change. */
    private Type type;

    /**
     * Type of session event.
     */
    public static enum Type {
        CLOSING {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.closing(e);
            }
        },
        CONNECTED {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.connected(e);
            }
        },
        DISCONNECTED {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.disconnected(e);
            }
        },
        OPENED {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.opened(e.getSession());
            }
        },
        RESUMING {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.resuming(e);
            }
        },
        SUSPENDED {
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.suspended(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(SessionEvent e, SessionListener l);
    }

    /**
     * Constructs a new SessionEvent.
     *
     * @param  session  Session related to this event.
     * @param  type     type of session change.
     */
    public SessionEvent(Session session, Type type) {
        super(session);
        this.session = session;
        this.type = type;
    }

    /**
     * Constructs a new SessionEvent.
     *
     * @param  session  Session related to this event.
     * @param  type     type of session change.
     * @param  event    event that brought about this event.
     */
    public SessionEvent(Session session, Type type, Event event) {
        this(session, type);
        this.event = event;
    }

    /**
     * Returns the JDI event object that caused this event to happen.
     *
     * @return  event that caused this event; may be null.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the Session relating to this event.
     *
     * @return  Session for this event.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get the session event type.
     *
     * @return  session event type.
     */
    public Type getType() {
        return type;
    }
}
