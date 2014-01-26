/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        helpCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/31/99        Initial version
 *      nf      09/03/01        'help' without help description
 *      nf      09/06/01        Fixed bug #226
 *      nf      01/09/02        Fixed bug #384
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'help' command.
 *
 * $Id: helpCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.MacroManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.util.StringTokenizer;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Defines the class that handles the 'help' command.
 *
 * @author  Nathan Fiedler
 */
public class helpCommand extends JSwatCommand {
    /** The resource bundle contained in this object. */
    private static ResourceBundle resourceBundle;
    /** The current interactive help category. */
    protected String currentCategory;

    static {
        // Retrieve the resource bundle for the help system.
        resourceBundle = ResourceBundle.getBundle(
            "com.bluemarsh.jswat.command.help");
    }

    /**
     * User has selected a help category.
     *
     * @param  out      Output to write messages to.
     * @param  cmdman   CommandManager that's calling us.
     * @param  input    Trimmed input from user.
     */
    protected void handleCategorySelection(Log out, CommandManager cmdman,
                                           String input) {
        int subnum = -1;
        try {
            subnum = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            // Message is printed below.
        }

        // Read the valid sub-category names from the resource bundle.
        String subStr = resourceBundle.getString(currentCategory + ".subs");
        String[] subs = StringUtils.tokenize(subStr);
        if (subnum > 0 && subnum <= subs.length) {
            // Category numbers are one-based but the array is zero-based.
            subnum--;
            // Those categories beginning with '_' are names of commands.
            if (subs[subnum].charAt(0) == '_') {

                // This category is actually a specific command.
                String cmnd = subs[subnum].substring(1);
                out.write(Bundle.getString("help.helpFor"));
                out.write(": ");
                out.writeln(cmnd);
                JSwatCommand command = cmdman.getCommand(cmnd);
                if (command != null) {
                    command.help(out);
                }
                out.writeln(Bundle.getString("help.separator"));
                printFooter(out, currentCategory);
            } else {
                // Build out the new category name.
                currentCategory = currentCategory + '.' + subs[subnum];
                printCategory(out, currentCategory);
            }
        } else {
            out.writeln(Bundle.getString("help.invalidCategory"));
        }

        cmdman.grabInput(this);
    } // handleCategorySelection

    /**
     * Go up one help category.
     *
     * @param  out      Output to write messages to.
     * @param  cmdman   CommandManager that's calling us.
     */
    protected void handleUp(Log out, CommandManager cmdman) {
        if (currentCategory.equals("top")) {
            out.writeln(Bundle.getString("help.atTopAlready"));
        } else {
            int dot = currentCategory.lastIndexOf('.');
            currentCategory = currentCategory.substring(0, dot);
            printCategory(out, currentCategory);
        }
        cmdman.grabInput(this);
    } // handleUp

    /**
     * Perform the 'help' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        CommandManager cmdman = (CommandManager)
            session.getManager(CommandManager.class);

        // Display help information. String tokenizer may provide
        // an argument for which to display help information.
        if (!args.hasMoreTokens()) {
            // Enter the interactive help system.
            // The initial category is 'top' for lack of a better name.
            currentCategory = "top";
            printCategory(out, currentCategory);
            cmdman.grabInput(this);

        } else {
            MacroManager macroManager = (MacroManager)
                session.getManager(MacroManager.class);

            String argument = args.nextToken();
            JSwatCommand command = cmdman.getCommand(argument, false);

            // Try to display information about the argument.
            if (argument.equals("commands")) {
                // List the loaded commands.
                cmdman.listCommands();
                // List the command aliases created by the user.
                cmdman.listAliases();
                // List the command macros created by the user.
                cmdman.listMacros();

            } else if (cmdman.getAlias(argument) != null) {
                // Argument is an alias.
                out.writeln(argument + ' ' +
                            Bundle.getString("help.isaAlias"));

            } else if (macroManager.getMacro(argument) != null) {
                // Argument is a macro.
                out.writeln(argument + ' ' +
                            Bundle.getString("help.isaMacro"));

            } else if (command != null) {
                // Argument is a command, show its help.
                command.help(out);

            } else {
                // Maybe it is a help category.
                try {
                    resourceBundle.getString("top." + argument + ".1");
                    currentCategory = "top." + argument;
                    printCategory(out, currentCategory);
                    cmdman.grabInput(this);
                } catch (MissingResourceException mre) {
                    // Guess not.
                    out.writeln(Bundle.getString("help.unknownCommand"));
                }
            }
        }
    } // perform

    /**
     * Prints the strings for the named help category, preceeded by
     * the standard category header, and followed by the standard
     * category footer.
     *
     * @param  log       Output to write messages to.
     * @param  category  Help category to display.
     */
    protected void printCategory(Log out, String category) {
        // Display the standard category header.
        int ii = 1;
        while (true) {
            try {
                String key = "header." + ii;
                String str = resourceBundle.getString(key);
                out.writeln(str);
            } catch (MissingResourceException mre) {
                // No more strings in the header.
                break;
            }
            ii++;
        }
        out.writeln("");

        // Display the category strings from the resource bundle.
        ii = 1;
        while (true) {
            try {
                String key = category + '.' + ii;
                String str = resourceBundle.getString(key);
                out.writeln(str);
            } catch (MissingResourceException mre) {
                // No more strings in this category.
                break;
            }
            ii++;
        }
        out.writeln("");
        printFooter(out, category);
    } // printCategory

    /**
     * Prints the standard category footer, along with the given
     * category name in square brackets (e.g. "[cat]:").
     *
     * @param  out       Output to write messages to.
     * @param  category  Help category to display.
     */
    protected void printFooter(Log out, String category) {
        // Display the standard category footer.
        int ii = 1;
        while (true) {
            try {
                String key = "footer." + ii;
                String str = resourceBundle.getString(key);
                out.writeln(str);
            } catch (MissingResourceException mre) {
                // No more strings in the footer.
                break;
            }
            ii++;
        }

        // Show the current category name.
        out.write("[");
        out.write(category);
        out.writeln("]:");
    } // printFooter

    /**
     * Called by the CommandManager when new input has been received
     * from the user. This asynchronously follows a call to
     * <code>CommandManager.grabInput()</code>
     *
     * @param  session  JSwat session on which to operate.
     * @param  out      Output to write messages to.
     * @param  cmdman   CommandManager that's calling us.
     * @param  input    Input from user.
     */
    public void receiveInput(Session session, Log out,
                             CommandManager cmdman, String input) {
        input = input.trim();

        if (input.equals("u")) {
            // Go up to parent category.
            handleUp(out, cmdman);
        } else if (input.equals("m")) {
            // Redisplay the help category.
            printCategory(out, currentCategory);
            cmdman.grabInput(this);
        } else if (input.equals("q")) {
            // Exit the interactive help mode.
            out.writeln(Bundle.getString("help.doneInteractive"));
        } else {
            // Visit the chosen subcategory.
            handleCategorySelection(out, cmdman, input);
        }
    } // receiveInput
} // helpCommand
