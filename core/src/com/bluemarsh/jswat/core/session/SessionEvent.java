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
 * are Copyright (C) 2002-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
            @Override
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.closing(e);
            }
        },
        CONNECTED {
            @Override
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.connected(e);
            }
        },
        DISCONNECTED {
            @Override
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.disconnected(e);
            }
        },
        OPENED {
            @Override
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.opened(e.getSession());
            }
        },
        RESUMING {
            @Override
            public void fireEvent(SessionEvent e, SessionListener l) {
                l.resuming(e);
            }
        },
        SUSPENDED {
            @Override
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
