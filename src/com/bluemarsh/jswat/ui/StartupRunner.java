/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * $Id: StartupRunner.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandManager;
import java.io.File;
import java.io.IOException;

/**
 * This utility class is responsible for running the JSwat startup
 * files, which contain JSwat commands to be run just after JSwat has
 * started.
 *
 * @author  Nathan Fiedler
 */
public class StartupRunner {

    /**
     * Load and run a startup file.
     *
     * @param  script  name of the startup file.
     * @param  cmdman  CommandManager to process rc file.
     * @return  null if okay, error message if failure.
     */
    protected static String runRCFile(File script, CommandManager cmdman) {
        // If the file is readable, and has non-zero size...
        if (script.canRead() && script.length() > 0) {
            try {
                // Have the command manager process it.
                cmdman.runScript(script.getCanonicalPath());
                return null;
            } catch (CommandException ce) {
                return ce.getMessage();
            } catch (IOException ioe) {
                StringBuffer buf = new StringBuffer(
                    Bundle.getString("errorReadingFile"));
                buf.append(' ');
                buf.append(script.toString());
                return buf.toString();
            }
        }
        // Treat it as normal.
        return null;
    } // runRCFile

    /**
     * Look for startup files in the local directory and all of it
     * parents and run them. Also run the .jswat/init file in the user's
     * home directory if there is one.
     *
     * @param  cmdman  CommandManager to process rc file.
     * @return  null if okay, error message if failure.
     */
    public static String runRCFiles(CommandManager cmdman) {
        // If there is a file in the user home directory, try running it.
        File homeScript = new File(System.getProperty("user.home")
                                   + File.separator + ".jswat",
                                   "init");
        if (homeScript.canRead()) {
            String err = runRCFile(homeScript, cmdman);
            if (err != null) {
                return err;
            }
        }

        // Check the current directory and all it parents for a rc file.
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            File script = new File(dir, "jswat.init");
            if (script.canRead() && !homeScript.equals(script)) {
                // If we find a rc file and if it is not the same as the rc
                // file in the home directory, run it.
                String err = runRCFile(script, cmdman);
                if (err != null) {
                    return err;
                }
            }
            dir = dir.getParentFile();
        }
        return null;
    } // runRCFiles
} // StartupRunner
