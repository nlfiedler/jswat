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
 * are Copyright (C) 1999-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
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
    private ContextEventType type;
    /** The Session in which the change occurred. */
    private transient Session session;
    /** Indicates if Session was suspending when this event occurred. */
    private boolean suspending;

    /**
     * Constructs a ContextEvent.
     *
     * @param  source      source of this event.
     * @param  session     the associated Session.
     * @param  type        the type of the change.
     * @param  suspending  true if Session is suspending as a result of this.
     */
    public ContextEvent(Object source, Session session, ContextEventType type,
            boolean suspending) {
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
    public ContextEventType getType() {
        return type;
    }

    /**
     * Indicates if the Session for this event is the current one.
     *
     * @return  true if event relates to current session, false otherwise.
     */
    public boolean isCurrentSession() {
        return SessionProvider.isCurrentSession(session);
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
