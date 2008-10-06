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
 * $Id: elementsTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the elements command.
 */
public class elementsTest extends CommandTestCase {

    public elementsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(elementsTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_elements() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");
        runCommand(session, "clear all");

        // no-arg case tested elsewhere
        // inactive case tested elsewhere
        // no thread case tested elsewhere

        runCommand(session, "runto locals:45");
        waitForSuspend(ssl);

        try {
            runCommand(session, "elements not_defined");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // field not an object
            runCommand(session, "elements counter");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // field not an object
            runCommand(session, "elements counter.field");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // object not a collection
            runCommand(session, "elements aString");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            runCommand(session, "elements 10");
            fail("expected CommandException");
        } catch (MissingArgumentsException mae) {
            // expected
        }

        try {
            runCommand(session, "elements 10 20");
            fail("expected CommandException");
        } catch (MissingArgumentsException mae) {
            // expected
        }

        runCommand(session, "elements myList");
        runCommand(session, "elements 10 myList");
        runCommand(session, "elements 10 20 myList");

        runCommand(session, "runto locals:58");
        waitForSuspend(ssl);
        runCommand(session, "elements myMap");
        try {
            runCommand(session, "elements 10 myMap");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "elements 10 20 myMap");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

// TODO: currently IdentifierNode does not seem to like null variables
//        runCommand(session, "runto locals:74");
//        waitForSuspend(ssl);
//        // arr is null, should be handled okay
//        runCommand(session, "elements arr");

        runCommand(session, "runto locals:81");
        waitForSuspend(ssl);
        runCommand(session, "elements arr");

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
