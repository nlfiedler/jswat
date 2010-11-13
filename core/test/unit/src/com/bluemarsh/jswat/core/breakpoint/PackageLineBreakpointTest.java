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
 * are Copyright (C) 2006-2010. All Rights Reserved.
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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the PackageLineBreakpoint class.
 *
 * @author Nathan Fiedler
 */
public class PackageLineBreakpointTest {

    @Test
    public void test_Breakpoint_PackageLine() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has a package name that does not
        // correspond to the directory structure of the source code.
        //
        int[] lines = new int[] { 36 };
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
