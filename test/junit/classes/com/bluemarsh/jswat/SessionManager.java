/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: SessionManager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.graphical.GraphicalAdapter;
import com.bluemarsh.jswat.util.JVMArguments;
import com.bluemarsh.jswat.util.SessionSettings;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Class SessionManager manages Sessions. It starts and stops a single
 * Session instance. Attempts to call the operative methods repeatedly
 * are silently ignored, by design.
 *
 * @author  Nathan Fiedler
 */
public class SessionManager {
    /** Reference to the single open Session. */
    private static Session openSession;
    /** Number of times beginSession() has been called, minus the number
     * of times endSession() was called. */
    private static int openCount;
    /** Number of times launchDebuggee/launchSimple() has been called,
     * minus the number of times deactivate() was called. */
    private static int activeCount;
    /** Original value of the stopOnMain preference setting. */
    private static boolean originalStopOnMain;
    /** Name of the original session settings. */
    private static String originalSettings;
    /** Name of the unit testing session. */
    private static final String TEST_SESSION = "unitest";
    /** Our session listener. */
    private static TestSessionListener listener;

    static {
        listener = new TestSessionListener();
        // Perform the main initialization once.
        Main.init();
        Main.setUIAdapter(GraphicalAdapter.class);
    }

    /**
     * Request the open Session, possibly creating it if needed. The
     * Session may already be open in which case this method returns
     * that reference. The caller must follow up this method call with a
     * call to endSession().
     *
     * @return  open Session.
     * @see #endSession()
     */
    public static synchronized Session beginSession() {
        if (openSession == null) {
            openSession = Main.newSession();
            openSession.addListener(listener);
            // Ensure we don't stop unexpectedly.
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/breakpoint");
            originalStopOnMain = prefs.getBoolean(
                "stopOnMain", Defaults.STOP_ON_MAIN);
            prefs.putBoolean("stopOnMain", false);
            // Load a special unit testing session to mess around with.
            originalSettings = SessionSettings.currentSettings();
            SessionSettings.loadSettings(TEST_SESSION);
        }
        openCount++;
        return openSession;
    } // beginSession

    /**
     * Terminate the active debugging session.
     *
     * @param  forceExit  true to force debuggee to exit; always true
     *                    if the debuggee was launched.
     */
    public static void deactivate(boolean forceExit) {
        if (openSession == null) {
            throw new IllegalStateException("session not open");
        }
        if (activeCount > 0 && --activeCount == 0) {
            openSession.deactivate(forceExit, openSession);
        }
    } // deactivate

    /**
     * Signal the end of the caller's use of the open Session. The
     * Session will be terminated when the open count reaches zero.
     *
     * @see #beginSession()
     */
    public static synchronized void endSession() {
        if (openCount > 0 && --openCount == 0) {
            SessionSettings.loadSettings(originalSettings);
            // Restore the original setting.
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/breakpoint");
            prefs.putBoolean("stopOnMain", originalStopOnMain);
            // We do not want to exit the JVM.
            openSession.removeListener(listener);
            Main.endSession(openSession, false);
            try {
                SessionSettings.deleteSettings(TEST_SESSION);
            } catch (BackingStoreException bse) {
                bse.printStackTrace();
            }
            openSession = null;
        }
    } // endSession

    /**
     * Returns true if this session is active and attached to a virtual
     * machine.
     *
     * @return  true if active, false otherwise.
     */
    public static boolean isActive() {
        return activeCount > 0;
    } // isActive

    /**
     * Indicates of the Session is presently open.
     *
     * @return  true if Session is open, false otherwise.
     */
    public static boolean isOpen() {
        return openCount > 0;
    } // isOpen

    /**
     * Builds a VMConnection and activates the open Session using that
     * connection. Causes the debuggee to be created but will be left
     * suspended.
     *
     * @param  javaHome       home of JVM or null for default.
     * @param  jvmExecutable  name of JVM executable file or null for default.
     * @param  options        VM options to pass or null for none.
     * @param  cmdline        class to launch (with optional arguments).
     */
    public static void launchDebuggee(String javaHome,
                                      String jvmExecutable,
                                      String options,
                                      String cmdline) {
        if (openSession == null) {
            throw new IllegalStateException("session not open");
        }
        PathManager pm = (PathManager) openSession.getManager(
            PathManager.class);
        // Unit tests must be run from the top-level directory.
        pm.setClassPath("test/build/classes");
        pm.setSourcePath("test/classes");
        // Must pass the classpath to the debuggee.
        JVMArguments jvmArgs = new JVMArguments(
            (options == null ? "" : options) + ' ' + cmdline);
        String classpath = pm.getClassPathAsString();
        String jvmopts = jvmArgs.normalizedOptions(classpath);
        // Build the VM connection.
        VMConnection conn = VMConnection.buildConnection(
            javaHome, jvmExecutable, jvmopts, cmdline);
        conn.launchDebuggee(openSession, false);
        activeCount++;
    } // launchDebuggee

    /**
     * Builds a VMConnection and activates the open Session using that
     * connection. Causes the debuggee to be created but will be left
     * suspended. Does nothing if the Session is already active, even
     * with a different 'main' argument.
     *
     * @param  main  class to launch (with optional arguments).
     */
    public static void launchSimple(String main) {
        if (openSession == null) {
            throw new IllegalStateException("session not open");
        }
        if (!isActive()) {
            launchDebuggee(null, null, null, main);
        } else {
            activeCount++;
        }
    } // launchSimple

    /**
     * Sets the UI adapter assigned to new Sessions.
     *
     * @param  clazz  new ui adapter class.
     * @return  previous ui adapter class.
     */
    public static Class setUIAdapter(Class clazz) {
        Class old = Main.getAdapterClass();
        Main.setUIAdapter(clazz);
        return old;
    } // setUIAdapter

    /**
     * Resume the currently open session and wait for it to suspend again.
     * Typically this means the method will not return until a breakpoint
     * has been hit.
     */
    public static synchronized void resumeAndWait() {
        if (openSession == null) {
            throw new IllegalStateException("session not open");
        }

        listener.resetSuspended();
        openSession.resumeVM(listener, true, true);
        try {
            synchronized (listener) {
                // Wait until the listener has been suspended.
                while (!listener.isSuspended()) {
                    listener.wait(1000);
                }
            }
        } catch (InterruptedException ie) {
            // nothing to do
        }
    }

    /**
     * Listens to the session for the suspend event.
     */
    protected static class TestSessionListener implements SessionListener {
        private boolean suspended;

        public void activated(SessionEvent sevt) {
        }

        public void closing(SessionEvent sevt) {
        }

        public synchronized void deactivated(SessionEvent sevt) {
            // This is equivalent to being suspended for our purposes.
            suspended = true;
            notifyAll();
        }

        public synchronized boolean isSuspended() {
            return suspended;
        }

        public void opened(com.bluemarsh.jswat.Session session) {
        }

        public synchronized void resetSuspended() {
            suspended = false;
        }

        public synchronized void resuming(SessionEvent sevt) {
            suspended = false;
        }

        public synchronized void suspended(SessionEvent sevt) {
            suspended = true;
            notifyAll();
        }
    }
} // SessionManager
