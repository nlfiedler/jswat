/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: setTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the set command.
 */
public class setTest extends CommandTestCase {

    public setTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(setTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_set() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");

        // inactive case tested elsewhere
        // missing arguments case tested elsewhere
        // no current thread case tested elsewhere

        runCommand(session, "clear all");
        runCommand(session, "runto locals:100");
        waitForSuspend(ssl);

        try {
            // fails because missing arguments
            runCommand(session, "set var =");
            fail("expected MissingArgumentsException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // invalid expression
            runCommand(session, "set counter = 1 + 2 *");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // invalid type
            runCommand(session, "set c = true");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

// TODO: assignment operator needs to handle down-casting
//        runCommand(session, "set staticCounter = 1 + 2 * 3");
        runCommand(session, "set fieldBoolean = true");
        runCommand(session, "set staticBoolean = false");
        for (int ii = 1; ii < 11; ii++) {
            runCommand(session, "set counter = " + ii);
        }
        runCommand(session, "set c = 'd'");
        for (int ii = 1; ii < 11; ii++) {
            runCommand(session, "set s = \"" + ii + "\"");
        }
        for (int ii = 1; ii < 11; ii++) {
            runCommand(session, "set counter = invoke2(" + ii + ")");
        }
        runCommand(session, "set aString = invoke1(\"abc\", 'd', 123, true)");

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
