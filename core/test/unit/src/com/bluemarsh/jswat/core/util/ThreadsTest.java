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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointHelper;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Threads class.
 *
 * @author Nathan Fiedler
 */
public class ThreadsTest {

    public ThreadsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Session session = SessionHelper.getSession();
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        Breakpoint bp = bf.createThreadBreakpoint("thread1", true, true);
        BreakpointHelper.prepareBreakpoint(bp, session);
        SessionHelper.launchDebuggee(session, "ThreadBreakpointTestCode");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        SessionHelper.resumeAndWait();
    }

    @Test
    public void testFindThread() {
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        // The thread1 thread does not actually exist yet, even though
        // we are stopped at the point at which it is starting.
        ThreadReference result = Threads.findThread(vm, "main");
        assertEquals("main", result.name());
        result = Threads.findThread(vm, "1");
        assertEquals("main", result.name());
    }

    @Test
    public void testGetIdentifier() {
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        ThreadReference thread = Threads.findThread(vm, "main");
        String result = Threads.getIdentifier(thread);
        assertEquals("main", result);
    }

    @Test
    public void testGetThreadPool() {
        ExecutorService result = Threads.getThreadPool();
        assertNotNull(result);
        assertFalse(result.isShutdown());
        assertFalse(result.isTerminated());
    }

    @Test
    public void testIterateGroups() {
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        List<ThreadGroupReference> groups = vm.topLevelThreadGroups();
        Iterator result = Threads.iterateGroups(groups);
        assertNotNull(result);
        assertTrue(result.hasNext());
        assertNotNull(result.next());
        // For code coverage...
        while (result.hasNext()) {
            assertNotNull(result.next());
        }
    }

    @Test
    public void testThreadStatus() {
        Session session = SessionHelper.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        {
            ThreadReference thread = Threads.findThread(vm, "main");
            String result = Threads.threadStatus(thread);
            assertEquals("Running", result);
        }
        {
            ThreadReference thread = Threads.findThread(vm, "Finalizer");
            String result = Threads.threadStatus(thread);
            assertEquals("Waiting", result);
        }
    }
}
