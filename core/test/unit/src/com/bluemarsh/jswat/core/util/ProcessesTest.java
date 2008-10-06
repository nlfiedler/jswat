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
 * $Id: ProcessesTest.java 6 2007-05-16 07:14:24Z nfiedler $
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
