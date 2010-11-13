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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.connect;

import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests the JvmConnection class.
 */
public class JvmConnectionTest {

    private static JavaRuntime getDefault() {
        // Find the default runtime instance.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        String base = rf.getDefaultBase();
        return rm.findByBase(base);
    }

    @Test
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

    @Test
    public void test_JvmConnection_Launching() {
        // Test a bunch of methods at once to save time.

        ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
        JavaRuntime rt = getDefault();
        JvmConnection conn = factory.createLaunching(rt, null, "someclass arg1 arg2 arg3");
        assertFalse(conn.isRemote());
        assertNull(conn.getVM());
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

    @Test
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
