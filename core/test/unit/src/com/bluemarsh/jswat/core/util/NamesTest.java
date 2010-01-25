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
 * are Copyright (C) 2003-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the Names class.
 */
public class NamesTest {

    @Test
    public void test_Names_classnameToFilename() {
        String fn = "com/bluemarsh/jswat/Main.java";
        fn = fn.replace('/', File.separatorChar);
        assertEquals("classnameToFilename() failed", fn,
                Names.classnameToFilename("com.bluemarsh.jswat.Main"));
        assertEquals("classnameToFilename() failed", fn,
                Names.classnameToFilename("com.bluemarsh.jswat.Main$Inner"));
        fn = "com/bluemarsh/jswat/SrcMain.java";
        fn = fn.replace('/', File.separatorChar);
        assertEquals("classnameToFilename() failed", fn,
                Names.classnameToFilename("com.bluemarsh.jswat.Main", "SrcMain.java"));
    }

    @Test
    public void testGetPackageName() {
        assertNull(Names.getPackageName(null));
        assertTrue(Names.getPackageName("abc").isEmpty());
        assertEquals("com.pkg", Names.getPackageName("com.pkg.Class"));
    }

    @Test
    public void testIsJavaIdentifier() {
        assertFalse(Names.isJavaIdentifier(null));
        assertFalse(Names.isJavaIdentifier(""));
        assertFalse(Names.isJavaIdentifier("  "));
        assertFalse(Names.isJavaIdentifier("1abc"));
        assertFalse(Names.isJavaIdentifier("abc+def"));
        assertTrue(Names.isJavaIdentifier("_abcDef"));
    }

    @Test
    public void test_Names_getShortClassName() {
        assertNull(Names.getShortClassName(null));
        assertEquals("", Names.getShortClassName(""));
        assertEquals("abc", Names.getShortClassName("abc"));
        assertEquals("abc", Names.getShortClassName("com.abc"));
        assertEquals("abc", Names.getShortClassName("com.package.abc"));
    }

    @Test
    public void test_Names_isMethodIdentifier() {
        assertTrue(!Names.isMethodIdentifier(null));
        assertTrue(!Names.isMethodIdentifier(""));
        assertTrue(Names.isMethodIdentifier("ident"));
        assertTrue(Names.isMethodIdentifier("_ident"));
        assertTrue(!Names.isMethodIdentifier("128_ident"));
        assertTrue(Names.isMethodIdentifier("ide_nt"));
        assertTrue(Names.isMethodIdentifier("<init>"));
        assertTrue(Names.isMethodIdentifier("<clinit>"));
    }

    @Test
    public void testIsValidClassname() {
        assertFalse(Names.isValidClassname(null, false));
        assertFalse(Names.isValidClassname("", false));
        assertFalse(Names.isValidClassname("  ", false));
        assertFalse(Names.isValidClassname("1abc", false));
        assertFalse(Names.isValidClassname("abc+def", false));
        assertTrue(Names.isValidClassname("_abcDef", false));
        assertTrue(Names.isValidClassname("java.lang.String", false));
        assertTrue(Names.isValidClassname("java.lang.String", true));
        assertTrue(Names.isValidClassname("java.lang.*", true));
        assertTrue(Names.isValidClassname("*.String", true));
        assertFalse(Names.isValidClassname("java.*.String", true));
    }
}
