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
 * FILE:        AbstractGutterDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/01/02        Initial version
 *      nf      11/22/03        Renamed
 *
 * $Id: AbstractGutterDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * Class AbstractGutterDrawLayer provides a simple default
 * implementation of the GutterDrawLayer interface.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractGutterDrawLayer implements GutterDrawLayer {
    /** True if this draw layer is actively affecting the text area. */
    private boolean active;

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
} // GutterDrawLayer
