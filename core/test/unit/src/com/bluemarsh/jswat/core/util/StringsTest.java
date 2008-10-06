/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: StringsTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the Strings class.
 */
public class StringsTest extends TestCase {

    public StringsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(StringsTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Strings_listToString() {
        // String listToString(List list);
        assertNull(Strings.listToString(null));
        List<String> list = new ArrayList<String>(3);
        assertEquals("", Strings.listToString(list));
        list.add("abc");
        assertEquals("abc", Strings.listToString(list));
        list.add("def");
        assertEquals("abc, def", Strings.listToString(list));
        list.add("ghi");
        assertEquals("abc, def, ghi", Strings.listToString(list));
        assertEquals("abc;def;ghi", Strings.listToString(list, ";"));
    }

    public void test_Strings_stringToList() {
        // List stringToList(String str);
        List list = Strings.stringToList(null);
        assertNull(list);
        list = Strings.stringToList("");
        assertEquals(0, list.size());
        list = Strings.stringToList("abc");
        assertEquals(1, list.size());
        assertEquals("abc", list.get(0));
        list = Strings.stringToList("abc,def");
        assertEquals(2, list.size());
        assertEquals("abc", list.get(0));
        assertEquals("def", list.get(1));
        list = Strings.stringToList("abc , def , ghi");
        assertEquals(3, list.size());
        assertEquals("abc", list.get(0));
        assertEquals("def", list.get(1));
        assertEquals("ghi", list.get(2));
    }

    public void test_Strings_toHexString() {
        // String toHexString(int i);
        assertEquals("0001", Strings.toHexString(1));
        assertEquals("0080", Strings.toHexString(128));
        assertEquals("04d2", Strings.toHexString(1234));
        assertEquals("ffff", Strings.toHexString(65535));
        assertEquals("0000ffff", Strings.toHexString(65535, 8));
        assertEquals("ffffffff", Strings.toHexString(-1));
        assertEquals("00000000ffffffff", Strings.toHexString(-1, 16));
        assertEquals("000000000000000000000000ffffffff",
                     Strings.toHexString(-1, 32));
    }

    public void test_Strings_trimQuotes() {
        // String trimQuotes(String str);
        assertEquals("abc", Strings.trimQuotes("abc"));
        assertEquals("abc", Strings.trimQuotes("'abc'"));
        assertEquals("abc'", Strings.trimQuotes("abc'"));
        assertEquals("abc", Strings.trimQuotes("\"abc\""));
        assertEquals("abc\"", Strings.trimQuotes("abc\""));
        assertEquals("'abc", Strings.trimQuotes("'abc"));
        assertEquals("\"abc", Strings.trimQuotes("\"abc"));
    }
}
