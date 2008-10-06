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
 * FILE:        ActiveFailCases.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: ActiveFailCases.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests performed while the Session is active which are expected to
 * fail.
 */
public class ActiveFailCases extends CommandTestCase {

    public ActiveFailCases(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(ActiveFailCases.class), true);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testNotAllowedActive() {
        Session session = SessionManager.beginSession();

        String[] commands = new String[] {
            "classpath not_while_active"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
                fail("expected CommandException for " + commands[ii]);
            } catch (CommandException ce) {
                // expected
            }
            // any other exception is _not_ expected
        }

        SessionManager.endSession();
    }

    public void testFailedMatch() {
        Session session = SessionManager.beginSession();

        String[] commands = new String[] {
            "classes not_defined"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
                fail("expected CommandException for " + commands[ii]);
            } catch (CommandException ce) {
                // expected
            }
            // any other exception is _not_ expected
        }

        SessionManager.endSession();
    }

    public void testActiveMissingArgs() {
        Session session = SessionManager.beginSession();
        // there's usually a 'main' thread
        runCommand(session, "thread main");

        String[] commands = new String[] {
            "class",
            "disablegc",
            "dump",
            "elements",
            "enablegc",
            "fields",
            "frame",
            "hotswap",
            "interrupt",
            "invoke",
            "lines",
            "locks",
            "methods",
            "runto",
            "set"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
                fail("expected MissingArgumentsException for " + commands[ii]);
            } catch (MissingArgumentsException ce) {
                // expected
            }
            // any other exception is _not_ expected
        }

        SessionManager.endSession();
    }

    public void testActiveNoThread() {
        Session session = SessionManager.beginSession();
        // clear the current thread setting
        ContextManager contextManager = (ContextManager)
            session.getManager(ContextManager.class);
        contextManager.setCurrentThread(null);

        // these check for debuggee state before checking for arguments
        String[] commands = new String[] {
            "bytecodes",
            "disablegc",
            "down",
            "dump",
            "elements",
            "enablegc",
            "frame",
            "invoke",
            "list",
            "locals",
            "locks",
            "next", "nexti",
            "set variable = value",
            "step", "stepi",
            "thread",
            "threadlocks",
            "up",
            "where"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
                fail("expected CommandException for " + commands[ii]);
            } catch (CommandException ce) {
                // expected
            }
            // any other exception is _not_ expected
        }

        SessionManager.endSession();
    }

    // technically there is no difference between this case and
    // the no thread case, but it is a possible point of failure
    public void testActiveNoLocation() {
        Session session = SessionManager.beginSession();
        // clear the current location setting
        ContextManager contextManager = (ContextManager)
            session.getManager(ContextManager.class);
        contextManager.setCurrentLocation(null, false);

        String[] commands = new String[] {
            "bytecodes",
            "list",
            "next", "nexti",
            "step", "stepi"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
                fail("expected CommandException for " + commands[ii]);
            } catch (CommandException ce) {
                // expected
            }
            // any other exception is _not_ expected
        }

        SessionManager.endSession();
    }
}
