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
 * are Copyright (C) 2001-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides utility methods for handling Strings.
 *
 * @author  Nathan Fiedler
 */
public class Strings {

    /**
     * Creates a new instance of Strings.
     */
    private Strings() {
    }

    /**
     * Substitute any non-printable characters with their escaped form,
     * allowing them to be interpreted by the user.
     *
     * @param  input   String to be processed (may be null).
     * @param  maxLen  maximum length of output string (0 for no limit).
     * @return  result with escaped characters and length limited;
     *          "null" if input was null.
     */
    public static String cleanForPrinting(String input, int maxLen) {
        if (input == null) {
            return "null";
        }

        StringBuilder buf = new StringBuilder();
        StringTokenizer st = new StringTokenizer(input, "\r\n\t\b\f", true);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (s.equals("\t")) {
                buf.append("\\t");
            } else if (s.equals("\n")) {
                buf.append("\\n");
            } else if (s.equals("\b")) {
                buf.append("\\b");
            } else if (s.equals("\f")) {
                buf.append("\\f");
            } else if (s.equals("\r")) {
                buf.append("\\r");
            } else {
                buf.append(s);
            }
        }

        if (maxLen > 0 && buf.length() > maxLen) {
            // Cut out a portion in the middle to shorten the length.
            String pfx = buf.substring(0, maxLen / 2 - 3);
            String sfx = buf.substring(buf.length() - maxLen / 2 + 2);
            buf.setLength(0);
            buf.append(pfx);
            buf.append("<...>");
            buf.append(sfx);
        }
        return buf.toString();
    }

    /**
     * Return a string representation of an exception, which should
     * include the class, message, and stack trace (as returned by
     * calling <code>printStackTrace</code> on the exception).
     *
     * @param  t  throwable, may be null.
     * @return  throwable details.
     */
    public static String exceptionToString(Throwable t) {
        StringWriter sw = new StringWriter(256);
        if (t != null) {
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    /**
     * Converts the list to a comma-separated String of values.
     *
     * @param  list  list to convert to string.
     * @return  String representing the list; empty if list is empty;
     *          null if list is null.
     */
    public static String listToString(List<?> list) {
        return listToString(list, ", ");
    }

    /**
     * Converts the list to a String separated by the given string.
     *
     * @param  list  list to convert to string.
     * @param  sep   string separator to use.
     * @return  String representing the list; empty if list is empty;
     *          null if list is null.
     */
    public static String listToString(List<?> list, String sep) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = list.iterator();
        if (iter.hasNext()) {
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(sep);
                sb.append(iter.next());
            }
        }
        return sb.toString();
    }

    /**
     * Converts the String of comma-separated values to a list of
     * String elements. The string elements are trimmed (leading and
     * trailing whitespace is removed) before being added to the list.
     *
     * @param  str  string to convert to list.
     * @return  list of String values; empty if str is empty;
     *          null if str is null.
     */
    public static List<String> stringToList(String str) {
        return stringToList(str, ",");
    }

    /**
     * Converts the String of separated values to a list of String elements.
     * The string elements are trimmed (leading and trailing whitespace is
     * removed) before being added to the list.
     *
     * @param  str  string to convert to list.
     * @param  sep  entry separator (used as tokenizer delimiter).
     * @return  list of String values; empty if str is empty;
     *          null if str is null.
     */
    public static List<String> stringToList(String str, String sep) {
        if (str == null) {
            return null;
        }
        List<String> list = new LinkedList<String>();
        StringTokenizer tokenizer = new StringTokenizer(str, sep);
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken().trim());
        }
        return list;
    }

    /**
     * Converts the given integer to a four-digit, hexadecimal string,
     * padding with zeros as needed.
     *
     * @param  i  integer to convert.
     * @return  String representation.
     */
    public static String toHexString(int i) {
        return toHexString(i, 4);
    }

    /**
     * Converts the given integer to a hexadecimal string, padding with
     * zeros as needed.
     *
     * @param  i  integer to convert.
     * @param  w  width in characters, padded with zeros.
     * @return  String representation.
     */
    public static String toHexString(int i, int w) {
        String hex = Integer.toHexString(i);
        if (hex.length() < w) {
            char[] zeros = new char[w - hex.length()];
            Arrays.fill(zeros, '0');
            hex = new String(zeros) + hex;
        }
        return hex;
    }

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
        if (str != null && str.trim().length() > 0) {
            char fch = str.charAt(0);
            int last = str.length() - 1;
            char lch = str.charAt(last);
            if ((fch == '"' || fch == '\'') && fch == lch) {
                return str.substring(1, last);
            }
        }
        return str;
    }
}
