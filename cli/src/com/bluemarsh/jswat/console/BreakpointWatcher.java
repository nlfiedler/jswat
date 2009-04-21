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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.core.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Strings;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * BreakpointWatcher displays a message for some of the breakpoint events.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointWatcher implements BreakpointListener,
        SessionManagerListener {
    /** Place where messages are written. */
    private OutputWriter outputWriter;

    /**
     * Creates a new instance of BreakpointWatcher.
     */
    public BreakpointWatcher() {
        outputWriter = OutputProvider.getWriter();
        SessionManager sessionMgr = SessionProvider.getSessionManager();
        Iterator<Session> iter = sessionMgr.iterateSessions();
        while (iter.hasNext()) {
            addListeners(iter.next());
        }
    }

    /**
     * Register as a listener with certain components.
     *
     * @param  session  session.
     */
    private void addListeners(Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.addBreakpointListener(this);
        Iterator<Breakpoint> biter = bm.getDefaultGroup().breakpoints(true);
        while (biter.hasNext()) {
            Breakpoint bp = biter.next();
            bp.addPropertyChangeListener(this);
        }
    }

    @Override
    public void breakpointAdded(BreakpointEvent event) {
        Breakpoint bp = event.getBreakpoint();
        bp.addPropertyChangeListener(this);
    }

    @Override
    public void breakpointRemoved(BreakpointEvent event) {
        Breakpoint bp = event.getBreakpoint();
        bp.removePropertyChangeListener(this);
    }

    @Override
    public void breakpointStopped(BreakpointEvent event) {
        // Get the breakpoint message immediately to avoid timing issues.
        Breakpoint bp = event.getBreakpoint();
        String msg = bp.describe(event.getEvent());
        // Make the breakpoint's session the current one.
        BreakpointGroup bg = bp.getBreakpointGroup();
        Session session = BreakpointProvider.getSession(bg);
        SessionManager sm = SessionProvider.getSessionManager();
        if (!sm.getCurrent().equals(session)) {
            // This must be done in the EQ to avoid deadlock.
            sm.setCurrent(session);
        }
        // Show the breakpoint's message in the output window.
        outputWriter.printOutput(msg);
    }

    @Override
    public void errorOccurred(BreakpointEvent event) {
        Breakpoint bp = event.getBreakpoint();
        outputWriter.printError(NbBundle.getMessage(
                BreakpointWatcher.class, "BreakpointWatcher.error",
                bp.getDescription()));
        outputWriter.printError(Strings.exceptionToString(
                event.getException()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }

    /**
     * Unregister as a listener with certain components.
     *
     * @param  session  session.
     */
    private void removeListeners(Session session) {
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        bm.removeBreakpointListener(this);
        Iterator<Breakpoint> biter = bm.getDefaultGroup().breakpoints(true);
        while (biter.hasNext()) {
            Breakpoint bp = biter.next();
            bp.removePropertyChangeListener(this);
        }
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        addListeners(e.getSession());
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        removeListeners(e.getSession());
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
    }
}
