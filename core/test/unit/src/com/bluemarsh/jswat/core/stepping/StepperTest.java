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
 * $Id$
 */

package com.bluemarsh.jswat.core.stepping;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointHelper;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.sun.jdi.Location;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StepperTest extends TestCase {

    public StepperTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(StepperTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Stepper() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        List<String> args = new ArrayList<String>(1);
        args.add("java.lang.String[]");
        try {
            Breakpoint bp = bf.createMethodBreakpoint(
                    "SteppingTestCode", "main", args);
            BreakpointHelper.prepareBreakpoint(bp, session);
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        }

        // Set the excludes to avoid going places we don't want to go.
        CoreSettings cs = CoreSettings.getDefault();
        List<String> excludes = new ArrayList<String>();
        excludes.add("com.sun.*");
        excludes.add("sun.*");
        excludes.add("java.*");
        excludes.add("javax.*");
        cs.setSteppingExcludes(excludes);
        // Don't worry about restoring the values, since using get/set is
        // annoying due to stupid unchecked generics, and it is not worth
        // the trouble since we are the only ones doing single-stepping.

        SessionHelper.launchDebuggee(session, "SteppingTestCode");
        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);

        String[] methods = new String[] {
            "main",
            "<init>",
            "main",
            "main",
            "method_I",
            "method_I",
            "main",
            "method_STS",
            "method_STS",
            "main",
            "<init>",
            "<init>",
            "main",
            "main",
            "stepMethod",
            "stepMethod",
            "main",
        };
        for (int ii = 0; ii < methods.length; ii++) {
            String method = methods[ii];
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for entry " + ii, loc);
            assertEquals("entry " + ii, method, loc.method().name());
            try {
                SessionHelper.stepIntoAndWait(session);
            } catch (SteppingException se) {
                fail(se.toString());
            }
        }

        // The debuggee will have exited now and the session is inactive.
    }
}
