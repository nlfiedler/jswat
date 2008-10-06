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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: LineBreakpointTest.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import java.io.File;
import java.net.MalformedURLException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LineBreakpointTest extends TestCase {

    public LineBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(LineBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Line() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has no package name.
        //
        int tfLine = 63;
        int[] lines = new int[] { 33, 46, 53, 81, tfLine };
        String srcpath = System.getProperty("test.src.dir");
        File subdir = new File(new File(srcpath, "jswat"), "test");
        File subdira = new File(new File(srcpath, "jswat"), "testa");
        File srcfile = new File(srcpath, "LineBreakpointTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            for (int line : lines) {
                Breakpoint bp = bf.createLineBreakpoint(url, null, line);
                BreakpointHelper.prepareBreakpoint(bp, session);
                if (line == tfLine) {
                    bp.setThreadFilter("thread1");
                    bp.setExpireCount(0);
                    // Note that this breakpoint will not be automatically
                    // deleted since the expiration count is zero.
                }
            }
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "LineBreakpointTestCode");
        assertTrue(session.isConnected());

        for (int line : lines) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            assertTrue(session.isConnected());
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("failed for line " + line, loc);
            assertEquals(line, loc.lineNumber());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());
        // Because of the breakpoint w/o expire count defined above,
        // we must delete the remaining breakpoints.
        BreakpointHelper.deleteAll(session);

        //
        // Now test using a class inside a package.
        //
        lines = new int[] { 35, 46, 53, 64 };
        srcfile = new File(subdir, "LineBreakpointTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            for (int line : lines) {
                Breakpoint bp = bf.createLineBreakpoint(url, "jswat.test", line);
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "jswat.test.LineBreakpointTestCode");
        assertTrue(session.isConnected());

        for (int line : lines) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            assertTrue(session.isConnected());
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("failed for line " + line, loc);
            assertEquals(line, loc.lineNumber());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());

        //
        // Test matching the location by source path (no package).
        //
        srcfile = new File(subdira, "SourceNameTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            Breakpoint bp = bf.createLineBreakpoint(url, null, 34);
            BreakpointHelper.prepareBreakpoint(bp, session);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }
        SessionHelper.launchDebuggee(session, "jswat.testa.SourceNameTestCode");
        assertTrue(session.isConnected());
        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        assertTrue(session.isConnected());
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull("failed to stop at line 34", loc);
        assertEquals(34, loc.lineNumber());
        assertEquals("hit wrong source file", "packageA", loc.method().name());

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());

        //
        // Test matching the location by source path (with package).
        //
        srcfile = new File(subdir, "SourceNameTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            Breakpoint bp = bf.createLineBreakpoint(url, null, 34);
            BreakpointHelper.prepareBreakpoint(bp, session);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }
        SessionHelper.launchDebuggee(session, "jswat.testa.SourceNameTestCode");
        assertTrue(session.isConnected());
        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        assertTrue(session.isConnected());
        // We are supposedly at a breakpoint, verify that this is so.
        loc = BreakpointHelper.getLocation(session);
        assertNotNull("failed to stop at line 34", loc);
        assertEquals(34, loc.lineNumber());
        assertEquals("hit wrong source file", "packageB", loc.method().name());

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());
    }
}
