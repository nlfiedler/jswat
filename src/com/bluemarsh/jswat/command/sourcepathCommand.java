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
 * FILE:        sourcepathCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/21/99        Initial version
 *      nf      05/03/02        Fixed bug #516
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'sourcepath' command.
 *
 * $Id: sourcepathCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;

/**
 * Defines the class that handles the 'sourcepath' command.
 *
 * @author  Nathan Fiedler
 */
public class sourcepathCommand extends JSwatCommand {

    /**
     * Perform the 'sourcepath' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        if (!args.hasMoreTokens()) {
            // Display the current source path, if any.
            String[] paths = pathman.getSourcePath();
            if ((paths != null) && (paths.length != 0)) {
                StringBuffer buf = new StringBuffer
                    (Bundle.getString("sourcepath.path"));
                buf.append('\n');
                for (int i = 0; i < paths.length; i++) {
                    buf.append(paths[i]);
                    buf.append('\n');
                }
                out.write(buf.toString());
            } else {
                out.writeln(Bundle.getString("sourcepath.nopath"));
            }
        } else {
            // Set the sourcepath to the one given.
            String path = args.restTrim();
            int last = path.length() - 1;
            if ((path.charAt(0) == '"') && (path.charAt(last) == '"') ||
                (path.charAt(0) == '\'') && (path.charAt(last) == '\'')) {
                // Remove the enclosing quotes.
                path = path.substring(1, last);
            }
            pathman.setSourcePath(path);
            out.writeln(Bundle.getString("sourcepath.set") + ' ' + path);
        }
    } // perform
} // sourcepathCommand
