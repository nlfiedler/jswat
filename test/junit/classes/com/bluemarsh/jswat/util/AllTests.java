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

package com.bluemarsh.jswat.util;

import junit.extensions.*;
import junit.framework.*;

/**
 * Runs all of the JSwat tests.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("JSwat Util Tests");
        suite.addTest(EventListenerListTest.suite());
        suite.addTest(NamesTest.suite());
        suite.addTest(PriorityListTest.suite());
        suite.addTest(SessionSettingsTest.suite());
        suite.addTest(SkipListTest.suite());
        suite.addTest(StringsTest.suite());
        suite.addTest(TypesTest.suite());
        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
