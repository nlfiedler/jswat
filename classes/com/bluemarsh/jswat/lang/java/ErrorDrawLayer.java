/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: ErrorDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.view.HighlightDrawLayer;
import java.awt.Color;

/**
 * Class ErrorDrawLayer is responsible for drawing the highlight on the
 * line within the text area that contains an error.
 *
 * @author  Nathan Fiedler
 */
public class ErrorDrawLayer extends HighlightDrawLayer {
    /** Our draw layer priority. */
    private static final int PRIORITY = 192;

    /**
     * Constructs a ErrorDrawLayer to highlight using the default color.
     */
    public ErrorDrawLayer() {
        super(new Color(255, 128, 128));
        setExtendsEOL(true);
    } // ErrorDrawLayer

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
} // ErrorDrawLayer
