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
 * FILE:        aliasTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/26/02        Initial version
 *
 * $Id: aliasTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the alias command.
 */
public class aliasTest extends CommandTestCase {

    public aliasTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(aliasTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_alias() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "alias not_there");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "alias aliasTest some stuff");
        runCommand(session, "alias aliasTest");
        // leave alias defined for now
        SessionManager.endSession();
    }

    public void test_unalias() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "unalias not_there");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        // remove the alias we defined earlier
        runCommand(session, "unalias aliasTest");
        SessionManager.endSession();
    }
}
