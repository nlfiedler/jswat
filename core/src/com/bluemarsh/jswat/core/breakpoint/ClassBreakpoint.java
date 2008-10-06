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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClassBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A ClassBreakpoint stops each time a particular class is loaded or
 * unloaded during the execution of the debuggee.
 *
 * @author  Nathan Fiedler
 */
public interface ClassBreakpoint extends Breakpoint {
    /** Name of 'stopOnPrepare' property. */
    public static final String PROP_STOPONPREPARE = "stopOnPrepare";
    /** Name of 'stopOnUnload' property. */
    public static final String PROP_STOPONUNLOAD = "stopOnUnload";

    /**
     * Returns the stop-on-prepare status.
     *
     * @return  true if stopping when class prepares.
     */
    public boolean getStopOnPrepare();

    /**
     * Returns the stop-on-unload status.
     *
     * @return  true if stopping when class unloads.
     */
    public boolean getStopOnUnload();

    /**
     * Sets the stop-on-prepare status.
     *
     * @param  stop  true to stop when class prepares.
     */
    public void setStopOnPrepare(boolean stop);

    /**
     * Sets the stop-on-unload status.
     *
     * @param  stop  true to stop when class unloads.
     */
    public void setStopOnUnload(boolean stop);
}
