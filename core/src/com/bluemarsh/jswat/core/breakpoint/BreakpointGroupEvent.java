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
 * $Id: BreakpointGroupEvent.java 15 2007-06-03 00:01:17Z nfiedler $
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
