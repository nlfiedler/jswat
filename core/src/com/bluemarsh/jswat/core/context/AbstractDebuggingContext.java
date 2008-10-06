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
 * $Id: AbstractDebuggingContext.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.context;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.sun.jdi.VMDisconnectedException;
import org.openide.ErrorManager;

/**
 * Class AbstractDebuggingContext provides an abstract implementation of
 * DebuggingContext for concrete implementations to subclass. It maintains
 * a set of ContextListener instances and fires events on request.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractDebuggingContext
        implements DebuggingContext, SessionListener {
    /** List of context listeners. */
    private ContextListener listeners;
    /** The Session instance we belong to. */
    private Session ourSession;

    /**
     * Constructs a new DebuggingContext object.
     */
    protected AbstractDebuggingContext() {
    }

    public void addContextListener(ContextListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = ContextEventMulticaster.add(listeners, listener);
            }
        }
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
    }

    public void disconnected(SessionEvent sevt) {
    }

    /**
     * Let all the change listeners know of a recent change in the context.
     * This creates an event and sends it out to the listeners.
     *
     * @param  type        type of the context change.
     * @param  suspending  true if Session is suspending as a result of this.
     */
    protected void fireChange(ContextEvent.Type type, boolean suspending) {
        try {
            ContextListener cl;
            synchronized (this) {
                cl = listeners;
            }
            if (cl != null) {
                ContextEvent event = new ContextEvent(
                        this, ourSession, type, suspending);
                event.getType().fireEvent(event, cl);
            }
        } catch (VMDisconnectedException vmde) {
            // This happens quite often, so ignore it.
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    public void opened(Session session) {
        ourSession = session;
    }

    public void removeContextListener(ContextListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = ContextEventMulticaster.remove(listeners, listener);
            }
        }
    }

    public void resuming(SessionEvent sevt) {
    }

    public void suspended(SessionEvent sevt) {
    }
}
