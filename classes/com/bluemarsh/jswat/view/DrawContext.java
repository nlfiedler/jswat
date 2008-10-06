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
 * FILE:        DrawContext.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/08/01        Initial version
 *
 * $Id: DrawContext.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import java.awt.Font;

/**
 * This interface provides methods for getting and setting various
 * drawing attributes. During painting, each draw layer receives a
 * DrawContext and has the opportunity to modify the context.
 *
 * @author  Nathan Fiedler
 */
public interface DrawContext {

    /**
     * Get current background color.
     *
     * @return  background color.
     */
    Color getBackColor();

    /**
     * Get current font.
     *
     * @return  font.
     */
    Font getFont();

    /**
     * Get current foreground color.
     *
     * @return  foreground color.
     */
    Color getForeColor();

    /**
     * Set current background color.
     *
     * @param  color  background color.
     */
    void setBackColor(Color color);

    /**
     * Set current foreground color.
     *
     * @param  color  foreground color.
     */
    void setForeColor(Color color);

    /**
     * Set current font.
     *
     * @param  font  new font.
     */
    void setFont(Font font);
} // DrawContext
