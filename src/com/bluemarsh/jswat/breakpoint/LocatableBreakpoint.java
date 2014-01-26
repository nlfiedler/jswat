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
 * FILE:        LocatableBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/17/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the locatable breakpoint interface.
 *
 * $Id: LocatableBreakpoint.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.sun.jdi.Location;
import java.io.File;

/**
 * Interface LocatableBreakpoint is implemented by those breakpoints
 * have a location associated with them. This includes method and line
 * breakpoints, but not exception or watch breakpoints.
 *
 * @author  Nathan Fiedler
 */
public interface LocatableBreakpoint {

    /**
     * Make a guess as to whether this breakpoint's location may reside
     * in the given source file. This is only useful if the breakpoint
     * is not yet resolved, otherwise <code>getLocation()</code> would
     * be the desirable method to call.
     *
     * @param  file  source file that may contain this breakpoint.
     * @return  true if this breakpoint probably lives in the given file.
     */
    //public boolean definedIn(File file);

    /**
     * Return the name of the class that this breakpoint is located in.
     * This could be a fully-qualified class name or a wild-carded name
     * pattern containing a single asterisk (e.g. "*.cname").
     *
     * @return  Class name if known, null if not.
     */
    public String getClassName();

    /**
     * Retrieve the line number associated with this breakpoint.
     * Not all breakpoints will have a particular line associated
     * with them (such as method breakpoints). In such cases, this
     * method will return -1.
     *
     * @return  line number of breakpoint, if applicable; -1 if not.
     */
    public int getLineNumber();

    /**
     * Retrieve the location associated with this breakpoint. The caller
     * may want to call <code>isResolved()</code> before calling this
     * method. An unresolved breakpoint will not have a location yet.
     *
     * @return  location of breakpoint, or null if not resolved.
     */
    public Location getLocation();
} // LocatableBreakpoint
