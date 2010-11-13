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
package com.bluemarsh.jswat.core.path;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointHelper;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the PathManager class.
 */
public class PathManagerTest {

    @Test
    public void test_PathManger() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test finding source for a non-public class.
        //
        String[] classes = new String[]{
            "PathManagerTestCode",
            "PathManagerTestCode$Inner",
            "PathManagerTestCode$1",
            "PMSecond"
        };
        String[] methods = new String[]{
            "method1",
            "method_I",
            "run",
            "method_PMS"
        };
        List<String> empty = Collections.emptyList();
        try {
            for (int ii = 0; ii < methods.length; ii++) {
                Breakpoint bp = bf.createMethodBreakpoint(
                        classes[ii], methods[ii], empty);
                BreakpointHelper.prepareBreakpoint(bp, session);
            }
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        }

        SessionHelper.launchDebuggee(session, "PathManagerTestCode");

        PathManager pm = PathProvider.getPathManager(session);
        String spath = SessionHelper.getTestSourcepath();
        List<String> roots = new ArrayList<String>(1);
        roots.add(spath);
        pm.setSourcePath(roots);
        roots = pm.getSourcePath();
        assertNotNull("source path not defined!", roots);
        for (int ii = 0; ii < 4; ii++) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            Location loc = BreakpointHelper.getLocation(session);
            assertNotNull("missed hitting breakpoint", loc);
            try {
                // Assert our general location.
                assertEquals("PathManagerTestCode.java", loc.sourceName());
                PathEntry pe = pm.findSource(loc);
                assertNotNull("source for location not found", pe);
                assertEquals("PathManagerTestCode.java", pe.getName());
                pe = pm.findSource(loc.declaringType());
                assertNotNull("source for class not found", pe);
                assertEquals("PathManagerTestCode.java", pe.getName());
                pe = pm.findSource(loc.declaringType().name());
                assertNotNull("source for name not found", pe);
                // Finding source by name for a class that is not the
                // public class in the source file doesn't work if we
                // cannot read the byte code, so expect one of two
                // possible values for this unit test.
                if ((!pe.getName().equals("PathManagerTestCode.java"))
                        && !pe.getName().equals("PMSecond.class")) {
                    fail("source by name faild");
                }
                pe = pm.findByteCode(loc.declaringType());
                assertNotNull("byte code for class not found", pe);
                String cname = String.format("%s.class", classes[ii]);
                assertEquals(cname, pe.getName());
            } catch (AbsentInformationException aie) {
                fail(aie.toString());
            }
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
    }
}
