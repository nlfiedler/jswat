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
 * $Id: ConditionTest.java 15 2007-06-03 00:01:17Z nfiedler $
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

public class ConditionTest extends TestCase {

    public ConditionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ConditionTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Condition() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has no package name.
        //
        int line = 27;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "HitCountTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            Breakpoint bp = bf.createLineBreakpoint(url, null, line);
            BreakpointHelper.prepareBreakpoint(bp, session);
            Condition cond = bf.createCondition("ii == 5");
            bp.addCondition(cond);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "HitCountTestCode");

        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull("failed to hit breakpoint", loc);
        assertEquals(line, loc.lineNumber());
        assertTrue("loop variable has wrong value",
                BreakpointHelper.compareVariable(session, "ii", new Integer(5)));

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }
}
