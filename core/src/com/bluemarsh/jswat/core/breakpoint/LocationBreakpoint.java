/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: LocationBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.Location;

/**
 * A LocationBreakpoint stops at a specific JDI Location (i.e. a code index
 * within a particular class). It does not need to resolve against any class
 * since it is given an exact Location at which to stop. Because of this,
 * it must self-destruct when the session disconnects from the debuggee.
 *
 * @author Nathan Fiedler
 */
public interface LocationBreakpoint extends Breakpoint {
    /** Name of the 'location' property. */
    public static final String PROP_LOCATION = "location";

    /**
     * Retrieve the location associated with this breakpoint.
     *
     * @return  location of breakpoint.
     */
    public Location getLocation();

    /**
     * Set the location at which this breakpoint should stop.
     *
     * @param  location  location at this this breakpoint should stop.
     */
    public void setLocation(Location location);
}
