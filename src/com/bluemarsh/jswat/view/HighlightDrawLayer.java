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
 * FILE:        HighlightDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/01        Initial version
 *
 * DESCRIPTION:
 *      This file contains the HighlightDrawLayer class definition.
 *
 * $Id: HighlightDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;

/**
 * Class HighlightDrawLayer is responsible for drawing the highlight
 * on a particular region in the text area.
 *
 * @author  Nathan Fiedler
 */
public class HighlightDrawLayer extends BasicDrawLayer {
    /** Color used to highlight some area. */
    protected Color highlightColor;
    /** Start of the text highlight. */
    protected int highlightStart;
    /** End of the text highlight. */
    protected int highlightEnd;

    /**
     * Constructs a HighlightDrawLayer to highlight using the
     * given color.
     *
     * @param  color  highlight color.
     */
    public HighlightDrawLayer(Color color) {
        highlightColor = color;
    } // HighlightDrawLayer

    /**
     * Selects the text found between the specified start and end
     * locations.
     *
     * @param  start  start offset of the highlight.
     * @param  end    end offset of the highlight.
     */
    public void setHighlight(int start, int end) {
        highlightStart = start;
        highlightEnd = end;
        if (start == end) {
            active = false;
        } else {
            active = true;
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
        if (offset >= highlightEnd) {
            return Integer.MAX_VALUE;
        }
        ctx.setBackColor(highlightColor);
        return highlightEnd;
    } // updateContext
} // HighlightDrawLayer
