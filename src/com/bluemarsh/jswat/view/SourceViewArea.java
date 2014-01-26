/*********************************************************************
 *
 *      Copyright (C) 2000 Nathan Fiedler
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
 * FILE:        SourceViewArea.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      NF      11/21/00        Initial version
 *
 * DESCRIPTION:
 *      This file contains the SourceViewArea interface definition.
 *
 * $Id: SourceViewArea.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Point;

/**
 * The interface for defining areas of the source view. An area can
 * translate a view coordinate (x,y) to a one-based text line number.
 * This is to be used with the source view popup breakpoint setting
 * gadget.
 *
 * @author  Nathan Fiedler
 * @version 1.0  11/21/00
 */
interface SourceViewArea {

    /**
     * Turns a view coordinate into a one-based line number.
     *
     * @param  pt  Point within the view coordinates.
     * @return  One-based line number corresponding to the point.
     *          If the returned value is -1 then there was an error.
     */
    public int viewToLine(Point pt);
} // SourceViewArea
