/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * FILE:        SessionSettingsTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/08/03        Initial version
 *
 * $Id: SessionSettingsTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.prefs.BackingStoreException;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the SessionSettings class.
 */
public class SessionSettingsTest extends TestCase {

    public SessionSettingsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionSettingsTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testSessionCopies() {
        // Test the support for multiple session properties.
        try {
            String name1 = "SessionTestSession1";
            SessionSettings.copySettings(name1);
            assertEquals(name1, SessionSettings.currentSettings());
            String[] keys = SessionSettings.getAvailableSettings();
            boolean found = false;
            for (int ii = 0; ii < keys.length; ii++) {
                if (keys[ii].equals(name1)) {
                    found = true;
                    break;
                }
            }
            assertTrue("stored session not found", found);
            SessionSettings.deleteSettings(name1);
            assertTrue(!name1.equals(SessionSettings.currentSettings()));

            String name2 = "SessionTestSession2";
            SessionSettings.loadSettings(name2);
            assertEquals(name2, SessionSettings.currentSettings());
            SessionSettings.deleteSettings(name2);
        } catch (BackingStoreException bse) {
            fail(bse.toString());
        }
    }
}
