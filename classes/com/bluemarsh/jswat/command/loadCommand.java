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
 * PROJECT:     JSwat
 * MODULE:      JSwat Commands
 * FILE:        loadCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/29/99        Initial version
 *      nf      03/11/01        Use session settings
 *      nf      05/02/01        Fixed bug 107, added a wait cursor
 *      nf      08/06/01        Undoing last change, it's not GUI-less
 *      nf      08/17/01        Moved code to VMConnection class
 *      nf      10/10/01        Fixed bug 260
 *      nf      12/12/01        Fixed bug 335
 *      nf      04/04/02        Cleaned up a bit
 *      nf      05/10/02        Fixed bug 530
 *
 * $Id: loadCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.util.JVMArguments;

/**
 * Defines the class that handles the 'load' command.
 *
 * @author  Nathan Fiedler
 */
public class loadCommand extends JSwatCommand {
    /** True if the VM we start should be suspended. */
    protected boolean startSuspended = true;

    /**
     * Perform the 'load' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (session.isActive()) {
            // Deactivate current session.
            session.deactivate(false, this);
        }

        String mainClass = session.getProperty("mainClass");
        String jvmOptions = session.getProperty("jvmOptions");

        JVMArguments jvmArgs = null;
        if (!args.hasMoreTokens()) {
            // Re-evaluate the options every time, in case the
            // classpath changed, or classic VM option changed.
            jvmArgs = new JVMArguments(jvmOptions + ' ' + mainClass);
            // Could be getting a main class or jar file name here.
            mainClass = jvmArgs.stuffAfterOptions();
        } else {
            // Parse the user-provided arguments.
            args.returnAsIs(true);
            jvmArgs = new JVMArguments(args.rest());
            // Could be getting a main class or jar file name here.
            mainClass = jvmArgs.stuffAfterOptions();
        }

        PathManager pathman = (PathManager)
            session.getManager(PathManager.class);
        String classpath = pathman.getClassPathAsString();
        jvmOptions = jvmArgs.normalizedOptions(classpath);
        if (mainClass == null || mainClass.length() == 0) {
            // Missing the classname to load.
            throw new MissingArgumentsException(
                Bundle.getString("load.missingClass"));
        }

        // Build the VM connection.
        VMConnection connection = VMConnection.buildConnection(
            null, null, jvmOptions, mainClass);

        // Display the options and classname that we're going to use.
        out.writeln(connection.loadingString());

        // Save the load parameters for later reuse.
        session.setProperty("mainClass", mainClass);
        session.setProperty("jvmOptions", jvmArgs.parsedOptions());
        session.setProperty("startSuspended",
                            String.valueOf(startSuspended));

        // Launch the debuggee VM.
        if (connection.launchDebuggee(session, true)) {
            if (!startSuspended) {
                // Now that the Session has completely activated,
                // we may resume the debuggee VM.
                session.resumeVM(this, false, false);
            }
        } else {
            StringBuffer buf = new StringBuffer();
            buf.append(com.bluemarsh.jswat.Bundle.getString("vmLoadFailed"));
            if (!startSuspended) {
                buf.append('\n');
                buf.append(Bundle.getString("run.tryLoadInstead"));
            }
            throw new CommandException(buf.toString());
        }
    } // perform
} // loadCommand
