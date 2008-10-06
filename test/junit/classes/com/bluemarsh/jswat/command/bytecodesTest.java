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
 * FILE:        bytecodesTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/29/02        Initial version
 *
 * $Id: bytecodesTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the bytecodes command.
 */
public class bytecodesTest extends CommandTestCase {

    public bytecodesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(bytecodesTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_bytecodes() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");
        runCommand(session, "clear all");

        // no-arg case tested elsewhere

        runCommand(session, "stop locals.main(java.lang.String[])");
        resumeAndWait(session, ssl);

        try {
            runCommand(session, "bytecodes not_defined");
            fail("expected MissingArgumentsException");
        } catch (MissingArgumentsException mae) {
            // expected
	} catch (Exception e) {
	    // jclasslib may fail for any of a number of reasons
	    System.out.println("bytecodes command: " + e);
        }

        try {
            runCommand(session, "bytecodes not_defined main");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
	} catch (Exception e) {
	    // jclasslib may fail for any of a number of reasons
	    System.out.println("bytecodes command: " + e);
        }

        try {
            runCommand(session, "bytecodes locals no_method");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
	} catch (Exception e) {
	    // jclasslib may fail for any of a number of reasons
	    System.out.println("bytecodes command: " + e);
        }

	try {
	    runCommand(session, "bytecodes");
	    runCommand(session, "bytecodes locals main");
	} catch (Exception e) {
	    // jclasslib may fail for any of a number of reasons
	    System.out.println("bytecodes command: " + e);
	}

        runCommand(session, "clear all");
        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
