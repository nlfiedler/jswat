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
 * are Copyright (C) 2002-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core;

import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.stepping.Stepper;
import com.bluemarsh.jswat.core.stepping.SteppingException;
import com.bluemarsh.jswat.core.stepping.SteppingProvider;
import java.util.concurrent.Semaphore;

/**
 * Class SessionTestManager manages Sessions. It starts and stops a single
 * Session instance and provides methods for launching a debuggee and
 * waiting for the debuggee to stop.
 *
 * @author  Nathan Fiedler
 */
public class SessionHelper {
    /** Our session listener. */
    private static TestSessionListener listener;
    /** Semaphore for the session suspended notification. */
    private static Semaphore suspendedSem;

    static {
        listener = new TestSessionListener();
        suspendedSem = new Semaphore(1);
    }

    /**
     * Get the current Session instance from the SessionManager.
     *
     * @return  a Session instance.
     */
    public static Session getSession() {
        SessionManager sm = SessionProvider.getSessionManager();
        return sm.getCurrent();
    }

    /**
     * Returns the classpath for the unit tests, such that it may be used
     * to run the test code.
     *
     * @return  unit test classpath.
     */
    public static String getTestClasspath() {
        String clspath = System.getProperty("test.build.dir");
        if (clspath == null || clspath.length() == 0) {
            throw new RuntimeException("test.build.dir sysproperty required");
        }
        return clspath;
    }

    /**
     * Returns the sourcepath for the unit tests, such that it may be used
     * to find the test code source files.
     *
     * @return  unit test sourcepath.
     */
    public static String getTestSourcepath() {
        String clspath = System.getProperty("test.src.dir");
        if (clspath == null || clspath.length() == 0) {
            throw new RuntimeException("test.src.dir sysproperty required");
        }
        return clspath;
    }

    /**
     * Builds a connection and activates the given Session using that
     * connection. Causes the debuggee to be created but will be left
     * suspended. Exceptions if the session is already active.
     *
     * @param  session  Session to connect to the debuggee.
     * @param  main     class to launch (with optional arguments).
     */
    public static synchronized void launchDebuggee(Session session, String main) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (session.isConnected()) {
            throw new IllegalStateException("session must be disconnected");
        }
        String cp = "-cp " + getTestClasspath();
        // Find the default runtime instance.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        String base = rf.getDefaultBase();
        JavaRuntime rt = rm.findByBase(base);
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        JvmConnection conn = factory.createLaunching(rt, cp, main);
        try {
            conn.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        session.connect(conn);
        // Need to give the event handling thread a chance to dispatch
        // events for the session activation. This is merely to simulate
        // the natural delay that comes with being a user-driven app.
        try {
            // Note that yield() is not effective on some systems.
            // Also, a value of 1 used to work, but it seems that after the
            // event handling changes in revision 2843, a longer delay is
            // necessary for some of the breakpoint tests to succeed.
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            // ignored
        }
    }

    /**
     * Resume the given session and wait for it to suspend again. Typically
     * this means the method will not return until a breakpoint has been hit.
     *
     * @param  session  Session to resume and wait for.
     */
    public static synchronized void resumeAndWait(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (!session.isConnected()) {
            throw new IllegalStateException("session must be connected");
        }

        session.addSessionListener(listener);
        // Drain all of the permits from the semaphore first so that later
        // we are forced to wait until a permit has been released.
        suspendedSem.drainPermits();
        // Let the session resume, which may suspend immediately.
        // It will not matter because...
        session.resumeVM();
        // ...we will try to re-acquire the semaphore again. This can only
        // succeed if the listener has received a suspending event and
        // released the semaphore.
        try {
            suspendedSem.acquire();
        } catch (InterruptedException ie) {
            // ignored
        }
        session.removeSessionListener(listener);
    }

    /**
     * Performs a single-step operation using the Stepper interface.
     * Resumes the given session and waits for it to suspend again.
     *
     * @param  session  Session to resume and wait for.
     * @throws  SteppingException
     *          if current thread is not set.
     */
    public static synchronized void stepIntoAndWait(Session session) throws
            SteppingException {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }
        if (!session.isConnected()) {
            throw new IllegalStateException("session must be connected");
        }
        // See the resumeAndWait() method for the explanation of this code.
        session.addSessionListener(listener);
        suspendedSem.drainPermits();
        Stepper stepper = SteppingProvider.getStepper(session);
        stepper.stepInto();
        try {
            suspendedSem.acquire();
        } catch (InterruptedException ie) {
            // ignored
        }
        session.removeSessionListener(listener);
    }

    /**
     * Listens to the session for the suspending events.
     */
    protected static class TestSessionListener implements SessionListener {

        public void closing(SessionEvent sevt) {
        }

        public void connected(SessionEvent sevt) {
        }

        public void disconnected(SessionEvent sevt) {
            // This is equivalent to being suspended for our purposes.
            suspendedSem.release();
        }

        public void opened(Session session) {
        }

        public void resuming(SessionEvent sevt) {
        }

        public void suspended(SessionEvent sevt) {
            suspendedSem.release();
        }
    }
}
