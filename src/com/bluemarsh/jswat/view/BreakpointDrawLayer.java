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
 * FILE:        SourceViewHeader.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/01/02        Initial version
 *
 * DESCRIPTION:
 *      This file contains the BreakpointDrawLayer class definition.
 *
 * $Id: BreakpointDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LocatableBreakpoint;
import com.bluemarsh.util.IntHashtable;
import com.sun.jdi.Location;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;

/**
 * Class BreakpointDrawLayer is responsible for drawing color indicators
 * at the appropriate lines to indicate where breakpoints exist, and in
 * what state they are presently in.
 *
 * @author  Nathan Fiedler
 */
class BreakpointDrawLayer extends BasicGutterDrawLayer implements BreakpointListener {
    /** Source view text area we are attached to. */
    protected SourceViewTextArea textArea;
    /** Collection of line numbers and their associated Colors. */
    protected IntHashtable lineColors;
    /** Classlines of the parsed source file. */
    protected List classLines;

    /**
     * Constructs a breakpoint draw layer, attached to the given
     * source view text area.
     *
     * @param  textArea  source view text area.
     */
    public BreakpointDrawLayer(SourceViewTextArea textArea) {
        super();
        this.textArea = textArea;
        lineColors = new IntHashtable();
    } // BreakpointDrawLayer

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointAdded(BreakpointEvent be) {
        setBreakpoint(be.getBreakpoint());
        textArea.repaintGutter();
    } // breakpointAdded

    /**
     * Invoked when a breakpoint has been modified.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointModified(BreakpointEvent be) {
        // Have to rebuild the entire list from scratch since we can't
        // rely on the breakpoint staying in the same location.
        setBreakpoints((BreakpointManager) be.getSource());
    } // breakpointModified

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointRemoved(BreakpointEvent be) {
        // Is it a locatable breakpoint?
        Breakpoint bp = be.getBreakpoint();
        if (matches(bp)) {

            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint locbp = (LocatableBreakpoint) bp;
                Location loc = locbp.getLocation();
                if (loc != null) {
                    // Use the breakpoint's Location.
                    // This works for resolved method breakpoints.
                    setLineColor(loc.lineNumber(), null);
                } else {
                    int line = locbp.getLineNumber();
                    if (line > 0) {
                        // Use the breakpoint's line number.
                        // This works for unresolved line breakpoints.
                        setLineColor(line, null);
                    }
                }
                textArea.repaintGutter();
            }
        }
    } // breakpointRemoved

    /**
     * Returns the color that best represents the state of this
     * breakpoint. If the breakpoint is diabled, the color would be
     * gray. On the other hand, if the breakpoint is enabled and
     * ready, the color would be red. This is useful for those
     * classes that want to visually represent the breakpoint
     * using some colored object.
     *
     * @return  the breakpoint "color".
     */
    protected static Color getBreakpointColor(Breakpoint bp) {
        if (bp.hasExpired()) {
            return Color.red;
        } else if (!bp.isEnabled()) {
            return Color.gray;
        } else if (!bp.isResolved()) {
            return Color.blue;
        } else if (bp.isSkipping()) {
            return Color.yellow;
        } else {
            return Color.green;
        }
    } // getBreakpointColor

    /**
     * Returns true if this draw layer wants to take part in the
     * current painting event.
     *
     * @return  true if active, false otherwise.
     */
    public boolean isActive() {
        // We're active if we have breakpoints.
        return lineColors.size() > 0;
    } // isActive

    /**
     * Check if the Breakpoint is in the class list of class definitions.
     *
     * @param  bp  breakpoint to check against.
     * @return  True if breakpoint source name same as our filename.
     */
    protected boolean matches(Breakpoint bp) {
        // Check if the breakpoint has a location.
        if (bp instanceof LocatableBreakpoint) {
            LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
            Location loc = lbp.getLocation();
            if (loc != null && matches(loc)) {
                return true;
            }

            String clazz = lbp.getClassName();

            // Check if the breakpoint's classname matches what we know.
            if (classLines == null) {
                return false;
            }
            Iterator iter = classLines.iterator();
            while (iter.hasNext()) {
                ClassDefinition cd = (ClassDefinition) iter.next();
                if (cd.getClassName().equals(clazz)) {
                    return true;
                }
            }
        }

        // Well, probably does not belong to us.
        return false;
    } // matches

    /**
     * Check if the Location is in the class list of class definitions.
     *
     * @param  location  Location to check.
     * @return  True if breakpoint source name same as our filename.
     */
    protected boolean matches(Location location) {
        if (classLines == null) {
            return false;
        } else {
            Iterator iter = classLines.iterator();
            String clazz = location.declaringType().name();
            while (iter.hasNext()) {
                ClassDefinition cd = (ClassDefinition) iter.next();
                if (cd.getClassName().equals(clazz)) {
                    return true;
                }
            }
        }
        return false;
    } // matches

    /**
     * Adds a line attribute to the source row header, appropriate
     * for the given breakpoint. Checks to make sure the breakpoint
     * is set in our file.
     *
     * @param  bp  breakpoint.
     */
    protected void setBreakpoint(Breakpoint bp) {
        if (matches(bp)) {
            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint locbp = (LocatableBreakpoint) bp;
                Location loc = locbp.getLocation();
                if (loc != null) {
                    // Use the breakpoint's Location.
                    // This works for resolved method breakpoints.
                    setLineColor(loc.lineNumber(),
                                 getBreakpointColor(bp));
                } else {
                    int line = locbp.getLineNumber();
                    if (line > 0) {
                        // Use the breakpoint's line number.
                        // This works for unresolved line breakpoints.
                        setLineColor(line, getBreakpointColor(bp));
                    }
                }
            }
        }
    } // setBreakpoint

    /**
     * Iterate all existing breakpoints and create row header
     * attributes as appropriate.
     *
     * @param  bpman  BreakpointManager.
     */
    void setBreakpoints(BreakpointManager bpman) {
        lineColors.clear();
        Iterator iter = bpman.breakpoints(true);
        while (iter.hasNext()) {
            setBreakpoint((Breakpoint) iter.next());
        }
        textArea.repaintGutter();
    } // setBreakpoints

    /**
     * Set the list of class definitions.
     *
     * @param  lines  List of ClassDefinition objects.
     */
    void setClassDefs(List lines) {
        classLines = lines;
    } // setClassDefs

    /**
     * Set the color for the given line.
     *
     * @param  line   Line number (one-based value).
     * @param  color  Color for this line, null to remove the existing
     *                color, if any.
     */
    protected void setLineColor(int line, Color color) {
        // 'line' is 1-based but our table is 0-based.
        line--;
        if (color == null) {
            lineColors.remove(line);
        } else {
            lineColors.put(line, color);
        }
    } // setLineColor

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties.
     *
     * @param  ctx   draw context.
     * @param  line  line number where drawing is presently taking place
     *               (zero-based value).
     */
    public void updateContext(DrawContext ctx, int line) {
        Color c = (Color) lineColors.get(line);
        if (c != null) {
            ctx.setBackColor(c);
        }
    } // updateContext
} // BreakpointDrawLayer
