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
 * $Id: FancyTextArea.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * Class FancyTextArea is a text area with a bit more. It has the ability
 * to auto-scroll when new text is appended, and it can automatically trim
 * the text from the top of the area as new text is appended.
 *
 * @author  Nathan Fiedler
 */
class FancyTextArea extends JTextArea implements ComponentListener, DocumentListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Vertical scrollbar for the text area for auto-scrolling. */
    protected JScrollBar verticalScrollBar;
    /** Horizontal scrollbar for the text area for auto-scrolling. */
    protected JScrollBar horizontalScrollBar;
    /** Maximum number of lines we allow in this text area. If zero,
     * no limit is enforced. */
    protected int maxLineCount;
    /** True if we are listening to the document. */
    protected boolean listeningToDocument;
    /** Runnable to remove the text at the top. */
    protected Runnable textRemover;
    /** Runnable to scroll the text area down. */
    protected Runnable downScroller;

    /**
     * Constructs a FancyTextArea.
     */
    public FancyTextArea() {
        super();
        textRemover = new Runnable() {
                public void run() {
                    int lc = getLineCount();
                    if (lc > maxLineCount) {
                        // Number of lines to be removed from the top.
                        lc -= maxLineCount;
                        try {
                            // Find the start of that line.
                            int pos = getLineStartOffset(lc);
                            // Remove the text.
                            getDocument().remove(0, pos);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace();
                        }
                    }
                }
            };
        downScroller = new Runnable() {
                public void run() {
                    if (verticalScrollBar != null) {
                        verticalScrollBar.setValue(
                            verticalScrollBar.getMaximum());
                    }
                    if (horizontalScrollBar != null) {
                        horizontalScrollBar.setValue(
                            horizontalScrollBar.getMinimum());
                    }
                }
            };
    } // FancyTextArea

    /**
     * Turn on the auto-scroll feature of the FancyTextArea.
     * This uses the horizontal and vertical scrollbars of the
     * given scroll pane to ensure the bottom-left corner of the
     * text area is made visible each time new text is appended.
     *
     * @param  pane  scroll pane displaying the text area, or null to
     *               deactivate the autoscrolling feature.
     */
    public void autoScroll(JScrollPane pane) {
        if (pane == null) {
            if (verticalScrollBar != null || horizontalScrollBar != null) {
                // Stop being a component listener.
                removeComponentListener(this);
            }
            verticalScrollBar = null;
            horizontalScrollBar = null;
        } else {
            verticalScrollBar = pane.getVerticalScrollBar();
            horizontalScrollBar = pane.getHorizontalScrollBar();
            if (verticalScrollBar != null || horizontalScrollBar != null) {
                // Become a component listener so that we can scroll.
                addComponentListener(this);
            }
        }
    } // autoScroll

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param  e  the document event.
     */
    public void changedUpdate(DocumentEvent e) {
    } // changedUpdate

    /**
     * Invoked when the component has been made invisible.
     */
    public void componentHidden(ComponentEvent e) {
    } // componentHidden

    /**
     * Invoked when the component's position changes.
     */
    public void componentMoved(ComponentEvent e) {
    } // componentMoved

    /**
     * Invoked when the component's size changes.
     */
    public void componentResized(ComponentEvent e) {
        // Use this event for scrolling the text area, as it
        // guarantees we scroll when the text area has increased
        // in size. Scrolling immediately after adding text to
        // the text area may happen before the component has
        // actually grown in size physically.
        SwingUtilities.invokeLater(downScroller);
    } // componentResized

    /**
     * Invoked when the componenthas been made visible.
     */
    public void componentShown(ComponentEvent e) {
    } // componentShown

    /**
     * Gives notification that there was an insert into the document. The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param  e  the document event.
     */
    public void insertUpdate(DocumentEvent e) {
        // Can't mutate the document during notification.
        SwingUtilities.invokeLater(textRemover);
    } // insertUpdate

    /**
     * Gives notification that a portion of the document has been
     * removed. The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param  e  the document event.
     */
    public void removeUpdate(DocumentEvent e) {
        // It seems that removing text causes a scroll to the top.
        SwingUtilities.invokeLater(downScroller);
    } // removeUpdate

    /**
     * Sets the maximum number of lines allowed in this text area.
     * It is possible due to the timing of events that the text area
     * may contain more lines than <code>count</code>. Although, it
     * will be only for a very brief period of time.
     *
     * @param  count  number of lines allowed.
     */
    public void setMaxLineCount(int count) {
        if (count < 0) {
            count = 0;
        }
        maxLineCount = count;
        if (!listeningToDocument && count > 0) {
            getDocument().addDocumentListener(this);
            listeningToDocument = true;
        } else if (listeningToDocument && count == 0) {
            getDocument().removeDocumentListener(this);
            listeningToDocument = false;
        }
    } // setMaxLineCount
} // FancyTextArea
