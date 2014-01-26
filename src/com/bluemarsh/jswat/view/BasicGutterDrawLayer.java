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
 * FILE:        BasicGutterDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/01/02        Initial version
 *
 * DESCRIPTION:
 *      This file contains the BasicGutterDrawLayer class definition.
 *
 * $Id: BasicGutterDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * Class BasicGutterDrawLayer provides a simple default implementation
 * of the GutterDrawLayer interface.
 *
 * @author  Nathan Fiedler
 */
public class BasicGutterDrawLayer implements GutterDrawLayer {
    /** True if this draw layer is actively affecting the text area. */
    protected boolean active;

    /**
     * Returns true if this draw layer wants to take part in the
     * current painting event.
     *
     * @return  true if active, false otherwise.
     */
    public boolean isActive() {
        return active;
    } // isActive

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties.
     *
     * @param  ctx   draw context.
     * @param  line  line number where drawing is presently taking place.
     */
    public void updateContext(DrawContext ctx, int line) {
    } // updateContext
} // GutterDrawLayer
