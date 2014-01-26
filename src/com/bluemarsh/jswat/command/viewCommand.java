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
 * FILE:        viewCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/99        Initial version
 *      nf      07/21/01        Added better source file lookup
 *      nf      09/03/01        Fixing bug 231
 *      nf      01/12/02        Added line command arguments
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'view' command.
 *
 * $Id: viewCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.FileSource;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ReferenceType;
import java.io.*;
import java.util.List;

/**
 * Defines the class that handles the 'view' command.
 *
 * @author  Nathan Fiedler
 */
public class viewCommand extends JSwatCommand {

    /**
     * Perform the 'view' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String idClass = args.nextToken();
        SourceSource source = null;
        try {
            // Try to find the named class.
            List classes = findClassesByPattern(session, idClass);
            if ((classes != null) && (classes.size() > 0)) {
                // Use the first matching class.
                ReferenceType clazz = (ReferenceType) classes.get(0);
                try {
                    source = pathman.mapSource(clazz);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        } catch (NotActiveException nse) { }

        if (source == null) {
            // If we couldn't resolve the classname, we'll use what the
            // user entered as-is.
            try {
                source = pathman.mapSource(idClass);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        if (source == null) {
            // Maybe the user entered a filename.
            source = new FileSource(idClass);
        }

        if (source == null) {
            out.writeln(swat.getResourceString("couldntMapSrcFile"));
        } else {
            // Have the ui adapter open the file.
            UIAdapter adapter = session.getUIAdapter();
            if (adapter.canShowFile()) {
                int startLine = 0;
                if (args.hasMoreTokens()) {
                    String lineStr = args.nextToken();
                    try {
                        startLine = Integer.parseInt(lineStr);
                    } catch (NumberFormatException nfe) {
                        out.write(Bundle.getString("view.invalidLine"));
                        out.write(" ");
                        out.writeln(lineStr);
                        return;
                    }
                }

                int endLine = startLine;
                if (args.hasMoreTokens()) {
                    String lineStr = args.nextToken();
                    try {
                        endLine = Integer.parseInt(lineStr);
                    } catch (NumberFormatException nfe) {
                        out.write(Bundle.getString("view.invalidLine"));
                        out.write(" ");
                        out.writeln(lineStr);
                        return;
                    }
                }

                int count = endLine - startLine + 1;
                if (!adapter.showFile(source, startLine, count)) {
                    out.writeln(swat.getResourceString("couldntOpenFileMsg"));
                }
            } else {
                out.writeln(Bundle.getString("view.noShowFile"));
            }
        }
    } // perform
} // viewCommand
