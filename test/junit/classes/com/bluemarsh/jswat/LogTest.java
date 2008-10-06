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
 * FILE:        LogTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/25/02        Initial version
 *
 * $Id: LogTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the Log class.
 */
public class LogTest extends TestCase {

    public LogTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(LogTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testLogAttachments() {
        Log log = new Log();
        Writer w = new PrintWriter(System.out);
        log.attach((Writer) null);
        log.detach((Writer) null);
        log.detach((OutputStream) null);
        log.attach(w);
        log.attach(System.out);
        log.detach(w);
        log.detach(System.out);
    }

    public void testLogWriting() {
        Log log = new Log();
        StringWriter sw = new StringWriter(80);
        log.attach(sw);
        log.write("one");
        log.writeln("two");
        log.writeln("three");
        log.flush();
        String s = sw.toString();
        assertEquals("onetwo\nthree\n", s);
        log.detach(sw);

        sw = new StringWriter(80);
        log.attach(sw);
        for (int ii = 0; ii < 1000; ii++) {
            log.writeln("0123456789");
            if (ii % 10 == 0) {
                // non-threaded Log needs a flush every now and then
                log.flush();
            }
        }
        // flush the last bits out
        log.flush();
        s = sw.toString();
        assertEquals(11000, s.length());
        log.detach(sw);
    }

    public void testLogThreaded() {
        Log log = new Log();
        StringWriter sw = new StringWriter(80);
        log.attach(sw);

        log.start();
        for (int ii = 0; ii < 1000; ii++) {
            log.writeln("0123456789");
        }
        // flush the last bits out
        log.flush();
        String s = sw.toString();
        assertEquals(11000, s.length());

        log.stop();
        log.detach(sw);
    }
}
