/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BasicParserTest.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the CommandParser class.
 */
public class BasicParserTest extends TestCase {

    public BasicParserTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(BasicParserTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_CommandParser_Basic() {
        TestData[] datum = new TestData[] {
            new TestData("", "", "Nothing should beget nothing."),
            new TestData("echo"),
            new TestData("echo hello", "hello\n"),
            new TestData("  echo hello  ", "hello\n"),
            // bang-bang input processor
            new TestData("!!", "hello\n"),
            // bang-prefix input processor
            new TestData("!ec", "hello\n"),
            // unique command-name prefix
            new TestData("ec hello", "hello\n"),
        };
        CommandParser parser = new DefaultCommandParser();
        ParserHelper helper = new ParserHelper();
        helper.performTest(this, parser, datum);

        // Ensure the duplicated commands did not impact the history.
        Iterator<String> history = parser.getHistory(true);
        int count = 0;
        while (history.hasNext()) {
            history.next();
            count++;
        }
        assertEquals("duplicates impacted history", 3, count);

        // Test more input processors, including combinations.
        parser.setAlias("ohce1", "echo hello");
        parser.setAlias("ohce2", "3 echo hello");
        parser.setAlias("ohce3", "echo hello;echo hello;echo hello");
        parser.setAlias("ohce4", "3 ohce1");
        datum = new TestData[] {
            // alias input processor
            new TestData("ohce1", "hello\n"),
            // repeater input processor
            new TestData("3 echo hello", "hello\nhello\nhello\n"),
            new TestData("3 ohce1", "hello\nhello\nhello\n"),
            new TestData("ohce2", "hello\nhello\nhello\n"),
            new TestData("ohce4", "hello\nhello\nhello\n"),
            // the following !! is invoking the test above this line
            new TestData("1 echo hello;ohce1;!!", "hello\nhello\nhello\nhello\nhello\n"),
            // multiple command input processor
            new TestData("echo hello ; echo hello;echo hello", "hello\nhello\nhello\n"),
            new TestData("ohce1 ; ohce1;ohce1", "hello\nhello\nhello\n"),
            new TestData("ohce3", "hello\nhello\nhello\n"),
            // bang-bang input processor
            new TestData("!!", "hello\nhello\nhello\n"),
        };
        helper.performTest(this, parser, datum);
    }
}
