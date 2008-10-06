/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionTest.java 15 2007-06-03 00:01:17Z nfiedler $
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
import java.io.File;
import java.net.MalformedURLException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.bluemarsh.jswat.core.context.DebuggingContext;

public class SessionTest extends TestCase {

    public SessionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Session_addRemoveListener() {
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

    public void test_Session_active_deactivate() {
        Session session = SessionHelper.getSession();
        assertFalse(session.isConnected());
        SessionHelper.launchDebuggee(session, "irrelevant");
        assertTrue(session.isConnected());
        session.disconnect(true);
        assertFalse(session.isConnected());
    }

    public void test_Session_resume_suspend() {
        Session session = SessionHelper.getSession();
        assertFalse(session.isConnected());
        SessionHelper.launchDebuggee(session, "SessionTestCode");
        assertTrue(session.isConnected());
        session.resumeVM();
        assertTrue(session.isConnected());
        assertFalse(session.isSuspended());
        session.suspendVM();
        assertTrue(session.isSuspended());

        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "SessionTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            Breakpoint bp = bf.createLineBreakpoint(url, null, 35);
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

//    private static void showLocation(Session session) {
//        DebuggingContext dc = ContextProvider.getContext(session);
//        Location loc = dc.getLocation();
//        System.out.println(loc.declaringType().name() + "." +
//                loc.method().name() + "@" + loc.lineNumber());
//    }

    public class TestListener implements SessionListener {
        public boolean wasAdded;
        public boolean wasRemoved;

        public void closing(SessionEvent sevt) {
            wasRemoved = true;
        }

        public void connected(SessionEvent sevt) {
        }

        public void disconnected(SessionEvent sevt) {
        }

        public void opened(Session session) {
            wasAdded = true;
        }

        public void resuming(SessionEvent sevt) {
        }

        public void suspended(SessionEvent sevt) {
        }
    }
}
