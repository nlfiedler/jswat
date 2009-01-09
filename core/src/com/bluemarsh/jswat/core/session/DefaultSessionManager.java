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
 * are Copyright (C) 2004-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 * DefaultSessionManager manages Session instances persisted to properties
 * files in the userdir.
 *
 * @author  Nathan Fiedler
 */
public class DefaultSessionManager extends AbstractSessionManager {
    /** The prefix for session identifiers. */
    private static final String ID_PREFIX = "SID_";
    /** List of the open sessions. */
    private List<Session> openSessions;
    /** The currently selected Session instance. */
    private Session currentSession;

    /**
     * Creates a new instance of SessionManager.
     */
    public DefaultSessionManager() {
        super();
        openSessions = new LinkedList<Session>();
    }

    public synchronized void add(Session session) {
        // Give the session a name, if it doesn't have one already.
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        if (name == null || name.length() == 0) {
            name = generateName();
            session.setProperty(Session.PROP_SESSION_NAME, name);
        }

        openSessions.add(session);
        if (currentSession == null) {
            setCurrent(session);
        }
        fireEvent(new SessionManagerEvent(this, session,
                SessionManagerEvent.Type.ADDED));
    }

    public synchronized Session copy(Session session, String name) {
        SessionFactory factory = SessionProvider.getSessionFactory();
        String id = generateIdentifier();
        Session copy = factory.createSession(id);
        Iterator<String> keys = session.propertyNames();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = session.getProperty(key);
            copy.setProperty(key, value);
        }
        openSessions.add(copy);
        fireEvent(new SessionManagerEvent(this, copy,
                SessionManagerEvent.Type.ADDED));
        return copy;
    }

    /**
     * Find a Session instance by the given identifier.
     *
     * @param  id  session identifier.
     * @return  matching Session, or null if not found.
     */
    private Session findById(String id) {
        Iterator<Session> sessions = iterateSessions();
        while (sessions.hasNext()) {
            Session session = sessions.next();
            if (session.getIdentifier().equals(id)) {
                return session;
            }
        }
        return null;
    }

    public String generateIdentifier() {
        if (openSessions.isEmpty()) {
            return ID_PREFIX + '1';
        } else {
            int max = 0;
            for (Session session : openSessions) {
                String id = session.getIdentifier();
                id = id.substring(ID_PREFIX.length());
                try {
                    int i = Integer.parseInt(id);
                    if (i > max) {
                        max = i;
                    }
                } catch (NumberFormatException nfe) {
                    // This cannot happen as we generate the identifier
                    // and it will always have an integer suffix.
                }
            }
            max++;
            return ID_PREFIX + max;
        }
    }

    public synchronized Session getCurrent() {
        return currentSession;
    }

    @SuppressWarnings("unchecked")
    public synchronized void loadSessions() {
        // Read the persisted Sessions from disk.
        XMLDecoder decoder = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "sessions.xml";
            FileObject fo = fs.findResource(name);
            if (fo != null && fo.isData()) {
                InputStream is = fo.getInputStream();
                decoder = new XMLDecoder(is);
                decoder.setExceptionListener(new ExceptionListener() {
                    public void exceptionThrown(Exception e) {
                        ErrorManager.getDefault().notify(e);
                    }
                });
                openSessions = (List<Session>) decoder.readObject();
                // Get the ID of the current session.
                String id = (String) decoder.readObject();
                Session session = findById(id);
                if (session != null) {
                    setCurrent(session);
                }
            }
        } catch (Exception e) {
            // Parser, I/O, and various runtime exceptions may occur,
            // need to report them and gracefully recover.
            ErrorManager.getDefault().notify(e);
            // SessionProvider will ensure that a current session exists.
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
    }

    public synchronized Iterator<Session> iterateSessions() {
        // Make sure the caller cannot modify the list.
        List<Session> ro = Collections.unmodifiableList(openSessions);
        return ro.iterator();
    }

    public synchronized void remove(Session session) {
        if (currentSession == session) {
            throw new IllegalArgumentException("cannot delete current session");
        }
        openSessions.remove(session);
        fireEvent(new SessionManagerEvent(this, session,
                SessionManagerEvent.Type.REMOVED));
    }

    public synchronized void saveSessions(boolean close) {
        if (close) {
            for (Session session : openSessions) {
                if (session.isConnected()) {
                    session.disconnect(false);
                }
                session.close();
            }
        }
        FileLock lock = null;
        try {
            FileSystem fs = Repository.getDefault().getDefaultFileSystem();
            String name = "sessions.xml";
            FileObject fo = fs.findResource(name);
            if (fo == null) {
                fo = fs.getRoot().createData(name);
            }
            lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(openSessions);
            encoder.writeObject(currentSession.getIdentifier());
            encoder.close();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }

    public synchronized void setCurrent(Session session) {
        currentSession = session;
        fireEvent(new SessionManagerEvent(this, session,
                SessionManagerEvent.Type.CURRENT));
    }

    /**
     * Listens for exceptions while calling XMLDecoder.
     */
    private static class DecoderListener implements ExceptionListener {
        private Exception exception;

        /**
         * This method is called when a recoverable exception has been caught. 
         * 
         * @param e The exception that was caught. 
         */
        public void exceptionThrown(Exception e) {
            exception = e;
        }

        /**
         * @return  exception thrown by XMLDecoder.
         */
        public Exception getException() {
            return exception;
        }
    }
}
