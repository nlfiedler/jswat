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
 * $Id: ContextChangeEvent.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.event;

import java.util.EventObject;

/**
 * An event which indicates that the debugger context has changed. This
 * includes the current thread, current stack frame, and current
 * stepping location.
 *
 * @author  Nathan Fiedler
 */
public class ContextChangeEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The current thread changed event type. */
    public static final int TYPE_THREAD = 0x01;
    /** The current frame changed event type. */
    public static final int TYPE_FRAME = 0x02;
    /** The current location changed event type. */
    public static final int TYPE_LOCATION = 0x04;
    /** The type flags of this context change.
     * @serial */
    private int types;
    /** True if this event is expected to be brief in duration. */
    private boolean isBrief;

    /**
     * Constructs a new ContextChangeEvent.
     *
     * @param  source  Source of this event.
     * @param  types   A set of type flags.
     * @param  brief   true if expected to be brief in duration.
     */
    public ContextChangeEvent(Object source, int types, boolean brief) {
        super(source);
        this.types = types;
        isBrief = brief;
    } // ContextChangeEvent

    /**
     * Indicates if this event marks the beginning of a brief change.
     * That is, in a short amount of time the state is expected to
     * change again.
     *
     * @return  true if event is brief; false otherwise.
     */
    public boolean isBrief() {
        return isBrief;
    } // isBrief

    /**
     * Compares the type of this event to the given argument and returns
     * true if they match. This event may match more than one type of
     * event. For instance, if a thread change occurs, the frame and
     * location will also change at the same time.
     *
     * @param  type  One of <code>THREAD</code>,
     *                      <code>FRAME</code>, or
     *                      <code>LOCATION</code>.
     * @return  true if this event is of the given type
     */
    public boolean isType(int type) {
        return (this.types & type) > 0;
    } // isType

    /**
     * Returns a String representation of this ContextChangeEvent.
     *
     * @return  A String representation of this ContextChangeEvent.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("ContextChange=[source=");
        buf.append(getSource());
        buf.append(", types=");
        if ((types & TYPE_THREAD) > 0) {
            buf.append("<thread>");
        }
        if ((types & TYPE_FRAME) > 0) {
            buf.append("<frame>");
        }
        if ((types & TYPE_LOCATION) > 0) {
            buf.append("<location>");
        }
        buf.append(", brief=");
        buf.append(isBrief);
        buf.append(']');
        return buf.toString();
    } // toString
} // ContextChangeEvent
