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
 * $Id: MethodBreakpointTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.sun.jdi.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MethodBreakpointTest extends TestCase {

    public MethodBreakpointTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(MethodBreakpointTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Breakpoint_Method() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a class that has no package name.
        //
        String[] classes = new String[] {
            "MethodBreakpointTestCode",
            "MethodBreakpointTestCode$Inner",
            "MBSecond",
            "MethodBreakpointTestCode",
            "MethodBreakpointTestCode",
        };
        String[] methods = new String[] {
            "method_MBTC",
            "method_I",
            "method_MBS",
            "method_params",
            "method_params",
        };
        List<String> empty = Collections.emptyList();
        List<List<String>> arguments = new ArrayList<List<String>>();
        arguments.add(empty);
        arguments.add(empty);
        arguments.add(empty);
        List<String> params = new ArrayList<String>();
        params.add("String");
        params.add("int");
        params.add("boolean");
        arguments.add(params);
        params = new ArrayList<String>();
        params.add("char");
        params.add("double");
        arguments.add(params);
        try {
            for (int ii = 0; ii < methods.length; ii++) {
                Breakpoint bp = bf.createMethodBreakpoint(
                        classes[ii], methods[ii], arguments.get(ii));
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        }

        SessionHelper.launchDebuggee(session, "MethodBreakpointTestCode");

        for (String method : methods) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for method " + method, loc);
            assertEquals(method, loc.method().name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.

        //
        // Now test using a class inside a package.
        //
        classes = new String[] {
            "jswat.test.MethodBreakpointTestCode",
            "jswat.test.MethodBreakpointTestCode$Inner",
            "jswat.test.MBSecond",
            "jswat.test.MethodBreakpointTestCode",
            "jswat.test.MethodBreakpointTestCode",
        };
        try {
            for (int ii = 0; ii < methods.length; ii++) {
                Breakpoint bp = bf.createMethodBreakpoint(
                        classes[ii], methods[ii], arguments.get(ii));
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        }

        SessionHelper.launchDebuggee(session, "jswat.test.MethodBreakpointTestCode");

        for (String method : methods) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for method " + method, loc);
            assertEquals(method, loc.method().name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
    }
}
