/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: ClassNameTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang;

import java.io.File;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the ClassName class.
 *
 * @author  Nathan Fiedler
 */
public class ClassNameTest extends TestCase {

    public ClassNameTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ClassNameTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_ClassName_compareTo() {
        assertEquals(0, new ClassName("Class").compareTo("Class"));
        assertEquals(0, new ClassName("Class").compareTo(
            new ClassName("Class")));
        assertEquals(0, new ClassName("Class$Inner").compareTo("Class$Inner"));
        assertEquals(0, new ClassName("Class$Inner").compareTo(
            new ClassName("Class$Inner")));
        assertEquals(0, new ClassName("Class+1").compareTo("Class+1"));
        assertEquals(0, new ClassName("Class+1").compareTo(
            new ClassName("Class+1")));
        assertEquals(0, new ClassName("Class$1").compareTo("Class+1"));
        assertEquals(0, new ClassName("Class$1").compareTo(
            new ClassName("Class+1")));
    }

    public void test_ClassName_getDisplayName() {
        assertEquals("Class", new ClassName("Class").getDisplayName());
        assertEquals("Class$1", new ClassName("Class$1").getDisplayName());
        assertEquals("Class+1", new ClassName("Class+1").getDisplayName());
        assertEquals("Class$Inner",
                     new ClassName("Class$Inner").getDisplayName());
    }

    public void test_ClassName_getName() {
        assertEquals("Class", new ClassName("Class").getName());
        assertEquals("Class+1", new ClassName("Class$1").getName());
        assertEquals("Class+1", new ClassName("Class+1").getName());
        assertEquals("Class$Inner", new ClassName("Class$Inner").getName());
    }

    public void test_ClassName_normalizeName() {
        assertEquals("Class", ClassName.normalizeName("Class"));
        assertEquals("Class$Inner", ClassName.normalizeName("Class$Inner"));
        assertEquals("Class+1", ClassName.normalizeName("Class$1"));
        assertEquals("Class+1", ClassName.normalizeName("Class+1"));
    }

    public void test_ClassName_toFilename() {
        String fn = "com/bluemarsh/jswat/Main.java";
        fn = fn.replace('/', File.separatorChar);
        ClassName cn = new ClassName("com.bluemarsh.jswat.Main");
        assertEquals("classnameToFilename() failed", fn, cn.toFilename());
        cn = new ClassName("com.bluemarsh.jswat.Main$Inner");
        assertEquals("classnameToFilename() failed", fn, cn.toFilename());
        fn = "com/bluemarsh/jswat/SrcMain.java";
        fn = fn.replace('/', File.separatorChar);
        cn = new ClassName("com.bluemarsh.jswat.Main", "SrcMain.java");
        assertEquals("classnameToFilename() failed", fn, cn.toFilename());
    }
}
