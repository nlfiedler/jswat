/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: captureCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Capture;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Defines the class that handles the 'capture' command.
 *
 * @author  Nathan Fiedler
 */
public class captureCommand extends JSwatCommand {
    
    /**
     * Perform the 'capture' command.
     *
     * @param  session  debugging session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        // Get the current capture settings.
        boolean captureStdout = Capture.isOutputEnabled(session);
        boolean captureFile = Capture.isFileEnabled(session);
        String filename = Capture.getFilename(session);

        if (args.hasMoreTokens()) {
            // Modify the capture settings.
            
            while (args.hasMoreTokens()) {
                String token = args.nextToken();
                boolean enable = false;
                boolean disable = false;
                if (token.startsWith("+")) {
                    enable = true;
                    token = token.substring(1);
                } else if (token.startsWith("-")) {
                    disable = true;
                    token = token.substring(1);
                }
                if (token.equals("stdout")) {
                    captureStdout = enable || !disable && !captureStdout;
                } else if (token.equals("file")) {
                    captureFile = enable || !disable && !captureFile;
                    if (captureFile) {
                        if (args.hasMoreTokens()) {
                            filename = args.nextToken();
                        } else {
                            throw new MissingArgumentsException(
                                Bundle.getString("capture.missingFilename"));
                        }
                    }
                } else {
                    throw new CommandException(Bundle.getString(
                        "capture.unknownType") + ' ' + token);
                }
            }

            Capture.setOutputEnabled(captureStdout, session);

            File file = null;
            if (captureFile) {
                // Create the file and check that it exists.
                file = new File(filename);
                if (file.exists() && !file.canWrite()) {
                    throw new CommandException(Bundle.getString(
                        "capture.readOnlyFile"));
                }
            }

            // Enable or disable the file capture.
            try {
                Capture.setFileEnabled(captureFile, file, session);
            } catch (FileNotFoundException fnfe) {
                throw new CommandException(Bundle.getString(
                    "capture.fileNotFound"));
            } catch (IOException ioe) {
                throw new CommandException(ioe);
            }
        } else {
            // Display the capture settings.
            StringBuffer buf = new StringBuffer();
            if (captureStdout) {
                buf.append(Bundle.getString("capture.stdout"));
                buf.append('\n');
            }
            if (captureFile) {
                buf.append(Bundle.getString("capture.file") + ' ' + filename);
                buf.append('\n');
            }
            if (buf.length() > 0) {
                out.write(buf.toString());
            } else {
                out.writeln(Bundle.getString("capture.none"));
            }
        }
} // perform
} // captureCommand
