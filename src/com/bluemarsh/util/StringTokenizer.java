/********************************************************************
 *
 *	Copyright (C) 2000 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     Utilities
 * MODULE:      Util
 * FILE:        StringTokenizer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *	Name	Date		Description
 *      ----    ----            -----------
 *      NF      07/16/00        Initial version
 *
 * DESCRIPTION:
 *      See the class documentation for more details.
 *
 * $Id: StringTokenizer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 *******************************************************************/

package com.bluemarsh.util;

import java.util.NoSuchElementException;

/**
 * Class StringTokenizer is API-compatible with the
 * <code>java.util.StringTokenizer</code> class, with some additional
 * functionality and improvements.
 *
 * @author  Nathan Fiedler
 * @version 1.0  7/16/00
 */
public class StringTokenizer implements Cloneable {
    /** The string to be tokenized. */
    protected String string;
    /** Length of <code>string</code> in characters. */
    protected int lastPosition;
    /** Current position within <code>string</code>. */
    protected int currPosition;
    /** Pre-computed position of next non-delimiter character, or -1. */
    protected int nextNonDelim;
    /** The token delimitors, one character each. */
    protected String delimiters;
    /** Smallest delimiter character value. */
    protected char minDelimChar;
    /** Largest delimiter character value. */
    protected char maxDelimChar;
    /** True to return the delimiters themselves. */
    protected boolean returnDelims;
    /** The default delimiters. */
    protected static final String defaultDelims = " \t\n\r\f";

    /**
     * Constructs a string tokenizer for the specified string.
     * The tokenizer uses the default delimiter set, which is
     * <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>: the space
     * character, the tab character, the newline character, the
     * carriage-return character, and the form-feed character.
     * Delimiter characters themselves will not be treated as tokens.
     *
     * @param  str  a string to be parsed.
     */
    public StringTokenizer(String str) {
	this(str, null, false);
    } // StringTokenizer

    /**
     * Constructs a string tokenizer for the specified string. The
     * characters in the <code>delim</code> argument are the delimiters
     * for separating tokens. Delimiter characters themselves will not
     * be treated as tokens.
     *
     * @param  str    a string to be parsed.
     * @param  delim  the delimiters.
     */
    public StringTokenizer(String str, String delim) {
	this(str, delim, false);
    } // StringTokenizer

    /**
     * Constructs a string tokenizer for the specified string. All
     * characters in the <code>delim</code> argument are the delimiters
     * for separating tokens.
     * <p>
     * If the <code>returnDelims</code> flag is <code>true</code>, then
     * the delimiter characters are also returned as tokens. Each
     * delimiter is returned as a string of length one. If the flag is
     * <code>false</code>, the delimiter characters are skipped and only
     * serve as separators between tokens.
     *
     * @param  str           a string to be parsed.
     * @param  delim         the delimiters. If null, uses defaults.
     * @param  returnDelims  flag indicating whether to return the
     *                       delimiters as tokens.
     */
    public StringTokenizer(String str, String delim, boolean returnDelims) {
        string = str;
        setDelimiters(delim);
        this.returnDelims = returnDelims;
        if (str != null) {
            lastPosition = str.length();
        } else {
            lastPosition = 0;
        }
        currPosition = 0;
    } // StringTokenizer

    /**
     * Creates and returns a copy of this object.
     *
     * @return  a clone of this instance.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            // Won't happen.
            return null;
        }
    } // clone

    /**
     * Calculates the number of times that this tokenizer's
     * <code>nextToken</code> method can be called before it
     * generates an exception. The current position is not advanced.
     *
     * @return  the number of tokens remaining in the string using the
     *          current delimiter set.
     * @see  #nextToken()
     */
    public int countTokens() {
        int count = 0;
        int curr = currPosition;
        while (curr < lastPosition) {
            // Skip delimiters.
            curr = skipDelimiters(curr);
            if (curr >= lastPosition) {
                break;
            }
            // Scan token.
            curr = scanToken(curr);
            // Increment count.
            count++;
        }
        return count;
    } // countTokens

    /**
     * Returns the set of delimiters used by this tokenizer.
     *
     * @return  delimiters being used.
     */
    public String getDelimiters() {
        return delimiters;
    } // getDelimiters

    /**
     * Returns the same value as the <code>hasMoreTokens</code>
     * method. It exists so that this class can implement the
     * <code>Enumeration</code> interface.
     *
     * @return  <code>true</code> if there are more tokens;
     *          <code>false</code> otherwise.
     * @see  java.util.Enumeration
     * @see  #hasMoreTokens()
     */
    public boolean hasMoreElements() {
	return hasMoreTokens();
    } // hasMoreElements

    /**
     * Tests if there are more tokens available from this tokenizer's
     * string. If this method returns <code>true</code>, then a subsequent
     * call to <code>nextToken</code> with no argument will successfully
     * return a token.
     *
     * @return  <code>true</code> if and only if there is at least one token
     *          in the string after the current position; <code>false</code>
     *          otherwise.
     */
    public boolean hasMoreTokens() {
        nextNonDelim = skipDelimiters(currPosition);
        return nextNonDelim < lastPosition;
    } // hasMoreTokens

    /**
     * Returns the same value as the <code>nextToken</code> method,
     * except that its declared return value is <code>Object</code>
     * rather than <code>String</code>. It exists so that this class
     * can implement the <code>Enumeration</code> interface. 
     *
     * @return  the next token in the string.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     * @see  java.util.Enumeration
     * @see  #nextToken()
     */
    public Object nextElement() {
	return nextToken();
    } // nextElement

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return  the next token from this string tokenizer.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     */
    public String nextToken() {
        // Skip delimiters, possibly using pre-computed value.
        currPosition = nextNonDelim >= 0 ?
            nextNonDelim : skipDelimiters(currPosition);
        nextNonDelim = -1;

        // Check for no more tokens.
        if (currPosition >= lastPosition) {
            throw new NoSuchElementException();
        }

        // Scan token and save the next current position.
        int start = currPosition;
        currPosition = scanToken(currPosition);
        // Return token substring.
        return string.substring(start, currPosition);
    } // nextToken

    /**
     * Returns the next token in this string tokenizer's string.
     * First, the set of characters considered to be delimiters by this
     * <code>StringTokenizer</code> object is changed to be the characters
     * in the string <code>delim</code>. Then the next token in the string
     * after the current position is returned. The current position is
     * advanced beyond the recognized token. The new delimiter set
     * remains the default after this call.
     *
     * @param  delim  the new delimiters. If null, uses defaults.
     * @return  the next token, after switching to the new delimiter set.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     */
    public String nextToken(String delim) {
        setDelimiters(delim);
        return nextToken();
    } // nextToken

    /**
     * Like <code>nextToken()</code>, this returns the next token from
     * this string tokenizer. Hoewever, the current position is not
     * advanced.
     *
     * @return  the next token from this string tokenizer.
     */
    public String peek() {
        int savePosition = currPosition;
        String s = nextToken();
        currPosition = savePosition;
        return s;
    } // peek

    /**
     * Returns the rest of the tokenizer's string, including delimiters.
     * This does not change the current position.
     *
     * @return  Rest of tokenizer's string.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     */
    public String rest() {
        return rest(false);
    } // rest

    /**
     * Returns the rest of the tokenizer's string.
     * This does not change the current position.
     *
     * @param  trim  True to remove leading and trailing delimiters.
     * @return  Rest of tokenizer's string.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     */
    public String rest(boolean trim) {
        if (!trim) {
            // Simply return everything we've got.
            return string.substring(currPosition);
        }

        // Skip delimiters, possibly using pre-computed value.
        currPosition = nextNonDelim >= 0 ?
            nextNonDelim : skipDelimiters(currPosition);
        nextNonDelim = -1;

        // Check for no more tokens.
        if (currPosition >= lastPosition) {
            throw new NoSuchElementException();
        }

        // Find the last non-delimiter character.
        int start = currPosition;
        int last = lastPosition - 1;
        while (last > start) {
            char c = string.charAt(last);
            if ((c < minDelimChar) || (c > maxDelimChar) ||
                (delimiters.indexOf(c) < 0)) {
                // character is not a delimiter
                break;
            }
            last--;
        }
        // Return token substring.
        return string.substring(start, last + 1);
    } // rest

    /**
     * Returns the rest of the tokenizer's string, removing any
     * leading and trailing whitespace.
     * This does not change the current position.
     *
     * @return  Rest of tokenizer's string.
     * @exception  NoSuchElementException
     *             If there are no more tokens in this tokenizer's string.
     */
    public String restTrim() {
        // Find the first non-whitespace character.
        int start = currPosition;
        while (start < lastPosition) {
            if (!Character.isWhitespace(string.charAt(start))) {
                // character is not whitespace
                break;
            }
            start++;
        }

        // Check for no more tokens.
        if (currPosition >= lastPosition) {
            throw new NoSuchElementException();
        }

        // Find the last non-whitespace character.
        int last = lastPosition - 1;
        while (last > start) {
            if (!Character.isWhitespace(string.charAt(last))) {
                // character is not a delimiter
                break;
            }
            last--;
        }
        // Return token substring.
        return string.substring(start, last + 1);
    } // restTrim

    /**
     * Scans ahead looking for non-delimiters in the string.
     *
     * @param  startPos  Position from which to start skipping.
     *                   Should never be equal to lastPosition.
     * @return  the index of the next delimiter character encountered,
     *          or lastPosition if no such delimiter is found.
     */
    protected int scanToken(int startPos) {
        int pos = startPos;
        // Skip over non-delimiter characters.
        while (pos < lastPosition) {
            char c = string.charAt(pos);
            if ((c >= minDelimChar) && (c <= maxDelimChar) &&
                (delimiters.indexOf(c) >= 0)) {
                // character is a delimiter
                break;
            }
            pos++;
        }

        // Handle case of returning delimiters.
        if (returnDelims && (startPos == pos)) {
            // skip over the delimiter
            pos++;
        }
        return pos;
    } // scanToken

    /**
     * Set the delimiters for the tokenizer.
     *
     * @param  delim  token delimiters to use.
     */
    public void setDelimiters(String delim) {
        if (delim != null) {
            delimiters = delim;
        } else {
            delimiters = defaultDelims;
        }
        setDelimRange();
        // Reset this because we have new delimiters now.
        nextNonDelim = -1;
    } // setDelimiters

    /**
     * Used to set the minimum and maximum delimiter character values.
     * These are used as a hueristic to speed up the string tokenizing.
     */
    protected void setDelimRange() {
        char min = Character.MAX_VALUE;
        char max = Character.MIN_VALUE;
        // Must be sure we handle case of empty delimiter string.
        for (int i = 0; i < delimiters.length(); i++) {
            char c = delimiters.charAt(i);
            if (c < min) {
                min = c;
            }
            if (c > max) {
                max = c;
            }
        }
        minDelimChar = min;
        maxDelimChar = max;
    } // setDelimRange

    /**
     * Skips over the delimiters in the string.
     *
     * @param  pos  Position from which to start skipping.
     * @return  the position of the next non-delimiter or lastPosition,
     *          if <code>returnDelims</code> is false; otherwise, returns
     *          <code>pos</code> as given.
     */
    protected int skipDelimiters(int pos) {
        if (!returnDelims) {
            while (pos < lastPosition) {
                char c = string.charAt(pos);
                if ((c < minDelimChar) || (c > maxDelimChar) ||
                    (delimiters.indexOf(c) < 0)) {
                    // character is not a delimiter
                    break;
                }
                pos++;
            }
        }
        return pos;
    } // skipDelimiters
} // StringTokenizer
