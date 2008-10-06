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
 * $Id: ThreadBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A ThreadBreakpoint stops each time a thread has started or stopped
 * during the execution of the debuggee.
 *
 * @author  Nathan Fiedler
 */
public interface ThreadBreakpoint extends Breakpoint {
    /** Name of 'stopOnDeath' property. */
    public static final String PROP_STOPONDEATH = "stopOnDeath";
    /** Name of 'stopOnStart' property. */
    public static final String PROP_STOPONSTART = "stopOnStart";

    /**
     * Returns the stop-on-death status.
     *
     * @return  true if stopping when thread dies.
     */
    public boolean getStopOnDeath();

    /**
     * Returns the stop-on-start status.
     *
     * @return  true if stopping when thread starts.
     */
    public boolean getStopOnStart();

    /**
     * Sets the stop-on-death status.
     *
     * @param  stop  true to stop when thread dies.
     */
    public void setStopOnDeath(boolean stop);

    /**
     * Sets the stop-on-start status.
     *
     * @param  stop  true to stop when thread starts.
     */
    public void setStopOnStart(boolean stop);
}
