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
 * $Id: loadsessionTest.java 14 2007-06-02 23:50:55Z nfiedler $
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
 * Tests the loadsession command.
 */
public class loadsessionTest extends CommandTestCase {
    
    public loadsessionTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(loadsessionTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_loadsession() {
        Session session = SessionManager.beginSession();
        String current = SessionSettings.currentSettings();
        // no-arg case tested elsewhere
        runCommand(session, "loadsession l_s_unit_test_1");
        session.setProperty("l_s_unit_test", "abc");
        runCommand(session, "loadsession l_s_unit_test_2");
        assertNull(session.getProperty("l_s_unit_test"));
        runCommand(session, "loadsession l_s_unit_test_1");
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
