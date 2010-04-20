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
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Stepper class.
 *
 * @author Nathan Fiedler
 */
public class StepperTest {

    @BeforeClass
    public static void setupClass() {
        // Set the excludes to avoid going places we don't want to go.
        CoreSettings cs = CoreSettings.getDefault();
        List<String> excludes = new ArrayList<String>();
        excludes.add("com.sun.*");
        excludes.add("sun.*");
        excludes.add("java.*");
        excludes.add("javax.*");
        cs.setSteppingExcludes(excludes);
    }

    @Test
    public void singleStepping() throws SteppingException {
        Session session = SessionHelper.getSession();
        SessionHelper.launchDebuggee("SteppingTestCode",
                "SteppingTestCode:main(java.lang.String[])");
        String[] methods = new String[]{
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
            "main",};
        for (int ii = 0; ii < methods.length; ii++) {
            String method = methods[ii];
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("no location for entry " + ii, loc);
            assertEquals("entry " + ii, method, loc.method().name());
            SessionHelper.stepIntoAndWait(session);
        }

        // The debuggee will have exited now and the session is inactive.
    }

    @Test
    public void steppingOut() throws SteppingException {
        SessionHelper.launchDebuggee("SteppingTestCode",
                "SteppingTestCode$Inner:method_I()");
        Session session = SessionHelper.getSession();
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull(loc);
        assertEquals("method_I", loc.method().name());
        SessionHelper.stepOutAndWait(session);
        loc = BreakpointHelper.getLocation(session);
        assertNotNull(loc);
        assertEquals("main", loc.method().name());
        SessionHelper.resumeAndWait();
        // The debuggee will have exited now and the session is inactive.
    }
}
