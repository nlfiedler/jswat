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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Monitor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;

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
