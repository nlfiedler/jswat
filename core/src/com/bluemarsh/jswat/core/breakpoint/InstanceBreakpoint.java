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
 * $Id: InstanceBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.ObjectReference;

/**
 * An InstanceBreakpoint applies only to a particular object reference.
 * This is typically combined with other types of a breakpoints, such as
 * WatchBreakpoint.
 *
 * @author  Nathan Fiedler
 */
public interface InstanceBreakpoint extends Breakpoint {
    /** Name of 'objectReference' property. */
    public static final String PROP_OBJECTREFERENCE = "objectReference";

    /**
     * Returns the object reference this breakpoint is associated with.
     *
     * @return  object reference.
     */
    public ObjectReference getObjectReference();

    /**
     * Sets the object reference this breakpoint should filter on.
     *
     * @param  obj  object reference to filter against.
     */
    public void setObjectReference(ObjectReference obj);
}
