/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: HighlightDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;

/**
 * Class HighlightDrawLayer is responsible for drawing the highlight on
 * a particular region in the text area.
 *
 * @author  Nathan Fiedler
 */
public abstract class HighlightDrawLayer extends AbstractDrawLayer {
    /** Start of the text highlight. */
    private int highlightStart;
    /** End of the text highlight. */
    private int highlightEnd;
    /** Color used to highlight some area. */
    private Color highlightColor;

    /**
     * Constructs a HighlightDrawLayer to highlight using the given
     * color.
     *
     * @param  color  highlight color.
     */
    public HighlightDrawLayer(Color color) {
        highlightColor = color;
    } // HighlightDrawLayer

    /**
     * Returns the highlighted text's end position. Return 0 if the
     * document is empty, or the value of dot if there is no highlight.
     *
     * @return  the end position >= 0
     */
    public int getHighlightEnd() {
        return highlightEnd;
    } // getHighlightEnd

    /**
     * Returns the highlighted text's start position. Return 0 if the
     * document is empty, or the value of dot if there is no highlight.
     *
     * @return  the start position >= 0
     */
    public int getHighlightStart() {
        return highlightStart;
    } // getHighlightStart

    /**
     * Sets the color to be used for highlighting.
     *
     * @param  color  color to use for highlighting.
     */
    public void setColor(Color color) {
        highlightColor = color;
    } // setColor

    /**
     * Sets the start and end offsets of the text to be highlighted.
     *
     * @param  start  start offset of the highlight.
     * @param  end    end offset of the highlight.
     */
    public void setHighlight(int start, int end) {
        highlightStart = start;
        highlightEnd = end;
        if (start == end) {
            setActive(false);
        } else {
            setActive(true);
        }
    } // setHighlight

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties. After making the changes, the draw
     * layer should return of the offset at which it would like to
     * update the context again. This is an efficiency heuristic.
     *
     * @param  ctx     draw context.
     * @param  offset  offset into character buffer indicating where
     *                 drawing is presently taking place.
     * @return  offset into character buffer at which this draw
     *          layer would like to update the draw context again.
     *          In other words, how long this updated context is valid
     *          for in terms of characters in the buffer.
     */
    public int updateContext(DrawContext ctx, int offset) {
        if (offset < highlightStart) {
            return highlightStart;
        }
        // If this comparison is just > we loop forever because we
        // fail to advance past the last character of the highlight.
        if (offset >= highlightEnd || highlightColor == null) {
            return Integer.MAX_VALUE;
        }
        ctx.setBackColor(highlightColor);
        return highlightEnd;
    } // updateContext
} // HighlightDrawLayer
