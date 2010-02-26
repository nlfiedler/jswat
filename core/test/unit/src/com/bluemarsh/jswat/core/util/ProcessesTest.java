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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import com.bluemarsh.jswat.core.SessionHelper;
import java.io.File;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Processes class.
 *
 * @author Nathan Fiedler
 */
public class ProcessesTest {

    /** The classpath for the unit test code. */
    private static String classpath;
    /** Full pathname for the Java executable. */
    private static String javabin;

    @BeforeClass
    public static void setUpClass() {
        // Establish a proper testing environment.
        classpath = SessionHelper.getTestClasspath();
        String jre = System.getProperty("java.home");
        File bindir = new File(jre, "bin");
        File java = new File(bindir, "java");
        if (!java.exists()) {
            java = new File(bindir, "java.exe");
            if (!java.exists()) {
                fail("cannot find java executable in " + jre);
            }
        }
        javabin = java.getAbsolutePath();
    }

    @Test
    public void testWaitForBoth() throws IOException {
        String[] cmd = new String[]{
            javabin, "-cp", classpath, "ProcessesTestCode"
        };
        Process proc = Runtime.getRuntime().exec(cmd);
        String output = Processes.waitFor(proc);
        // Output comes before error.
        assertTrue(output.contains("output 1"));
        assertTrue(output.trim().contains("error 4"));
    }

    @Test
    public void testWaitForOutput() throws IOException {
        String[] cmd = new String[]{
            javabin, "-cp", classpath, "ProcessesTestCode", "--out"
        };
        Process proc = Runtime.getRuntime().exec(cmd);
        String output = Processes.waitFor(proc);
        assertTrue(output.contains("output 1"));
        assertTrue(output.trim().contains("output 4"));
    }

    @Test
    public void testWaitForError() throws IOException {
        String[] cmd = new String[]{
            javabin, "-cp", classpath, "ProcessesTestCode", "--err"
        };
        Process proc = Runtime.getRuntime().exec(cmd);
        String output = Processes.waitFor(proc);
        assertTrue(output.contains("error 1"));
        assertTrue(output.trim().contains("error 4"));
    }

    @Test
    public void testWaitForNoOutput() throws IOException {
        String[] cmd = new String[]{
            javabin, "-cp", classpath, "ProcessesTestCode", "--nop"
        };
        Process proc = Runtime.getRuntime().exec(cmd);
        String output = Processes.waitFor(proc);
        assertTrue(output.isEmpty());
    }

    @Test
    public void testWaitForJavaVersion() throws IOException {
        String[] cmd = new String[]{
                    javabin, "-version"
                };
        Process proc = Runtime.getRuntime().exec(cmd);
        String output = Processes.waitFor(proc);
        assertTrue(output.startsWith("java version"));
    }
}
