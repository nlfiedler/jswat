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
 * FILE:        threadlocksTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/06/02        Initial version
 *
 * $Id: threadlocksTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the threadlocks command.
 */
public class threadlocksTest extends CommandTestCase {

    public threadlocksTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(threadlocksTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_threadlocks() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("locals");

        // inactive case tested elsewhere
        // no thread case tested elsewhere

        try {
            runCommand(session, "threadlocks invalid");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "clear all");
        runCommand(session, "runto locals:189");
        waitForSuspend(ssl);

        boolean hotspot = false;
        try {
            runCommand(session, "threadlocks");
            runCommand(session, "threadlocks main");
            runCommand(session, "threadlocks all");
        } catch (CommandException ce) {
            // hotspot vm does not support that operation
            if (!(ce.getCause() instanceof UnsupportedOperationException)) {
                fail("got an unexpected exception: " + ce.getCause());
            }
            hotspot = true;
        }

        if (!hotspot) {
            // this thread is typically waiting
            runCommand(session, "resume Finalizer");
            try {
                // thread is not suspended
                runCommand(session, "threadlocks Finalizer");
                fail("expected CommandException");
            } catch (CommandException ce) {
                // expected
            }
        }

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
