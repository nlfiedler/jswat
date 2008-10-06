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
 * are Copyright (C) 1999-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointEvent.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;
import com.sun.jdi.event.Event;
import java.util.EventObject;

/**
 * An event which indicates that a breakpoint has changed status.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The breakpoint that changed. */
    private transient Breakpoint breakpoint;
    /** The type of breakpoint change. */
    private Type type;
    /** Event from the debugggee that caused this event, if available. */
    private transient Event event;
    /** Exception that caused this event, if any. */
    private Exception exception;

    /**
     * Type of breakpoint event.
     */
    public static enum Type {
        /** Breakpoint was added (to the BreakpointManager). */
        ADDED {
            public void fireEvent(BreakpointEvent e, BreakpointListener l) {
                l.breakpointAdded(e);
            }
        },
        /** An error occurred while processing the breakpoint. */
        ERROR {
            public void fireEvent(BreakpointEvent e, BreakpointListener l) {
                l.errorOccurred(e);
            }
        },
        /** Breakpoint was removed (from the BreakpointManager). */
        REMOVED {
            public void fireEvent(BreakpointEvent e, BreakpointListener l) {
                l.breakpointRemoved(e);
            }
        },
        /** Breakpoint has caused debuggee to suspend. */
        STOPPED {
            public void fireEvent(BreakpointEvent e, BreakpointListener l) {
                l.breakpointStopped(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(BreakpointEvent e, BreakpointListener l);
    }

    /**
     * Constructs a new BreakpointEvent.
     *
     * @param  bp  breakpoint that changed (source of event).
     * @param  t   type of breakpoint change.
     * @param  e   JDI event that caused this one.
     */
    public BreakpointEvent(Breakpoint bp, Type t, Event e) {
        super(bp);
        breakpoint = bp;
        type = t;
        event = e;
    }

    /**
     * Constructs a new BreakpointEvent.
     *
     * @param  bp   breakpoint that changed (source of event).
     * @param  exc  exception that caused a problem.
     */
    public BreakpointEvent(Breakpoint bp, Exception exc) {
        this(bp, Type.ERROR, null);
        exception = exc;
    }

    /**
     * Get the breakpoint that changed.
     *
     * @return  breakpoint request.
     */
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    /**
     * Returns the exception that occurred during processing of a breakpoint.
     *
     * @return  the exception that caused this event, if any.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get the event that caused this event in the first place. This
     * may be null since not all breakpoint events are caused by an
     * event in the debuggee.
     *
     * @return  original JDI event, or null if none.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Get the breakpoint event type.
     *
     * @return  breakpoint event type.
     */
    public Type getType() {
        return type;
    }
}
