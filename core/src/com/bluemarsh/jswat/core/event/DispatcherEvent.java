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
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DispatcherEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.event.Event;
import java.util.EventObject;

/**
 * An event which encapsulates an event from the debugger backend.
 *
 * @author  Nathan Fiedler
 */
public class DispatcherEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The Session to which we belong. */
    private transient Session session;
    /** The JDI event from the debugger backend. */
    private transient Event event;

    /**
     * Constructs a JdiEvent.
     *
     * @param  source   source of this event.
     * @param  session  the associated session.
     * @param  event    debugger event.
     */
    public DispatcherEvent(Object source, Session session, Event event) {
        super(source);
        this.session = session;
        this.event = event;
    }

    /**
     * Returns the JDI event enclosed in this instance.
     *
     * @return  the debugger event.
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
}
