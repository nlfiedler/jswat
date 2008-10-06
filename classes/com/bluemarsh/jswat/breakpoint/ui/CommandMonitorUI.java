/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * FILE:        CommandMonitorUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/02/01        Initial version
 *
 * $Id: CommandMonitorUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.CommandMonitor;
import java.awt.Component;

/**
 * Class CommandMonitorUI provides the interface adapter for the
 * CommandMonitor.
 *
 * @author  Nathan Fiedler
 */
public class CommandMonitorUI implements MonitorUI {
    /** The command monitor. */
    private CommandMonitor commandMon;

    /**
     * Constructs a CommandMonitorUI with the given monitor.
     *
     * @param  cmdmon  CommandMonitor object.
     */
    public CommandMonitorUI(CommandMonitor cmdmon) {
        commandMon = cmdmon;
    } // CommandMonitorUI

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
    } // commit

    /**
     * Generates a string descriptor of this monitor.
     *
     * @return  description.
     */
    public String descriptor() {
        return "command: " + commandMon.getCommand();
    } // descriptor

    /**
     * Returns the Monitor object this ui adapter represents.
     *
     * @return  Monitor object.
     */
    public Monitor getMonitor() {
        return commandMon;
    } // getMonitor

    /**
     * Return a reference to the user interface element that this
     * adapter uses to graphically represent the breakpoint, condition,
     * or monitor. This may be a container that has several user
     * interface elements inside it.
     *
     * @return  user interface ocmponent.
     */
    public Component getUI() {
        return null;
    } // getUI

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     */
    public void undo() {
    } // undo

    /**
     * Returns a description of the command monitor.
     *
     * @return  Command monitor descriptor.
     */
    public String toString() {
        return descriptor();
    } // toString
} // CommandMonitorUI
