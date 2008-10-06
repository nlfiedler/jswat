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
 * FILE:        loadTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/29/02        Initial version
 *
 * $Id: loadTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the load command.
 */
public class loadTest extends CommandTestCase {

    public loadTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(loadTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_load_kill_run() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        // no-arg case tested elsewhere
        try {
            runCommand(session, "load -classic");
            fail("expected MissingArgumentsException");
        } catch (MissingArgumentsException mae) {
            // expected
        }
        runCommand(session, "load locals");
        runCommand(session, "kill");
        runCommand(session, "load -classic locals abc 123");
        runCommand(session, "kill");
        runCommand(session, "clear all");
        runCommand(session, "run locals");

        // wait for the vm to terminate
        synchronized (ssl) {
            if (ssl.isRunning()) {
                try {
                    ssl.wait();
                } catch (InterruptedException ie) { }
            }
        }
        if (ssl.isActive()) {
            fail("VM was expected to terminate");
        }

        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
