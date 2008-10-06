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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ThreadBreakpointTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ThreadBreakpointTest extends TestCase {

    public ThreadBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ThreadBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Thread() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        String[] threads = new String[] {
            "thread1", "thread2", "thread3", "thread4"
        };
        for (String thread : threads) {
            Breakpoint bp = bf.createThreadBreakpoint(thread, true, true);
            BreakpointHelper.prepareBreakpoint(bp, session);
        }

        SessionHelper.launchDebuggee(session, "ThreadBreakpointTestCode");

        for (String thread : threads) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            ThreadReference thrd = BreakpointHelper.getThread(session);
            assertNotNull(thrd);
            assertEquals(thread, thrd.name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        Location loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }
}
