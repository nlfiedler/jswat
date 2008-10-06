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
 * FILE:        catchTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: catchTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the catch command.
 */
public class catchTest extends CommandTestCase {

    public catchTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(catchTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_catch() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "catch 12345");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "catch blah,duh!");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "catch go");
            fail("expected CommandException");
        } catch (MissingArgumentsException ce) {
            // expected
        }
        try {
            runCommand(session, "catch thread");
            fail("expected CommandException");
        } catch (MissingArgumentsException ce) {
            // expected
        }
        runCommand(session, "catch clazz");
        runCommand(session, "catch go clazz");
        runCommand(session, "catch thread clazz");
        runCommand(session, "clear all");
        SessionManager.endSession();
    }
}
