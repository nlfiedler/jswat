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
 * FILE:        GutterDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/01/02        Initial version
 *
 * DESCRIPTION:
 *      This file contains the GutterDrawLayer interface definition.
 *
 * $Id: GutterDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * A GutterDrawLayer is responsible for altering the graphics context in
 * a manner appropriate for the line number that is about to be drawn.
 *
 * @author  Nathan Fiedler
 */
public interface GutterDrawLayer {
    // The lower the priority number, the higher the precedence.
    // Otherwise we would have to reverse the list in order for
    // the higher precedence draw layers to draw last.

    public static final int PRIORITY_LOWEST = 512;
    /** Draw layer priority given to the layer indicating breakpoints. */
    public static final int PRIORITY_BREAKPOINT = 256;
    public static final int PRIORITY_HIGHEST = 64;

    /**
     * Returns true if this draw layer wants to take part in the
     * current painting event.
     *
     * @return  true if active, false otherwise.
     */
    public boolean isActive();

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties.
     *
     * @param  ctx   draw context.
     * @param  line  line number where drawing is presently taking place.
     */
    public void updateContext(DrawContext ctx, int line);
} // GutterDrawLayer
