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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClearedBreakpointTest.java 6 2007-05-16 07:14:24Z nfiedler $
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

public class ClearedBreakpointTest extends TestCase {

    public ClearedBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ClearedBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Removal() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);

        //
        // Test removing a breakpoint once it has been hit to ensure the
        // event requests are correctly removed.
        //
        int line = 33;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "ClearedBreakpointTestCode.java");
        Breakpoint bp = null;
        try {
            String url = srcfile.toURI().toURL().toString();
            bp = bf.createLineBreakpoint(url, null, line);
            // Do not use prepareBreakpoint because it sets the breakpoint
            // to expire, and delete itself on expiration, which would foil
            // our test.
            //BreakpointHelper.prepareBreakpoint(bp, session);
            bm.addBreakpoint(bp);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session, "ClearedBreakpointTestCode");
        assertTrue(session.isConnected());

        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        assertTrue(session.isConnected());
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull("failed to stop at line " + line, loc);
        assertEquals(line, loc.lineNumber());
        // Remove the breakpoint, which means we should no longer stop.
        bm.removeBreakpoint(bp);

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        if (session.isConnected()) {
            loc = BreakpointHelper.getLocation(session);
            fail("Still stopping at line " + loc.lineNumber());
        }
    }
}
