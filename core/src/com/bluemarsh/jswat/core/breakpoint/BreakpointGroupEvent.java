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
 * $Id: BreakpointGroupEvent.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;
import java.util.EventObject;

/**
 * An event which indicates that a breakpoint group has changed status.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointGroupEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The group that changed. */
    protected transient BreakpointGroup group;
    /** The type of group change. */
    protected Type type;
    /** Exception that caused this event, if any. */
    private Exception exception;

    /**
     * Type of breakpoint group event.
     */
    public static enum Type {
        ADDED {
            public void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l) {
                l.groupAdded(e);
            }
        },
        REMOVED {
            public void fireEvent(BreakpointGroupEvent e, BreakpointGroupListener l) {
                l.groupRemoved(e);
            }
        },
        ERROR {
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

    /**
     * Constructs a new GroupEvent.
     *
     * @param  group  group that changed (source of event).
     * @param  type   type of group change.
     */
    public BreakpointGroupEvent(BreakpointGroup group, Type type) {
        super(group);
        this.group = group;
        this.type = type;
    }

    /**
     * Constructs a new GroupEvent.
     *
     * @param  group  group that changed (source of event).
     * @param  e      exception that brought about this event.
     */
    public BreakpointGroupEvent(BreakpointGroup group, Exception e) {
        this(group, Type.ERROR);
        exception = e;
    }

    /**
     * Get the group that changed.
     *
     * @return  group request.
     */
    public BreakpointGroup getBreakpointGroup() {
        return group;
    }

    /**
     * Returns the exception that occurred during processing of a group.
     *
     * @return  the exception that caused this event, if any.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get the group change type.
     *
     * @return  group change type.
     */
    public Type getType() {
        return type;
    }
}
