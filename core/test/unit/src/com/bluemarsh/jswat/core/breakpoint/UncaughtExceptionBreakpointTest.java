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
 * $Id: UncaughtExceptionBreakpointTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UncaughtExceptionBreakpointTest extends TestCase {

    public UncaughtExceptionBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(UncaughtExceptionBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Uncaught_Exception() {
        //
        // Note that Java 1.5 now appears to have default handlers for
        // uncaught exceptions, as provided by the Thread implementations.
        // Not sure how long this unit test will continue working, and on
        // which platforms will it fail.
        //
        Session session = SessionHelper.getSession();

        SessionHelper.launchDebuggee(session, "UncaughtExceptionTestCode");

        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull(loc);
        assertEquals("badcode", loc.method().name());

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
    }
}
