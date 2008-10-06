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
 * $Id: InactiveCases.java 14 2007-06-02 23:50:55Z nfiedler $
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
 * succeed.
 */
public class InactiveCases extends CommandTestCase {

    public InactiveCases(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(InactiveCases.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_commands_inactive_simple() {
        Session session = SessionManager.beginSession();

        String[] commands = new String[] {
            "about",
            "alias",
            "brkinfo",
            "capture",
            "classpath",
            "copysession",
            "exclude",
            "help help",
            "history",
            "loadsession",
            "logging",
            "monitor",
            "options",
            "props",
            "rmsession",
            "sourcepath",
            "stderr ignore this",
            "stderr",
            "stdout ignore this",
            "stdout",
            "version"
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommand(session, commands[ii]);
            } catch (CommandException ce) {
                fail(commands[ii] + ": " + ce.getMessage());
            } catch (Exception e) {
                fail(commands[ii] + ": " + e.toString());
            }
        }

        SessionManager.endSession();
    }

    public void test_commands_inactive_multi() {
        Session session = SessionManager.beginSession();

        // these tests merely ensure the command parser doesn't blow up
        // the 'dummy' command is defined in this package as doing nothing
        String[] commands = new String[] {
            "",
            ";;;",
            "dummy;dummy;dummy",
            "dummy ; dummy ; dummy",
            "dummy \"string\"",
            "dummy \\\"string\\\"",
            "dummy \\\\\"string\\\\\"",
            "dummy \\\\ \\ blah",
            "dummy 'string'",
            "dummy \'string\'",
            "dummy \\\'string\\\'",
            "dummy \";\" ; dummy \"a\" ; dummy \"\\\"\"",
            "dummy \"a\" ; dummy \"b\" ; dummy \"\\\"c\"",
            "dummy \"a;b\" ; dummy 'c\"' ; dummy '\\'d'",
            "dummy \"a;b\""
        };

        for (int ii = 0; ii < commands.length; ii++) {
            try {
                runCommandMgr(session, commands[ii]);
            } catch (CommandException ce) {
                fail(commands[ii] + ": " + ce.getMessage());
            } catch (Exception e) {
                fail(commands[ii] + ": " + e.toString());
            }
        }

        SessionManager.endSession();
    }
}
