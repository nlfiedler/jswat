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
 * are Copyright (C) 1999-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.sun.jdi.VMDisconnectedException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class AbstractDebuggingContext provides an abstract implementation of
 * DebuggingContext for concrete implementations to subclass. It maintains a set
 * of ContextListener instances and fires events on request.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractDebuggingContext
        implements DebuggingContext, SessionListener {

    /**
     * Logger for gracefully reporting unexpected errors.
     */
    private static final Logger logger = Logger.getLogger(
            AbstractDebuggingContext.class.getName());
    /**
     * List of context listeners.
     */
    private ContextEventMulticaster eventMulticaster;
    /**
     * The Session instance we belong to.
     */
    private Session ourSession;

    /**
     * Constructs a new AbstractDebuggingContext object.
     */
    protected AbstractDebuggingContext() {
        eventMulticaster = new ContextEventMulticaster();
    }

    @Override
    public void addContextListener(ContextListener listener) {
        if (listener != null) {
            eventMulticaster.add(listener);
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void connected(SessionEvent sevt) {
    }

    @Override
    public void disconnected(SessionEvent sevt) {
    }

    /**
     * Let all the change listeners know of a recent change in the context. This
     * creates an event and sends it out to the listeners.
     *
     * @param type type of the context change.
     * @param suspending true if Session is suspending as a result of this.
     */
    protected void fireChange(ContextEventType type, boolean suspending) {
        try {
            ContextEvent event = new ContextEvent(
                    this, ourSession, type, suspending);
            event.getType().fireEvent(event, eventMulticaster);
        } catch (VMDisconnectedException vmde) {
            // This happens quite often, so ignore it.
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void opened(Session session) {
        ourSession = session;
    }

    @Override
    public void removeContextListener(ContextListener listener) {
        if (listener != null) {
            eventMulticaster.remove(listener);
        }
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
