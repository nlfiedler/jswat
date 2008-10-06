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
 * are Copyright (C) 2001-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Condition.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;

/**
 * Interface Condition defines a breakpoint conditional. Conditionals may
 * be added to breakpoints, in which case the breakpoint will not stop
 * the debuggee unless all of its conditions are satisfied.
 *
 * @author  Nathan Fiedler
 */
public interface Condition {

    /**
     * Returns true if this condition is satisfied.
     *
     * @param  bp     breakpoint instance.
     * @param  event  event that brought us here.
     * @return  true if satisfied, false otherwise.
     * @throws  Exception
     *          if a problem occurs.
     */
    boolean isSatisfied(Breakpoint bp, Event event) throws Exception;

    /**
     * Indicates if this condition is one meant to be seen by the user.
     *
     * @return  true if this condition should be visible to the user.
     */
    boolean isVisible();
}
