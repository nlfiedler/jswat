/*********************************************************************
 *
 *      Copyright (C) 2000-2002 Nathan Fiedler
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
 *      nf      04/04/02        Implemented 420
 *
 * DESCRIPTION:
 *      The main class of the program, Main, is defined in this
 *      file. It basically starts the program.
 *
 * $Id: Main.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.config.JConfigure;
import com.bluemarsh.jswat.ui.*;
import com.bluemarsh.jswat.util.JVMArguments;
import com.sun.jdi.Bootstrap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

/**
 * Class Main is the bootstrap for JSwat when launched from the
 * command line or via a shortcut. It creates a Session and an
 * instance of a UIAdapter, which is responsible for providing
 * the user interface. This class has several methods for
 * managing the open Sessions, such as creating new ones and
 * terminating old ones.
 *
 * @author  Nathan Fiedler
 */
public class Main {
    /** The class of the UIAdapter to be used when creating new Sessions. */
    protected static Class adapterClass;
    /** List of the open Sessions. */
    protected static Vector openSessions = new Vector();
    /** Reference to single instance of JSwat. */
    protected static JSwat swat;

    /**
     * Terminate the given Session.
     *
     * @param  session  Session to be ended.
     */
    public static void endSession(Session session) {
        UIAdapter uiAdapter = session.getUIAdapter();
        uiAdapter.saveSettings();
        AppSettings.instanceOf().commit();
        // Must deactivate session before destroying interface.
        if (session.isActive()) {
            session.deactivate(false);
        }
        uiAdapter.destroyInterface();
        session.close();
        openSessions.remove(session);

        if (openSessions.size() == 0) {
            // No more open sessions, notify the adapter so it
            // takes the appropriate action.
            uiAdapter.exit();
        }
    } // endSession

    /**
     * Performs basic program initialization. Creates an instance of the
     * <code>JSwat</code> class, loads the application preferences, and
     * migrates the old settings files to the new location.
     *
     * @param  appis  JSwat preferences file as a stream. This method
     *                reads the application preferences from this
     *                stream and merges them with the preferences in
     *                the user's home directory.
     * @return  true if initialization successful, false otherwise
     *          (which generally means a critical failure occurred).
     */
    public static boolean init(InputStream appis) {
        // Create a new JSwat object.
        swat = JSwat.instanceOf();

        File dir = new File(System.getProperty("user.home") +
                            File.separator + ".jswat");
        migrateOldFiles(dir);

        // Find the jswat preferences file in the user's home directory.
        JConfigure config = swat.getJConfigure();
        File f = new File(dir, "preferences");
        if (f.exists()) {
            try {
                InputStream fis = new FileInputStream(f);
                if (!config.upgrade(fis, appis)) {
                    throw new Error("Error upgrading user preferences!");
                }
                fis.close();
                // Must set the filename or it won't save preferences.
                config.setFilename(f.getCanonicalPath());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        } else {
            // Didn't find the preferences in the home directory,
            // try loading the default file.
            if (appis != null) {
                try {
                    if (!config.loadSettings(appis)) {
                        throw new Error("Error reading JSwat.preferences!");
                    }
                    // Must set the filename or it won't save preferences.
                    config.setFilename(f.getCanonicalPath());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
            } else {
                // Something terrible has happened.
                throw new Error("Cannot find JSwat.preferences!");
            }
        }
        try {
            appis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }

        // Load the jswat settings file.
        f = new File(dir, "settings");
        AppSettings props = AppSettings.instanceOf();
        props.load(f);
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
        try {
            // Test for the JDI package before we continue.
            Bootstrap.virtualMachineManager();
        } catch (NoClassDefFoundError ncdfe) {
            System.err.println("JSwat cannot find the JPDA package, and cannot work without it.");
            System.err.println("Make sure jpda.jar or tools.jar is in your classpath.");
            System.exit(1);
        }

        // Call the usual initialization code.
        if (!init(ClassLoader.getSystemResourceAsStream(
            "com/bluemarsh/jswat/resources/JSwat.preferences"))) {
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

        // Start the program by creating a Session.
        Session session = newSession();

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
            cmdman.parseInput(buf.toString());
        }
    } // main

    /**
     * Migrate the old JSwat files from the user's home directory to
     * the given directory.
     *
     * @param  dir  new jswat files directory (created if necessary).
     */
    protected static void migrateOldFiles(File dir) {
        // Make the .jswat directory unconditionally.
        dir.mkdir();

        String home = System.getProperty("user.home");
        File f = new File(home, ".jswat_settings");
        if (f.exists()) {
            f.renameTo(new File(dir, "settings"));
            f.delete();
        }

        f = new File(home, ".jswat_preferences");
        if (f.exists()) {
            f.renameTo(new File(dir, "preferences"));
            f.delete();
        }

        f = new File(home, ".jswat_session");
        if (f.exists()) {
            f.renameTo(new File(dir, "session"));
            f.delete();
        }

        f = new File(home, ".jswatrc");
        if (f.exists()) {
            f.renameTo(new File(dir, "jswatrc"));
            f.delete();
        }
    } // migrateOldFiles

    /**
     * Create a new Session and user interface.
     *
     * @return  new instance of Session, or null if error.
     */
    public static Session newSession() {
        // Construct Session but do not init() it yet.
        Session session = new Session();
        // Construct UI adapter and pass Session to it.
        Class[] paramTypes = new Class[] { Session.class };
        Constructor constructor = null;
        try {
            constructor = adapterClass.getConstructor(paramTypes);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            return null;
        }
        Object[] params = new Object[] { session };
        UIAdapter adapter;
        try {
            adapter = (UIAdapter) constructor.newInstance(params);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            return null;
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            return null;
        } catch (IllegalArgumentException iae2) {
            iae2.printStackTrace();
            return null;
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            return null;
        }
        session.init(adapter);
        adapter.buildInterface();
        adapter.initComplete();
        openSessions.add(session);
        return session;
    } // newSession

    /**
     * Sets the concrete implementatioin class of UIAdapter to be used
     * when creating new Sessions. The UI adapter must implement a
     * public constructor that takes a single argument of type Session.
     *
     * @param  adpater  class of UIAdapter.
     */
    public static void setUIAdapter(Class adapter) {
        if (!UIAdapter.class.isAssignableFrom(adapter)) {
            throw new IllegalArgumentException(
                "adapter does not extend UIAdapter");
        }
        adapterClass = adapter;
    } // setUIAdapter
} // Main
