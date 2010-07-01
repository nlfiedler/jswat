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
import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.Location;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import org.openide.util.NbBundle;

/**
 * Indicates when a session is launched, attached, dies, or is detached,
 * as well as interesting events that occur in relation to the sessions.
 *
 * @author  Nathan Fiedler
 */
public class SessionWatcher implements SessionListener, SessionManagerListener {
    /** Place where messages are written. */
    private OutputWriter outputWriter;

    /**
     * Creates a new instance of SessionWatcher. This instance should be
     * added as a listener to the SessionManager.
     */
    public SessionWatcher() {
        outputWriter = OutputProvider.getWriter();
    }

    @Override
    public void connected(SessionEvent sevt) {
        Session session = sevt.getSession();
        String name = session.getProperty(Session.PROP_SESSION_NAME);
        JvmConnection connection = session.getConnection();
        if (connection.isRemote()) {
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmAttached", name));
        } else {
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmLoaded", name));
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
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmDetached", name));
        } else {
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmClosed", name));
        }
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void resuming(SessionEvent sevt) {
        Session session = sevt.getSession();
        Session current = SessionProvider.getSessionManager().getCurrent();
        if (!session.equals(current)) {
            // Some other session resumed, inform the user.
            String name = session.getProperty(Session.PROP_SESSION_NAME);
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmResumed", name));
        }
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        // the session will discard its listeners
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
    }

    /**
     * Indicate the current state of the session with a message.
     *
     * @param  status  short message indicating program status.
     */
    private void showStatus(String status) {
        outputWriter.printOutput(status);
    }

    @Override
    public void suspended(SessionEvent sevt) {
        Session session = sevt.getSession();
        Session current = SessionProvider.getSessionManager().getCurrent();
        if (session.equals(current)) {
            Event event = sevt.getEvent();
            // Only respond to locatable events that are _not_ breakpoint
            // events, as those are handled by the breakpoint watcher.
            if (event != null && event instanceof LocatableEvent &&
                    !(event instanceof BreakpointEvent)) {
                LocatableEvent le = (LocatableEvent) event;
                Location loc = le.location();
                if (loc != null) {
                    // Show the location details in the output window.
                    String threadId = le.thread().name();
                    if (threadId == null || threadId.isEmpty()) {
                        threadId = String.valueOf(le.thread().uniqueID());
                    }
                    String args = Strings.listToString(loc.method().argumentTypeNames());
                    // JDB doesn't emit arg types, and doing so confuses Emacs.
                    if (Main.emulateJDB()) {
                        args = "";
                    }
                    Object[] params = {
                        loc.declaringType().name(),
                        loc.method().name(),
                        args,
                        String.valueOf(loc.lineNumber()),
                        threadId
                    };
                    String msg = NbBundle.getMessage(SessionWatcher.class,
                            "SessionWatcher.location", params);
                    showStatus(msg);
                }
            }
        } else {
            // Some other session suspended, let the user know.
            String name = session.getProperty(Session.PROP_SESSION_NAME);
            showStatus(NbBundle.getMessage(SessionWatcher.class,
                    "SessionWatcher.vmSuspended", name));
        }
    }
}
