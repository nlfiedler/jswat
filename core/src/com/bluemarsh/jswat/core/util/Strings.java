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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Strings.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    /** Array of zeros for padding numbers. */
    private static String[] zeros;

    static {
        zeros = new String[32];
        StringBuilder sb = new StringBuilder();
        for (int ii = 0; ii < zeros.length; ii++) {
            sb.append('0');
            zeros[ii] = sb.toString();
        }
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
        StringWriter sw = new StringWriter(512);
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
    public static String listToString(List list) {
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
    public static String listToString(List list, String sep) {
        if (list == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator iter = list.iterator();
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
            throw new IllegalArgumentException("'w' is out of range");
        }
        String hex = Integer.toHexString(i);
        if (hex.length() < w) {
            hex = zeros[w - 1 - hex.length()].concat(hex);
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
        if (str != null && str.length() > 0) {
            char fch = str.charAt(0);
            int last = str.length() - 1;
            char lch = str.charAt(last);
            if ((fch == '"' || fch == '\'') && fch == lch) {
                str = str.substring(1, last);
            }
        }
        return str;
    }
}
