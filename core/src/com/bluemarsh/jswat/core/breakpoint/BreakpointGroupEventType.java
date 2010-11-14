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
 * are Copyright (C) 2001-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

/**
 * Type of breakpoint group event.
 *
 * @author Nathan Fiedler
 */
public enum BreakpointGroupEventType {

    ADDED {

        @Override
        public void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l) {
            l.groupAdded(e);
        }
    },
    REMOVED {

        @Override
        public void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l) {
            l.groupRemoved(e);
        }
    },
    ERROR {

        @Override
        public void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l) {
            l.errorOccurred(e);
        }
    };

    /**
     * Dispatches the event to the listener.
     *
     * @param  e  event to dispatch.
     * @param  l  listener to receive event.
     */
    public abstract void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l);
}
