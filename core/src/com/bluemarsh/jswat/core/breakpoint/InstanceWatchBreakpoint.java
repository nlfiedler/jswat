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
 * $Id: InstanceWatchBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * An InstanceWatchBreakpoint is a watch breakpoint that is set to watch
 * a field within a particular instance of a class, using only references
 * to the JDI Field and ObjectReference, rather than field names and
 * class names. By its nature, this type of breakpoint is short-lived
 * and will delete itself when the session disconnects.
 *
 * @author Nathan Fiedler
 */
public interface InstanceWatchBreakpoint extends FieldBreakpoint,
        InstanceBreakpoint, WatchBreakpoint {

    /**
     * Return the name of the class containing the field being watched.
     *
     * @return  fully-qualified class name.
     */
    String getClassName();
}
