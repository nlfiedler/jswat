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
 * MODULE:      JSwat Commands
 * FILE:        optionsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/12/01        Initial version
 *      nf      12/15/01        Fixed bug 229
 *      nf      04/17/02        Uses new preferences facility
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'options' command.
 *
 * $Id: optionsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import java.util.prefs.Preferences;

/**
 * Defines the class that handles the 'options' command.
 *
 * @author  Nathan Fiedler
 */
public class optionsCommand extends JSwatCommand {

    /**
     * Perform the 'options' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (args.hasMoreTokens()) {
            // User is likely trying to set an option
            if (args.countTokens() != 2) {
                throw new MissingArgumentsException();
            } else {

                // Get the option name and new value.
                String optname = args.nextToken();
                String optval = args.nextToken();

                // Set the option's new value.
                if (optname.equals("shortClassNames")) {
                    Preferences prefs = Preferences.userRoot().node(
                        "com/bluemarsh/jswat/util");
                    prefs.put(optname, optval);
                    out.writeln(Bundle.getString("options.set"));
                } else if (optname.equals("useClassicVM") ||
                           optname.equals("defaultFileExtension")) {
                    Preferences prefs = Preferences.userRoot().node(
                        "com/bluemarsh/jswat");
                    prefs.put(optname, optval);
                    out.writeln(Bundle.getString("options.set"));
                } else if (optname.equals("addStarDot") ||
                           optname.equals("stopOnMain")) {
                    Preferences prefs = Preferences.userRoot().node(
                        "com/bluemarsh/jswat/breakpoint");
                    prefs.put(optname, optval);
                    out.writeln(Bundle.getString("options.set"));
                } else {
                    // Invalid option name.
                    throw new CommandException(
                        Bundle.getString("options.unknownOption"));
                }
            }
        } else {

            // Show the user the list of options.
            out.writeln(Bundle.getString("options.listOfOptions"));
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/util");
            printOption(out, "shortClassNames", prefs,
                        String.valueOf(Defaults.SHORT_CLASS_NAMES));
            prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat");
            printOption(out, "useClassicVM", prefs,
                        String.valueOf(Defaults.USE_CLASSIC_VM));
            printOption(out, "defaultFileExtension", prefs,
                        Defaults.FILE_EXTENSION);
            prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/breakpoint");
            printOption(out, "addStarDot", prefs,
                        String.valueOf(Defaults.ADD_STAR_DOT));
            printOption(out, "stopOnMain", prefs,
                        String.valueOf(Defaults.STOP_ON_MAIN));
        }
    } // perform

    /**
     * Print the current value of the named option.
     *
     * @param  out   output to write to.
     * @param  name  option name.
     * @param  pref  Preferences node.
     * @param  defv  default value.
     */
    protected void printOption(Log out, String name,
                               Preferences pref, String defv) {
        out.write(name);
        out.write(" = ");
        out.writeln(pref.get(name, defv));
    } // printOption
} // optionsCommand
