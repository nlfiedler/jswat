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
 * $Id: viewCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceFactory;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Classes;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.File;
import java.io.IOException;
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
    public void perform(Session session, CommandArguments args, Log out) {
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String idClass = args.nextToken();
        SourceSource source = null;
        if (session.isActive()) {
            // Try to find the named class.
            VirtualMachine vm = session.getConnection().getVM();
            List classes =  Classes.findClassesByPattern(vm, idClass);
            if (classes != null && classes.size() > 0) {
                // Use the first matching class.
                ReferenceType clazz = (ReferenceType) classes.get(0);
                try {
                    source = pathman.mapSource(clazz);
                } catch (IOException ioe) {
                    throw new CommandException(ioe);
                }
            }
        }

        if (source == null) {
            // If we couldn't resolve the classname, we'll use what the
            // user entered as-is.
            try {
                source = pathman.mapSource(idClass);
            } catch (IOException ioe) {
                throw new CommandException(ioe);
            }
        }

        if (source == null) {
            // Maybe the user entered a filename.
            File file = new File(idClass);
            if (file.exists()) {
                source = SourceFactory.getInstance().create(file, pathman);
            }
        }

        if (source == null || !source.exists()) {
            throw new CommandException(Bundle.getString("couldntMapSrcFile"));
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
                        throw new CommandException(
                            Bundle.getString("view.invalidLine") + ' '
                            + lineStr);
                    }
                }

                int endLine = startLine;
                if (args.hasMoreTokens()) {
                    String lineStr = args.nextToken();
                    try {
                        endLine = Integer.parseInt(lineStr);
                    } catch (NumberFormatException nfe) {
                        throw new CommandException(
                            Bundle.getString("view.invalidLine") + ' '
                            + lineStr);
                    }
                }

                int count = endLine - startLine + 1;
                if (!adapter.showFile(source, startLine, count)) {
                    throw new CommandException(
                        Bundle.getString("couldntOpenFileMsg"));
                }
            } else {
                throw new CommandException(
                    Bundle.getString("view.noShowFile"));
            }
        }
    } // perform
} // viewCommand
