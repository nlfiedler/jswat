/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        GroupListener.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/26/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the group change listener interface.
 *
 * $Id: GroupListener.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import java.util.EventListener;

/**
 * The listener interface for receiving changes to groups.
 *
 * @author  Nathan Fiedler
 */
public interface GroupListener extends EventListener {

    /**
     * Invoked when a group has been added.
     *
     * @param  event  group change event
     */
    public void groupAdded(GroupEvent event);

    /**
     * Invoked when a group has been disabled.
     *
     * @param  event  group change event
     */
    public void groupDisabled(GroupEvent event);

    /**
     * Invoked when a group has been enabled.
     *
     * @param  event  group change event
     */
    public void groupEnabled(GroupEvent event);

    /**
     * Invoked when a group has been removed.
     *
     * @param  event  group change event
     */
    public void groupRemoved(GroupEvent event);
} // GroupListener
