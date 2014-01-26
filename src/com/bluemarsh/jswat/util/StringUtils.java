/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      Utilities
 * FILE:        StringUtils.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/02/01        Initial version
 *      nf      10/07/01        Added toHexString()
 *      tr      12/09/01        Added cleanForPrinting()
 *      nf      03/20/02        Added list<->string methods
 *
 * DESCRIPTION:
 *      This file defines a String utility class.
 *
 * $Id: StringUtils.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides utility methods for handling Strings.
 *
 * @author  Nathan Fiedler
 */
public class StringUtils {
    /** Array of zeros for padding numbers. */
    protected static String[] zeros = new String[] { "0", "00", "000" };

    /**
     * Substitute the slash escaped printable characters with the
     * escaped equivalents, so the string can be displayed properly.
     * Also optionally limit the length of the string.
     * 
     * @param  input   string to be processed (may be null).
     * @param  maxLen  maximum length of output string; 0 for no limit.
     * @return  string with escapes escaped and length limited.
     */
    public static String cleanForPrinting(String input, int maxLen) {
        if (input == null) {
            return "null";
        }

        StringBuffer out = new StringBuffer();
        StringTokenizer st = new StringTokenizer(input, "\r\n\t\b\f", true);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (s.equals("\t") ) {
                out.append("\\t");
            } else if (s.equals("\n") ) {
                out.append("\\n");
            } else if (s.equals("\b") ) {
                out.append("\\b");
            } else if (s.equals("\f") ) {
                out.append("\\f");
            } else if (s.equals("\r")) {
                out.append("\\r");
            } else {
                out.append(s);
            }
        }

        if (maxLen > 0 && out.length() > maxLen) {
            String sfx = out.substring(out.length() - maxLen / 2 + 2);
            String pfx = out.substring(0, maxLen / 2 - 3);
            out.setLength(0);
            out.append(pfx);
            out.append("<...>");
            out.append(sfx);
        }
        return out.toString();
    } // cleanForPrinting

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, not preceeded by a forward slash (\). If a
     * character with value <code>ch</code> occurs in the character
     * sequence represented by this <code>String</code> object, then the
     * index of the first such occurrence is returned -- that is, the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch</pre>
     * </blockquote>
     * is <code>true</code>. If no such character occurs in this string,
     * then <code>-1</code> is returned.
     *
     * @param  s   the string.
     * @param  ch  a character.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object, or
     *          <code>-1</code> if the character does not occur.
     */
    public static int indexOfUnescaped(String s, char c) {
        return indexOfUnescaped(s, c, 0);
    } // indexOfUnescaped

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, not preceeded by a forward slash (\), starting
     * the search at the specified index.
     *
     * <p>If a character with value <code>ch</code> occurs in the character
     * sequence represented by this <code>String</code> object at an index
     * no smaller than <code>fromIndex</code>, then the index of the first
     * such occurrence is returned--that is, the smallest value <i>k</i>
     * such that:</p>
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) && (<i>k</i> &gt;= fromIndex)</pre>
     *</blockquote>
     * is true. If no such character occurs in this string at or after
     * position <code>fromIndex</code>, then <code>-1</code> is returned.
     *
     * <p>There is no restriction on the value of <code>fromIndex</code>.
     * If it is negative, it has the same effect as if it were zero: this
     * entire string may be searched. If it is greater than the length of
     * this string, it has the same effect as if it were equal to the length
     * of this string: <code>-1</code> is returned.</p>
     *
     * @param  s          the string.
     * @param  ch         a character.
     * @param  fromIndex  the index to start the search from.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object that is greater
     *          than or equal to <code>fromIndex</code>, or <code>-1</code>
     *          if the character does not occur.
     */
    public static int indexOfUnescaped(String s, int ch, int fromIndex) {
        int index = s.indexOf(ch, fromIndex);
        // Check if the character is escaped.
        while (index > 0 && s.charAt(index - 1) == '\\') {
            index = s.indexOf(ch, index + 1);
        }
        return index;
    } // indexOfUnescaped

    /**
     * Converts the list to a comma-separated String of values.
     *
     * @param  list  list to convert to string.
     * @return  String representing the list.
     */
    public static String listToString(List list) {
        StringBuffer buf = new StringBuffer();
        if (list.size() > 0) {
            buf.append(list.get(0).toString());
        }
        for (int ii = 1; ii < list.size(); ii++) {
            buf.append(", ");
            buf.append(list.get(ii).toString());
        }
        return buf.toString();
    } // listToString

    /**
     * Converts the String of comma-separated values to a list of
     * String elements. The string elements are trimmed (leading and
     * trailing whitespace is removed) before being added to the list.
     *
     * @param  str  string to convert to list.
     * @return  list of String values.
     */
    public static List stringToList(String str) {
        List list = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(str, ",");
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken().trim());
        }
        return list;
    } // stringToList

    /**
     * Converts the given integer to a four-digit, hexadecimal string,
     * padding with zeros as needed.
     *
     * @param  i  integer to convert.
     * @return  String representation.
     */
    public static String toHexString(int i) {
        String hex = Integer.toHexString(i);
        if (hex.length() < 4) {
            hex = zeros[3 - hex.length()].concat(hex);
        }
        return hex;
    } // toHexString

    /**
     * Take the given string and chop it up into a series of strings
     * on whitespace boundries. This is useful for trying to get an
     * array of strings out of the resource file. If input is null,
     * returns a zero-length array of String.
     *
     * @param  input  string to be split apart.
     * @return  array of strings from input.
     */
    public static String[] tokenize(String input) {
        if (input == null) {
            return new String[0];
        }
        StringTokenizer t = new StringTokenizer(input);
        int size = t.countTokens();
        String strings[] = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = t.nextToken();
        }
        return strings;
    } // tokenize
} // StringUtils
