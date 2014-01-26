/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: SteppingLineDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Defaults;
import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * Class SteppingLineDrawLayer is responsible for drawing the highlight
 * on current stepping line within the text area.
 *
 * @author  Nathan Fiedler
 */
public class SteppingLineDrawLayer extends HighlightDrawLayer {
    /** Our draw layer priority. */
    private static final int PRIORITY = 128;

    /**
     * Constructs a SteppingLineDrawLayer to highlight using the default
     * color.
     */
    public SteppingLineDrawLayer() {
        super(new Color(128, 128, 255));
        setExtendsEOL(true);
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/view");
        updateColor(prefs);
    } // SteppingLineDrawLayer

    /**
     * Gets the priority level of this particular draw layer. Typically
     * each type of draw layer has its own priority. Lower values are
     * higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

    /**
     * The user preferences have changed and the preferred colors may
     * have been modified. Update appropriately.
     *
     * @param  prefs  view Preferences node.
     * @throws  NumberFormatException
     *          if the specified color is improperly encoded.
     */
    void updateColor(Preferences prefs) throws NumberFormatException {
        String defaultColor = (String) Defaults.VIEW_COLORS.get(
            "colors.highlight");
        String color = prefs.get("colors.highlight", defaultColor);
        setColor(Color.decode(color));
    } // updateColor
} // SteppingLineDrawLayer
