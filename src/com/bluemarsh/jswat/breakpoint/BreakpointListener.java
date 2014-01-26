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
 * FILE:        BreakpointListener.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/26/01        Initial version
 *      nf      08/01/01        Redid the interface to simplify it
 *
 * DESCRIPTION:
 *      Defines the breakpoint change listener interface.
 *
 * $Id: BreakpointListener.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import java.util.EventListener;

/**
 * The listener interface for receiving changes to breakpoints.
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointListener extends EventListener {

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointAdded(BreakpointEvent event);

    /**
     * Invoked when a breakpoint has been changed in some way.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointModified(BreakpointEvent event);

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointRemoved(BreakpointEvent event);
} // BreakpointListener
