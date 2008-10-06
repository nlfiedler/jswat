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
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: JvmConnectionTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the JvmConnection class.
 */
public class JvmConnectionTest extends TestCase {

    public JvmConnectionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JvmConnectionTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    private static JavaRuntime getDefault() {
        // Find the default runtime instance.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        String base = rf.getDefaultBase();
        return rm.findByBase(base);
    }

    public void test_JvmConnection_buildConnection() {
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        try {
            factory.createLaunching(null, null, "blah blah blah");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // This is expected.
        }

        JavaRuntime rt = getDefault();
        JvmConnection conn = factory.createLaunching(rt, null, "blah blah blah");
        assertNotNull(conn);
    }

    public void test_JvmConnection_Launching() {
        // Test a bunch of methods at once to save time.

        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        JavaRuntime rt = getDefault();
        JvmConnection conn = factory.createLaunching(rt, null, "someclass arg1 arg2 arg3");
        assertTrue(!conn.isRemote());
        assertNull(conn.getVM());
        assertTrue(!conn.equals(null));
        assertTrue(!conn.equals(""));
        assertTrue(conn.equals(conn));
        JvmConnection conn2 = factory.createLaunching(rt, null, "blah blah blah");
        assertTrue(!conn.equals(conn2));

        try {
            conn.connect();
        } catch (Exception e) {
            fail("debuggee failed to launch");
        }
        assertNotNull(conn.getVM());
        Session session = SessionHelper.getSession();
        session.connect(conn);
        session.disconnect(true);
    }

    public void test_JvmConnection_Running() {
        JavaRuntime rt = getDefault();
        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        JvmConnection conn = factory.createLaunching(rt, null, "someclass");
        try {
            conn.connect();
        } catch (Exception e) {
            fail("debuggee failed to launch");
        }
        Session session = SessionHelper.getSession();
        session.connect(conn);
        session.resumeVM();
        session.disconnect(true);
    }
}
