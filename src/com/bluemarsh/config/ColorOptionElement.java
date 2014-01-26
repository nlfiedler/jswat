/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: ColorOptionElement.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Class ColorOptionElement defines the characteristics and behavior for 
 * the color option element. The ColorOptionElement class builds its user
 * interface to consist of a JButton whose color is that represented by
 * this option element. When the button is pressed, a JColorChooser is
 * presented to allow the user to change the color.
 *
 * @author  Nathan Fiedler
 */
public class ColorOptionElement extends OptionElement implements ActionListener {
    /** Array of zeros for padding numbers. */
    protected static String[] zeros = new String[] {
        "0", "00", "000", "0000", "00000"
    };
    /** Our UI component - a button to display the color chooser. */
    protected JButton changeButton;
    /** A "swatch" of color to show what the user has chosen. */
    protected JComponent colorSwatch;
    /** Panel to contain the color swatch and button. */
    protected JPanel uiComponent;
    /** Saved copy of the value from the color chooser. */
    protected String cachedValue;

    /**
     * Invoked when a button has been pressed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        Window win = getOwningWindow(e);
        if (e == null) {
            throw new RuntimeException("no parent window!");
        }
        Color c = JColorChooser.showDialog(
            win, "Choose Color", colorSwatch.getBackground());
        if (c != null) {
            colorSwatch.setBackground(c);
        }
    } // actionPerformed

    /**
     * Returns the type name for this option element.
     *
     * @return  Name of the option type ("color").
     */
    public String getTypeName() {
        return "color";
    } // getTypeName

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        if (uiComponent == null) {
            uiComponent = new JPanel();
            String l = getLabel();
            if (l != null && l.length() > 0) {
                // Add a colon to the end of the label.
                JLabel label = new JLabel(l + ':');
                uiComponent.add(label, "West");
            }
            JPanel colorPanel = new JPanel();
            uiComponent.add(colorPanel, "Center");
            colorPanel.setBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED));
            colorSwatch = new ColorComponent(20, 20);
            colorPanel.add(colorSwatch, "Center");
            changeButton = new JButton("Change");
            changeButton.addActionListener(this);
            uiComponent.add(changeButton, "East");

            if (cachedValue != null) {
                // Set the color to the one we saved earlier.
                setValue(cachedValue);
            }
        }
        return uiComponent;
    } // getUI

    /**
     * Returns the value for this option, if set.
     *
     * @return  Option value, or null if not yet set.
     */
    public String getValue() {
        if (colorSwatch != null) {
            // Turn the color into an RGB value in hex.
            Color bg = colorSwatch.getBackground();
            // Lop off the alpha portion.
            int rgb = bg.getRGB() & 0x00FFFFFF;
            String hex = Integer.toHexString(rgb);
            // Pad with zeros.
            if (hex.length() < 6) {
                hex = zeros[5 - hex.length()].concat(hex);
            }
            cachedValue = "0x" + hex;
        }
        return cachedValue;
    } // getValue

    /**
     * Invalidates the user interface components for this element.
     * Invoked when the element context has changed significantly.
     * Calling <code>getUI()</code> should recreate the UI.
     */
    public void invalidateUI() {
        super.invalidateUI();
        // Call getValue() to save the value to the cache.
        getValue();
        changeButton = null;
        colorSwatch = null;
        uiComponent = null;
    } // invalidateUI

    /**
     * Sets the value for this option.
     *
     * @param  value  Option value.
     */
    public void setValue(String value) {
        if (colorSwatch != null) {
            try {
                colorSwatch.setBackground(Color.decode(value));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        cachedValue = value;
    } // setValue

    /**
     * A simple component whose background color shows what the user
     * has selected from the color chooser.
     */
    protected class ColorComponent extends JComponent {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Our preferred size. */
        protected Dimension prefSize;

        /**
         * Constructs a ColorComponent with the specified height
         * and width.
         *
         * @param  width   preferred width.
         * @param  height  preferred height.
         */
        public ColorComponent(int width, int height) {
            super();
            prefSize = new Dimension(width, height);
            setPreferredSize(prefSize);
        } // ColorComponent

        /**
         * If the minimumSize has been set to a non-null value just
         * return it.
         *
         * @return  the value of the minimumSize property.
         */
        public Dimension getMinimumSize() {
            return prefSize;
        } // getMinimumSize

        /**
         * If the preferredSize has been set to a non-null value just
         * return it.
         *
         * @return  the value of the preferredSize property.
         */
        public Dimension getPreferredSize() {
            return prefSize;
        } // getPreferredSize

        /**
         * Paint this component to the given graphics context.
         *
         * @param g  the <code>Graphics</code> context in which to paint.
         */
        public void paint(Graphics g) {
            // May change to a debug graphics. Also sets font and color.
            g = getComponentGraphics(g);

            // Set up the clipping bounds.
            Rectangle clipRect = g.getClipBounds();
            int clipX;
            int clipY;
            int clipW;
            int clipH;
            int width = getWidth();
            int height = getHeight();
            if (clipRect == null) {
                clipX = getX();
                clipY = getY();
                clipW = width;
                clipH = height;
            } else {
                clipX = clipRect.x;
                clipY = clipRect.y;
                clipW = clipRect.width;
                clipH = clipRect.height;
            }

            if (clipW > width) {
                clipW = width;
            }
            if (clipH > height) {
                clipH = height;
            }
            
            g.setColor(getBackground());
            g.fillRect(clipX, clipY, clipW, clipH);
            super.paint(g);
        } // paint
    } // ColorComponent
} // ColorOptionElement
