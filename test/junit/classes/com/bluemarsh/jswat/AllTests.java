/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: AllTests.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import junit.extensions.*;
import junit.framework.*;

/**
 * Runs all of the JSwat tests.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("JSwat Tests");
        suite.addTest(LogTest.suite());
        suite.addTest(MonitorManagerTest.suite());
        suite.addTest(SessionListenerListTest.suite());
        suite.addTest(PathManagerTest.suite());
        suite.addTest(SessionTest.suite());
        suite.addTest(VMConnectionTest.suite());
        suite.addTest(com.bluemarsh.jswat.command.AllTests.suite());
        suite.addTest(com.bluemarsh.jswat.expr.AllTests.suite());
        suite.addTest(com.bluemarsh.jswat.lang.AllTests.suite());
        suite.addTest(com.bluemarsh.jswat.lang.java.AllTests.suite());
        suite.addTest(com.bluemarsh.jswat.ui.AllTests.suite());
        suite.addTest(com.bluemarsh.jswat.util.AllTests.suite());
        // Start up a Session for these tests.
        return new SessionSetup(suite);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
