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
 * FILE:        classbrkTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/26/02        Initial version
 *
 * $Id: classbrkTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the classbrk command.
 */
public class classbrkTest extends CommandTestCase {

    public classbrkTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(classbrkTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_classbrk() {
        Session session = SessionManager.beginSession();
        // no-arg case tested elsewhere
        try {
            runCommand(session, "classbrk go");
            fail("expected MissingArgumentsException");
        } catch (MissingArgumentsException mae) {
            // expected
        }
        try {
            runCommand(session, "classbrk thread");
            fail("expected MissingArgumentsException");
        } catch (MissingArgumentsException mae) {
            // expected
        }
        runCommand(session, "classbrk all");
        runCommand(session, "classbrk go all");
        runCommand(session, "classbrk thread all");
        runCommand(session, "classbrk clazz");
        runCommand(session, "classbrk go clazz");
        runCommand(session, "classbrk thread clazz");
        runCommand(session, "clear all");
        SessionManager.endSession();
    }
}
