/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * MODULE:      JSwat Commands
 * FILE:        JSwatCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/23/99        Initial version
 *      nf      05/19/01        Fixed bug 114
 *      nf      04/17/02        Removed JSwat instance
 *      nf      05/04/02        Use CommandArguments instead of tokenizer
 *
 * $Id: JSwatCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;

/**
 * Defines the abstract class for command classes within JSwat.
 *
 * @author  Nathan Fiedler
 */
public abstract class JSwatCommand {
    /** Name of this command, if it has been determined earlier. Use
     * getCommandName() to retrieve this value. */
    private String commandName;

    /**
     * Return a short, one-line description of this command.
     *
     * @return  One-line description of command.
     */
    public String description() {
        return Bundle.getString(getCommandName() + "Desc");
    } // description

    /**
     * Retrieves the name of this command (similar to the class name).
     *
     * @return  name of this command class.
     */
    public String getCommandName() {
        if (commandName == null) {
            commandName = getClass().getName();
            int dot = commandName.lastIndexOf('.');
            // Length of the "Command" suffix is 7.
            int end = commandName.length() - 7;
            commandName = commandName.substring(dot + 1, end);
        }
        return commandName;
    } // getCommandName

    /**
     * Display helpful information about this command, including the
     * possible arguments and their interpretation. First prints the
     * command's description.
     *
     * @param  out  output to write help message to.
     */
    public void help(Log out) {
        // Call description to create the commandName if not known.
        StringBuffer buf = new StringBuffer(512);
        buf.append(description());
        buf.append('\n');
        help(out, buf);
    } // help

    /**
     * Display helpful information about this command, including the
     * possible arguments and their interpretation.
     *
     * @param  out  output to write help message to.
     * @param  buf  string buffer to use for printing.
     */
    public void help(Log out, StringBuffer buf) {
        String commandName = getCommandName();
        // Use the command name to get the help string.
        String helpStr = Bundle.getString(commandName + "Help");
        if (helpStr != null && helpStr.length() > 0) {
            buf.append(helpStr);
            buf.append('\n');
        }
        out.write(buf.toString());
    } // help

    /**
     * Perform the command using the given arguments. Any normal output
     * that needs to be displayed should go to the <code>out</code>
     * object. All errors must be indicated by throwing a
     * <code>CommandException</code> which is caught by the
     * <code>CommandManager</code> and displayed to the user.
     *
     * <p>Errors are thrown using a <code>CommandException</code>, which
     * can wrap any other exception. The message, if provided, is
     * suitable for presenting to the user.</p>
     *
     * @param  session  session on which to operate.
     * @param  args     tokenized command arguments.
     * @param  out      output to write messages to.
     */
    public abstract void perform(Session session, CommandArguments args,
                                 Log out);

    /**
     * Called by the CommandManager when new input has been received
     * from the user. This asynchronously follows a call to
     * <code>CommandManager.grabInput()</code>
     *
     * @param  session  JSwat session on which to operate.
     * @param  out      Output to write messages to.
     * @param  cmdman  CommandManager that's calling us.
     * @param  input   Input from user.
     */
    public void receiveInput(Session session, Log out,
                             CommandManager cmdman, String input) {
    } // receiveInput

    /**
     * Called by the CommandManager to get the prompt to display when
     * input is being grabbed by this command.
     *
     * @return Prompt string to use while input is grabbed by this command.
     */
    public String getPromptString() {
        return getCommandName();
    } // getPromptString
} // JSwatCommand
