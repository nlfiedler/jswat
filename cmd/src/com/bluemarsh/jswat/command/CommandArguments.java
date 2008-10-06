/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandArguments.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import java.util.NoSuchElementException;

/**
 * Class <code>CommandArguments</code> provides functionality for parsing
 * the arguments to a command. Arguments are separated by spaces only. All
 * non-space characters are considered arguments. Arguments may be enclosed
 * inside single or double-quotes, in which case the quoted argument is
 * returned, without the surrounding quotes. Quote characters may be escaped
 * using a backward slash (\) character, as in \" or \'. The slashes may be
 * escaped by another slash. The extra slashes are removed.
 *
 * @author  Nathan Fiedler
 */
public class CommandArguments {
    /** The arguments to parse. */
    private String arguments;
    /** Length of arguments in characters. */
    private int lastPosition;
    /** Current position in parsing. */
    private int currPosition;
    /** Cached position of the next non-space, or -1. */
    private int nextNonSpace = -1;
    /** If true, do not perform the usual quotes and escape processing. */
    private boolean returnAsIs;

    /**
     * Constructs a CommandArguments to parse the given arguments.
     *
     * @param  args  arguments to parse.
     */
    public CommandArguments(String args) {
        arguments = args;
        if (args != null) {
            lastPosition = args.length();
        } else {
            lastPosition = 0;
        }
    }

    /**
     * Calculates the number of times that nextToken() method can be
     * called before it generates an exception.
     *
     * @return  the number of tokens remaining in the arguments.
     */
    public int countTokens() {
        int count = 0;
        int curr = currPosition;
        while (curr < lastPosition) {
            // Skip spaces.
            curr = skipSpaces(curr);
            if (curr >= lastPosition) {
                break;
            }
            // Scan argument.
            curr = scanArgument(curr);
            // Increment count.
            count++;
        }
        return count;
    }

    /**
     * Test if there are more tokens available from the arguments.
     *
     * @return  true if and only if there is at least one token in the
     *          arguments after the current position; false otherwise.
     */
    public boolean hasMoreTokens() {
        nextNonSpace = skipSpaces(currPosition);
        return nextNonSpace < lastPosition;
    }

    /**
     * Returns the next token from the arguments. Deals with quotes,
     * escaped quotes, and escaped escape-characters (slash). Removes
     * the surrounding quotes, if any, before returning the argument.
     * Escaped quotes are unescaped before the argument is returned.
     *
     * @return  the next token from the arguments.
     */
    public String nextToken() {
        // Skip over spaces.
        currPosition = nextNonSpace >= 0
            ? nextNonSpace : skipSpaces(currPosition);
        nextNonSpace = -1;
        if (currPosition == lastPosition) {
            throw new NoSuchElementException("reached end of arguments");
        }

        // Find end of argument.
        int start = currPosition;
        currPosition = scanArgument(currPosition);
        int end = currPosition;
        char sch = arguments.charAt(start);
        char ech = arguments.charAt(end - 1);

        // Remove surrounding quotes.
        if (!returnAsIs && (sch == '"' || sch == '\'')) {
            if (ech == sch && (end - start) > 1) {
                start++;
                end--;
            } else {
                throw new IllegalArgumentException("missing ending quote");
            }
        }

        String n = arguments.substring(start, end);
        if (!returnAsIs && n.indexOf('\\') > -1) {
            // Remove the slashes (but not the character after it).
            n = translateEscapes(n);
        }

        return n;
    }

    /**
     * Returns the next token from the arguments, without advancing
     * the current position.
     *
     * @return  the next token from the arguments.
     */
    public String peek() {
        int pos = currPosition;
        String t = nextToken();
        currPosition = pos;
        return t;
    }

    /**
     * Return the command arguments to its initial state, as if it
     * the nextToken() method had never been called. Also sets the
     * return-as-is flag to false.
     */
    public void reset() {
        currPosition = 0;
        nextNonSpace = -1;
        returnAsIs = false;
    }

    /**
     * Returns the rest of the string of arguments, using the current
     * position as the starting point.
     *
     * @return  the rest of the string of arguments.
     */
    public String rest() {
        String n = arguments.substring(currPosition).trim();
        if (!returnAsIs
            && (n.indexOf('\'') > -1 || n.indexOf('"') > -1
                || n.indexOf('\\') > -1)) {
            // Remove quotes and translate escaped quotes and slashes.
            n = translateEscapes(n);
        }
        return n;
    }

    /**
     * Enable or disable the "return as-is" feature, in which the
     * arguments returned are given as-is, without any extra
     * processing. In particular, any quotes are left in place, as
     * well as any backward slash escape sequences.
     *
     * @param  asis  true to disable extra processing.
     */
    public void returnAsIs(boolean asis) {
        returnAsIs = asis;
    }

    /**
     * Finds the end of the argument, taking into consideration
     * quotes and escaped quotes.
     *
     * @param  pos  position from which to start scanning.
     * @return  the offset of the end of the argument, or lastPosition
     *          if end of arguments is reached.
     */
    protected int scanArgument(int pos) {
        // Use a simple finite-state machine to parse the input.
        byte state = 0;
        char ch = '\0';
        while (pos < lastPosition) {
            ch = arguments.charAt(pos);
            switch (state) {
            case 0:
                // Not inside a quoted string.
                if (ch == '"') {
                    state = 1;
                } else if (ch == '\'') {
                    state = 2;
                } else if (ch == '\\') {
                    state = 3;
                } else if (Character.isSpaceChar(ch)) {
                    // Found end of argument.
                    // Return now so we don't increment this again.
                    return pos;
                }
                break;

            case 1:
                // Inside a double-quoted string.
                if (ch == '"') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 4;
                }
                break;

            case 2:
                // Inside a single-quoted string.
                if (ch == '\'') {
                    state = 0;
                } else if (ch == '\\') {
                    state = 5;
                }
                break;

            case 3:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 0;
                break;

            case 4:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 1;
                break;

            case 5:
                // Previous character was a slash.
                // Simply skip the character and move on.
                state = 2;
                break;
            default:
                throw new IllegalStateException("scanner got confused");
            }
            pos++;
        }
        return pos;
    }

    /**
     * Skips over space characters and returns the offset of the next
     * non-space character.
     *
     * @param  pos  position from which to start skipping.
     * @return  the offset of the next non-space, or lastPosition if
     *          end of arguments is reached.
     */
    protected int skipSpaces(int pos) {
        // Skip over space characters.
        while (pos < lastPosition) {
            char c = arguments.charAt(pos);
            if (!Character.isSpaceChar(c)) {
                break;
            }
            pos++;
        }
        return pos;
    }

    /**
     * Removes unescaped quotes. Translates escaped quotes to just quotes,
     * and translates escaped backward slashes to just backward slashes.
     *
     * @param  str  the string.
     * @return  the string processed.
     */
    protected String translateEscapes(String str) {
        int size = str.length();
        StringBuilder sb = new StringBuilder(size);
        char ch = '\0';
        for (int ii = 0; ii < size; ii++) {
            char prevch = ch;
            ch = str.charAt(ii);
            if (ch == '"' || ch == '\'') {
                if (prevch == '\\') {
                    // Remove the previous backward slash, and
                    // reset the previous character.
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(ch);
                    ch = '\0';
                }
                // Else, the quote is discarded.
            } else if (ch == '\\') {
                if (prevch != '\\') {
                    sb.append(ch);
                } else {
                    // Else, the second slash is discarded, and
                    // we reset the previous character.
                    ch = '\0';
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
