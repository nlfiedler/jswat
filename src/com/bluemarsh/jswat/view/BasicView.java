/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        BasicView.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/17/01        Initial version
 *      nf      11/11/01        Fixed bug 283
 *      nf      01/06/02        Fixed bugs 379, 381, 382
 *
 * DESCRIPTION:
 *      This file contains the BasicView class definition.
 *
 * $Id: BasicView.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.util.CharArrayIterator;
import com.bluemarsh.util.KMPMatcher;
import com.bluemarsh.util.StringMatcher;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Class BasicView provides the basic support for displaying textual
 * data in a scrollable, searchable area.
 *
 * @author  Nathan Fiedler
 */
public abstract class BasicView extends JSwatView {
    /** Debug reporting category. */
    protected static Category logCategory = Category.instanceOf("sourceview");
    /** The title of our view, used for reporting. */
    protected String viewTitle;
    /** Text area for displaying the source. */
    protected SourceViewTextArea textComponent;
    /** Text from the text component. Used in the findString() method. */
    protected char[] viewContent;
    /** Highlighter used to highlight the current stepping line.
     * Subclasses must add this to the text area's draw layers. */
    protected HighlightDrawLayer lineHighlighter;

    /**
     * Creates a BasicView object.
     *
     * @param  title  title of the view, for reporting purposes.
     */
    public BasicView(String title) {
        this.viewTitle = title;

        // Make a line highlighter with a nice blue color.
        // This is used by showHighlight() to highlight a line.
        lineHighlighter = new HighlightDrawLayer(new Color(128, 128, 255));
        lineHighlighter.setExtendsEOL(true);
    } // BasicView

    /**
     * Look for the given string in the source view's text area.
     * Uses the text area's current selection as the starting point.
     * Will wrap around if the string was not found after the current
     * selection.
     *
     * @param  str         string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found somewhere, false if string
     *          does not exist in this view.
     */
    public boolean findString(String str, boolean ignoreCase) {
        // Prepare for the search.
        CharacterIterator iter = new CharArrayIterator(viewContent);
        StringMatcher matcher = new KMPMatcher();
        matcher.ignoreCase(ignoreCase);
        matcher.init(str);
        int startFrom = textComponent.getSelectionEnd();
        iter.setIndex(startFrom);

        // Search for the string.
        if (logCategory.isEnabled()) {
            logCategory.report("searching for '" + str + "' in " +
                               viewTitle);
        }
        int foundAt = matcher.find(iter, str);
        if (foundAt > -1) {
            foundString(str, foundAt);
        } else {

            // Not found - wrap around to the beginning and try again.
            iter.setIndex(iter.getBeginIndex());
            if (logCategory.isEnabled()) {
                logCategory.report("'" + str + "' not found, wrapping in " +
                                   viewTitle);
            }
            foundAt = matcher.find(iter, str);
            if (foundAt > -1) {
                foundString(str, foundAt);
            } else {
                // String was not found anywhere.
                if (logCategory.isEnabled()) {
                    logCategory.report("'" + str + "' not found in " +
                                       viewTitle);
                }
                return false;
            }
        }
        return true;
    } // findString

    /**
     * Show that we found the string we were looking for.
     *
     * @param  str    string we were looking for.
     * @param  start  start offset of matched string.
     */
    protected void foundString(String str, int start) {
        // Select the string we found (have to set it visible).
        textComponent.select(start, start + str.length());
        try {
            scrollToLine(getLineOfOffset(start) + 1);
            if (logCategory.isEnabled()) {
                logCategory.report("found '" + str + "' at " + start +
                                   " in " + viewTitle);
            }
        } catch (BadLocationException ble) {
            if (logCategory.isEnabled()) {
                logCategory.report("error getting line of offset '" +
                                   start + " in " + viewTitle);
            }
        }
    } // foundString

    /**
     * Get the offset of the end of the given line.
     *
     * @param  line  zero-based line for which to find the end.
     * @return  offset of end of line.
     * @exception  BadLocationException
     *             Thrown if line is invalid.
     */
    protected int getLineEndOffset(int line) throws BadLocationException {
        return textComponent.getContent().getLineEndOffset(line);
    } // getLineEndOffset

    /**
     * Find the line containing the given offset.
     *
     * @param  offset  offset within document >= 0.
     * @return  zero-based line containing that offset.
     * @exception  BadLocationException
     *             Thrown if offset is invalid.
     */
    protected int getLineOfOffset(int offset) throws BadLocationException {
        return textComponent.getContent().getLineOfOffset(offset);
    } // getLineOfOffset

    /**
     * Get the offset of the start of the given line.
     *
     * @param  line  zero-based line for which to find the start.
     * @return  offset of start of line.
     * @exception  BadLocationException
     *             Thrown if line is invalid.
     */
    protected int getLineStartOffset(int line) throws BadLocationException {
        return textComponent.getContent().getLineStartOffset(line);
    } // getLineStartOffset

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
                    if (logCategory.isEnabled()) {
                        logCategory.report("removed old highlight in " +
                                           viewTitle);
                    }
                }
            });
    } // removeHighlight

    /**
     * Scrolls the source view to the given line, if possible.
     * Any value less than one is ignored.
     *
     * @param  line  line to scroll to (1-based).
     */
    public void scrollToLine(final int line) {
        if (line > 0) {

            if (logCategory.isEnabled()) {
                logCategory.report("scrolling to line " + line +
                                   " in " + viewTitle);
            }

            if (textComponent.getVisibleRect().height <= 0) {
                // Apparently the window has not been realized.
                if (logCategory.isEnabled()) {
                    logCategory.report("source view not visible yet for " +
                                       viewTitle +
                                       ", will scroll in 10ms");
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
                            if (logCategory.isEnabled()) {
                                logCategory.report
                                    ("scrolling to cooridates " +
                                     visible + " in " + viewTitle);
                            }
                            textComponent.scrollRectToVisible(visible);
                        }
                    }
                });
        }
    } // scrollToLine

    /**
     * Highlight the given line in the text area, on the AWT event
     * dispatching thread.
     *
     * @param  line  line in text area to be highlighted.
     */
    protected void showHighlight(final int line) {
        if (textComponent.getVisibleRect().height <= 0) {
            // Apparently the window has not been realized.
            if (logCategory.isEnabled()) {
                logCategory.report("source view not visible yet for " +
                                   viewTitle +
                                   ", will show highlight in 10ms");
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
                    // Set text highlight.
                    try {
                        int p0 = getLineStartOffset(line);
                        int p1 = getLineEndOffset(line);
                        lineHighlighter.setHighlight(p0, p1);
                        textComponent.repaint();
                        if (logCategory.isEnabled()) {
                            logCategory.report("highlighted line " +
                                               (line + 1) + " in " +
                                               viewTitle);
                        }
                    } catch (BadLocationException ble) {
                        logCategory.report("error highlighting line " +
                                           (line + 1) + " in " + viewTitle);
                    }
                }
            });
    } // showHighlight
} // BasicView
