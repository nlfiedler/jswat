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
 * FILE:        CommandArgumentsTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/22/02        Initial version
 *
 * $Id: CommandArgumentsTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import java.util.NoSuchElementException;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the CommandArguments class.
 */
public class CommandArgumentsTest extends TestCase {

    public CommandArgumentsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CommandArgumentsTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Evaluate the given arguments, testing for correct behavior in
     * the CommandArguments class.
     *
     * @param  args       string of arguments.
     * @param  count      expected token count.
     * @param  isIllegal  true if expression is expected to be illegal.
     */
    protected static void evalArgs(String args, int count, boolean isIllegal) {
        CommandArguments cargs = new CommandArguments(args);
        try {
            assertEquals(count, cargs.countTokens());
            for (int ii = 0; ii < count; ii++) {
                cargs.nextToken();
            }
        } catch (NoSuchElementException nsee) {
            fail("countTokens() reported incorrect value");
        } catch (IllegalArgumentException iae) {
            if (isIllegal) {
                // Yep, we expected that.
                return;
            } else {
                fail(iae.toString());
            }
        }

        try {
            cargs.nextToken();
            fail("expected NoSuchElement from nextToken()");
        } catch (NoSuchElementException nsee) {
        }

        cargs.reset();
        cargs.returnAsIs(true);
        assertEquals(args, cargs.rest());
    }

    public void testCommandArgumentsBasic() {
        evalArgs("load -client locals abc 123", 5, false);
        evalArgs("load -client 'locals abc' 123", 4, false);
        evalArgs("load -client \"locals abc\" 123", 4, false);
        evalArgs("load -client \\\"locals abc\\\" 123", 5, false);
        evalArgs("load -client \\'locals abc\\' 123", 5, false);
        evalArgs("load -client \\' 123", 4, false);
        evalArgs("load -client \\ 123", 3, false);
        evalArgs("load -client \\\\ 123", 4, false);
        evalArgs("load -client \\\\\\= 123", 4, false);
        evalArgs("next is error ' 123", 4, true);
        evalArgs("\"next is error\" \"", 2, true);
        evalArgs("classpath 'C:\\My Files\\java\\classes'", 2, false);
    }

    public void testCommandArgumentsPeek() {
        CommandArguments cargs = new CommandArguments("one");
        assertEquals(1, cargs.countTokens());
        assertEquals("one", cargs.peek());
        assertEquals(true, cargs.hasMoreTokens());
        assertEquals("one", cargs.rest());
    }
}
