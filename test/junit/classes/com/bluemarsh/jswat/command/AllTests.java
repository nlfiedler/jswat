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
 * $Id: AllTests.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.SessionSetup;
import junit.extensions.*;
import junit.framework.*;

/**
 * Runs all of the JSwat command tests.
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("JSwat Command Tests");
        suite.addTest(CommandArgumentsTest.suite());
        suite.addTest(InactiveTests.suite());
        suite.addTest(ActiveTests.suite());

        // These test cases manage their own session activity.
        suite.addTest(aliasTest.suite());
        suite.addTest(aproposTest.suite());
        suite.addTest(brkinfoTest.suite());
        suite.addTest(brkmonTest.suite());
        suite.addTest(bytecodesTest.suite());
        suite.addTest(captureTest.suite());
        suite.addTest(catchTest.suite());
        suite.addTest(classTest.suite());
        suite.addTest(classbrkTest.suite());
        suite.addTest(clearTest.suite());
        suite.addTest(conditionTest.suite());
        suite.addTest(copysessionTest.suite());
        suite.addTest(disableTest.suite());
        suite.addTest(disablegcTest.suite());
        suite.addTest(downTest.suite());
        suite.addTest(dumpTest.suite());
        suite.addTest(elementsTest.suite());
        suite.addTest(enableTest.suite());
        suite.addTest(excludeTest.suite());
        suite.addTest(fieldsTest.suite());
        suite.addTest(filterTest.suite());
        suite.addTest(historyTest.suite());
        suite.addTest(hotswapTest.suite());
        suite.addTest(interruptTest.suite());
        suite.addTest(invokeTest.suite());
        suite.addTest(linesTest.suite());
        suite.addTest(listTest.suite());
        suite.addTest(loadTest.suite());
        suite.addTest(loadsessionTest.suite());
        suite.addTest(localsTest.suite());
        suite.addTest(locksTest.suite());
        suite.addTest(loggingTest.suite());
        suite.addTest(monitorTest.suite());
        suite.addTest(nextTest.suite());
        suite.addTest(optionsTest.suite());
        suite.addTest(printTest.suite());
        suite.addTest(propsTest.suite());
        suite.addTest(readTest.suite());
        suite.addTest(rmsessionTest.suite());
        suite.addTest(runtoTest.suite());
//        suite.addTest(setTest.suite());
        suite.addTest(stepTest.suite());
        suite.addTest(stopTest.suite());
        suite.addTest(suspendTest.suite());
        suite.addTest(threadTest.suite());
        suite.addTest(threadbrkTest.suite());
        suite.addTest(threadlocksTest.suite());
        suite.addTest(traceTest.suite());
        suite.addTest(viewTest.suite());
        suite.addTest(watchTest.suite());
        suite.addTest(whereTest.suite());

        return new SessionSetup(suite);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
