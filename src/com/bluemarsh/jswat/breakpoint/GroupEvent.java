/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: GroupEvent.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import java.util.EventObject;

/**
 * An event which indicates that a group has changed status.
 *
 * @author  Nathan Fiedler
 */
public class GroupEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The group added event type. */
    public static final int TYPE_ADDED = 1;
    /** The group disabled event type. */
    public static final int TYPE_DISABLED = 2;
    /** The group enabled event type. */
    public static final int TYPE_ENABLED = 3;
    /** The group removed event type. */
    public static final int TYPE_REMOVED = 4;
    /** The group that changed.
     * @serial */
    protected BreakpointGroup group;
    /** The type of group change.
     * @serial */
    protected int type;

    /**
     * Constructs a new GroupEvent.
     *
     * @param  source  Source of this event.
     * @param  bp      Group that changed.
     * @param  type    Type of group change.
     */
    public GroupEvent(Object source, BreakpointGroup group, int type) {
        super(source);
        this.group = group;
        this.type = type;
    } // GroupEvent

    /**
     * Get the group that changed.
     *
     * @return  group request.
     */
    public BreakpointGroup getGroup() {
        return group;
    } // getGroup

    /**
     * Get the group change type.
     *
     * @return  group change type (one of TYPE_* from GroupEvent).
     */
    public int getType() {
        return type;
    } // getType

    /**
     * Returns a String representation of this GroupEvent.
     *
     * @return  string representation of this GroupEvent.
     */
    public String toString() {
        return "GroupEvent=[source=" + getSource() +
            ", group=" + group +
            ", type=" + type + "]";
    } // toString
} // GroupEvent
