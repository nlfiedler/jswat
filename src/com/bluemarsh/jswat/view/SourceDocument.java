/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: SourceDocument.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.io.PrintStream;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

/**
 * Class SourceDocument holds the content for a styled source view.
 * It extends the default styled document and adds line-awareness,
 * such that the number of lines, and line offsets can be queried.
 *
 * @author  Nathan Fiedler
 */
public class SourceDocument extends DefaultStyledDocument {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Array of start-of-line offsets. */
    private int[] lineStartOffset;
    /** Array of end-of-line offsets. */
    private int[] lineEndOffset;
    /** Number of lines in document. */
    private int lineCount;

    /**
     * Constructs a SourceDocument.
     */
    public SourceDocument() {
        lineStartOffset = new int[50];
        lineEndOffset = new int[50];
        // First line offset is obvious.
        lineStartOffset[0] = 0;
        lineEndOffset[0] = 0;
        lineCount++;
    } // SourceDocument

    /**
     * Add another entry into the offset array.
     *
     * @param  start  start-of-line offset to add.
     */
    private void addLineOffset(int start) {
        if (lineCount == lineStartOffset.length) {
            increaseCapacity();
        }
        lineStartOffset[lineCount] = start;
        lineEndOffset[lineCount - 1] = start;
        lineCount++;
    } // addLineOffset

    /**
     * Prints diagnostic information to the given output stream.
     *
     * @param  out  stream to dump to.
     */
    public void dump(PrintStream out) {
        try {
            out.print("Line count: ");
            out.println(getLineCount());
            out.println("Line offsets: ");
            out.print("   0: ");
            out.print(getLineStartOffset(0));
            out.print(", ");
            out.println(getLineEndOffset(0));
            for (int ii = 1; ii < lineCount; ii++) {
                if (ii < 10) {
                    out.print("   ");
                } else if (ii < 100) {
                    out.print("  ");
                } else if (ii < 1000) {
                    out.print(" ");
                }
                out.print(ii);
                out.print(": ");
                out.print(getLineStartOffset(ii));
                out.print(", ");
                out.println(getLineEndOffset(ii));
            }
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    } // dump

    /**
     * Returns the number of lines in this document.
     * Lines are sequences of text separated by carriage return,
     * line feed, or the pair of carriage return and line feed.
     *
     * @return  line count.
     */
    public int getLineCount() {
        return lineCount;
    } // getLineCount

    /**
     * Get the offset of the end of the given line.
     *
     * @param  line  zero-based line for which to find the end.
     * @return  offset of end of line.
     * @exception  BadLocationException
     *             Thrown if line is invalid.
     */
    public int getLineEndOffset(int line) throws BadLocationException {
        if (line < lineCount) {
            if (lineEndOffset[line] == 0) {
                // That last line end offset wasn't set yet.
                lineEndOffset[line] = getLength();
            }
            return lineEndOffset[line];
        }
        throw new BadLocationException("no such line", line);
    } // getLineEndOffset

    /**
     * Find the line containing the given offset.
     *
     * @param  offset  offset within document >= 0.
     * @return  zero-based line containing that offset.
     * @exception  BadLocationException
     *             Thrown if offset is invalid.
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
     * @exception  BadLocationException
     *             Thrown if line is invalid.
     */
    public int getLineStartOffset(int line) throws BadLocationException {
        if (line < lineCount) {
            return lineStartOffset[line];
        }
        throw new BadLocationException("no such line", line);
    } // getLineStartOffset

    /**
     * Increase the size of the line-offset array.
     */
    private void increaseCapacity() {
        int[] newArray = new int[lineStartOffset.length * 2];
        System.arraycopy(lineStartOffset, 0, newArray, 0,
                         lineStartOffset.length);
        lineStartOffset = newArray;

        newArray = new int[lineEndOffset.length * 2];
        System.arraycopy(lineEndOffset, 0, newArray, 0,
                         lineEndOffset.length);
        lineEndOffset = newArray;
    } // increaseCapacity

    /**
     * Inserts a string of content. See the superclass documentation
     * for all the details.
     *
     * @param  offset  the offset into the document to insert the content
     *                 >= 0. All positions that track change at or after
     *                 the given location will move.
     * @param  str     the string to insert.
     * @param  a       the attributes to associate with the inserted content.
     *                 This may be null if there are no attributes.
     * @exception  BadLocationException
     *             the given insert position is not a valid position
     *             within the document
     */
    public void insertString(int offset, String str, AttributeSet a)
        throws BadLocationException {

        // Enforce append-only behavior.
        if (offset < getLength()) {
            throw new BadLocationException("offset must equal length", offset);
        }

        super.insertString(offset, str, a);
        int ii = 0;
        int strlen = str.length();
        while (ii < strlen) {
            // Look for \r, \n, or \r\n sequences in str.
            // String.charAt() is a little slower but memory efficient.
            char ch = str.charAt(ii);
            ii++;

            // For each one found, add the offset into the array
            // using 'offset' plus the offset into str.
            if (ch == '\r') {
                if (ii < strlen) {
                    ch = str.charAt(ii);
                    ii++;
                    if (ch == '\n') {
                        // DOS end of line.
                        addLineOffset(offset + ii);
                    } else if (ch == '\r') {
                        // Found a blank Unix line.
                        addLineOffset(offset + ii - 1);
                        addLineOffset(offset + ii);
                    } else {
                        // Macintosh end of line.
                        addLineOffset(offset + ii - 1);
                    }
                } else {
                    // Have to assume Macintosh end of line.
                    addLineOffset(offset + ii);
                }
            } else if (ch == '\n') {
                // Unix end of line.
                addLineOffset(offset + ii);
            }
        }
    } // insertString

    /**
     * Find and replace tabs with spaces.
     *
     * @param  str       string on which to operate.
     * @param  tabWidth  width of tabs.
     * @return  result of replacement.
     */
    public static String replaceTabs(String str, int tabWidth) {
        // Work-around to Java bug 4188841.

        // I would have done this in scanner/java.flex but JFlex
        // produces widely invalid yycolumn values. So, we have
        // to do the whole thing ourselves.
        if (str.indexOf('\t') > -1) {
            // There's tabs in that there string.
            int strlen = str.length();
            StringBuffer buf = new StringBuffer(strlen * 2);
            int ii = 0;
            int column = 0;
            while (ii < strlen) {
                char ch = str.charAt(ii);
                if (ch == '\t') {

                    // Add the appropriate number of spaces.
                    int spaces = tabWidth - (column % tabWidth);
                    for (int jj = 0; jj < spaces; jj++) {
                        buf.append(' ');
                    }
                    column += spaces;
                } else if ((ch == '\r') || (ch == '\n')) {
                    buf.append(ch);
                    column = 0;
                } else {
                    buf.append(ch);
                    column++;
                }
                ii++;
            }
            str = buf.toString();
        }
        return str;
    } // replaceTabs
} // SourceDocument
