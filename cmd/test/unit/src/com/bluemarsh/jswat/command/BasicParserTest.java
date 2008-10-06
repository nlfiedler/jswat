/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BasicParserTest.java 15 2007-06-03 00:01:17Z nfiedler $
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
