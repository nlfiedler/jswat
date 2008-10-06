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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TraceBreakpoint.java 6 2007-05-16 07:14:24Z nfiedler $
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
