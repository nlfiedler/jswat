/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * $Id: Strings.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides utility methods for handling Strings.
 *
 * @author  Nathan Fiedler
 */
public class Strings {
    /** Array of zeros for padding numbers. */
    private static String[] zeros;

    static {
        zeros = new String[32];
        StringBuffer sb = new StringBuffer();
        for (int ii = 0; ii < zeros.length; ii++) {
            sb.append('0');
            zeros[ii] = sb.toString();
        }
    }

    /**
     * Substitute the slash escaped printable characters with the
     * escaped equivalents, so the string can be displayed properly.
     * Also optionally limit the length of the string.
     *
     * @param  input   string to be processed (may be null).
     * @param  maxLen  maximum length of output string; 0 for no limit.
     * @return  string with escapes escaped and length limited;
     *          "null" if input was null.
     */
    public static String cleanForPrinting(String input, int maxLen) {
        if (input == null) {
            return "null";
        }

        StringBuffer out = new StringBuffer();
        StringTokenizer st = new StringTokenizer(input, "\r\n\t\b\f", true);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            s = s.intern();
            if (s == "\t") {
                out.append("\\t");
            } else if (s == "\n") {
                out.append("\\n");
            } else if (s == "\b") {
                out.append("\\b");
            } else if (s == "\f") {
                out.append("\\f");
            } else if (s == "\r") {
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
     * Return a string representation of this exception, which should
     * include the class, message, and stack trace (as returned by
     * calling <code>printStackTrace</code> on the exception).
     *
     * @param  t  throwable.
     * @return  string of throwable.
     */
    public static String exceptionToString(Throwable t) {
        StringWriter sw = new StringWriter(512);
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    } // exceptionToString

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
     * @param  s  the string.
     * @param  c  a character.
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
        if (s == null) {
            return -1;
        }
        int index = s.indexOf(ch, fromIndex);
        // Check if the character is escaped.
        while (index > 0) {
            if (s.charAt(index - 1) != '\\'
                || index > 1 && s.charAt(index - 2) == '\\') {
                break;
            }
            index = s.indexOf(ch, index + 1);
        }
        return index;
    } // indexOfUnescaped

    /**
     * Converts the list to a comma-separated String of values.
     *
     * @param  list  list to convert to string.
     * @return  String representing the list; empty if list is empty;
     *          null if list is null.
     */
    public static String listToString(List list) {
        return listToString(list, ", ");
    } // listToString

    /**
     * Converts the list to a String separated by the given string.
     *
     * @param  list  list to convert to string.
     * @param  sep   string separator to use.
     * @return  String representing the list; empty if list is empty;
     *          null if list is null.
     */
    public static String listToString(List list, String sep) {
        if (list == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        if (list.size() > 0) {
            buf.append(list.get(0));
        }
        for (int ii = 1; ii < list.size(); ii++) {
            buf.append(sep);
            buf.append(list.get(ii));
        }
        return buf.toString();
    } // listToString

    /**
     * Splits the given string on the newline character (\n).
     *
     * @param  str  string to split.
     * @return  array of strings; empty array if str is empty;
     *          null if str is null.
     */
    public static String[] splitOnNewline(String str) {
        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, "\n");
        int size = st.countTokens();
        String[] arr = new String[size];
        for (int ii = 0; ii < size; ii++) {
            arr[ii] = st.nextToken();
        }
        return arr;
    } // splitOnNewline

    /**
     * Converts the String of comma-separated values to a list of
     * String elements. The string elements are trimmed (leading and
     * trailing whitespace is removed) before being added to the list.
     *
     * @param  str  string to convert to list.
     * @return  list of String values; empty if str is empty;
     *          null if str is null.
     */
    public static List stringToList(String str) {
        if (str == null) {
            return null;
        }
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
        return toHexString(i, 4);
    } // toHexString

    /**
     * Converts the given integer to a four-digit, hexadecimal string,
     * padding with zeros as needed.
     *
     * @param  i  integer to convert.
     * @param  w  width in characters, padded with zeros. Allowed
     *            range is one to 32.
     * @return  String representation.
     */
    public static String toHexString(int i, int w) {
        if (w < 1 || w > zeros.length) {
            throw new IllegalArgumentException("invalid w value");
        }
        String hex = Integer.toHexString(i);
        if (hex.length() < w) {
            hex = zeros[w - 1 - hex.length()].concat(hex);
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
     * @return  array of strings from input; empty array if input is
     *          null or empty.
     */
    public static String[] tokenize(String input) {
        if (input == null) {
            return new String[0];
        }
        StringTokenizer t = new StringTokenizer(input);
        int size = t.countTokens();
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = t.nextToken();
        }
        return strings;
    } // tokenize

    /**
     * Trim any quotes from the beginning and end of the string. This
     * includes single and double quotes. This trims only one set of
     * quotes from either end of the string, and only if they are
     * matching quotes (single for single, double for double).
     *
     * @param  str  string to trim.
     * @return  quote-trimmed string.
     */
    public static String trimQuotes(String str) {
        if (str.length() > 0) {
            char fch = str.charAt(0);
            int last = str.length() - 1;
            char lch = str.charAt(last);
            if ((fch == '"' || fch == '\'') && fch == lch) {
                str = str.substring(1, last);
            }
        }
        return str;
    } // trimQuotes
} // Strings
