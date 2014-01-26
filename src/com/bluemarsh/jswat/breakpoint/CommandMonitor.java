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
 * FILE:        CommandMonitor.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/02/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the CommandMonitor class.
 *
 * $Id: CommandMonitor.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.CommandMonitorUI;
import com.bluemarsh.jswat.breakpoint.ui.MonitorUI;

/**
 * Class CommandMonitor executes a given command when invoked.
 *
 * @author  Nathan Fiedler
 */
public class CommandMonitor implements Monitor {
    /** The command to be run. */
    protected String commandToRun;
    /** serial version */
    static final long serialVersionUID = 8111078038910378398L;

    /**
     * Constructs a CommandMonitor that runs the given command
     * each time this monitor is executed.
     *
     * @param  cmd  command to run.
     */
    public CommandMonitor(String cmd) {
        commandToRun = cmd;
    } // CommandMonitor

    /**
     * Returns the command that is run by this monitor.
     *
     * @return  Command string.
     */
    public String getCommand() {
        return commandToRun;
    } // getCommand

    /**
     * Returns the user interface widget for customizing this monitor.
     *
     * @return  Monitor user interface adapter.
     */
    public MonitorUI getUIAdapter() {
        return new CommandMonitorUI(this);
    } // getUIAdapter

    /**
     * Perform the action that this monitor is defined to do.
     *
     * @param  session  Session in which to operate.
     */
    public void perform(Session session) {
        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);
        cmdman.parseInput(commandToRun);
    } // perform

    /**
     * Returns a string representation of this.
     *
     * @return  String representing this.
     */
    public String toString() {
        return "CommandMonitor=[" + commandToRun + "]";
    } // toString
} // CommandMonitor
