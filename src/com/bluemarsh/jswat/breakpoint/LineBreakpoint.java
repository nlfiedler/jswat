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
 * MODULE:      Breakpoints
 * FILE:        LineBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/02/01        Initial version
 *      nf      08/21/01        Removed errorMessageFor()
 *
 * DESCRIPTION:
 *      Defines the line breakpoint class.
 *
 * $Id: LineBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.breakpoint.ui.LineBreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import java.util.List;

/**
 * Class LineBreakpoint extends the LocationBreakpoint class. Its
 * properties include a class and a line number in that class at
 * which the breakpoint should stop.
 *
 * @author Nathan Fiedler
 */
public class LineBreakpoint extends LocationBreakpoint {
    /** serial version */
    static final long serialVersionUID = 6312985062131690679L;

    /**
     * Constructs a LineBreakpoint for the given class at the specified
     * line within that class.
     *
     * @param  classPattern  name of class in which to set breakpoint,
     *                       possibly using wildcards.
     * @param  line          line at which to stop.
     * @exception  ClassNotFoundException
     *             Thrown if classPattern is not a valid identifier.
     */
    public LineBreakpoint(String classPattern, int line)
        throws ClassNotFoundException {

        super(classPattern);
        lineNumber = line;
    } // LineBreakpoint

    /**
     * Returns the user interface widget for customizing this breakpoint.
     * This method returns a new ui adapter each time it is called.
     *
     * @return  Breakpoint user interface adapter.
     */
    public BreakpointUI getUIAdapter() {
        return new LineBreakpointUI(this);
    } // getUIAdapter

    /**
     * Determine the location at which to set the breakpoint using
     * the given class type. 
     *
     * @param  clazz  ClassType against which to resolve.
     * @return  Location at which to create breakpoint.
     */
    protected Location resolveLocation(ClassType clazz)
        throws ResolveException {
        List locs;
        try {
            locs = clazz.locationsOfLine(lineNumber);
        } catch (AbsentInformationException aie) {
            throw new ResolveException(aie);
        }
        if (locs.size() == 0) {
            throw new ResolveException(new LineNotFoundException());
        }
        // We assume the first location for this line is good enough.
        return (Location) locs.get(0);
    } // resolveLocation

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
    } // setLineNumber

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     */
    public String toString(boolean terse) {
        StringBuffer buf = new StringBuffer(80);
        String cname = referenceSpec.toString();
        if (terse) {
            cname = ClassUtils.justTheName(cname);
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
    } // toString
} // LineBreakpoint
