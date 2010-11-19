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
 * are Copyright (C) 2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.Preferences;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the ConsolePlatformService class.
 *
 * @author Nathan Fiedler
 */
public class ConsolePlatformServiceTest {

    private static final String FILE_NAME = "testfile.txt";
    private static final String FILE_CONTENTS = "foobar";

    @Test
    public void testFileHandling() throws IOException {
        ConsolePlatformService instance = new ConsolePlatformService();
        OutputStream output = instance.writeFile(FILE_NAME);
        assertNotNull(output);
        PrintStream out = new PrintStream(output);
        out.println(FILE_CONTENTS);
        out.close();
        instance.releaseLock(FILE_NAME);
        InputStream input = instance.readFile(FILE_NAME);
        BufferedReader in = new BufferedReader(new InputStreamReader(input));
        String actual = in.readLine();
        assertNotNull(actual);
        in.close();
        assertEquals(FILE_CONTENTS, actual);
        instance.deleteFile(FILE_NAME);
    }

    @Test
    public void testGetSourceName() throws Exception {
        InputStream clazz = this.getClass().getResourceAsStream("ConsolePlatformServiceTest.class");
        assertNotNull("failed to load unit test class", clazz);
        String name = "com.bluemarsh.jswat.console.ConsolePlatformServiceTest";
        ConsolePlatformService instance = new ConsolePlatformService();
        String expResult = "ConsolePlatformServiceTest.java";
        String result = instance.getSourceName(clazz, name);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPreferences() {
        Class<?> clazz = ConsolePlatformService.class;
        ConsolePlatformService instance = new ConsolePlatformService();
        Preferences result = instance.getPreferences(clazz);
        assertNotNull(result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartProgress() {
        ConsolePlatformService instance = new ConsolePlatformService();
        instance.startProgress(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStopProgress() {
        ConsolePlatformService instance = new ConsolePlatformService();
        instance.stopProgress(null);
    }
}
