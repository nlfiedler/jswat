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
 * $Id: printTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the print command.
 */
public class printTest extends CommandTestCase {

    public printTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(printTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_print() {
        Session session = SessionManager.beginSession();

        try {
            // fails because session is inactive
            runCommand(session, "print invalid");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "print 1 + 2 * 3");

        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");

        try {
            // fails because there's no thread/stack
            runCommand(session, "print invalid");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "clear all");
        runCommand(session, "runto locals:190");
        waitForSuspend(ssl);

// TODO: presently this prints "invalid" as long as there is a context
//        try {
//            // fails because 'invalid' is not known
//            runCommand(session, "print invalid");
//            fail("expected CommandException");
//        } catch (CommandException ce) {
//            // expected
//        }

        try {
            // missing second operand for *
            runCommand(session, "print 1 + 2 *");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "print 1 + 2 * 3");
        runCommand(session, "print j");
        runCommand(session, "print i");
        runCommand(session, "print \"literal\"");
        runCommand(session, "print fieldBoolean");
        runCommand(session, "print staticBoolean");
        runCommand(session, "print staticCounter");
        runCommand(session, "print counter");
        runCommand(session, "print aString");
// TODO: evaluator does not like null references
//        runCommand(session, "print myClass");
        // print can invoke, too
        runCommand(session, "print invoke2(12)");
        runCommand(session, "print invoke1(\"abc\", 'd', 123, true)");

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
