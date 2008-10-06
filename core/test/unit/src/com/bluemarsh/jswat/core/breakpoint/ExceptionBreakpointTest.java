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
 * $Id: ExceptionBreakpointTest.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ExceptionBreakpointTest extends TestCase {

    public ExceptionBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ExceptionBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Exception() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        String[] exceptions = new String[] {
            "java.lang.IllegalArgumentException",
            "java.lang.NullPointerException",
            "java.lang.IndexOutOfBoundsException"
        };
        try {
            for (String exception : exceptions) {
                Breakpoint bp = bf.createExceptionBreakpoint(exception, true, true);
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        }

        SessionHelper.launchDebuggee(session, "ExceptionBreakpointTestCode");

        String[] methods = new String[] {
            "throwIllArg",
            "throwNullPt",
            "throwIndexBounds"
        };
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
