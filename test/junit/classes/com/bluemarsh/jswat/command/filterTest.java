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
 * FILE:        filterTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: filterTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the filter command.
 */
public class filterTest extends CommandTestCase {

    public filterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(filterTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_filter() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "filter add a1 thread 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "filter add -1 thread 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        // clear all breakpoints so number is reset to zero
        runCommand(session, "clear all");
        try {
            runCommand(session, "filter add 1 thread 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "stop clazz:123");
        // first new breakpoint will be numbered 1
        try {
            runCommand(session, "filter bad 1 thread 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "filter add 1 bad 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "filter add 1 thread 1");
        try {
            runCommand(session, "filter del 1 baddie 1");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        try {
            runCommand(session, "filter del 1 class clazz");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "filter del 1 thread 1");
        runCommand(session, "clear all");
        SessionManager.endSession();
    }
}
