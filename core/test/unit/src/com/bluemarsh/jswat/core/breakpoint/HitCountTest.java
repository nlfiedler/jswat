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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import java.io.File;
import java.net.MalformedURLException;
import org.junit.Test;
import static org.junit.Assert.*;

public class HitCountTest {

    @Test
    public void countEqual() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);

        //
        // Test with a class that has no package name.
        //
        int line = 33;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "HitCountTestCode.java");
        Breakpoint bp = null;
        try {
            String url = srcfile.toURI().toURL().toString();
            bp = bf.createLineBreakpoint(url, null, line);
            HitCountCondition cond = new HitCountCondition();
            cond.setCount(2);
            cond.setType(HitCountConditionType.EQUAL);
            bp.addCondition(cond);
            bm.addBreakpoint(bp);
            // For code coverage, exercise additional methods.
            assertNotNull(cond.describe());
            assertEquals(2, cond.getCount());
            assertEquals(HitCountConditionType.EQUAL, cond.getType());
            assertTrue(cond.isVisible());
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "HitCountTestCode");

        // Stops only on the second hit.
        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull("failed for hit 2", loc);
        assertEquals(line, loc.lineNumber());
        assertEquals(2, bp.getHitCount());
        assertTrue("loop variable has wrong value",
                BreakpointHelper.compareVariable(session, "ii", new Integer(1)));
        bm.removeBreakpoint(bp);

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }

    @Test
    public void countGreater() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);

        //
        // Test with a class that has no package name.
        //
        int line = 33;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "HitCountTestCode.java");
        Breakpoint bp = null;
        try {
            String url = srcfile.toURI().toURL().toString();
            bp = bf.createLineBreakpoint(url, null, line);
            HitCountCondition cond = new HitCountCondition();
            cond.setCount(2);
            cond.setType(HitCountConditionType.GREATER);
            bp.addCondition(cond);
            bm.addBreakpoint(bp);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "HitCountTestCode");

        // First two hits are skipped, then after five it expires.
        for (int ii = 2; ii < 5; ii++) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            assertEquals(ii + 1, bp.getHitCount());
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("failed for hit " + ii, loc);
            assertEquals(line, loc.lineNumber());
            assertTrue("loop variable has wrong value",
                    BreakpointHelper.compareVariable(session, "ii", new Integer(ii)));
        }
        bm.removeBreakpoint(bp);

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        Location loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }

    @Test
    public void countMultiple() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);

        //
        // Test with a class that has no package name.
        //
        int line = 33;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "HitCountTestCode.java");
        Breakpoint bp = null;
        try {
            String url = srcfile.toURI().toURL().toString();
            bp = bf.createLineBreakpoint(url, null, line);
            HitCountCondition cond = new HitCountCondition();
            cond.setCount(2);
            cond.setType(HitCountConditionType.MULTIPLE);
            bp.addCondition(cond);
            bm.addBreakpoint(bp);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "HitCountTestCode");

        // Stops on every other hit.
        for (int ii = 2; ii <= 10; ii += 2) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            assertEquals(ii, bp.getHitCount());
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("failed for hit " + ii, loc);
            assertEquals(line, loc.lineNumber());
            assertTrue("loop variable has wrong value",
                    BreakpointHelper.compareVariable(session, "ii", new Integer(ii - 1)));
        }
        bm.removeBreakpoint(bp);

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        Location loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }
}
