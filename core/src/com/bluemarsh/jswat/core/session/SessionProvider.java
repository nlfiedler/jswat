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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import org.openide.util.Lookup;

/**
 * SessionProvider manages an instance of SessionFactory and SessionManager.
 *
 * @author Nathan Fiedler
 */
public class SessionProvider {
    /** The SessionManager instance, if it has already been retrieved. */
    private static SessionManager sessionManager;
    /** The SessionFactory instance, if it has already been retrieved. */
    private static SessionFactory sessionFactory;

    /**
     * Creates a new instance of SessionProvider.
     */
    private SessionProvider() {
    }

    /**
     * Retrieve the current Session.
     *
     * @return  current Session.
     */
    public static Session getCurrentSession() {
        return getSessionManager().getCurrent();
    }

    /**
     * Retrieve the SessionFactory instance, creating one if necessary.
     *
     * @return  SessionFactory instance.
     */
    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // Perform lookup to find a SessionFactory instance.
            sessionFactory = Lookup.getDefault().lookup(SessionFactory.class);
        }
        return sessionFactory;
    }

    /**
     * Retrieve the SessionManager instance, creating one if necessary.
     *
     * @return  SessionManager instance.
     */
    public static synchronized SessionManager getSessionManager() {
        if (sessionManager == null) {
            // Perform lookup to find the SessionManager instance.
            sessionManager = Lookup.getDefault().lookup(SessionManager.class);
            // Load the persisted sessions.
            sessionManager.loadSessions();
            // Make sure there is a current session instance.
            Session session = sessionManager.getCurrent();
            if (session == null) {
                // No current session, we must create it.
                String id = sessionManager.generateIdentifier();
                SessionFactory sf = getSessionFactory();
                session = sf.createSession(id);
                sessionManager.add(session);
            }
        }
        return sessionManager;
    }

    /**
     * Checks if the given Session is the current session or not.
     *
     * @param  session  session to compare.
     * @return  true if current session, false otherwise.
     */
    public static boolean isCurrentSession(Session session) {
        return getSessionManager().getCurrent().equals(session);
    }
}
