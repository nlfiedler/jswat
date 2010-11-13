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
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.Location;
import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the breakpoint factory to ensure it creates the expected breakpoints.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointFactoryTest {

    @Test
    public void test_BreakpointFactory_NoContext() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        DebuggingContext context = ContextProvider.getContext(session);

        String[] lspecs = new String[]{
            "String:123",
            "java.lang.String:123"
        };
        for (String spec : lspecs) {
            try {
                Breakpoint bp = bf.createBreakpoint(spec, context);
                assertNotNull(bp);
                assertTrue(bp instanceof LineBreakpoint);
                LineBreakpoint lb = (LineBreakpoint) bp;
                assertEquals(123, lb.getLineNumber());
                assertTrue(lb.getSourceName().endsWith("String.java"));
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%s>", ex, spec));
            }
        }

        try {
            bf.createBreakpoint("123", context);
            fail("should fail without class name and context");
        } catch (AmbiguousClassSpecException ex) {
            // This is expected
        } catch (Exception ex) {
            fail("unexpected exception: " + ex.toString());
        }

        String[] mspecs = new String[]{
            "String:valueOf",
            "String:valueOf()",
            "String:valueOf(int)",
            "java.lang.String:valueOf",
            "java.lang.String:valueOf()",
            "java.lang.String:valueOf(int)"
        };
        for (String spec : mspecs) {
            try {
                Breakpoint bp = bf.createBreakpoint(spec, context);
                assertNotNull(bp);
                assertTrue(bp instanceof MethodBreakpoint);
                MethodBreakpoint mb = (MethodBreakpoint) bp;
                assertEquals("valueOf", mb.getMethodName());
                assertTrue(mb.getClassName().equals("String") ||
                        mb.getClassName().equals("java.lang.String"));
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%s>", ex, spec));
            }
        }

        try {
            bf.createBreakpoint("valueOf(int)", context);
            fail("should fail without class name and context");
        } catch (AmbiguousClassSpecException ex) {
            // This is expected
        } catch (Exception ex) {
            fail("unexpected exception: " + ex.toString());
        }

        try {
            bf.createBreakpoint("123.pkg.Malformed?$1:method", context);
            fail("should fail due to malformed class name");
        } catch (MalformedClassNameException ex) {
            // This is expected
        } catch (Exception ex) {
            fail("unexpected exception: " + ex.toString());
        }

        try {
            bf.createBreakpoint("java.lang.String:123abc", context);
            fail("should fail due to malformed member name");
        } catch (MalformedMemberNameException ex) {
            // This is expected
        } catch (Exception ex) {
            fail("unexpected exception: " + ex.toString());
        }

        // The session should be inactive.
        assertFalse(session.isConnected());
    }

    @Test
    public void test_BreakpointFactory_Method() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        DebuggingContext context = ContextProvider.getContext(session);

        //
        // Test with a class that has no package name.
        //
        String[] specs = new String[]{
            "method_MBTC",
            "method_params(String, int, boolean)",
            "method_params(char, double)"
        };
        for (String spec : specs) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        "MethodBreakpointTestCode:" + spec, context);
                assertNotNull(bp);
                assertTrue(bp instanceof MethodBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%s>", ex, spec));
            }
        }

        // Test that the breakpoints actually work.
        SessionHelper.launchDebuggee(session, "MethodBreakpointTestCode");

        for (String spec : specs) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for method " + spec, loc);
            assertTrue(spec.startsWith(loc.method().name()));
        }

        // Verify that we can set a breakpoint without a class name
        // as long as the current location is set.
        try {
            Breakpoint bp = bf.createBreakpoint("method_MBTC", context);
            assertNotNull(bp);
            assertTrue(bp instanceof MethodBreakpoint);
        } catch (Exception ex) {
            fail("unexpected exception: " + ex);
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());

        //
        // Now test using a class inside a package.
        //
        for (String spec : specs) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        "jswat.test.MethodBreakpointTestCode:" + spec, context);
                assertNotNull(bp);
                assertTrue(bp instanceof MethodBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%s>", ex, spec));
            }
        }

        // Test that the breakpoints actually work.
        SessionHelper.launchDebuggee(session, "jswat.test.MethodBreakpointTestCode");

        for (String spec : specs) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for method " + spec, loc);
            assertTrue(spec.startsWith(loc.method().name()));
        }

        // Verify that we can set a breakpoint without a class name
        // as long as the current location is set.
        try {
            Breakpoint bp = bf.createBreakpoint("method_MBTC", context);
            assertNotNull(bp);
            assertTrue(bp instanceof MethodBreakpoint);
        } catch (Exception ex) {
            fail("unexpected exception: " + ex);
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());
    }

    @Test
    public void test_BreakpointFactory_Line() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        DebuggingContext context = ContextProvider.getContext(session);

        //
        // Test with a class that has no package name.
        //
        int[] lines = new int[]{33, 46, 53, 63, 81};
        for (int line : lines) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        String.format("LineBreakpointTestCode:%d", line),
                        context);
                assertNotNull(bp);
                assertTrue(bp instanceof LineBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%d>", ex, line));
            }
        }

        // Test that the breakpoints actually work.
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

        // Verify that we can set a breakpoint without a class name
        // as long as the current location is set.
        try {
            Breakpoint bp = bf.createBreakpoint(String.valueOf(lines[0]), context);
            assertNotNull(bp);
            assertTrue(bp instanceof LineBreakpoint);
        } catch (Exception ex) {
            fail("unexpected exception: " + ex);
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());

        //
        // Now test using a class inside a package.
        //
        lines = new int[]{35, 46, 53, 64};
        for (int line : lines) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        String.format("jswat.test.LineBreakpointTestCode:%d",
                        line), context);
                assertNotNull(bp);
                assertTrue(bp instanceof LineBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%d>", ex, line));
            }
        }

        // Test that the breakpoints actually work.
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

        // Verify that we can set a breakpoint without a class name
        // as long as the current location is set.
        try {
            Breakpoint bp = bf.createBreakpoint(String.valueOf(lines[0]), context);
            assertNotNull(bp);
            assertTrue(bp instanceof LineBreakpoint);
        } catch (Exception ex) {
            fail("unexpected exception: " + ex);
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        assertFalse(session.isConnected());
    }

    @Test
    public void test_BreakpointFactory_LineUsingSource() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        DebuggingContext context = ContextProvider.getContext(session);

        //
        // Test with a class that has no package name, using the
        // name of the source file (with a made up path since the
        // slash must be there to look like a file name).
        //
        int[] lines = new int[]{33, 46, 53, 63, 81};
        String path = "foo" + File.separator;
        for (int line : lines) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        String.format("%sLineBreakpointTestCode.java:%d",
                        path, line), context);
                assertNotNull(bp);
                assertTrue(bp instanceof LineBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%d>", ex, line));
            }
        }

        // Test that the breakpoints actually work.
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

        //
        // Now test using a class inside a package.
        //
        lines = new int[]{35, 46, 53, 64};
        path = "jswat" + File.separator + "test" + File.separator;
        for (int line : lines) {
            try {
                Breakpoint bp = bf.createBreakpoint(
                        String.format("%sLineBreakpointTestCode.java:%d",
                        path, line), context);
                assertNotNull(bp);
                assertTrue(bp instanceof LineBreakpoint);
                BreakpointHelper.prepareBreakpoint(bp, session);
            } catch (Exception ex) {
                fail(String.format("unexpected exception <%s> for <%d>", ex, line));
            }
        }

        // Test that the breakpoints actually work.
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
    }
}
