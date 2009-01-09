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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SuspendPolicyTest extends TestCase {

    public SuspendPolicyTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SuspendPolicyTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Suspend_Policy() {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();

        //
        // Test with a monitor that requires suspending.
        //
        int line = 33;
        String srcpath = System.getProperty("test.src.dir");
        File srcfile = new File(srcpath, "SuspendPolicyTestCode.java");
        Breakpoint bp = null;
        try {
            String url = srcfile.toURI().toURL().toString();
            bp = bf.createLineBreakpoint(url, null, line);
            BreakpointHelper.prepareBreakpoint(bp, session);
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedURLException mue) {
            fail(mue.toString());
        }
        // Set the suspend policy to none since by default it will
        // suspend the debuggee, and the whole point is to test how
        // it behaves when a particular type of monitor is attached.
        bp.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        SuspendMonitor monitor = new SuspendMonitor(true);
        bp.addMonitor(monitor);

        SessionHelper.launchDebuggee(session, "SuspendPolicyTestCode");

        // Resume in order to hit the breakpoint, which will then resume
        // immediately because of the suspend policy.
        SessionHelper.resumeAndWait(session);
        Location loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        // Verify that the monitor was performed while suspended.
        assertTrue("monitor not performed in suspended state",
                monitor.hasSuspended());

        //
        // Test with a monitor that does not require suspending.
        //
        bp.removeMonitor(monitor);
        monitor = new SuspendMonitor(false);
        bp.addMonitor(monitor);

        SessionHelper.launchDebuggee(session, "SuspendPolicyTestCode");

        // Resume in order to hit the breakpoint, which will then resume
        // immediately because of the suspend policy.
        SessionHelper.resumeAndWait(session);
        loc = BreakpointHelper.getLocation(session);
        assertNull("failed to terminate", loc);
        // Verify that the monitor was performed while _not_ suspended.
        assertFalse("monitor performed in suspended state",
                monitor.hasSuspended());
    }

    /**
     * A monitor that indicates if the debuggee suspended or not.
     */
    private static class SuspendMonitor implements Monitor {
        private boolean suspended;
        private boolean requiresThread;

        /**
         * @param  thread  true to force debuggee to suspend.
         */
        public SuspendMonitor(boolean thread) {
            requiresThread = thread;
        }

        /**
         * @return  true if the monitor suspended the debuggee, as expected.
         */
        public boolean hasSuspended() {
            return suspended;
        }

        public void perform(BreakpointEvent event) {
            Event evt = event.getEvent();
            if (evt instanceof LocatableEvent) {
                LocatableEvent le = (LocatableEvent) evt;
                ThreadReference thread = le.thread();
                if (thread != null) {
                    List stack = null;
                    try {
                        stack = thread.frames();
                        suspended = true;
                        return;
                    } catch (IncompatibleThreadStateException itse) {
                    } catch (ObjectCollectedException oce) {
                    }
                    suspended = false;
                }
            }
        }

        public boolean requiresThread() {
            return requiresThread;
        }
    }
}
