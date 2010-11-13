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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the uncaught exception breakpoint implementation.
 *
 * @author Nathan Fiedler
 */
public class UncaughtExceptionBreakpointTest {

    @Test
    public void test_Breakpoint_Uncaught_Exception() {
        //
        // Note that Java 1.5 now appears to have default handlers for
        // uncaught exceptions, as provided by the Thread implementations.
        // Not sure how long this unit test will continue working, and on
        // which platforms will it fail.
        //
        Session session = SessionHelper.getSession();
        // Help along the breakpoint manager by forcing it to initialize.
        // Normally some UI code would have done this for us.
        BreakpointProvider.getBreakpointManager(session);
        SessionHelper.launchDebuggee(session, "UncaughtExceptionTestCode");

        // Resume in order to hit the breakpoint.
        SessionHelper.resumeAndWait(session);
        // We are supposedly at a breakpoint, verify that this is so.
        Location loc = BreakpointHelper.getLocation(session);
        assertNotNull(loc);
        assertEquals("badcode", loc.method().name());

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
    }
}
