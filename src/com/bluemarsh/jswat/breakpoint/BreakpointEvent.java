/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: BreakpointEvent.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.Breakpoint;
import java.util.EventObject;

/**
 * An event which indicates that a breakpoint has changed status.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The breakpoint added event type. */
    public static final int TYPE_ADDED = 1;
    /** The breakpoint enabled event type. */
    public static final int TYPE_MODIFIED = 2;
    /** The breakpoint removed event type. */
    public static final int TYPE_REMOVED = 3;
    /** The breakpoint that changed.
     * @serial */
    protected Breakpoint bp;
    /** The type of breakpoint change.
     * @serial */
    protected int type;

    /**
     * Constructs a new BreakpointEvent.
     *
     * @param  source  Source of this event.
     * @param  bp      Breakpoint that changed.
     * @param  type    Type of breakpoint change.
     */
    public BreakpointEvent(Object source, Breakpoint bp, int type) {
        super(source);
        this.bp = bp;
        this.type = type;
    } // BreakpointEvent

    /**
     * Get the breakpoint that changed.
     *
     * @return  breakpoint request.
     */
    public Breakpoint getBreakpoint() {
        return bp;
    } // getBreakpoint

    /**
     * Get the breakpoint change type.
     *
     * @return  breakpoint change type (one of TYPE_* from BreakpointEvent).
     */
    public int getType() {
        return type;
    } // getType

    /**
     * Returns a String representation of this BreakpointEvent.
     *
     * @return  string representation of this BreakpointEvent.
     */
    public String toString() {
        return "BreakpointEvent=[source=" + getSource() +
            ", breakpoint=" + bp +
            ", type=" + type + "]";
    } // toString
} // BreakpointEvent
