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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
