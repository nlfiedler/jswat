/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Unit Tests
 * FILE:        VMConnectionTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/25/02        Initial version
 *
 * $Id: VMConnectionTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the VMConnection class.
 */
public class VMConnectionTest extends TestCase {
    private Session session;

    public VMConnectionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(VMConnectionTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected void setUp() {
        session = SessionManager.beginSession();
    }

    protected void tearDown() {
        SessionManager.endSession();
    }

    public void testVMCBuildConnection() {
        try {
            VMConnection.buildConnection("/place/that/does/not/exist",
                                         null, null, "blah blah blah");
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // This is expected.
        }

        VMConnection conn = VMConnection.buildConnection(
            null, null, null, "blah blah blah");
        assertNotNull(conn);
    }

    public void testVMCLaunching() {
        // Test a bunch of methods at once to save time.

        // REQUIRES: 'locals' class in the classpath
        VMConnection conn = VMConnection.buildConnection(
            null, null, null, "locals arg1 arg2 arg3");
        assertTrue(!conn.isRemote());
        assertEquals("locals", conn.getMainClass());
        assertNull(conn.getVM());
        assertNotNull(conn.getConnector());
        assertNotNull(conn.getConnectArgs());

        assertTrue(!conn.equals(null));
        assertTrue(!conn.equals(""));
        assertTrue(conn.equals(conn));
        VMConnection conn2 = VMConnection.buildConnection(
            null, null, null, "blah blah blah");
        assertTrue(!conn.equals(conn2));

        boolean okay = conn.launchDebuggee(session, false);
        assertTrue("debuggee failed to launch", okay);
        assertNotNull(conn.getVM());

        session.deactivate(true, this);
    }

    public void testVMCRunning() {
        // REQUIRES: 'locals' class in the classpath
        VMConnection conn = VMConnection.buildConnection(
            null, null, null, "locals");
        boolean okay = conn.launchDebuggee(session, false);
        assertTrue("debuggee failed to launch", okay);
        session.resumeVM(this, false, true);

        session.deactivate(true, this);
    }
}
