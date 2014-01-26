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
 * $Id: SelectionDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import javax.swing.JTextField;

/**
 * SelectionDrawLayer is responsible for showing the current text
 * selection in the text area. It also keeps track of the current text
 * selection.
 *
 * @author  Nathan Fiedler
 */
public class SelectionDrawLayer extends HighlightDrawLayer {
    /** Our draw layer priority. */
    private static final int PRIORITY = 64;

    /**
     * Constructs a SelectionDrawLayer using the default color.
     */
    public SelectionDrawLayer() {
        super(new JTextField().getSelectionColor());
    } // SelectionDrawLayer

    /**
     * Gets the priority level of this particular draw layer. Typically
     * each type of draw layer has its own priority. Lower values are
     * higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

    /**
     * Returns the selected text's end position. Return 0 if the
     * document is empty, or the value of dot if there is no selection.
     *
     * @return  the end position >= 0
     */
    public int getSelectionEnd() {
        return getHighlightEnd();
    } // getSelectionEnd

    /**
     * Returns the selected text's start position. Return 0 if the
     * document is empty, or the value of dot if there is no selection.
     *
     * @return  the start position >= 0
     */
    public int getSelectionStart() {
        return getHighlightStart();
    } // getSelectionStart

    /**
     * Selects the text found between the specified start and end
     * locations. If the start location is after the end location, the
     * values will be swapped before the selection is made.
     *
     * @param  start  start offset of the selection.
     * @param  end    end offset of the selection.
     */
    public void setSelection(int start, int end) {
        if (end < start) {
            int t = start;
            start = end;
            end = t;
        }
        // Just a translation to the superclass method.
        setHighlight(start, end);
    } // setSelection
} // SelectionDrawLayer
