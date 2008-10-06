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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointListener.java 6 2007-05-16 07:14:24Z nfiedler $
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
