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
 * FILE:        DrawContext.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/08/01        Initial version
 *
 * DESCRIPTION:
 *      This file contains the DrawContext interface definition.
 *
 * $Id: DrawContext.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import java.awt.Font;

/**
 * This interface provides methods for getting and setting various
 * drawing attributes. During painting, each DrawLayer receives a
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
    public Color getBackColor();

    /**
     * Get current font.
     *
     * @return  font.
     */
    public Font getFont();

    /**
     * Get current foreground color.
     *
     * @return  foreground color.
     */
    public Color getForeColor();

    /**
     * Set current background color.
     *
     * @param  color  background color.
     */
    public void setBackColor(Color color);

    /**
     * Set current foreground color.
     *
     * @param  color  foreground color.
     */
    public void setForeColor(Color color);

    /**
     * Set current font.
     *
     * @param  font  new font.
     */
    public void setFont(Font font);
} // DrawContext
