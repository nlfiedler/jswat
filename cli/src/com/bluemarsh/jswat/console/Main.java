/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.console;

import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.CommandParser;
import com.bluemarsh.jswat.command.CommandProvider;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.Bootstrap;
import java.io.Console;
import java.io.PrintWriter;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Bootstrap class for the console interface of JSwat.
 *
 * @author  Nathan Fiedler
 */
public class Main {

    /**
     * My class, private.
     */
    private Main() {
        // None shall construct us.
    }

    /**
     * Kicks off the application.
     *
     * @param  args  the command line arguments.
     */
    public static void main(String[] args) {
        // Make sure we have a Console to work with.
        // This appears to work from bash and emacs shell.
        Console console = System.console();
        if (console == null) {
            // When run in NetBeans, or when the output streams are
            // directed elsewhere, there will not be a Console.
            System.out.println(NbBundle.getMessage(Main.class, "MSG_Main_NoConsole"));
            System.exit(1);
        }

        PrintWriter output = console.writer();

        // Make sure we have the JPDA classes.
        try {
            Bootstrap.virtualMachineManager();
        } catch (NoClassDefFoundError ncdfe) {
            output.println(NbBundle.getMessage(Main.class, "MSG_Main_NoJPDA"));
            System.exit(1);
        }

        // Add a shutdown hook to make sure we exit cleanly.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                // Save the command aliases.
                CommandParser parser = CommandProvider.getCommandParser();
                parser.saveSettings();
                // Save the runtimes to persistent storage.
                RuntimeManager rm = RuntimeProvider.getRuntimeManager();
                rm.saveRuntimes();
                // Save the sessions to persistent storage and have them
                // close down in preparation to exit.
                SessionManager sm = SessionProvider.getSessionManager();
                sm.saveSessions(true);
                System.out.println(NbBundle.getMessage(Main.class, "MSG_Main_Goodbye"));
            }
        }));

        // Initialize the default session instance.
        Session session = SessionProvider.getCurrentSession();

        // Initialize the command parser and load the aliases.
        CommandParser parser = CommandProvider.getCommandParser();
        parser.loadSettings();
        parser.setOutput(output);

        // Create an OutputAdapter to display debuggee output.
        OutputAdapter adapter = new OutputAdapter(output);
        SessionManager sessionMgr = SessionProvider.getSessionManager();
        sessionMgr.addSessionManagerListener(adapter);
        // Create a SessionWatcher to monitor the session status.
        SessionWatcher swatcher = new SessionWatcher();
        sessionMgr.addSessionManagerListener(swatcher);
        // Create a BreakpointWatcher to monitor the breakpoints.
        BreakpointWatcher bwatcher = new BreakpointWatcher();
        sessionMgr.addSessionManagerListener(bwatcher);

        // Add the watchers and adapters to the open sessions.
        Iterator iter = sessionMgr.iterateSessions();
        while (iter.hasNext()) {
            Session s = (Session) iter.next();
            s.addSessionListener(adapter);
            s.addSessionListener(swatcher);
        }

        // Display a helpful greeting.
        output.println(NbBundle.getMessage(Main.class, "MSG_Main_Welcome"));

        // If command line arguments were given, assume that they are
        // commands to be run by the command parser.
        if (args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg);
                sb.append(' ');
            }
            // Show what the user entered so the subsequent output makes sense.
            String input = sb.toString().trim();
            output.format("[%s] > %s\n", session.getProperty(
                    Session.PROP_SESSION_NAME), input);
            // Run the command the user provided.
            performCommand(output, parser, input);
        }

        // Enter the main loop of processing user input.
        while (true) {
            String input = console.readLine("[%s] > ",
                    session.getProperty(Session.PROP_SESSION_NAME));
            // Console returns null to indicate end of stream.
            if (input != null) {
                performCommand(output, parser, input);
            }
        }
    }

    /**
     * Interprets the given command via the command parser.
     *
     * @param  output  where to write error messages.
     * @param  parser  the command interpreter.
     * @param  input   the input command.
     */
    private static void performCommand(PrintWriter output, CommandParser parser,
            String input) {
        // Send the input to the command parser, which will run the
        // command and send output to the writer it was assigned earlier.
        try {
            parser.parseInput(input);
        } catch (MissingArgumentsException mae) {
            output.println(mae.getMessage());
            output.println(NbBundle.getMessage(Main.class,
                    "ERR_Main_HelpCommand"));
        } catch (CommandException ce) {
            // Print the message which should explain everything.
            // If there is a root cause, show that, too.
            output.println(ce.getMessage());
            Throwable cause = ce.getCause();
            if (cause != null) {
                String cmsg = cause.getMessage();
                if (cmsg != null) {
                    output.println(cmsg);
                }
            }
        }
    }
}
