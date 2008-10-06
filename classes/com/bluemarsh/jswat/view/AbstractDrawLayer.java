/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * FILE:        AbstractDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/01        Initial version
 *      nf      11/22/03        Renamed
 *
 * $Id: AbstractDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * AbstractDrawLayer provides a default implementation of the DrawLayer
 * interface.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractDrawLayer implements DrawLayer {
    /** True if this draw layer is actively affecting the text area. */
    private boolean active;
    /** True if this draw layer draws beyond the end of the line. */
    private boolean extendsEOL;

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
     * Controls the active state of this draw layer.
     *
     * @param  active  true to be active, false to be inactive.
     */
    public void setActive(boolean active) {
        this.active = active;
    } // setActive

    /**
     * Sets the extends end-of-line property.
     *
     * @param  extendsEOL  true to extend past the end of the line,
     *                     false otherwise.
     */
    public void setExtendsEOL(boolean extendsEOL) {
        this.extendsEOL = extendsEOL;
    } // setExtendsEOL
} // DrawLayer
