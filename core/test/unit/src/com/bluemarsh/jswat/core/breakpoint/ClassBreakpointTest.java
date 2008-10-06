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
 * $Id: ClassBreakpointTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ClassBreakpointTest extends TestCase {

    public ClassBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ClassBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Class() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has no package name.
        //
        String[] classes = new String[] { "CBSecond" };
        for (String clazz : classes) {
            Breakpoint bp = bf.createClassBreakpoint(clazz, true, true);
            BreakpointHelper.prepareBreakpoint(bp, session);
        }

        SessionHelper.launchDebuggee(session, "ClassBreakpointTestCode");

        String[] methods = new String[] { "loadCBSecond" };
        for (String method : methods) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull(loc);
            assertEquals(method, loc.method().name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.

        //
        // Now test using a class inside a package.
        //
        classes = new String[] { "jswat.test.CBSecond" };
        for (String clazz : classes) {
            Breakpoint bp = bf.createClassBreakpoint(clazz, true, true);
            BreakpointHelper.prepareBreakpoint(bp, session);
        }

        SessionHelper.launchDebuggee(session, "jswat.test.ClassBreakpointTestCode");

        for (String method : methods) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull(loc);
            assertEquals(method, loc.method().name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
    }
}
