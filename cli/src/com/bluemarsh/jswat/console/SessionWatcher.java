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

package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Displays a message for each session event. If the session suspends,
 * the main window is brought forward to make it visible. This class
 * manages the program counter annotation in the source editor.
 *
 * @author  Nathan Fiedler
 */
public class SessionWatcher implements ContextListener, SessionListener,
        SessionManagerListener {
    /** Place where messages are written. */
    private OutputWriter outputWriter;

    /**
     * Creates a new instance of SessionWatcher. This instance should be
     * added as a listener to the SessionManager.
     */
    public SessionWatcher() {
        outputWriter = OutputProvider.getWriter();
        Session session = SessionProvider.getCurrentSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    @Override
    public void changedFrame(ContextEvent ce) {
        // Ignore suspending events, that's handled in suspended().
        if (!ce.isSuspending()) {
            contextChanged(ce.getSession());
        }
    }

    @Override
    public void changedLocation(ContextEvent ce) {
        // Ignore suspending events, that's handled in suspended().
        if (!ce.isSuspending()) {
            contextChanged(ce.getSession());
        }
    }

    @Override
    public void changedThread(ContextEvent ce) {
        // Ignore suspending events, that's handled in suspended().
        if (!ce.isSuspending()) {
            contextChanged(ce.getSession());
        }
    }

    @Override
    public void connected(SessionEvent sevt) {
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        JvmConnection connection = session.getConnection();
        if (connection.isRemote()) {
            showStatus(NbBundle.getMessage(
                    SessionWatcher.class, "SessionWatcher.vmAttached", name));
        } else {
            showStatus(NbBundle.getMessage(
                    SessionWatcher.class, "SessionWatcher.vmLoaded", name));
        }
    }

    private void contextChanged(Session session) {
        Session current = SessionProvider.getSessionManager().getCurrent();
        if (session.equals(current)) {
            showProgramCounter(session, true, false);
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        JvmConnection connection = session.getConnection();
        if (connection.isRemote()) {
            showStatus(NbBundle.getMessage(
                    SessionWatcher.class, "SessionWatcher.vmDetached", name));
        } else {
            showStatus(NbBundle.getMessage(
                    SessionWatcher.class, "SessionWatcher.vmClosed", name));
        }
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void resuming(SessionEvent sevt) {
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        showStatus(NbBundle.getMessage(
                SessionWatcher.class, "SessionWatcher.vmResumed", name));
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        // the session will discard its listeners
        Session session = e.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        Session current = e.getSession();
        SessionManager sm = (SessionManager) e.getSource();
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            if (session.equals(current)) {
                showProgramCounter(session, false, false);
            }
        }
    }

    /**
     * Adds the program counter annotation to the source file corresponding
     * to the current program counter.
     *
     * @param  session   Session for which to show program counter.
     * @param  source    true to open source and scroll to line.
     * @param  location  true to show the location details.
     */
    private void showProgramCounter(Session session, boolean source,
            boolean location) {
        DebuggingContext dc = ContextProvider.getContext(session);
        Location loc = dc.getLocation();
        if (loc != null) {
            if (location) {
                // Show the location details in the output window.
                String args = Strings.listToString(loc.method().argumentTypeNames());
                Object[] params = {
                    loc.declaringType().name(),
                    loc.method().name(),
                    args,
                    String.valueOf(loc.lineNumber())
                };
                String msg = NbBundle.getMessage(SessionWatcher.class,
                        "SessionWatcher.location", params);
                showStatus(msg);
            }
        }
    }

    /**
     * Indicate the current the status of the session with a message.
     *
     * @param  status  short message indicating program status.
     */
    private void showStatus(final String status) {
        outputWriter.printOutput(status);
    }

    @Override
    public void suspended(SessionEvent sevt) {
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        showStatus(NbBundle.getMessage(
                SessionWatcher.class, "SessionWatcher.vmSuspended", name));
        Session current = SessionProvider.getSessionManager().getCurrent();
        Event event = sevt.getEvent();
        if (session.equals(current) && event != null) {
            // Suspended due to a JDI event.
            showProgramCounter(session, true, true);
        }
    }
}
