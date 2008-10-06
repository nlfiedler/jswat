/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractSessionManager.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import java.util.Iterator;

/**
 * Class AbstractSessionManager provides an abstract implementation of a
 * SessionManager for the concrete implementations to subclass. It takes
 * care of basic functionality such as managing listeners.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractSessionManager implements SessionManager {
    /** The prefix for generating session names. */
    private static final String GEN_NAME_PREFIX = "Session ";
    /** Session manager listeners list. */
    private SessionManagerListener sessionManagerListener = null;

    /**
     * Constructs a new instance of AbstractSessionManager.
     */
    protected AbstractSessionManager() {
    }

    public void addSessionManagerListener(SessionManagerListener l) {
        if (l != null) {
            synchronized (this) {
                sessionManagerListener = SessionManagerEventMulticaster.add(
                        sessionManagerListener, l);
            }
        }
    }

    /**
     * Dispatches the event to the registered listeners.
     *
     * @param  event  event to be dispatched.
     */
    protected void fireEvent(SessionManagerEvent event) {
        SessionManagerListener listener;
        synchronized (this) {
            listener = sessionManagerListener;
        }
        if (listener != null) {
            event.getType().fireEvent(event, listener);
        }
    }

    /**
     * Generates a new, unique session name.
     *
     * @return  new session name.
     */
    protected String generateName() {
        Iterator<Session> iter = iterateSessions();
        if (!iter.hasNext()) {
            return GEN_NAME_PREFIX + '1';
        } else {
            int max = 0;
            while (iter.hasNext()) {
                Session session = iter.next();
                String name = session.getProperty(Session.PROP_SESSION_NAME);
                if (name.startsWith(GEN_NAME_PREFIX)) {
                    name = name.substring(GEN_NAME_PREFIX.length());
                    try {
                        int i = Integer.parseInt(name);
                        if (i > max) {
                            max = i;
                        }
                    } catch (NumberFormatException nfe) {
                        // skip it, it won't matter
                    }
                }
            }
            max++;
            return GEN_NAME_PREFIX + max;
        }
    }

    public void removeSessionManagerListener(SessionManagerListener l) {
        if (l != null) {
            synchronized (this) {
                sessionManagerListener = SessionManagerEventMulticaster.remove(
                    sessionManagerListener, l);
            }
        }
    }
}
