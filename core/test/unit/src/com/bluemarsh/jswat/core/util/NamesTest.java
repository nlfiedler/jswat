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
 * are Copyright (C) 2003-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NamesTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import java.io.File;
import junit.framework.*;

/**
 * Tests the Names class.
 */
public class NamesTest extends TestCase {

    public NamesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(NamesTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

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

    public void test_Names_getShortClassName() {
        assertNull(Names.getShortClassName(null));
        assertEquals("", Names.getShortClassName(""));
        assertEquals("abc", Names.getShortClassName("abc"));
        assertEquals("abc", Names.getShortClassName("com.abc"));
        assertEquals("abc", Names.getShortClassName("com.package.abc"));
    }

    public void test_Names_isMethodIdentifier() {
        // boolean isMethodIdentifier(String s);
        assertTrue(!Names.isMethodIdentifier(null));
        assertTrue(!Names.isMethodIdentifier(""));
        assertTrue(Names.isMethodIdentifier("ident"));
        assertTrue(Names.isMethodIdentifier("_ident"));
        assertTrue(!Names.isMethodIdentifier("128_ident"));
        assertTrue(Names.isMethodIdentifier("ide_nt"));
        assertTrue(Names.isMethodIdentifier("<init>"));
        assertTrue(Names.isMethodIdentifier("<clinit>"));
    }
}
