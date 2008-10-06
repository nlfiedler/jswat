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
 * FILE:        monitorTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: monitorTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the monitor command.
 */
public class monitorTest extends CommandTestCase {

    public monitorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(monitorTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_monitor() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        runCommand(session, "unmonitor all");
        runCommand(session, "monitor foo");
        runCommand(session, "monitor foo bar");
        runCommand(session, "monitor foo bar baz");
        runCommand(session, "monitor foo bar baz fum");
        // test no-arg when monitors are defined
        runCommand(session, "monitor");
        SessionManager.endSession();
    }

    public void test_unmonitor() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "unmonitor undefined");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "unmonitor 1");
        runCommand(session, "unmonitor 2 3 4");
        runCommand(session, "monitor foo");
        runCommand(session, "monitor foo bar");
        runCommand(session, "monitor foo bar baz");
        runCommand(session, "monitor foo bar baz fum");
        runCommand(session, "unmonitor all");
        try {
            runCommand(session, "unmonitor 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        SessionManager.endSession();
    }
}
