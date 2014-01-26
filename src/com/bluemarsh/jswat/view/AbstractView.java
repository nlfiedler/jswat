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
 * PROJECT:     JSwat
 * MODULE:      View
 * FILE:        AbstractView.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/17/01        Initial version
 *      nf      11/11/01        Fixed bug 283
 *      nf      01/06/02        Fixed bugs 379, 381, 382
 *      nf      05/07/02        Implemented RFE 470
 *      nf      10/21/02        Fixed bug 637
 *
 * $Id: AbstractView.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;

/**
 * Class AbstractView provides the basic support for displaying textual
 * data in a scrollable, searchable area.
 *
 * @author  Nathan Fiedler
 */
abstract class AbstractView implements View {
    /** Logger. */
    protected static Logger logger;
    /** The title of our view, used for reporting. */
    protected String viewTitle;
    /** Text area for displaying the source. */
    protected SourceViewTextArea textComponent;
    /** Text from the text component. Used in the findString() method. */
    protected char[] viewContent;
    /** Highlighter used to highlight the current stepping line.
     * Subclasses must add this to the text area's draw layers. */
    protected SteppingLineDrawLayer lineHighlighter;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.view");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Creates a AbstractView object.
     *
     * @param  title  title of the view, for reporting purposes.
     */
    public AbstractView(String title) {
        this.viewTitle = title;

        // This is used by showHighlight() to highlight a line.
        lineHighlighter = new SteppingLineDrawLayer();
    } // AbstractView

    /**
     * Look for the given string in the source view's text area. Uses
     * the text area's current selection as the starting point. Will
     * wrap around if the string was not found after the current
     * selection.
     *
     * @param  str         string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found somewhere, false if string
     *          does not exist in this view.
     */
    public boolean findString(String str, boolean ignoreCase) {
        // Prepare for the search.
        CharArraySequence seq = new CharArraySequence(viewContent);
        Pattern patt = null;
        if (ignoreCase) {
            patt = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
        } else {
            patt = Pattern.compile(str);
        }
        Matcher matcher = patt.matcher(seq);

        // Search for the string.
        if (logger.isLoggable(Level.INFO)) {
            logger.info("searching for '" + str + "' in " + viewTitle);
        }
        if (matcher.find(textComponent.getSelectionEnd())) {
            foundString(matcher.start(), matcher.end());
        } else {

            // Not found - wrap around to the beginning and try again.
            if (logger.isLoggable(Level.INFO)) {
                logger.info("'" + str + "' not found, wrapping in "
                            + viewTitle);
            }
            if (matcher.find(0)) {
                foundString(matcher.start(), matcher.end());
            } else {
                // String was not found anywhere.
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("'" + str + "' not found in "
                                + viewTitle);
                }
                return false;
            }
        }
        return true;
    } // findString

    /**
     * Show that we found the string we were looking for.
     *
     * @param  start  start offset of matched string.
     * @param  end    end offset of matched string.
     */
    protected void foundString(int start, int end) {
        // Select the string we found (have to set it visible).
        textComponent.select(start, end);
        scrollToRange(start, end);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("found pattern at " + start + " in " + viewTitle);
        }
    } // foundString

    /**
     * Get the offset of the end of the given line.
     *
     * @param  line  zero-based line for which to find the end.
     * @return  offset of end of line.
     * @throws  BadLocationException
     *          if line is invalid.
     */
    protected int getLineEndOffset(int line) throws BadLocationException {
        return textComponent.getContent().getLineEndOffset(line);
    } // getLineEndOffset

    /**
     * Find the line containing the given offset.
     *
     * @param  offset  offset within document >= 0.
     * @return  zero-based line containing that offset.
     * @throws  BadLocationException
     *          if offset is invalid.
     */
    protected int getLineOfOffset(int offset) throws BadLocationException {
        return textComponent.getContent().getLineOfOffset(offset);
    } // getLineOfOffset

    /**
     * Get the offset of the start of the given line.
     *
     * @param  line  zero-based line for which to find the start.
     * @return  offset of start of line.
     * @throws  BadLocationException
     *          if line is invalid.
     */
    protected int getLineStartOffset(int line) throws BadLocationException {
        return textComponent.getContent().getLineStartOffset(line);
    } // getLineStartOffset

    /**
     * Returns the long version of title of this view. This may be a
     * file name and path, a fully-qualified class name, or whatever is
     * appropriate for the type of view.
     *
     * @return  long view title.
     */
    public abstract String getLongTitle();

    /**
     * Returns the title of this view. This may be a file name, a class
     * name, or whatever is appropriate for the type of view.
     *
     * @return  view title.
     */
    public String getTitle() {
        return viewTitle;
    } // getTitle

    /**
     * Removes the highlight from the text area, on the AWT event
     * dispatching thread.
     */
    protected void removeHighlight() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (textComponent == null) {
                        // Text area is already gone, do nothing.
                        return;
                    }
                    lineHighlighter.setHighlight(0, 0);
                    textComponent.repaint();
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("removed old highlight in " + viewTitle);
                    }
                }
            });
    } // removeHighlight

    /**
     * Scrolls the source view to the given line, if possible. Any value
     * less than one is ignored.
     *
     * @param  line  line to scroll to (1-based).
     */
    public void scrollToLine(final int line) {
        if (line > 0) {

            if (logger.isLoggable(Level.INFO)) {
                logger.info("scrolling to line " + line + " in " + viewTitle);
            }

            if (textComponent.getVisibleRect().height <= 0) {
                // Apparently the window has not been realized.
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("source view not visible yet for "
                                + viewTitle + ", will scroll in 10ms");
                }
                Timer t = new Timer(10, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            // Maybe the view is visible now.
                            scrollToLine(line);
                        }
                    });
                t.setRepeats(false);
                t.start();
            }

            // Scroll the text area to this line (on the AWT thread).
            SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        if (textComponent == null) {
                            // Text area is already gone, do nothing.
                            return;
                        }
                        Rectangle visible = textComponent.getVisibleRect();

                        // Compute the upper and lower bounds of what
                        // is acceptable.
                        int upper = visible.y + (visible.height / 3);
                        int lower = visible.y - (visible.height / 2);

                        // Get the font height value and create a rectangle
                        // used for scrolling the text area.
                        Font font = textComponent.getFont();
                        FontMetrics metrics =
                            textComponent.getFontMetrics(font);
                        int fontHeight = metrics.getHeight();

                        // Compute the point to scroll to.
                        visible.y = (line - 1) * fontHeight;
                        // Subtract half to center the line in the view.
                        visible.y -= (visible.height / 2);

                        // See if we really should scroll the text area.
                        if ((visible.y < lower) || (visible.y > upper)) {
                            // Check that we're not scrolling past the end.
                            int newbottom = visible.y + visible.height;
                            int textheight = textComponent.getHeight();
                            if (newbottom > textheight) {
                                visible.y -= (newbottom - textheight);
                            }
                            // Perform the text area scroll.
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info(
                                    "scrolling to cooridates "
                                    + visible + " in " + viewTitle);
                            }
                            textComponent.scrollRectToVisible(visible);
                        }
                    }
                });
        }
    } // scrollToLine

    /**
     * Scrolls the source view to the given range of characters. The
     * range of characters may span more than one line.
     *
     * @param  start  first character to make visible.
     * @param  end    last character to make visible.
     */
    public void scrollToRange(final int start, final int end) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("scrolling to range " + start + "-" + end
                + " in " + viewTitle);
        }

        // We expect the text component to be ready at this point.

        // Scroll the text area on the AWT thread.
        SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (textComponent == null) {
                        // Text area is already gone, do nothing.
                        return;
                    }
                    Point p1;
                    Point p2;
                    if (start > end) {
                        p1 = textComponent.modelToView(end);
                        p2 = textComponent.modelToView(start);
                    } else {
                        p1 = textComponent.modelToView(start);
                        p2 = textComponent.modelToView(end);
                    }
                    if (p1 == null || p2 == null) {
                        logger.info("scrolling to invalid area: "
                            + start + "-" + end);
                        return;
                    }

                    // Calculate the rectangle defined by the character range.
                    Font font = textComponent.getFont();
                    FontMetrics metrics = textComponent.getFontMetrics(font);
                    int height = metrics.getHeight();
                    Rectangle rect = new Rectangle();
                    rect.x = p1.x;
                    rect.y = p1.y - height;
                    rect.height = height * 3;
                    rect.width = Math.abs(p2.x - p1.x);
                    textComponent.scrollRectToVisible(rect);
                }
            });
    } // scrollToRange

    /**
     * Highlight the given line in the text area, on the AWT event
     * dispatching thread.
     *
     * @param  line  line in text area to be highlighted.
     */
    protected void showHighlight(final int line) {
        if (textComponent.getVisibleRect().height <= 0) {
            // Apparently the window has not been realized.
            if (logger.isLoggable(Level.INFO)) {
                logger.info("source view not visible yet for "
                            + viewTitle + ", will show highlight in 10ms");
            }
            Timer t = new Timer(10, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        // Maybe the view is visible now.
                        showHighlight(line);
                    }
                });
            t.setRepeats(false);
            t.start();
        }

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (textComponent == null) {
                        // Text area is already gone, do nothing.
                        return;
                    }
                    // Set text highlight.
                    try {
                        int p0 = getLineStartOffset(line);
                        int p1 = getLineEndOffset(line);
                        lineHighlighter.setHighlight(p0, p1);
                        textComponent.repaint();
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("highlighted line "
                                + (line + 1) + " in " + viewTitle);
                        }
                    } catch (BadLocationException ble) {
                        logger.info("BLE while highlighting line "
                            + (line + 1) + " in " + viewTitle);
                    }
                }
            });
    } // showHighlight

    /**
     * Implements the CharSequence interface for character arrays.
     */
    protected class CharArraySequence implements CharSequence {
        /** Our character array. */
        private char[] array;
        /** First valid character offset. */
        private int start;
        /** Last valid character offset. */
        private int end;

        /**
         * Constructs a CharArraySequence for the given array.
         *
         * @param  array  character array to sequence.
         */
        public CharArraySequence(char[] array) {
            this(array, 0, array.length);
        } // CharArraySequence

        /**
         * Constructs a CharArraySequence for the given array.
         *
         * @param  array  character array to sequence.
         * @param  start  first valid character offset.
         * @param  end    last valid character offset.
         */
        public CharArraySequence(char[] array, int start, int end) {
            if (start < 0 || end < 0 || start > end) {
                throw new IndexOutOfBoundsException();
            }
            this.array = array;
            this.start = start;
            this.end = end;
        } // CharArraySequence

        /**
         * Returns the character at the specified index. An index ranges
         * from zero to length() - 1. The first character of the
         * sequence is at index zero, the next at index one, and so on,
         * as for array indexing.
         *
         * @param  index  the index of the character to be returned.
         * @return  the specified character.
         */
        public char charAt(int index) {
            if (index < start || index >= end) {
                throw new IndexOutOfBoundsException();
            }
            return array[index];
        } // charAt

        /**
         * Returns the length of this character sequence. The length is
         * the number of 16-bit Unicode characters in the sequence.
         *
         * @return  the number of characters in this sequence.
         */
        public int length() {
            return end - start;
        } // length

        /**
         * Returns a new character sequence that is a subsequence of
         * this sequence. The subsequence starts with the character at
         * the specified index and ends with the character at index end
         * - 1. The length of the returned sequence is end - start, so
         * if start == end then an empty sequence is returned.
         *
         * @param  start  the start index, inclusive
         * @param  end    the end index, exclusive.
         * @return  character sequence.
         */
        public CharSequence subSequence(int start, int end) {
            if (end > length()) {
                throw new IndexOutOfBoundsException();
            }
            return new CharArraySequence(array, start, end);
        } // subSequence
    } // CharArraySequence
} // AbstractView
