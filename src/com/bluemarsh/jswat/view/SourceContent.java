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
 * MODULE:      View
 * FILE:        SourceContent.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/08/01        Initial version
 *      nf      03/28/02        Fixed bug 413 (contributed by masaru o.)
 *
 * $Id: SourceContent.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.io.CharArrayWriter;
import javax.swing.text.BadLocationException;

/**
 * Class SourceContent holds the content for a source view.
 *
 * @author  Nathan Fiedler
 */
public class SourceContent {
    /** Array of start-of-line offsets. */
    private int[] lineStartOffset;
    /** Array of end-of-line offsets. */
    private int[] lineEndOffset;
    /** Array of end-of-line sizes. */
    private int[] lineEndSize;
    /** Number of lines in content. */
    private int lineCount;
    /** The text buffer, sized to fit. */
    private char[] textBuffer;
    /** Length in characters of the longest line. */
    private int longestLineLength;

    /**
     * Constructs a SourceContent.
     *
     * @param  buf  array of text, sized to fit (may be null).
     */
    public SourceContent(char[] buf) {
        textBuffer = buf;
        lineStartOffset = new int[500];
        lineEndOffset = new int[500];
        lineEndSize = new int[500];
        // First line offset is obvious.
        lineStartOffset[0] = 0;
        lineEndOffset[0] = 0;
        lineCount = 1;
        if (buf != null) {
            findLines(buf);
        }
    } // SourceContent

    /**
     * Add another entry into the offset array.
     *
     * @param  start    start-of-line offset to add.
     * @param  eolsize  number of end-of-line characters (1 or 2).
     */
    protected void addLineOffset(int start, int eolsize) {
        if (lineCount == lineStartOffset.length) {
            int[] newArray = new int[lineStartOffset.length * 2];
            System.arraycopy(lineStartOffset, 0, newArray, 0,
                             lineStartOffset.length);
            lineStartOffset = newArray;

            newArray = new int[lineEndOffset.length * 2];
            System.arraycopy(lineEndOffset, 0, newArray, 0,
                             lineEndOffset.length);
            lineEndOffset = newArray;

            newArray = new int[lineEndSize.length * 2];
            System.arraycopy(lineEndSize, 0, newArray, 0,
                             lineEndSize.length);
            lineEndSize = newArray;
        }
        lineStartOffset[lineCount] = start;
        lineEndOffset[lineCount - 1] = start;
        lineEndSize[lineCount - 1] = eolsize;
        lineCount++;

        // Keep the longest line length up to date.
        int thisLineLength = start - lineStartOffset[lineCount - 2];
        if (thisLineLength > longestLineLength) {
            longestLineLength = thisLineLength;
        }
    } // addLineOffset

    /**
     * Determines the line information.
     *
     * @param  text  the text to analyze.
     */
    protected void findLines(char[] text) {
        int ii = 0;
        int strlen = text.length;
        while (ii < strlen) {
            // Look for \r, \n, or \r\n sequences in str.
            char ch = text[ii];
            ii++;

            // For each one found, add the offset into the array.
            if (ch == '\r') {
                if (ii < strlen) {
                    ch = text[ii];
                    ii++;
                    if (ch == '\n') {
                        // DOS end of line.
                        addLineOffset(ii, 2);
                    } else if (ch == '\r') {
                        // Found a blank Macintosh line.
                        addLineOffset(ii - 1, 1);
                        addLineOffset(ii, 1);
                    } else {
                        // Macintosh end of line.
                        addLineOffset(ii - 1, 1);
                    }
                } else {
                    // Must be a Macintosh end of line.
                    addLineOffset(ii, 1);
                }
            } else if (ch == '\n') {
                // Unix end of line.
                addLineOffset(ii, 1);
            }
        }

        // Set the last line's end offset and size.
        int last = lineCount - 1;
        lineEndOffset[last] = strlen;
        lineEndSize[last] = 0;
        for (ii = strlen - 1 ; ii >= lineStartOffset[last] ; ii--) {
            char ch = text[ii];
            if (ch != '\r' && ch != '\n') {
                break;
            }
            lineEndSize[last]++;
        }
    } // findLines

    /**
     * Returns the length of this content, in characters.
     *
     * @return  number of characters in this content.
     */
    public int getLength() {
        return textBuffer.length;
    } // getLength

    /**
     * Returns the number of lines in this content.
     * Lines are sequences of text separated by carriage return,
     * line feed, or the pair of carriage return and line feed.
     *
     * @return  line count.
     */
    public int getLineCount() {
        return lineCount;
    } // getLineCount

    /**
     * Get the offset of the end of the given line. This does not include
     * the end-of-line characters for the given line.
     *
     * @param  line  zero-based line for which to find the end.
     * @return  offset of end of line.
     * @throws  BadLocationException
     *          if line is invalid.
     */
    public int getLineEndOffset(int line) throws BadLocationException {
        if (line > -1 && line < lineCount) {
            return lineEndOffset[line] - lineEndSize[line];
        }
        throw new BadLocationException("no such line", line);
    } // getLineEndOffset

    /**
     * Find the line containing the given offset.
     *
     * @param  offset  offset within content >= 0.
     * @return  zero-based line containing that offset.
     * @throws  BadLocationException
     *          if offset is invalid.
     */
    public int getLineOfOffset(int offset) throws BadLocationException {
        // Walk the array looking for an offset greater than
        // that which was given.
        for (int ii = 0; ii < lineCount; ii++) {
            if (lineEndOffset[ii] >= offset) {
                return ii;
            }
        }
        throw new BadLocationException("no such offset", offset);
    } // getLineOfOffset

    /**
     * Get the offset of the start of the given line.
     *
     * @param  line  zero-based line for which to find the start.
     * @return  offset of start of line.
     * @throws  BadLocationException
     *          if line is invalid.
     */
    public int getLineStartOffset(int line) throws BadLocationException {
        if (line > -1 && line < lineCount) {
            return lineStartOffset[line];
        }
        throw new BadLocationException("no such line", line);
    } // getLineStartOffset

    /**
     * Returns the length in characters of the longest line in this
     * content object.
     *
     * @return  longest line lenght.
     */
    public int getLongestLineLength() {
        return longestLineLength;
    } // getLongestLineLength

    /**
     * Returns the actual character buffer. Changes should not be made
     * directly to this buffer. It is provided for efficiency only.
     *
     * @return  character buffer.
     */
    public char[] getBuffer() {
        return textBuffer;
    } // getBuffer

    /**
     * Find and replace tabs with spaces.
     *
     * @param  str       string on which to operate.
     * @param  tabWidth  width of tabs.
     * @return  result of replacement.
     */
    public static char[] replaceTabs(char[] str, int tabWidth) {
        boolean tabsFound = false;
        for (int ii = 0; ii < str.length; ii++) {
            if (str[ii] == '\t') {
                tabsFound = true;
                break;
            }
        }
        if (tabsFound) {
            // There's tabs in that thar string.
            int strlen = str.length;
            CharArrayWriter caw = new CharArrayWriter(strlen * 2);
            int ii = 0;
            int column = 0;
            while (ii < strlen) {
                char ch = str[ii];
                if (ch == '\t') {

                    // Add the appropriate number of spaces.
                    int spaces = tabWidth - (column % tabWidth);
                    for (int jj = 0; jj < spaces; jj++) {
                        caw.write(' ');
                    }
                    column += spaces;
                } else if ((ch == '\r') || (ch == '\n')) {
                    caw.write(ch);
                    column = 0;
                } else {
                    caw.write(ch);
                    column++;
                }
                ii++;
            }
            str = caw.toCharArray();
            caw.close();
        }
        return str;
    } // replaceTabs
} // SourceContent
