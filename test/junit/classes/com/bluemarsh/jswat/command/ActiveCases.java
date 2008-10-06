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
 * FILE:        ActiveCases.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/26/02        Initial version
 *
 * $Id: ActiveCases.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests performed while the Session is active which are expected to
 * succeed.
 */
public class ActiveCases extends CommandTestCase {

    public ActiveCases(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(ActiveCases.class), true);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testActiveSimple() {
        Session session = SessionManager.beginSession();

        String[] commands = new String[] {
            "classes",
            "classes java",
            "support",
            "threadgroups",
            "version",
            "vminfo"
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
}
