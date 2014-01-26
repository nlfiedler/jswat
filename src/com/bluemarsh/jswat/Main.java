/*********************************************************************
 *
 *      Copyright (C) 2000-2003 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        Main.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/13/01        Initial version
 *      nf      08/25/01        Fixed bug 199
 *      nf      08/26/01        Fixed bug 195
 *      nf      09/11/01        Making this a little more reusable
 *      nf      04/04/02        Implemented RFE 420
 *
 * $Id: Main.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.ui.console.ConsoleAdapter;
import com.bluemarsh.jswat.ui.graphical.GraphicalAdapter;
import com.sun.jdi.Bootstrap;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.LogManager;
import java.util.prefs.Preferences;

/**
 * Class Main is the bootstrap for JSwat when launched from the command
 * line or via a shortcut. It creates a Session and an instance of a
 * UIAdapter, which is responsible for providing the user interface.
 * This class has several methods for managing the open Sessions, such
 * as creating new ones and terminating old ones.
 *
 * @author  Nathan Fiedler
 */
public class Main {
    /** The class of the UIAdapter to be used when creating new Sessions. */
    private static Class adapterClass;
    /** List of the open Sessions. */
    private static Vector openSessions = new Vector();

    /**
     * Detects if the JPDA classes (in particular, com.sun.jdi.Bootstrap)
     * are available. If so, the method silently returns. If not, an error
     * message is displayed and the adapter is asked to exit.
     *
     * @param  adapter  interface adapter in which to display message.
     * @return  true if JPDA was detected, false otherwise.
     */
    public static boolean detectJPDA(UIAdapter adapter) {
        try {
            // Test for the JDI package before we continue.
            Bootstrap.virtualMachineManager();
            return true;
        } catch (NoClassDefFoundError ncdfe) {
            adapter.showMessage(UIAdapter.MESSAGE_ERROR,
                                Bundle.getString("missingJPDA"));
            adapter.exit();
            return false;
        }
    } // detectJPDA

    /**
     * Terminate the given Session. This is the same as calling
     * <code>endSession(session, true)</code> (allow the JVM to
     * exit).
     *
     * @param  session  Session to be ended.
     */
    public static void endSession(Session session) {
        endSession(session, true);
    } // endSession

    /**
     * Terminate the given Session.
     *
     * @param  session    Session to be ended.
     * @param  allowExit  true to allow exiting the JVM.
     */
    public static void endSession(Session session, boolean allowExit) {
        UIAdapter uiAdapter = session.getUIAdapter();
        uiAdapter.saveSettings();
        // Must deactivate session before destroying interface.
        if (session.isActive()) {
            session.deactivate(false, session);
        }
        uiAdapter.destroyInterface();
        session.close(session);
        openSessions.remove(session);

        if (allowExit && openSessions.size() == 0) {
            // No more open sessions, notify the adapter so it
            // takes the appropriate action.
            uiAdapter.exit();
        }
    } // endSession

    /**
     * Returns the class of the UI adapter.
     *
     * @return  class of ui adapter.
     */
    public static Class getAdapterClass() {
        return adapterClass;
    } // getAdapterClass

    /**
     * Performs basic program initialization. This method is typically
     * followed by invocation of the <code>setUIAdapter()</code> method,
     * followed by a call to <code>newSession()</code> to begin the
     * debugging session.
     *
     * @return  true if initialization successful, false otherwise
     *          (which generally means a critical failure occurred).
     */
    public static boolean init() {
        // Set the logging properties.
        LogManager manager = LogManager.getLogManager();
        InputStream is = Main.class.getResourceAsStream("logging.properties");
        try {
            manager.readConfiguration(is);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    } // init

    /**
     * Main method for JSwat program. Gets things started. Note that
     * this returns to the JVM but the app still runs. It is an event-
     * driven application and thus will not exit until we call
     * <code>System.exit()</code>.
     *
     * @param  args  list of command-line arguments.
     */
    public static void main(String[] args) {
        // Call the usual initialization code.
        if (!init()) {
            // An appropriate error message was already printed.
            System.exit(1);
        }

        // Default to the graphical adapter.
        setUIAdapter(GraphicalAdapter.class);

        // Check for the -console switch.
        if (args.length > 0) {
            if (args[0].equals("-console")) {
                // Use the console adapter.
                setUIAdapter(ConsoleAdapter.class);
                // Remove the -console switch from the arguments.
                String[] newargs = new String[args.length - 1];
                System.arraycopy(args, 1, newargs, 0, newargs.length);
                args = newargs;
            }
        }

        // Create the interface adapter.
        UIAdapter adapter = newUIAdapter();

        // Detect if the JPDA classes exist.
        detectJPDA(adapter);

        // Start the program by creating a Session.
        Session session = newSession(adapter);

        // Tell the upgrading user that the settings moved.
        File settingsFile = new File(System.getProperty("user.home"),
                                     ".jswat" + File.separator + "settings");
        Preferences prefs = Preferences.userRoot().node("com/bluemarsh/jswat");
        if (settingsFile.exists() && !prefs.getBoolean("ran2.0", false)) {
            // Display the 2.0 upgrading message.
            adapter.showURL(Bundle.getResource("upgrade2xFile"),
                            Bundle.getString("upgrade2xTitle"));
        }
        prefs.putBoolean("ran2.0", true);

        String useShmem = session.getProperty("useShmem");
        if (useShmem != null) {
            session.setProperty("useShare", useShmem);
            session.setProperty("useShmem", null);
        }

        if (args.length > 0) {
            // User has (hopefully) given us a command to run.
            StringBuffer buf = new StringBuffer(80);
            buf.append(args[0]);
            for (int i = 1; i < args.length; i++) {
                buf.append(' ');
                buf.append(args[i]);
            }
            // Pass the arguments to the command manager.
            CommandManager cmdman = (CommandManager)
                session.getManager(CommandManager.class);
            try {
                cmdman.parseInput(buf.toString());
            } catch (Exception e) {
                session.getStatusLog().writeStackTrace(e);
            }
        }
    } // main

    /**
     * Create a new Session and user interface.
     *
     * @return  new instance of Session, or null if error.
     */
    public static Session newSession() {
        UIAdapter adapter = newUIAdapter();
        if (adapter != null) {
            return newSession(adapter);
        } else {
            return null;
        }
    } // newSession

    /**
     * Create a new Session for the given user interface.
     *
     * @param  adapter  interface adapter for the session.
     * @return  new instance of Session.
     */
    public static Session newSession(UIAdapter adapter) {
        // This sequence of events has been carefully developed.
        Session session = new BasicSession();
        session.init(adapter);
        adapter.init(session);
        adapter.buildInterface();
        session.initComplete();
        adapter.initComplete();
        openSessions.add(session);
        return session;
    } // newSession

    /**
     * Constructs an instance of the UIAdapter and returns it.
     *
     * @return  new adapter, or null if an error occurred.
     */
    public static UIAdapter newUIAdapter() {
        UIAdapter adapter = null;
        try {
            adapter = (UIAdapter) adapterClass.newInstance();
        } catch (InstantiationException ie) {
            ie.printStackTrace();
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        return adapter;
    } // newUIAdapter

    /**
     * Sets the concrete implementatioin class of UIAdapter to be used
     * when creating new Sessions. The UI adapter must implement a
     * public constructor that takes a single argument of type Session.
     *
     * @param  adapter  class of UIAdapter.
     */
    public static void setUIAdapter(Class adapter) {
        if (!UIAdapter.class.isAssignableFrom(adapter)) {
            throw new IllegalArgumentException(
                "adapter does not extend UIAdapter");
        }
        adapterClass = adapter;
    } // setUIAdapter
} // Main
