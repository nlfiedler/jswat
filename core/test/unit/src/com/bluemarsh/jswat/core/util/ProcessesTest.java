/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ProcessesTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.SessionHelper;
import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ProcessesTest extends TestCase {

    public ProcessesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ProcessesTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Processes_waitFor() {
        // Establish a proper testing environment.
        String clspath = SessionHelper.getTestClasspath();
        String jre = System.getProperty("java.home");
        File bindir = new File(jre, "bin");
        File javabin = new File(bindir, "java");
        if (!javabin.exists()) {
            javabin = new File(bindir, "java.exe");
            if (!javabin.exists()) {
                fail("cannot find java executable in " + jre);
            }
        }

        // Start the JVM running our test code and collect its output.
        String[] cmd = new String[] {
            javabin.getAbsolutePath(), "-cp", clspath, "ProcessesTestCode"
        };
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        try {
            Process proc = rt.exec(cmd);
            String output = Processes.waitFor(proc);
            // Output comes before error.
            assertTrue(output.startsWith("output 1"));
            assertTrue(output.trim().endsWith("error 4"));
        } catch (IOException ioe) {
            fail(ioe.toString());
        }

        // Ask the JVM what version it is, and verify the output.
        cmd = new String[] {
            javabin.getAbsolutePath(), "-version"
        };
        try {
            Process proc = rt.exec(cmd);
            String output = Processes.waitFor(proc);
            assertTrue(output.startsWith("java version"));
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }
}
