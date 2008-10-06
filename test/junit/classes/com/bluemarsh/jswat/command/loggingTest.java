/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: loggingTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the logging command.
 */
public class loggingTest extends CommandTestCase {

    public loggingTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(loggingTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_logging() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        // + and - alone case tested elsewhere
        try {
            runCommand(session, "logging not_there");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        runCommand(session, "logging +breakpoint -event");
        runCommand(session, "logging -breakpoint +event");
        runCommand(session, "logging breakpoint event");
        runCommand(session, "logging breakpoint");
        SessionManager.endSession();
    }
}
