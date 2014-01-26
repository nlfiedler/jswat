/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * FILE:        listCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/21/02        Initial version
 *      nf      02/15/02        Fixed bug 391
 *      nf      02/22/02        Fixed bug 402
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'list' command.
 *
 * $Id: listCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Location;
import java.io.File;
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
    public void perform(Session session, StringTokenizer args, Log out) {
        // Can we show source code at all?
        UIAdapter adapter = session.getUIAdapter();
        if (!adapter.canShowFile()) {
            out.writeln(Bundle.getString("list.noShowFile"));
            return;
        }

        // Get the current location, if any.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        Location loc = conman.getCurrentLocation();
        if (loc == null) {
            out.writeln(Bundle.getString("list.noLocation"));
            return;
        }

        // Determine the file containing this location.
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        SourceSource source = null;
        try {
            source = pathman.mapSource(loc.declaringType());
        } catch (IOException ioe) {
            out.writeln(swat.getResourceString("couldntMapSrcFile"));
            return;
        }
        if (source == null) {
            out.writeln(swat.getResourceString("couldntMapSrcFile"));
            return;
        }

        int startLine = loc.lineNumber();

        boolean center = false;
        int count = 10;
        while (args.hasMoreTokens()) {
            String arg = args.peek();
            if (arg.equals("center")) {
                // Center the display around the start line.
                center = true;
                // Consume the matching token.
                args.nextToken();
            } else if (arg.equals("count")) {
                // Show a specific number of lines.
                // Consume the matching token.
                args.nextToken();
                // Next token is the count itself.
                if (args.hasMoreTokens()) {
                    arg = args.nextToken();
                    try {
                        count = Integer.parseInt(arg);
                    } catch (NumberFormatException nfe) {
                        out.writeln(Bundle.getString("list.invalidCount"));
                        return;
                    }
                } else {
                    out.writeln(Bundle.getString("list.missingCount"));
                    return;
                }
            } else {
                // Consume the token.
                args.nextToken();
                try {
                    startLine = Integer.parseInt(arg);
                } catch (NumberFormatException nfe) {
                    out.writeln(Bundle.getString("badLineNumber"));
                    return;
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
            out.writeln(swat.getResourceString("couldntOpenFileMsg"));
        }
    } // perform
} // listCommand
