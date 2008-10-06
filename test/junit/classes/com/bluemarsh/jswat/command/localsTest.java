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
 * FILE:        localsTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/03/02        Initial version
 *
 * $Id: localsTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the locals command.
 */
public class localsTest extends CommandTestCase {

    public localsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(localsTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_locals() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");
        runCommand(session, "clear all");

        // no-arg case tested elsewhere

        // there's usually a thread called 'main'
        runCommand(session, "thread main");
        try {
            // thread not yet running
            runCommand(session, "locals");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "runto locals:189");
        waitForSuspend(ssl);

        try {
            runCommand(session, "locals a1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            runCommand(session, "locals -1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            runCommand(session, "locals 12345");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "locals");

        SessionManager.deactivate(true);

        // test thread not suspended case
        SessionManager.launchSimple("tutorial");
        runCommand(session, "clear all");

        // stop when main thread dies since we know everything has
        // started up by the time that happens
        runCommand(session, "threadbrk main death");
        resumeAndWait(session, ssl);
        runCommand(session, "clear all");
        runCommand(session, "resume");
        // we assume this thread always exists
        runCommand(session, "thread Finalizer");
        try {
            runCommand(session, "locals");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
