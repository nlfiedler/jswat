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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointHelper;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.sun.jdi.ThreadReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionTest {

    @Test
    public void properties() {
        Session session = SessionHelper.getSession();
        PropListener pl = new PropListener();
        assertFalse(pl.changed);
        session.addPropertyChangeListener(pl);
        assertFalse(pl.changed);
        session.setProperty("foobar", "bazquux");
        assertTrue(pl.changed);
        pl.changed = false;
        session.setProperty("foobar", null);
        assertTrue(pl.changed);
        session.removePropertyChangeListener(pl);
    }

    @Test
    public void createClose() {
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit_test_c");
        session.close();
    }

    @Test(expected = IllegalStateException.class)
    public void alreadyDisconnected() {
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit_test_d");
        session.disconnect(true);
    }

    @Test(expected = IllegalStateException.class)
    public void isSuspendedDisconnected() {
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit_test_isd");
        session.isSuspended();
    }

    @Test(expected = IllegalStateException.class)
    public void resumeDisconnected() {
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit_test_rd");
        session.resumeVM();
    }

    @Test(expected = IllegalStateException.class)
    public void suspendDisconnected() {
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit_test_sd");
        session.suspendVM();
    }

    @Test
    public void isEquals() {
        Session session = SessionHelper.getSession();
        assertFalse(session.equals(this));
        assertTrue(session.equals(session));
    }

    @Test
    public void addRemoveListener() {
        Session session = SessionHelper.getSession();
        session.addSessionListener(null);
        session.removeSessionListener(null);
        TestListener tl = new TestListener();
        session.removeSessionListener(tl);
        // Technically it is silly to remove a listener that is not registered,
        // but due to the implementation, the session cannot tell otherwise.
        assertTrue(tl.wasRemoved);
        tl.wasRemoved = false;
        assertFalse(tl.wasAdded);
        assertFalse(tl.wasRemoved);
        session.addSessionListener(tl);
        session.removeSessionListener(tl);
        assertTrue(tl.wasAdded);
        assertTrue(tl.wasRemoved);
    }

    @Test
    public void active_deactivate() {
        Session session = SessionHelper.getSession();
        assertFalse(session.isConnected());
        assertEquals("", session.getAddress());
        assertEquals("Java", session.getStratum());
        SessionHelper.launchDebuggee(session, "irrelevant");
        try {
            session.connect(null);
        } catch (IllegalStateException ise) {
            // expected
        }
        try {
            session.close();
        } catch (IllegalStateException ise) {
            // expected
        }
        assertTrue(session.isConnected());
        assertEquals("(launched)", session.getAddress());
        assertEquals("Java", session.getStratum());
        session.disconnect(true);
        assertFalse(session.isConnected());
    }

    @Test
    public void resume_suspend() {
        Session session = SessionHelper.getSession();
        assertFalse(session.isConnected());
        assertEquals("Disconnected", session.getState());
        SessionHelper.launchDebuggee(session, "SessionTestCode");
        assertTrue(session.isConnected());
        session.resumeVM();
        assertTrue(session.isConnected());
        assertFalse(session.isSuspended());
        assertEquals("Running", session.getState());
        session.suspendVM();
        assertTrue(session.isSuspended());
        assertEquals("Stopped", session.getState());

        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "SessionTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            Breakpoint bp = bf.createLineBreakpoint(url, null, 41);
            BreakpointHelper.prepareBreakpoint(bp, session);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.resumeAndWait(session);
        assertTrue(session.isConnected());

        // Now we are at the breakpoint, use the thread to invoke a method.
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        int frame = dc.getFrame();
        Evaluator eval = new Evaluator("SessionTestCode.stopWaiting()");
        try {
            eval.evaluate(thread, frame);
        } catch (EvaluationException ee) {
            ee.printStackTrace();
        }
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());
    }

    private static class TestListener implements SessionListener {

        public boolean wasAdded;
        public boolean wasRemoved;

        @Override
        public void closing(SessionEvent sevt) {
            wasRemoved = true;
        }

        @Override
        public void connected(SessionEvent sevt) {
        }

        @Override
        public void disconnected(SessionEvent sevt) {
        }

        @Override
        public void opened(Session session) {
            wasAdded = true;
        }

        @Override
        public void resuming(SessionEvent sevt) {
        }

        @Override
        public void suspended(SessionEvent sevt) {
        }
    }

    private static class PropListener implements PropertyChangeListener {
        public boolean changed;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            changed = true;
        }
    }
}
