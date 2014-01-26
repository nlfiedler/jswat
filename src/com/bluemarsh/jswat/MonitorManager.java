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
 * MODULE:      JSwat
 * FILE:        MonitorManager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/04/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the class responsible for managing monitors.
 *
 * $Id: MonitorManager.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * Class MonitorManager is responsible for managing monitors. Monitors
 * are executable objects that can be associated with breakpoints.
 * Each time the debuggee VM halts execution, the monitors will be
 * executed. If a monitor is associated with a specific breakpoint,
 * it is executed only if that breakpoint caused execution to halt.
 *
 * @author  Nathan Fiedler
 */
public class MonitorManager extends DefaultManager {

    // Plan A:
    // Breakpoints manage their own monitors.
    // MonitorManager provides utility functions.
    // Common monitors are managed by MonitorManager.
    // Pro: More intuitable user interface.
    // Con: No support for monitors shared among select breakpoints.

    // Plan B:
    // MonitorManager manages all monitors.
    // Breakpoints merely reference monitors by number.
    // Monitors must have a reference count to determine when they
    //   are no longer referenced by any breakpoints.
    // Common monitors start with a reference count of one so they
    //   are kept around whether breakpoints refer to them or not.
    // Pro: Allows sharing of monitors among breakpoints.
    // Con: More complex user interface.

} // MonitorManager
