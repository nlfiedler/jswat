/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: StringsTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.*;
import junit.extensions.*;
import junit.framework.*;

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
        // String cleanForPrinting(String input, int maxLen);
        assertEquals("null", Strings.cleanForPrinting(null, 0));
        assertEquals("abc123", Strings.cleanForPrinting("abc123", 0));
        assertEquals("abc\\n123\\rdef\\t456\\bghi\\f789",
                     Strings.cleanForPrinting(
                         "abc\n123\rdef\t456\bghi\f789", 0));
        assertEquals("a long <...>hortened",
                     Strings.cleanForPrinting(
                         "a long string that should be shortened", 20));
    }

    public void test_Strings_indexOfUnescaped() {
        // int indexOfUnescaped(String s, int ch, int fromIndex);
        assertEquals(-1, Strings.indexOfUnescaped(null, 0, 0));
        assertEquals(5, Strings.indexOfUnescaped(
                         "a\\'bc'defghi", '\'', 0));
        assertEquals(5, Strings.indexOfUnescaped(
                         "a\\'bc'defghi", '\'', 5));
        assertEquals(-1, Strings.indexOfUnescaped(
                         "a\\'bc'defghi", '\'', 6));
    }

    public void test_Strings_listToString() {
        // String listToString(List list);
        assertNull(Strings.listToString(null));
        List list = new ArrayList();
        assertEquals("", Strings.listToString(list));
        list.add("abc");
        assertEquals("abc", Strings.listToString(list));
        list.add("def");
        assertEquals("abc, def", Strings.listToString(list));
        list.add("ghi");
        assertEquals("abc, def, ghi", Strings.listToString(list));
    }

    public void test_Strings_splitOnNewline() {
        // String[] splitOnNewline(String str);
        String[] actual = Strings.splitOnNewline(null);
        assertNull(actual);
        actual = Strings.splitOnNewline("abcdef");
        assertEquals(1, actual.length);
        assertEquals("abcdef", actual[0]);
        actual = Strings.splitOnNewline("abc\ndef");
        assertEquals(2, actual.length);
        assertEquals("abc", actual[0]);
        assertEquals("def", actual[1]);
        actual = Strings.splitOnNewline("abc\ndef\nghi");
        assertEquals(3, actual.length);
        assertEquals("abc", actual[0]);
        assertEquals("def", actual[1]);
        assertEquals("ghi", actual[2]);
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

    public void test_Strings_tokenize() {
        // String toHexString(int i);
        String[] actual = Strings.tokenize(null);
        assertEquals(0, actual.length);
        actual = Strings.tokenize("");
        assertEquals(0, actual.length);
        actual = Strings.tokenize("abc");
        assertEquals(1, actual.length);
        assertEquals("abc", actual[0]);
        actual = Strings.tokenize("abc def");
        assertEquals(2, actual.length);
        assertEquals("abc", actual[0]);
        assertEquals("def", actual[1]);
        actual = Strings.tokenize("abc def ghi");
        assertEquals(3, actual.length);
        assertEquals("abc", actual[0]);
        assertEquals("def", actual[1]);
        assertEquals("ghi", actual[2]);
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
