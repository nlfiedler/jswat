/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointType.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.ClassBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.ExceptionBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.LocationBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MethodBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.ThreadBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.TraceBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.UncaughtExceptionBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.WatchBreakpoint;

/**
 * Represents a type of breakpoint, for building the appropriate editors
 * for creating a new breakpoint.
 *
 * @author Nathan Fiedler
 */
public enum BreakpointType {
    CLASS, EXCEPTION, LINE, LOCATION, METHOD, THREAD, TRACE, UNCAUGHT, WATCH;

    /**
     * Returns the type that the given breakpoint represents.
     *
     * @param  bp  breakpoint instance.
     * @return  the type of the breakpoint.
     */
    public static BreakpointType getType(Breakpoint bp) {
        if (bp instanceof ClassBreakpoint) {
            return CLASS;
        } else if (bp instanceof ExceptionBreakpoint) {
            return EXCEPTION;
        } else if (bp instanceof LineBreakpoint) {
            return LINE;
        } else if (bp instanceof LocationBreakpoint) {
            return LOCATION;
        } else if (bp instanceof MethodBreakpoint) {
            return METHOD;
        } else if (bp instanceof ThreadBreakpoint) {
            return THREAD;
        } else if (bp instanceof TraceBreakpoint) {
            return TRACE;
        } else if (bp instanceof UncaughtExceptionBreakpoint) {
            return UNCAUGHT;
        } else if (bp instanceof WatchBreakpoint) {
            return WATCH;
        } else {
            throw new IllegalArgumentException("unknown breakpoint type");
        }
    }
}
