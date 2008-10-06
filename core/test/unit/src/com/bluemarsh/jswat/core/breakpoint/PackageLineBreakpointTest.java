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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PackageLineBreakpointTest.java 15 2007-06-03 00:01:17Z nfiedler $
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

public class PackageLineBreakpointTest extends TestCase {

    public PackageLineBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(PackageLineBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_PackageLine() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has a package name that does not
        // correspond to the directory structure of the source code.
        //
        int[] lines = new int[] { 30 };
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "PackageLineBreakpointTestCode.java");
        try {
            String url = srcfile.toURI().toURL().toString();
            for (int line : lines) {
                // Must set package name for this breakpoint to resolve,
                // since the source is in a non-package directory structure.
                Breakpoint bp = bf.createLineBreakpoint(url, "org.acme.test", line);
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }

        SessionHelper.launchDebuggee(session,
                "org.acme.test.PackageLineBreakpointTestCode");
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
