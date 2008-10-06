/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: LineBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.LineBreakpointUI;
import com.bluemarsh.jswat.util.Names;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.EventRequest;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Class LineBreakpoint extends the LocationBreakpoint class. Its
 * properties include a class and a line number in that class at which
 * the breakpoint should stop.
 *
 * @author Nathan Fiedler
 */
public class LineBreakpoint extends LocationBreakpoint {
    /** Name of the source file, if provided. */
    private String sourceName;

    /**
     * Default constructor for deserialization.
     */
    LineBreakpoint() {
    }

    /**
     * Constructs a LineBreakpoint for the given class at the specified
     * line within that class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  line          line at which to stop.
     * @throws  ClassNotFoundException
     *          if classPattern is not a valid identifier.
     */
    public LineBreakpoint(String classPattern, int line)
        throws ClassNotFoundException {

        super(classPattern);
        lineNumber = line;
    }

    /**
     * Constructs a LineBreakpoint for the given class at the specified
     * line within that class, defined in the named source file.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  source        name of the source file.
     * @param  line          line at which to stop.
     * @throws  ClassNotFoundException
     *          if classPattern is not a valid identifier.
     */
    public LineBreakpoint(String classPattern, String source, int line)
        throws ClassNotFoundException {

        this(classPattern, line);
        sourceName = source;
    }

    /**
     * Returns the name of the package for the class this breakpoint is
     * set within. May not be known, in which case null is returned.
     *
     * @return  package name, or null if unknown.
     */
    public String getPackageName() {
        if (referenceSpec.isExact()) {
            String id = referenceSpec.getIdentifier();
            int idx = id.lastIndexOf('.');
            if (idx > 0) {
                return id.substring(0, idx);
            } else {
                return "";
            }
        }
        return null;
    }

    /**
     * Returns the name of the source file for the class this breakpoint
     * is set within. May not be known, in which case null is returned.
     *
     * @return  source name, or null if unknown.
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new LineBreakpointUI(this);
    }

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        sourceName = prefs.get("sourceName", null);
        return super.readObject(prefs);
    }

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type.
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     * @throws  ResolveException
     *          if the location failed to resolve.
     */
    protected Location resolveLocation(ClassType clazz)
        throws ResolveException {
        List locs = null;
        try {
            locs = clazz.locationsOfLine(lineNumber);
            if (locs.isEmpty()) {
                List inners = clazz.nestedTypes();
                Iterator iter = inners.iterator();
                while (iter.hasNext() && locs.isEmpty()) {
                    ReferenceType type = (ReferenceType) iter.next();
                    locs = type.locationsOfLine(lineNumber);
                }
                if (locs.isEmpty()) {
                    throw new ResolveException(new LineNotFoundException());
                }
            }
        } catch (AbsentInformationException aie) {
            throw new ResolveException(aie);
        }
        // We assume the first location for this line is good enough.
        return (Location) locs.get(0);
    }

    /**
     * Set the line number at which this breakpoint is set.
     * This method will force the breakpoint to be unresolved.
     * It must be resolved again before it will be effective.
     *
     * @param  line  line number at this this breakpoint is set.
     */
    public void setLineNumber(int line) {
        lineNumber = line;
        // Reset ourselves so we get resolved all over again.
        deleteEventRequest();
        fireChange();
    }

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string of this.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        if (referenceSpec == null) {
            return "<not initialized>";
        }
        String cname = referenceSpec.toString();
        if (terse) {
            cname = Names.justTheName(cname);
        }
        buf.append(cname);
        buf.append(':');
        buf.append(lineNumber);
        if (!terse) {
            buf.append(' ');
            if (suspendPolicy == EventRequest.SUSPEND_ALL) {
                buf.append(Bundle.getString("suspendAll"));
            } else if (suspendPolicy == EventRequest.SUSPEND_EVENT_THREAD) {
                buf.append(Bundle.getString("suspendThread"));
            } else if (suspendPolicy == EventRequest.SUSPEND_NONE) {
                buf.append(Bundle.getString("suspendNone"));
            }
        }
        return buf.toString();
    }

    /**
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        if (sourceName != null) {
            prefs.put("sourceName", sourceName);
        }
        return true;
    }
}
