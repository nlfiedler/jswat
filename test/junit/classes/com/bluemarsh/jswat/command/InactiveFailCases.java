/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: InactiveFailCases.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests performed while the Session is inactive which are expected to
 * fail.
 */
public class InactiveFailCases extends CommandTestCase {

    public InactiveFailCases(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(InactiveFailCases.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_commands_inactive_fail() {
        Session session = SessionManager.beginSession();

        // these check for debuggee state before checking for arguments
        String[] commands = new String[] {
            "bytecodes",
            "capture + -",
            "class",
            "classes",
            "close",
            "disablegc",
            "down",
            "dump",
            "elements",
            "enablegc",
            "fields",
            "frame",
            "hotswap",
            "interrupt",
            "invoke",
            "kill",
            "lines",
            "list",
            "locals",
            "locks",
            "logging + -",
            "methods",
            "next", "nexti",
            "resume",
            "runto",
            "set",
            "stdin",
            "step", "stepi",
            "step out",
            "support",
            "suspend",
            "thread",
            "threadgroups",
            "threadlocks",
            "threads",
            "up",
            "vminfo",
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

    public void test_commands_inactive_missing_args() {
        Session session = SessionManager.beginSession();

        String[] commands = new String[] {
            "apropos",
            "brkmon",
            "capture file",
            "catch",
            "classbrk",
            "clear",
            "condition",
            "disable",
            "enable",
            "filter",
            "print",
            "props arg", // requires zero or two args
            "options arg", // requires zero or two args
            "read",
            "stop",
            "trace",
            "unalias",
            "unmonitor",
            "view",
            "watch"
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
}
