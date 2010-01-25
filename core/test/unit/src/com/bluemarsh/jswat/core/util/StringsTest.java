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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Strings class.
 *
 * @author  Nathan Fiedler
 */
public class StringsTest {

    @Test
    public void test_Strings_cleanForPrinting() {
        assertEquals("null", Strings.cleanForPrinting(null, 0));
        assertEquals("blah", Strings.cleanForPrinting("blah", 0));
        assertEquals("blah", Strings.cleanForPrinting("blah", 10));
        assertEquals("this<...>tring", Strings.cleanForPrinting(
                "this is a very long string", 15));
        assertEquals("abc\\rdef", Strings.cleanForPrinting("abc\rdef", 0));
        assertEquals("\\t\\n\\b\\f", Strings.cleanForPrinting("\t\n\b\f", 0));
    }

    @Test
    public void test_Strings_listToString() {
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

    @Test
    public void test_Strings_stringToList() {
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

    @Test
    public void test_Strings_toHexString() {
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

    @Test
    public void test_Strings_trimQuotes() {
        assertNull(Strings.trimQuotes(null));
        assertEquals("", Strings.trimQuotes(""));
        assertEquals("  ", Strings.trimQuotes("  "));
        assertEquals("abc", Strings.trimQuotes("abc"));
        assertEquals("abc", Strings.trimQuotes("'abc'"));
        assertEquals("abc'", Strings.trimQuotes("abc'"));
        assertEquals("abc", Strings.trimQuotes("\"abc\""));
        assertEquals("abc\"", Strings.trimQuotes("abc\""));
        assertEquals("'abc", Strings.trimQuotes("'abc"));
        assertEquals("\"abc", Strings.trimQuotes("\"abc"));
    }

    @Test
    public void testExceptionToString() {
        assertEquals("", Strings.exceptionToString(null));
        RuntimeException re = new RuntimeException();
        String actual = Strings.exceptionToString(re);
        assertTrue(actual.contains("RuntimeException"));
    }
}
