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
import com.sun.jdi.ThreadReference;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the thread breakpoint implementation.
 *
 * @author Nathan Fiedler
 */
public class ThreadBreakpointTest {

    @Test
    public void test_Breakpoint_Thread() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        String[] threads = new String[]{
            "thread1", "thread2", "thread3", "thread4"
        };
        for (String thread : threads) {
            Breakpoint bp = bf.createThreadBreakpoint(thread, true, true);
            BreakpointHelper.prepareBreakpoint(bp, session);
        }

        SessionHelper.launchDebuggee(session, "ThreadBreakpointTestCode");

        for (String thread : threads) {
            // Resume in order to hit the breakpoint.
            SessionHelper.resumeAndWait(session);
            // We are supposedly at a breakpoint, verify that this is so.
            ThreadReference thrd = BreakpointHelper.getThread(session);
            assertNotNull(thrd);
            assertEquals(thread, thrd.name());
        }

        // Resume once more to let the program exit.
        SessionHelper.resumeAndWait(session);
        // The debuggee will have exited now and the session is inactive.
        Location loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        assertFalse("failed to disconnect", session.isConnected());
    }
}
