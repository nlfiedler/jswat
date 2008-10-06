/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CoreInstall.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.connect.ConnectionFactory;
import com.bluemarsh.jswat.core.connect.ConnectionProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.path.PathConverter;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.runtime.JavaRuntime;
import com.bluemarsh.jswat.core.runtime.RuntimeFactory;
import com.bluemarsh.jswat.core.runtime.RuntimeManager;
import com.bluemarsh.jswat.core.runtime.RuntimeProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionFactory;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Strings;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * Manages the core module's lifecycle.
 *
 * @author  Nathan Fiedler
 */
public class CoreInstall extends ModuleInstall implements Runnable, WindowListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Identifier for the special launch-API session. */
    private static final String LAUNCH_SESSION_ID = "LAUNCH";

    /**
     * Called when all modules agreed with closing and the IDE will be closed.
     */
    public void close() {
        // Save the runtimes to persistent storage.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        rm.saveRuntimes();
        // Save the sessions to persistent storage.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.saveSessions(true);
        super.close();
    }

    /**
     * Connect to the remote debuggee using the system property values.
     */
    private void connect() {
        String transport = System.getProperty("jswat.transport");
        String address = System.getProperty("jswat.address");
        // Without transport setting, nothing is going to be done.
        if (transport != null) {
            Session session = SessionProvider.getCurrentSession();
            if (address == null || address.length() == 0) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "API ERROR: jswat.address must be specified");
                return;
            } else {
                // Try to connect to the debuggee.
                JvmConnection connection = null;
                try {
                    ConnectionFactory factory =
                            ConnectionProvider.getConnectionFactory();
                    if (transport.equals("dt_socket")) {
                        // Split address into host and port values.
                        int idx = address.indexOf(':');
                        String host = "";
                        String port = null;
                        if (idx > 0) {
                            host = address.substring(0, idx);
                            port = address.substring(idx + 1);
                        } else {
                            port = address;
                        }
                        connection = factory.createSocket(host, port);
                        session.setProperty(Session.PROP_CONNECTOR,
                                Session.PREF_SOCKET);
                        session.setProperty(Session.PROP_SOCKET_HOST, host);
                        session.setProperty(Session.PROP_SOCKET_PORT, port);
                    } else if (transport.equals("dt_shmem")) {
                        connection = factory.createShared(address);
                        session.setProperty(Session.PROP_CONNECTOR,
                                Session.PREF_SHARED);
                        session.setProperty(Session.PROP_SHARED_NAME, address);
                    } else {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "API ERROR: jswat.transport has unknown value, " +
                                "should be dt_socket or dt_shmem");
                        return;
                    }
                    connection.connect();
                    session.connect(connection);
                } catch (Exception e) {
                    ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                            "API ERROR: exception occurred: " + e);
                    return;
                }
            }

            // Set the sourcepath if one is given.
            setSourcePath(session);

            // If runto is given, set a breakpoint, and resume the debuggee.
            setBreakpoint(session);
        }
    }

    /**
     * Launch a local debuggee using the system property values.
     */
    private void launch() {
        Session session = SessionProvider.getCurrentSession();
        // These properties are mandatory.
        String launch = System.getProperty("jswat.launch");
        String cpath = System.getProperty("jswat.classpath");
        // These properties are optional.
        String jvmhome = System.getProperty("jswat.jvmhome");
        String jvmexec = System.getProperty("jswat.jvmexec");
        String jvmopts = System.getProperty("jswat.jvmopts");

        // Deal with the classpath setting.
        if (cpath == null || cpath.length() == 0) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "API ERROR: classpath is required with 'launch'");
            return;
        } else {
            PathManager pm = PathProvider.getPathManager(session);
            pm.setClassPath(Strings.stringToList(cpath, File.pathSeparator));
        }

        // Set up the runtime, if one is specified, else use the default.
        RuntimeManager rm = RuntimeProvider.getRuntimeManager();
        RuntimeFactory rf = RuntimeProvider.getRuntimeFactory();
        JavaRuntime rt = null;
        if (jvmhome != null) {
            // See if this runtime already exists, and if not, create it.
            rt = rm.findByBase(jvmhome);
            if (rt == null) {
                rt = rf.createRuntime(jvmhome, rm.generateIdentifier());
                rm.add(rt);
            }
            if (jvmexec != null && !jvmexec.equals(rt.getExec())) {
                rt.setExec(jvmexec);
            }
        } else {
            rt = rm.findByBase(rf.getDefaultBase());
        }

        // Create the JVM connection to launch the debuggee.
        JvmConnection connection = null;
        ConnectionFactory cf = ConnectionProvider.getConnectionFactory();
        if (jvmopts == null) {
            jvmopts = "-cp \"" + cpath + '"';
        } else {
            jvmopts += " -cp \"" + cpath + '"';
        }
        connection = cf.createLaunching(rt, jvmopts, launch);
        try {
            connection.connect();
            session.connect(connection);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                    "API ERROR: exception occurred: " + e);
            return;
        }

        String cname = launch.trim();
        String params = null;
        int idx = cname.indexOf(' ');
        if (idx > 0) {
            cname = launch.substring(0, idx);
            params = launch.substring(idx + 1);
        }
        session.setProperty(Session.PROP_CLASS_NAME, cname);
        session.setProperty(Session.PROP_CLASS_PARAMS, params);
        session.setProperty(Session.PROP_JAVA_PARAMS, jvmopts);
        session.setProperty(Session.PROP_RUNTIME_ID, rt.getIdentifier());

        // Set the sourcepath if one is given.
        setSourcePath(session);

        // If runto is given, set a breakpoint, and resume the debuggee.
        setBreakpoint(session);
    }

    /**
     * Called when an already-installed module is restored (during IDE startup).
     */
    public void restored() {
        super.restored();
        String transport = System.getProperty("jswat.transport");
        String launch = System.getProperty("jswat.launch");
        if (transport != null || launch != null) {
            // Wait for the main window to open so we avoid the output window
            // generating an exception about the default editor mode.
            EventQueue.invokeLater(this);
        }
    }

    public void run() {
        Frame frame = WindowManager.getDefault().getMainWindow();
        if (frame.isShowing()) {
            // At this point we know the user is invoking the launch API.
            // Set the special session as the current one, creating it
            // if necessary. It will be used to store the session settings.
            setCurrentSession();
            String transport = System.getProperty("jswat.transport");
            String launch = System.getProperty("jswat.launch");
            if (transport != null) {
                connect();
            } else if (launch != null) {
                launch();
            }
        } else {
            frame.addWindowListener(this);
        }
    }

    /**
     * Sets a breakpoint if the appropriate system property is defined,
     * then resumes the VM to cause the debuggee to hit the breakpoint.
     *
     * @param  session  Session for which to set sourcepath.
     */
    private void setBreakpoint(Session session) {
        String runto = System.getProperty("jswat.runto");
        if (runto != null) {
            // Parse the breakpoint specification, which is of the form
            //     <class>.<method>([<arglist>])
            // where <arglist> is a comma-separated list of argument types.
            int parenIdx = runto.indexOf('(');
            if (parenIdx == -1) {
                // Malformed breakpoint specification.
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "API ERROR: malformed breakpoint specification, missing (");
                return;
            }
            String method = runto.substring(0, parenIdx);
            int lastDotIdx = method.lastIndexOf('.');
            if (lastDotIdx == -1) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "API ERROR: malformed breakpoint specification, missing class name");
                return;
            }
            String klass = method.substring(0, lastDotIdx);
            method = method.substring(lastDotIdx + 1);
            int lastParenIdx = runto.lastIndexOf(')');
            if (lastParenIdx == -1) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "API ERROR: malformed breakpoint specification, missing )");
                return;
            }
            String arglist = runto.substring(parenIdx + 1, lastParenIdx);
            List<String> args = Strings.stringToList(arglist);
            BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
            try {
                Breakpoint bp = bf.createMethodBreakpoint(klass, method, args);
                BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
                bp.setExpireCount(1);
                bp.setDeleteOnExpire(true);
                bm.addBreakpoint(bp);
                session.resumeVM();
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.EXCEPTION,
                        "API ERROR: exception occurred: " + e);
                return;
            }
        }
    }

    /**
     * Locate the special session for the launch API to use. If it does not
     * exist, create it. Ultimately set this session as the current one.
     */
    private static void setCurrentSession() {
        SessionManager sm = SessionProvider.getSessionManager();
        Iterator<Session> sessions = sm.iterateSessions();
        Session session = null;
        // Find the session for use by the launch API.
        while (sessions.hasNext()) {
            Session s = sessions.next();
            if (s.getIdentifier().equals(LAUNCH_SESSION_ID)) {
                session = s;
                break;
            }
        }
        if (session == null) {
            // Our special session has not been created yet, or the
            // user deleted it.
            SessionFactory sf = SessionProvider.getSessionFactory();
            session = sf.createSession(LAUNCH_SESSION_ID);
            sm.add(session);
            session.setProperty(Session.PROP_SESSION_NAME, "Launch-API");
        }
        // Make it the current one so we can use it for launching.
        sm.setCurrent(session);
    }

    /**
     * Sets the sourcepath if the appropriate system property is defined.
     *
     * @param  session  Session for which to set sourcepath.
     */
    private void setSourcePath(Session session) {
        String sourcepath = System.getProperty("jswat.sourcepath");
        if (sourcepath != null) {
            PathManager pm = PathProvider.getPathManager(session);
            List<FileObject> roots = PathConverter.toFileObject(sourcepath);
            pm.setSourcePath(roots);
        }
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
        EventQueue.invokeLater(this);
        e.getWindow().removeWindowListener(this);
    }
}
