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
 * are Copyright (C) 1999-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ContextEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import java.util.EventObject;

/**
 * An event which indicates that the debugger context has changed. This
 * includes the current thread, current stack frame, and current location.
 *
 * @author  Nathan Fiedler
 */
public class ContextEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The type of context change. */
    private Type type;
    /** The Session in which the change occurred. */
    private transient Session session;
    /** Indicates if Session was suspending when this event occurred. */
    private boolean suspending;

    /** Type of context change. */
    public static enum Type {
        /** The frame and location have changed, but not thread. */
        FRAME {
            public void fireEvent(ContextEvent e, ContextListener l) {
                l.changedFrame(e);
            }
        },
        /** The location changed; frame and/or thread may have changed. */
        LOCATION {
            public void fireEvent(ContextEvent e, ContextListener l) {
                l.changedLocation(e);
            }
        },
        /** The thread, frame and location have changed. */
        THREAD {
            public void fireEvent(ContextEvent e, ContextListener l) {
                l.changedThread(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(ContextEvent e, ContextListener l);
    }

    /**
     * Constructs a ContextEvent.
     *
     * @param  source      source of this event.
     * @param  session     the associated Session.
     * @param  type        the type of the change.
     * @param  suspending  true if Session is suspending as a result of this.
     */
    public ContextEvent(Object source, Session session, Type type, boolean suspending) {
        super(source);
        this.session = session;
        this.type = type;
        this.suspending = suspending;
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
     * Returns the type of the context change.
     *
     * @return  an instance of the Type enum.
     */
    public Type getType() {
        return type;
    }

    /**
     * Indicates if Session was suspending when this event occurred.
     *
     * @return  true if Session suspended as a result, false otherwise.
     */
    public boolean isSuspending() {
        return suspending;
    }
}
