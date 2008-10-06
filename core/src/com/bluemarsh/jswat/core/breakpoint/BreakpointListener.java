/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointListener.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import java.beans.PropertyChangeListener;

/**
 * The listener interface for receiving events related to breakpoints.
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointListener extends PropertyChangeListener {

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  event  breakpoint event.
     */
    void breakpointAdded(BreakpointEvent event);

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  event  breakpoint event.
     */
    void breakpointRemoved(BreakpointEvent event);

    /**
     * Invoked when a breakpoint has caused the debuggee to stop.
     *
     * @param  event  breakpoint event.
     */
    void breakpointStopped(BreakpointEvent event);

    /**
     * An exception occurred during the processing of a breakpoint.
     *
     * @param  event  breakpoint event, provides exception and breakpoint.
     */
    void errorOccurred(BreakpointEvent event);
}
