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
 * FILE:        excludeTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: excludeTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the exclude command.
 */
public class excludeTest extends CommandTestCase {

    public excludeTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(excludeTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_exclude() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "exclude -");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "exclude +");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "exclude ..");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "exclude *..Foo");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "exclude *.Foo");
        runCommand(session, "exclude bar.*");
        // multiples should be ignored
        runCommand(session, "exclude bar.*");
        runCommand(session, "exclude bar.*");
        runCommand(session, "exclude bar.lang.String");
        runCommand(session, "exclude - *.Foo");
        runCommand(session, "exclude - bar.*");
        runCommand(session, "exclude - bar.lang.String");
        try {
            runCommand(session, "exclude - *.Bar");
        } catch (CommandException ce) {
            // expected
        }
        SessionManager.endSession();
    }
}
