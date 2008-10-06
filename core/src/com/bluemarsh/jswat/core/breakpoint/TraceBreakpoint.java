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
 * $Id: TraceBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * A TraceBreakpoint displays a message each time any method is entered or
 * exited during the execution of the debuggee.
 *
 * @author  Nathan Fiedler
 */
public interface TraceBreakpoint extends Breakpoint {
    /** Name of 'stopOnEnter' property. */
    public static final String PROP_STOPONENTER = "stopOnEnter";
    /** Name of 'stopOnExit' property. */
    public static final String PROP_STOPONEXIT = "stopOnExit";

    /**
     * Returns the stop-on-enter status.
     *
     * @return  true if stopping when method is entered.
     */
    public boolean getStopOnEnter();

    /**
     * Returns the stop-on-exit status.
     *
     * @return  true if stopping when method is exited.
     */
    public boolean getStopOnExit();

    /**
     * Sets the stop-on-enter status.
     *
     * @param  stop  true to stop when method is entered.
     */
    public void setStopOnEnter(boolean stop);

    /**
     * Sets the stop-on-exit status.
     *
     * @param  stop  true to stop when method is exited.
     */
    public void setStopOnExit(boolean stop);
}
