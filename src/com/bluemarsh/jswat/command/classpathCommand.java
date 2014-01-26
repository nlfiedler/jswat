/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        classpathCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/22/99        Initial version
 *      nf      09/06/01        Fixed bug #225
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'classpath' command.
 *
 * $Id: classpathCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import java.util.List;

/**
 * Defines the class that handles the 'classpath' command.
 *
 * @author  Nathan Fiedler
 */
public class classpathCommand extends JSwatCommand {

    /**
     * Perform the 'classpath' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        if (!args.hasMoreTokens()) {
            // Display the current class path, if available.
            String[] paths = pathman.getClassPath();
            if ((paths != null) && (paths.length != 0)) {
                StringBuffer buf = new StringBuffer
                    (Bundle.getString("classpath.path"));
                buf.append('\n');
                for (int i = 0; i < paths.length; i++) {
                    buf.append(paths[i]);
                    buf.append('\n');
                }
                out.write(buf.toString());
            } else {
                out.writeln(Bundle.getString("classpath.nopath"));
            }
        } else {
            if (session.isActive()) {
                // We can't set the classpath while active, it may
                // confuse things currently running.
                out.writeln(Bundle.getString("classpath.active"));
            } else {
                // Set the classpath to the one given.
                String path = args.restTrim();
                int last = path.length() - 1;
                if ((path.charAt(0) == '"') && (path.charAt(last) == '"') ||
                    (path.charAt(0) == '\'') && (path.charAt(last) == '\'')) {
                    // Remove the enclosing quotes.
                    path = path.substring(1, last);
                }
                pathman.setClassPath(path);
                out.writeln(Bundle.getString("classpath.set"));
            }
        }
    } // perform
} // classpathCommand
