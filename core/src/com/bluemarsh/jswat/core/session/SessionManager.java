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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import java.util.Iterator;

/**
 * A SessionManager manages all of the open instances of Session. It also
 * maintains a reference to the current Session that actions should be
 * directed toward. In addition, the manager keeps track of the properties
 * associated with a Session, persisting them to secondary storage as needed.
 *
 * <p>Concrete implementations of this interface are acquired from the
 * <code>SessionProvider</code> class.
 *
 * @author  Nathan Fiedler
 */
public interface SessionManager {

    /**
     * Adds a session to this manager's list. It is assumed that the
     * session has already been initialized.
     *
     * <p>If the current session has not been set, the given session
     * will become the current.</p>
     *
     * @param  session  session to be added.
     */
    void add(Session session);

    /**
     * Adds the session manager listener to this manager.
     *
     * @param  l  listener to add.
     */
    void addSessionManagerListener(SessionManagerListener l);

    /**
     * Makes a copy of the given session, returning the copy. The session
     * is added to this manager. The new session will be in a disconnected
     * state, despite whatever state the original was in.
     *
     * @param  session  session to be copied.
     * @param  name     name of new session instance, or null to generate.
     * @return  new session instance.
     */
    Session copy(Session session, String name);

    /**
     * Find a Session instance by the given identifier.
     *
     * @param  id  session identifier.
     * @return  matching Session, or null if not found.
     */
    Session findById(String id);

    /**
     * Generates a new, unique session identifier.
     *
     * @return  new session identifier.
     */
    String generateIdentifier();

    /**
     * Returns the session that is marked as the current one, which
     * is the target of user actions.
     *
     * @return  the current session.
     */
    Session getCurrent();

    /**
     * Creates an iterator of the currently open sessions.
     *
     * @return  session iterator.
     */
    Iterator<Session> iterateSessions();

    /**
     * Load all of the sessions previously saved to persistent storage.
     */
    void loadSessions();

    /**
     * Removes all traces of the given session. This does nothing to
     * the session itself, so it is up to the caller to disconnect and
     * close the session appropriately.
     *
     * <p>Additionally, it is up to the caller to set the current session
     * to one of the other available sessions before removing it.</p>
     *
     * @param  session  session to be removed.
     */
    void remove(Session session);

    /**
     * Removes the session manager listener from this manager.
     *
     * @param  l  listener to remove.
     */
    void removeSessionManagerListener(SessionManagerListener l);

    /**
     * Save all of the sessions to persistent storage.
     *
     * @param  close  true to close sessions before saving. This should be
     *                set when the application is exiting, otherwise some
     *                listeners will not have a chance to shut down properly.
     */
    void saveSessions(boolean close);

    /**
     * Marks the given session as the current session, to which actions
     * will direct their attention.
     *
     * @param  session  the session to make current.
     */
    void setCurrent(Session session);
}
