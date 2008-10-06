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
 * are Copyright (C) 2002-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: StringsTest.java 6 2007-05-16 07:14:24Z nfiedler $
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

    public void test_Strings_cleanForPrinting() {
        // String cleanForPrinting(String, int);
        assertEquals("null", Strings.cleanForPrinting(null, 0));
        assertEquals("blah", Strings.cleanForPrinting("blah", 0));
        assertEquals("this<...>tring", Strings.cleanForPrinting(
                "this is a very long string", 15));
        assertEquals("abc\\rdef", Strings.cleanForPrinting("abc\rdef", 0));
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
