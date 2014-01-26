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
 * FILE:        Monitor.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      06/14/01        Extends Serializable, provides
 *                              ui adapter
 *
 * DESCRIPTION:
 *      Defines the Monitor interface.
 *
 * $Id: Monitor.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.MonitorUI;
import java.io.Serializable;

/**
 * Interface Monitor defines a breakpoint monitor.
 *
 * @author  Nathan Fiedler
 */
public interface Monitor extends Serializable {

    /**
     * Returns the user interface widget for customizing this monitor.
     *
     * @return  Monitor user interface adapter.
     */
    public MonitorUI getUIAdapter();

    /**
     * Perform the action that this monitor is defined to do.
     *
     * @param  session  Session in which to operate.
     */
    public void perform(Session session);
} // Monitor
