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
 * FILE:        SessionTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/23/02        Initial version
 *
 * $Id: SessionTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the Session class.
 */
public class SessionTest extends TestCase {

    public SessionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(SessionTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testSessionProps() {
        // Test the Session properties support.
        Session session = SessionManager.beginSession();
        session.setProperty("SessionTestKey", "SessionTestValue");
        assertEquals("SessionTestValue",
                     session.getProperty("SessionTestKey"));
        String[] keys = session.getPropertyKeys();
        boolean found = false;
        for (int ii = 0; ii < keys.length; ii++) {
            if (keys[ii].equals("SessionTestKey")) {
                found = true;
                break;
            }
        }
        assertTrue("stored property not found", found);
        session.setProperty("SessionTestKey", null);
        assertNull(session.getProperty("SessionTestKey"));
        SessionManager.endSession();
    }
}
