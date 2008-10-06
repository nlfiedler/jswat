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
 * FILE:        readTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/28/02        Initial version
 *
 * $Id: readTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the read command.
 */
public class readTest extends CommandTestCase {

    public readTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(readTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_read() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "read undefined.file");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }
        SessionManager.endSession();
    }
}
