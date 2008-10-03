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
 * $Id: BreakpointWatcher.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
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
import com.bluemarsh.jswat.ui.editor.DebugAnnotation;
import com.bluemarsh.jswat.ui.editor.EditorConstants;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;

/**
 * Class BreakpointWatcher accomplishes a couple of tasks simultaneously.
 *
 * <ul>
 * <li>Displays a message for some of the breakpoint events.</li>
 * <li>Annotates lines in source code as breakpoints are added, removed.</li>
 * </ul>
 *
 * @author  Nathan Fiedler
 */
public class BreakpointWatcher implements BreakpointListener,
        PropertyChangeListener, SessionManagerListener {
    /** Place where messages are written. */
    private OutputWriter outputWriter;

    /**
     * Creates a new instance of BreakpointWatcher.
     */
    public BreakpointWatcher() {
        outputWriter = OutputProvider.getWriter();
        SessionManager sessionMgr = SessionProvider.getSessionManager();
        sessionMgr.addSessionManagerListener(this);
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

    public void breakpointAdded(BreakpointEvent event) {
        Breakpoint bp = event.getBreakpoint();
        bp.addPropertyChangeListener(this);
        if (bp instanceof LineBreakpoint) {
            updateAnnotation((LineBreakpoint) bp);
        }
    }

    public void breakpointRemoved(BreakpointEvent event) {
        Breakpoint bp = event.getBreakpoint();
        bp.removePropertyChangeListener(this);
        if (bp instanceof LineBreakpoint) {
            removeAnnotation(bp);
        }
    }

    public void breakpointStopped(final BreakpointEvent event) {
        // Get the breakpoint message immediately to avoid timing issues.
        final String msg = event.getBreakpoint().describe(event.getEvent());
        // The rest must be done on the AWT thread.
        Runnable runner = new Runnable() {
            public void run() {
                Breakpoint bp = event.getBreakpoint();
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
        };
        EventQueue.invokeLater(runner);
    }

    public void errorOccurred(final BreakpointEvent event) {
        Runnable runner = new Runnable() {
            public void run() {
                Breakpoint bp = event.getBreakpoint();
                outputWriter.ensureVisible();
                outputWriter.printError(NbBundle.getMessage(
                        BreakpointWatcher.class, "BreakpointWatcher.error",
                        bp.getDescription()));
                outputWriter.printError(Strings.exceptionToString(
                        event.getException()));
            }
        };
        EventQueue.invokeLater(runner);
    }

    public void propertyChange(PropertyChangeEvent event) {
        Object src = event.getSource();
        String pname = event.getPropertyName();
        // We need to ignore the annotation property changes, since
        // we are the ones causing them; otherwise stack overflows.
        if (!pname.equals(EditorConstants.PROP_ANNOTATION) &&
                src instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) src;
            updateAnnotation(lb);
        }
    }

    /**
     * Removes any annotation that had been set for this breakpoint.
     *
     * @param  bp  breakpoint for which to remove annotation.
     */
    private void removeAnnotation(Breakpoint bp) {
        Annotation ann = (Annotation) bp.getProperty(
                EditorConstants.PROP_ANNOTATION);
        if (ann != null) {
            EditorSupport.getDefault().removeAnnotation(ann);
            bp.setProperty(EditorConstants.PROP_ANNOTATION, null);
        }
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

    public void sessionAdded(SessionManagerEvent e) {
        addListeners(e.getSession());
    }

    public void sessionRemoved(SessionManagerEvent e) {
        removeListeners(e.getSession());
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        // Add annotations for the current session, remove all the others.
        Session current = e.getSession();
        SessionManager sm = (SessionManager) e.getSource();
        Iterator<Session> siter = sm.iterateSessions();
        while (siter.hasNext()) {
            Session session = siter.next();
            boolean isCurrent = session.equals(current);
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            BreakpointGroup bg = bm.getDefaultGroup();
            Iterator<Breakpoint> biter = bg.breakpoints(true);
            while (biter.hasNext()) {
                Breakpoint bp = biter.next();
                if (bp instanceof LineBreakpoint) {
                    if (isCurrent) {
                        updateAnnotation((LineBreakpoint) bp);
                    } else {
                        removeAnnotation(bp);
                    }
                }
            }
        }
    }

    /**
     * Removes the existing annotation for the given breakpoint, if any,
     * and creates a new annotation to reflect its current state.
     *
     * @param  bp  breakpoint to be annotated.
     */
    private void updateAnnotation(LineBreakpoint bp) {
        removeAnnotation(bp);
        EditorSupport es = EditorSupport.getDefault();
        Annotation ann = null;
        if (bp.isEnabled()) {
            ann = es.annotate(bp.getURL(), bp.getLineNumber(),
                DebugAnnotation.BREAKPOINT_TYPE, bp);
        } else {
            ann = es.annotate(bp.getURL(), bp.getLineNumber(),
                DebugAnnotation.DISABLED_BREAKPOINT_TYPE, bp);
        }
        bp.setProperty(EditorConstants.PROP_ANNOTATION, ann);
    }
}
