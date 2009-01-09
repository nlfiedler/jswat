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
 * $Id$
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
