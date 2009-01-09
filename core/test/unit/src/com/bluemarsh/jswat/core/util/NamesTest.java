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
 * are Copyright (C) 2003-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
