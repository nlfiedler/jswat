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

package com.bluemarsh.jswat.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the DefaultPlatformService class.
 *
 * @author Nathan Fiedler
 */
public class DefaultPlatformServiceTest {

    @Test
    public void testGetSourceName() throws Exception {
        DefaultPlatformService instance = new DefaultPlatformService();
        String result = instance.getSourceName(null, null);
        assertNull(result);
    }

    @Test
    public void testGetPreferences() {
        DefaultPlatformService instance = new DefaultPlatformService();
        Preferences result = instance.getPreferences(DefaultPlatformService.class);
        assertNotNull(result);
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadMissingFile() throws IOException {
        DefaultPlatformService instance = new DefaultPlatformService();
        instance.readFile("missing.file");
    }

    @Test
    public void testDeleteMissingFile() throws IOException {
        DefaultPlatformService instance = new DefaultPlatformService();
        instance.deleteFile("missing.file");
    }

    @Test
    public void testFileAccess() throws IOException {
        String name = "foobar.txt";
        String text = "bazquux";
        DefaultPlatformService instance = new DefaultPlatformService();
        OutputStream os = instance.writeFile(name);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(text);
        dos.close();
        instance.releaseLock(name);
        InputStream is = instance.readFile(name);
        DataInputStream dis = new DataInputStream(is);
        String result = dis.readUTF();
        dis.close();
        assertEquals(text, result);
        instance.deleteFile(name);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStartProgress() {
        DefaultPlatformService instance = new DefaultPlatformService();
        instance.startProgress(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStopProgress() {
        DefaultPlatformService instance = new DefaultPlatformService();
        instance.stopProgress(null);
    }
}
