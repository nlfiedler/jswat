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
 * FILE:        stopTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/09/02        Initial version
 *
 * $Id: stopTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the stop command.
 */
public class stopTest extends CommandTestCase {

    public stopTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(stopTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_stop() {
        Session session = SessionManager.beginSession();

        // missing arguments case tested elsewhere

        try {
            runCommand(session, "stop stuff");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof IllegalArgumentException)) {
                fail("expected IllegalArgumentException; got " +
                    ce.getCause());
            }
        }

        try {
            runCommand(session, "stop go stuff");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof IllegalArgumentException)) {
                fail("expected IllegalArgumentException; got " +
                    ce.getCause());
            }
        }

        try {
            runCommand(session, "stop thread stuff");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof IllegalArgumentException)) {
                fail("expected IllegalArgumentException; got " +
                    ce.getCause());
            }
        }

        try {
            runCommand(session, "stop stuff.123");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof MalformedMemberNameException)) {
                fail("expected MalformedMemberNameException; got " +
                    ce.getCause());
            }
        }

        try {
            runCommand(session, "stop stuff:method");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof NumberFormatException)) {
                fail("expected NumberFormatException; got " + ce.getCause());
            }
        }

        try {
            runCommand(session, "stop 123.method()");
            fail("expected CommandException");
        } catch (CommandException ce) {
            if (!(ce.getCause() instanceof ClassNotFoundException)) {
                fail("expected ClassNotFoundException; got " + ce.getCause());
            }
        }

        runCommand(session, "stop class:123");
        runCommand(session, "stop go class:123");
        runCommand(session, "stop thread class:123");
        runCommand(session, "stop class.method()");
        runCommand(session, "stop go class.method()");
        runCommand(session, "stop thread class.method()");
        runCommand(session, "stop class.method(arg1)");
        runCommand(session, "stop class.method(arg1, arg2)");
        runCommand(session, "stop class.method(arg1, arg2, arg3)");
        runCommand(session, "clear all");

        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");
        // Test wild-card-args support
        runCommand(session, "stop locals.tester(*)");
        resumeAndWait(session, ssl);
        // Test constructor breakpoint support
        runCommand(session, "stop locals.<init>(boolean)");
        resumeAndWait(session, ssl);
        // Test line-number-only support
        runCommand(session, "stop 192");
        resumeAndWait(session, ssl);
        SessionManager.deactivate(true);
        session.removeListener(ssl);

        SessionManager.endSession();
    }
}
