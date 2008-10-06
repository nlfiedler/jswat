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
 * $Id: ExceptionBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * An ExceptionBreakpoint stops when a named exception is thrown.
 *
 * @author Nathan Fiedler
 */
public interface ExceptionBreakpoint extends ResolvableBreakpoint {
    /** Name of 'stopOnCaught' property. */
    public static final String PROP_STOPONCAUGHT = "stopOnCaught";
    /** Name of 'stopOnUncaught' property. */
    public static final String PROP_STOPONUNCAUGHT = "stopOnUncaught";

    /**
     * Returns the stop-on-caught status.
     *
     * @return  true if stopping when caught exceptions are thrown.
     */
    public boolean getStopOnCaught();

    /**
     * Returns the stop-on-uncaught status.
     *
     * @return  true if stopping when uncaught exceptions are thrown.
     */
    public boolean getStopOnUncaught();

    /**
     * Sets the stop-on-caught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when caught exceptions are thrown.
     */
    public void setStopOnCaught(boolean stop);

    /**
     * Sets the stop-on-uncaught status. Caller must disable this
     * breakpoint before calling this method.
     *
     * @param  stop  true to stop when uncaught exceptions are thrown.
     */
    public void setStopOnUncaught(boolean stop);
}
