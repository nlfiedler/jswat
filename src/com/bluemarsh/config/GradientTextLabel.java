/*********************************************************************
 *
 *	Copyright (C) 2000-2005 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: GradientTextLabel.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * Class GradientTextLabel shows a text label over a gradient background.
 *
 * @author  Nathan Fiedler
 */
class GradientTextLabel extends JPanel {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our desired component width. */
    private static final int DESIRED_WIDTH = 250;
    /** Our desired component height. */
    private static final int DESIRED_HEIGHT = 30;
    /** Our textual label. */
    protected String textLabel;

    /**
     * Constructs a new GradientTextLabel object with the given label.
     *
     * @param  label  Textual label to display.
     */
    public GradientTextLabel(String label) {
        super();
        textLabel = label;
        setPreferredSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));
    } // GradientTextLabel

    /**
     * Paints this component to the given Graphics context.
     *
     * @param  g  Graphics context.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        // Set a horizontal gradient with a steep degrade.
        g2d.setPaint(new GradientPaint(0.0f, 0.0f, Color.gray,
                                       250.0f, 0.0f, getBackground()));
        int w = getWidth();
        int h = getHeight();
        g2d.fillRect(0, 0, w, h);

        // Must reset the color back to foreground color, otherwise
        // the text will be rendered using the same gradient paint.
        g2d.setColor(getForeground());
        // Use a bold font for improved readability.
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));
        g2d.drawString(textLabel, 10, 20);
    } // paint
} // GradientTextLabel
