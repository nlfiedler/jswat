/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointType.java 6 2007-05-16 07:14:24Z nfiedler $
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
