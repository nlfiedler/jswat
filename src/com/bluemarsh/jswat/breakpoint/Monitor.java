/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 *      nf      06/14/01        Provides ui adapter
 *      nf      06/15/02        Added number property
 *
 * $Id: Monitor.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.MonitorUI;
import java.util.prefs.Preferences;

/**
 * Interface Monitor defines a breakpoint monitor.
 *
 * @author  Nathan Fiedler
 */
public interface Monitor {

    /**
     * Return a friendly description of this monitor.
     *
     * @return  descriptor.
     */
    public String description();

    /**
     * Retrieves the number of this monitor, or zero if not set.
     *
     * @return  monitor number, or zero if not set.
     */
    public int getNumber();

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

    /**
     * Reads the monitor properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                monitor.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs);

    /**
     * Sets the number for this monitor. The number is used to
     * uniquely identify the monitor among all monitors.
     *
     * @param  n  new number for this monitor.
     */
    public void setNumber(int n);

    /**
     * Writes the monitor properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                monitor.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs);
} // Monitor
