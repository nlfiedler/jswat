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
 * FILE:        BasicDrawContext.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/09/01        Initial version
 *
 * $Id: BasicDrawContext.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import java.awt.Color;
import java.awt.Font;

/**
 * This class implements methods for getting and setting various
 * drawing attributes.
 *
 * @author  Nathan Fiedler
 */
public class BasicDrawContext implements DrawContext {
    /** Default background color. */
    private Color defaultBackColor;
    /** Default foreground color. */
    private Color defaultForeColor;
    /** Default font. */
    private Font defaultFont;
    /** Background color. */
    private Color backColor;
    /** Foreground color. */
    private Color foreColor;
    /** Font. */
    private Font font;

    /**
     * Creates a BasicDrawContext with the given default properties.
     * When the reset() method is called, these defaults are used.
     *
     * @param  fore  foreground color default.
     * @param  back  background color default.
     * @param  font  font default.
     */
    public BasicDrawContext(Color fore, Color back, Font font) {
        defaultForeColor = fore;
        foreColor = fore;
        defaultBackColor = back;
        backColor = back;
        defaultFont = font;
        this.font = font;
    } // BasicDrawContext

    /**
     * Get current background color.
     *
     * @return  background color.
     */
    public Color getBackColor() {
        return backColor;
    } // getBackColor

    /**
     * Get current font.
     *
     * @return  font.
     */
    public Font getFont() {
        return font;
    } // getFont

    /**
     * Get current foreground color.
     *
     * @return  foreground color.
     */
    public Color getForeColor() {
        return foreColor;
    } // getForeColor

    /**
     * Reset back to the default color and font properties.
     */
    public void reset() {
        foreColor = defaultForeColor;
        backColor = defaultBackColor;
        font = defaultFont;
    } // reset

    /**
     * Set current background color.
     *
     * @param  color  background color.
     */
    public void setBackColor(Color color) {
        backColor = color;
    } // setBackColor

    /**
     * Set current foreground color.
     *
     * @param  color  foreground color.
     */
    public void setForeColor(Color color) {
        foreColor = color;
    } // setForeColor

    /**
     * Set current font.
     *
     * @param  font  new font.
     */
    public void setFont(Font font) {
        this.font = font;
    } // setFont
} // DrawContext
