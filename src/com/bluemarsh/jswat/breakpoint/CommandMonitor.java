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
 * FILE:        CommandMonitor.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/02/01        Initial version
 *      nf      06/15/02        Extend AbstractMonitor
 *      nf      11/25/02        Added setCommand()
 *
 * $Id: CommandMonitor.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.breakpoint.ui.CommandMonitorUI;
import com.bluemarsh.jswat.breakpoint.ui.MonitorUI;
import java.util.prefs.Preferences;

/**
 * Class CommandMonitor executes a given command when invoked.
 *
 * @author  Nathan Fiedler
 */
public class CommandMonitor extends AbstractMonitor {
    /** The command to be run. */
    protected String commandToRun;

    /**
     * Default constructor for deserialization.
     */
    public CommandMonitor() {
    } // CommandMonitor

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
     * Return a friendly description of this monitor.
     *
     * @return  descriptor.
     */
    public String description() {
        return commandToRun;
    } // description

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
     * Reads the monitor properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                monitor.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        commandToRun = prefs.get("commandToRun", null);
        if (commandToRun == null) {
            return false;
        }
        return super.readObject(prefs);
    } // readObject

    /**
     * Sets the command that is run by this monitor.
     *
     * @param  command  new Command string.
     */
    public void setCommand(String command) {
        commandToRun = command;
    } // setCommand

    /**
     * Returns a string representation of this.
     *
     * @return  String representing this.
     */
    public String toString() {
        return "CommandMonitor=[" + commandToRun + "]";
    } // toString

    /**
     * Writes the monitor properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                monitor.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        if (!super.writeObject(prefs)) {
            return false;
        }
        prefs.put("class", this.getClass().getName());
        prefs.put("commandToRun", commandToRun);
        return true;
    } // writeObject
} // CommandMonitor
