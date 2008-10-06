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
 * FILE:        listTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/01/02        Initial version
 *
 * $Id: listTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the list command.
 */
public class listTest extends CommandTestCase {

    public listTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(listTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_list() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);

        // no-arg case tested elsewhere

        // inactive session tested elsewhere

        // no current location tested elsewhere

        SessionManager.launchSimple("locals");

        runCommand(session, "clear all");
        runCommand(session, "stop locals.main(java.lang.String[])");
        resumeAndWait(session, ssl);

        // missing or invalid 'count' argument
        try {
            runCommand(session, "list count");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "list count center");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "list count -1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        // invalid line number
        try {
            runCommand(session, "list -1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "list abc");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        // mix the order of arguments
        try {
            runCommand(session, "list count 5 10 center");
            runCommand(session, "list count 5 center 10");
            runCommand(session, "list center count 5 10");
        } catch (CommandException ce) {
            // this happens if the UI adapter doesn't support showFile()
            System.out.println("list unable to show source");
        }

        runCommand(session, "clear all");
        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
