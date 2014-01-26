/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Nathan Fiedler
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
 * FILE:        macroCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/24/00        Initial version
 *      nf      08/16/01        Changed to use MacroManager
 *      nf      11/01/01        Fixing bug 273
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'macro' command.
 *
 * $Id: macroCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.MacroManager;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import java.util.Vector;

/**
 * Defines the class that handles the 'macro' command.
 *
 * @author  Nathan Fiedler
 */
public class macroCommand extends JSwatCommand {
    /** Name of macro being created. */
    protected String macroName;
    /** List of commands that make up the macro. */
    protected Vector macroCommands;

    /**
     * Perform the 'macro' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (args.hasMoreTokens()) {
            // Name of macro is given as second argument.
            macroName = args.nextToken();
            macroCommands = new Vector();
            CommandManager cmdman = (CommandManager)
                session.getManager(CommandManager.class);
            out.writeln(Bundle.getString("macro.starting1") + '\n' +
                        Bundle.getString("macro.starting2"));
            cmdman.grabInput(this);
        } else {
            // We require a macro name.
            missingArgs(out);
        }
    } // perform

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
        input = input.trim();
        if (input.equalsIgnoreCase("endmacro")) {
            MacroManager macroManager = (MacroManager)
                session.getManager(MacroManager.class);
            // Macro definition is complete.
            if (macroCommands.size() == 0) {
                // No macro definition, delete any existing macro
                // of the same name.
                macroManager.removeMacro(macroName);
                out.writeln(Bundle.getString("macro.deleted"));
            } else {
                macroManager.createMacro(macroName, macroCommands);
                out.writeln(Bundle.getString("macro.complete"));
            }
        } else {

            if (input.charAt(0) == '!') {
                // CommandManager would get messed up if we allowed
                // the ! feature when macro input is not saved in
                // the history chain during macro invocation.
                out.writeln(swat.getResourceString("bangNotAllowed"));
                // We want to keep grabbing user input.
                cmdman.grabInput(this);
            } else if (input.startsWith(macroName)) {
                // Macro invokes itself.
                out.writeln(Bundle.getString("macro.invokesSelf"));
                // We want to keep grabbing user input.
                cmdman.grabInput(this);
            } else {
                // Add another command to the macro definition.
                macroCommands.add(input);
                // We want to keep grabbing user input.
                cmdman.grabInput(this);
            }
        }
    } // receiveInput
} // macroCommand
