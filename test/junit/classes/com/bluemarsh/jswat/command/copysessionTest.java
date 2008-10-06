/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: copysessionTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import com.bluemarsh.jswat.util.SessionSettings;
import java.util.prefs.BackingStoreException;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the copysession command.
 */
public class copysessionTest extends CommandTestCase {
    
    public copysessionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(copysessionTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_copysession() {
        Session session = SessionManager.beginSession();
        String current = SessionSettings.currentSettings();
        // no-arg case tested elsewhere
        SessionSettings.loadSettings("l_s_unit_test_1");
        session.setProperty("l_s_unit_test", "abc");
        runCommand(session, "copysession l_s_unit_test_2");
        assertNotNull(session.getProperty("l_s_unit_test"));
        SessionSettings.loadSettings("l_s_unit_test_1");
        assertNotNull(session.getProperty("l_s_unit_test"));
        try {
            SessionSettings.deleteSettings("l_s_unit_test_1");
            SessionSettings.deleteSettings("l_s_unit_test_2");
        } catch (BackingStoreException bse) {
            fail(bse.toString());
        }

        // Restore the original testing session.
        SessionSettings.loadSettings(current);
        SessionManager.endSession();
    }
}
