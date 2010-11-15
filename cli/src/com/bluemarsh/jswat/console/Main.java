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
import com.bluemarsh.jswat.core.PlatformProvider;
import com.bluemarsh.jswat.core.connect.ConnectionEvent;
import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionListener;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.Bootstrap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openide.util.NbBundle;

/**
 * Bootstrap class for the console interface of JSwat. Initializes all
 * of the necessary services and registers event listeners. Enters a
 * loop to process using input.
 *
 * @author  Nathan Fiedler
 */
public class Main {

    /** If true, the debugger attempts to emulate JDB output. */
    private static boolean jdbEmulationMode;
    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Creates a new instance of Main.
     */
    private Main() {
    }

    /**
     * Kicks off the application.
     *
     * @param  args  the command line arguments.
     */
    public static void main(String[] args) {
        //
        // An attempt was made early on to use the Console class in java.io,
        // but that is not designed to handle asynchronous output from
        // multiple threads, so we just use the usual System.out for output
        // and System.in for input. Note that automatic flushing is used to
        // ensure output is shown in a timely fashion.
        //

        //
        // Where console mode seems to work:
        // - bash
        // - emacs
        //
        // Where console mode does not seem to work:
        // - NetBeans: output from event listeners is never shown and the
        //   cursor sometimes lags behind the output
        //

        // Turn on flushing so printing the prompt will flush
        // all buffered output generated from other threads.
        PrintWriter output = new PrintWriter(System.out, true);

        // Make sure we have the JPDA classes.
        try {
            Bootstrap.virtualMachineManager();
        } catch (NoClassDefFoundError ncdfe) {
            output.println(NbBundle.getMessage(Main.class, "MSG_Main_NoJPDA"));
            System.exit(1);
        }

        // Ensure we can create the user directory by requesting the
        // platform service. Simply asking for it has the desired effect.
        PlatformProvider.getPlatformService();

        // Define the logging configuration.
        LogManager manager = LogManager.getLogManager();
        InputStream is = Main.class.getResourceAsStream("logging.properties");
        try {
            manager.readConfiguration(is);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }

        // Print out some useful debugging information.
        logSystemDetails();

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
            }
        }));

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
        Iterator<Session> iter = sessionMgr.iterateSessions();
        while (iter.hasNext()) {
            Session s = iter.next();
            s.addSessionListener(adapter);
            s.addSessionListener(swatcher);
        }

        // Find and run the RC file.
        try {
            runStartupFile(parser, output);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
        }

        // Process command line arguments.
        try {
            processArguments(args);
        } catch (ParseException pe) {
            // Report the problem and keep going.
            System.err.println("Option parsing failed: " + pe.getMessage());
            logger.log(Level.SEVERE, null, pe);
        }

        // Display a helpful greeting.
        output.println(NbBundle.getMessage(Main.class, "MSG_Main_Welcome"));
        if (jdbEmulationMode) {
            output.println(NbBundle.getMessage(Main.class,
                    "MSG_Main_Jdb_Emulation"));
        }

        // Enter the main loop of processing user input.
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in));
        while (true) {
            // Keep the prompt format identical to jdb for compatibility
            // with emacs and other possible wrappers.
            output.print("> ");
            output.flush();
            try {
                String command = input.readLine();
                // A null value indicates end of stream.
                if (command != null) {
                    performCommand(output, parser, command);
                }
                // Sleep briefly to give the event processing threads,
                // and the automatic output flushing, a chance to catch
                // up before printing the input prompt again.
                Thread.sleep(250);
            } catch (InterruptedException ie) {
                logger.log(Level.WARNING, null, ie);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, null, ioe);
                output.println(NbBundle.getMessage(Main.class,
                        "ERR_Main_IOError", ioe));
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

    /**
     * Sends system information to the log for debugging purposes.
     */
    private static void logSystemDetails() {
        logger.info(String.format("Log Session: %tc", new Date()));
        String version = NbBundle.getMessage(Main.class, "MSG_Main_version");
        logger.info(String.format("Product version: %s", version));
        logger.info(String.format("Operating System: %s %s on %s",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch")));
        logger.info(String.format("Java; VM; Vendor: %s; %s %s; %s",
                System.getProperty("java.version"),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("java.vendor")));
        logger.info(String.format("Java Home: %s",
                System.getProperty("java.home")));
        logger.info(String.format("Home Directory: %s",
                System.getProperty("user.home")));
        logger.info(String.format("Current Directory: %s",
                System.getProperty("user.dir")));
        logger.info(String.format("Class Path: %s",
                System.getProperty("java.class.path")));
    }

    /**
     * Find the startup file in one of several locations and by one
     * of several names, then run the commands found therein.
     *
     * @param  parser  the command interpreter.
     * @throws  IOException  if reading file fails.
     */
    private static void runStartupFile(CommandParser parser,
            PrintWriter consoleOutput) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter output = new PrintWriter(sw);
        PrintWriter savedOutput = parser.getOutput();
        parser.setOutput(output);
        File[] files = {
            new File(System.getProperty("user.dir"), ".jswatrc"),
            new File(System.getProperty("user.dir"), "jswat.ini"),
            new File(System.getProperty("user.home"), ".jswatrc"),
            new File(System.getProperty("user.home"), "jswat.ini")
        };
        try {
            for (File file : files) {
                if (file.canRead()) {
                    consoleOutput.println("Executing startup file: "
                            + file.getAbsolutePath());
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    try {
                        String line = br.readLine();
                        while (line != null) {
                            line = line.trim();
                            if (!line.isEmpty() && !line.startsWith("#")) {
                                performCommand(output, parser, line);
                            }
                            line = br.readLine();
                        }
                    } finally {
                        br.close();
                    }
                    // We just read the first file we find and stop.
                    break;
                }
            }
        } finally {
            // Show what happened when running the commands.
            logger.log(Level.INFO, sw.toString());
            parser.setOutput(savedOutput);
        }
    }

    /**
     * Process the given command line arguments.
     *
     * @param  args  command line arguments.
     * @throws  ParseException  if argument parsing fails.
     */
    private static void processArguments(String[] args) throws ParseException {
        Options options = new Options();
        // Option: h/help
        OptionBuilder.withDescription(NbBundle.getMessage(
                Main.class, "MSG_Main_Option_help"));
        OptionBuilder.withLongOpt("help");
        options.addOption(OptionBuilder.create("h"));

        // Option: attach <port>
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("port");
        OptionBuilder.withDescription(NbBundle.getMessage(
                Main.class, "MSG_Main_Option_attach"));
        options.addOption(OptionBuilder.create("attach"));

        // Option: sourcepath <path>
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("path");
        OptionBuilder.withDescription(NbBundle.getMessage(
                Main.class, "MSG_Main_Option_sourcepath"));
        options.addOption(OptionBuilder.create("sourcepath"));

        // Option: e/emacs
        OptionBuilder.withDescription(NbBundle.getMessage(
                Main.class, "MSG_Main_Option_jdb"));
        options.addOption(OptionBuilder.create("jdb"));

        // Parse the command line arguments.
        CommandLineParser parser = new GnuParser();
        CommandLine line = parser.parse(options, args);

        // Interrogate the command line options.
        jdbEmulationMode = line.hasOption("jdb");
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java com.bluemarsh.jswat.console.Main", options);
            System.exit(0);
        }
        if (line.hasOption("sourcepath")) {
            Session session = SessionProvider.getCurrentSession();
            PathManager pm = PathProvider.getPathManager(session);
            String path = line.getOptionValue("sourcepath");
            List<String> roots = Strings.stringToList(path, File.pathSeparator);
            pm.setSourcePath(roots);
        }
        if (line.hasOption("attach")) {
            final Session session = SessionProvider.getCurrentSession();
            String port = line.getOptionValue("attach");
            ConnectionFactory factory = ConnectionProvider.getConnectionFactory();
            final JvmConnection connection;
            try {
                connection = factory.createSocket("localhost", port);
                // The actual connection may be made some time from now,
                // so set up a listener to be notified at that time.
                connection.addConnectionListener(new ConnectionListener() {

                    @Override
                    public void connected(ConnectionEvent event) {
                        if (session.isConnected()) {
                            // The user already connected to something else.
                            JvmConnection c = event.getConnection();
                            c.getVM().dispose();
                            c.disconnect();
                        } else {
                            session.connect(connection);
                        }
                    }
                });
                connection.connect();
            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Returns {@code true} if we are attempting to emulate JDB
     * enough to run within clients (such as Emacs) that may invoke
     * JDB as a subprocess and parse its output.
     *
     * @return  true if emulating JDB output, false otherwise.
     */
    public static boolean emulateJDB() {
        return jdbEmulationMode;
    }
}
