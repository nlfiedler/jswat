/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: listCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.Location;
import java.io.IOException;

/**
 * Defines the class that handles the 'list' command.
 *
 * @author  Nathan Fiedler
 */
public class listCommand extends JSwatCommand {

    /**
     * Perform the 'list' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Can we show source code at all?
        UIAdapter adapter = session.getUIAdapter();
        if (!adapter.canShowFile()) {
            throw new CommandException(Bundle.getString("list.noShowFile"));
        }
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        // Get the current location, if any.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        Location loc = conman.getCurrentLocation();
        if (loc == null) {
            throw new CommandException(Bundle.getString("noCurrentLocation"));
        }

        // Determine the file containing this location.
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        SourceSource source = null;
        try {
            source = pathman.mapSource(loc);
        } catch (IOException ioe) {
            throw new CommandException(Bundle.getString("couldntMapSrcFile"));
        }
        if (source == null || !source.exists()) {
            throw new CommandException(Bundle.getString("couldntMapSrcFile"));
        }

        int startLine = loc.lineNumber();

        boolean center = false;
        int count = 10;
        while (args.hasMoreTokens()) {
            String arg = args.nextToken();
            if (arg.equals("center")) {
                // Center the display around the start line.
                center = true;
            } else if (arg.equals("count")) {
                // Show a specific number of lines.
                // Next token is the count itself.
                if (args.hasMoreTokens()) {
                    arg = args.nextToken();
                    try {
                        count = Integer.parseInt(arg);
                    } catch (NumberFormatException nfe) {
                        count = -1;
                    }
                    if (count <= 0) {
                        throw new CommandException(
                            Bundle.getString("list.invalidCount"));
                    }
                } else {
                    throw new CommandException(
                        Bundle.getString("list.missingCount"));
                }
            } else {
                try {
                    startLine = Integer.parseInt(arg);
                } catch (NumberFormatException nfe) {
                    startLine = -1;
                }
                if (startLine <= 0) {
                    throw new CommandException(
                        Bundle.getString("badLineNumber"));
                }
            }
        }

        if (center) {
            startLine -= (count / 2);
        }

        int endLine = startLine + count;
        if (startLine < 0) {
            endLine -= startLine;
            startLine = 0;
        }
        if (!adapter.showFile(source, startLine, count)) {
            throw new CommandException(Bundle.getString("couldntOpenFileMsg"));
        }
    } // perform
} // listCommand
