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
 * are Copyright (C) 2001-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * Interface Monitor defines a breakpoint monitor. A monitor is an action
 * that is invoked whenever a breakpoint has been hit. A breakpoint is
 * considered as hit when it is not skipping, or expired, and all of its
 * conditions are satisfied.
 *
 * @author  Nathan Fiedler
 */
public interface Monitor {

    /**
     * Return a string describing this monitor.
     *
     * @return  string description.
     */
    String describe();

    /**
     * Perform the action that this monitor is defined to do. The given
     * BreakpointEvent instance provides the breakpoint and JDI Event,
     * and permits access to the Session.
     *
     * <p><em>Note that when this method is invoked, the debugging context
     * is not yet available, as this is the first code to be executed after
     * the debuggee has suspended due to the breakpoint event.</em></p>
     *
     * @param  event  breakpoint event.
     */
    void perform(BreakpointEvent event);

    /**
     * Indicates if this monitor requires that the debuggee be suspended
     * at a breakpoint in order to be performed. If this returns true,
     * the breakpoint suspend policy will be overridden to ensure the
     * debuggee suspends, the monitors are performed, then the debuggee
     * is resumed.
     *
     * @return  true if debuggee should suspend, false otherwise.
     */
    boolean requiresThread();
}
