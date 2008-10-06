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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import org.openide.util.Lookup;

/**
 * RuntimeProvider manages an instance of SessionFactory and SessionManager.
 *
 * @author Nathan Fiedler
 */
public class SessionProvider {
    /** The SessionManager instance, if it has already been retrieved. */
    private static SessionManager sessionManager;
    /** The SessionFactory instance, if it has already been retrieved. */
    private static SessionFactory sessionFactory;

    /**
     * Creates a new instance of RuntimeProvider.
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
            sessionFactory = (SessionFactory)
                Lookup.getDefault().lookup(SessionFactory.class);
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
            sessionManager = (SessionManager)
                Lookup.getDefault().lookup(SessionManager.class);
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
}
