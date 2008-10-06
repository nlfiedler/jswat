/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: BreakpointDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LocatableBreakpoint;
import com.bluemarsh.jswat.lang.ClassDefinition;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.awt.Color;
import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Class BreakpointDrawLayer is responsible for drawing color indicators
 * at the appropriate lines to indicate where breakpoints exist, and in
 * what state they are presently in.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointDrawLayer extends AbstractGutterDrawLayer
    implements BreakpointListener {
    /** Our draw layer priority. */
    private static final int PRIORITY = 256;
    /** Source view text area we are attached to. */
    private SourceViewTextArea textArea;
    /** Collection of line numbers and their associated Colors. */
    private Hashtable lineColors;
    /** Classlines of the parsed source file. */
    private List classLines;
    /** Source file information. */
    private SourceSource sourceSrc;

    /**
     * Constructs a breakpoint draw layer, attached to the given
     * source view text area.
     *
     * @param  area  source view text area.
     * @param  src   source file information.
     */
    public BreakpointDrawLayer(SourceViewTextArea area, SourceSource src) {
        super();
        textArea = area;
        sourceSrc = src;
        lineColors = new Hashtable();
        // Default to active in the general case.
        setActive(true);
    }

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointAdded(BreakpointEvent be) {
        setBreakpoint(be.getBreakpoint());
        textArea.repaintGutter();
    }

    /**
     * Invoked when a breakpoint has been modified.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointModified(BreakpointEvent be) {
        // Have to rebuild the entire list from scratch since we can't
        // rely on the breakpoint staying in the same location.
        setBreakpoints((BreakpointManager) be.getSource());
    }

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  be  breakpoint event.
     */
    public void breakpointRemoved(BreakpointEvent be) {
        // Is it a locatable breakpoint?
        Breakpoint bp = be.getBreakpoint();
        if (matches(bp)) {
            LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
            int line = lbp.getLineNumber();
            if (line > 0) {
                // Use the breakpoint's line number.
                // This works for unresolved line breakpoints.
                setLineColor(line, null);
            }
            textArea.repaintGutter();
        }
    }

    /**
     * Returns the color that best represents the state of this breakpoint.
     * If the breakpoint is diabled, the color would be gray. On the other
     * hand, if the breakpoint is enabled and ready, the color would be
     * red. This is useful for those classes that want to visually
     * represent the breakpoint using some colored object.
     *
     * @param  bp  breakpoint to get color for.
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
    }

    /**
     * Gets the priority level of this particular draw layer. Typically
     * each type of draw layer has its own priority. Lower values are
     * higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * Returns true if this draw layer wants to take part in the
     * current painting event.
     *
     * @return  true if active, false otherwise.
     */
    public boolean isActive() {
        // We're active if we have breakpoints.
        return super.isActive() && lineColors.size() > 0;
    }

    /**
     * Check if the Breakpoint is in the list of class definitions.
     *
     * @param  bp  breakpoint to check against.
     * @return  true if breakpoint matches the source.
     */
    protected boolean matches(Breakpoint bp) {
        // Check if the breakpoint has a location.
        if (bp instanceof LocatableBreakpoint) {
            LocatableBreakpoint lbp = (LocatableBreakpoint) bp;

            // Check if the source has the same information.
            String pkg = sourceSrc.getPackage();
            String src = sourceSrc.getName();
            if (lbp.matchesSource(pkg, src)) {
                return true;
            }

            if (classLines != null) {
                // Iterate our class definitions to find a match.
                String cname = "";
                ReferenceType clazz = lbp.getReferenceType();
                if (clazz != null) {
                    cname = clazz.name();
                }
                Iterator iter = classLines.iterator();
                while (iter.hasNext()) {
                    ClassDefinition cd = (ClassDefinition) iter.next();
                    String cdname = cd.getClassName();
                    if (cdname.equals(cname) || lbp.matchesClassName(cdname)) {
                        return true;
                    }
                }
            }
        }

        // Probably does not belong to us anyway.
        return false;
    }

    /**
     * Adds a line attribute to the source row header, appropriate for
     * the given breakpoint. Checks to make sure the breakpoint is set
     * in our file.
     *
     * @param  bp  breakpoint.
     */
    protected void setBreakpoint(Breakpoint bp) {
        if (matches(bp)) {
            LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
            int line = lbp.getLineNumber();
            if (line > 0) {
                // Use the breakpoint's line number.
                // This works for unresolved line breakpoints.
                setLineColor(line, getBreakpointColor(bp));
            }
        }
    }

    /**
     * Iterate all existing breakpoints and create row header attributes
     * as appropriate.
     *
     * @param  bpman  BreakpointManager.
     */
    public void setBreakpoints(BreakpointManager bpman) {
        lineColors.clear();
        Iterator iter = bpman.breakpoints(true);
        while (iter.hasNext()) {
            setBreakpoint((Breakpoint) iter.next());
        }
        textArea.repaintGutter();
    }

    /**
     * Set the list of class definitions.
     *
     * @param  lines  list of class definitions.
     */
    public void setClassDefinitions(List lines) {
        classLines = lines;
    }

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
            lineColors.remove(new Integer(line));
        } else {
            lineColors.put(new Integer(line), color);
        }
    }

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties.
     *
     * @param  ctx   draw context.
     * @param  line  line number where drawing is presently taking place
     *               (zero-based value).
     */
    public void updateContext(DrawContext ctx, int line) {
        Color c = (Color) lineColors.get(new Integer(line));
        if (c != null) {
            ctx.setBackColor(c);
        }
    }
}
