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
 * FILE:        SelectionDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/01        Initial version
 *
 * DESCRIPTION:
 *      This file contains the SelectionDrawLayer class definition.
 *
 * $Id: SelectionDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import javax.swing.JTextField;

/**
 * SelectionDrawLayer is responsible for showing the current text
 * selection in the text area. It also keeps track of the current
 * text selection.
 *
 * @author  Nathan Fiedler
 */
public class SelectionDrawLayer extends HighlightDrawLayer {
    /** Color used to indicate selection. */
    protected static Color selectionColor;

    static {
        JTextField t = new JTextField();
        selectionColor = t.getSelectionColor();
    }

    /**
     * Constructs a SelectionDrawLayer using the default color.
     */
    public SelectionDrawLayer() {
        super(selectionColor);
    } // SelectionDrawLayer

    /**
     * Returns the selected text's end position. Return 0 if the document
     * is empty, or the value of dot if there is no selection.
     *
     * @return  the end position >= 0
     */
    public int getSelectionEnd() {
        return highlightEnd;
    } // getSelectionEnd

    /**
     * Returns the selected text's start position. Return 0 if the document
     * is empty, or the value of dot if there is no selection.
     *
     * @return  the start position >= 0
     */
    public int getSelectionStart() {
        return highlightStart;
    } // getSelectionStart

    /**
     * Selects the text found between the specified start and end
     * locations. If the start location is after the end location,
     * the values will be swapped before the selection is made.
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
} // DrawLayer
