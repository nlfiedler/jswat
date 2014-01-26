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
 * FILE:        BasicDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/01        Initial version
 *
 * DESCRIPTION:
 *      This file contains the BasicDrawLayer class definition.
 *
 * $Id: BasicDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * BasicDrawLayer provides a default implementation of the DrawLayer
 * interface.
 *
 * @author  Nathan Fiedler
 */
public class BasicDrawLayer implements DrawLayer {
    /** True if this draw layer is actively affecting the text area. */
    protected boolean active;
    /** True if this draw layer draws beyond the end of the line. */
    protected boolean extendsEOL;

    /**
     * Indicates that this layer wants to affect the background color
     * beyond the end of the line of text.
     *
     * @return  true to extend past EOL, false otherwise.
     */
    public boolean extendsEOL() {
        return extendsEOL;
    } // extendsEOL

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
     * Sets the extends end-of-line property.
     *
     * @param  extendsEOL  true to extend past the end of the line,
     *                     false otherwise.
     */
    public void setExtendsEOL(boolean extendsEOL) {
        this.extendsEOL = extendsEOL;
    } // setExtendsEOL

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
        return Integer.MAX_VALUE;
    } // updateContext
} // DrawLayer
