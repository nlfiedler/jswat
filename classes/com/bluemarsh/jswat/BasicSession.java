/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: BasicSession.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Names;
import com.bluemarsh.jswat.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.prefs.PreferenceChangeEvent;

/**
 * This class provides a basic implementation of a Session.
 *
 * @author  Nathan Fiedler
 */
public class BasicSession extends AbstractSession implements VMEventListener {
    /** Session is brand new. */
    private static final int SESSION_NEW = 1;
    /** Session is active. */
    private static final int SESSION_ACTIVE = 2;
    /** Session is inactive. */
    private static final int SESSION_INACTIVE = 3;
    /** Session is presently activating. */
    private static final int SESSION_ACTIVATING = 4;
    /** Session is presently deactivating. */
    private static final int SESSION_DEACTIVATING = 5;
    /** Session is presently closing. */
    private static final int SESSION_CLOSING = 6;
    /** Session is permanently closed. */
    private static final int SESSION_CLOSED = 7;
    /** Our very own user interface adapter. */
    private UIAdapter interfaceAdapter;
    /** Status log to which messages are written. */
    private Log statusLog;
    /** VMConnection for connecting to the debuggee VM. */
    private VMConnection vmConnection;
    /** State of the Session at this time. */
    private int sessionState;
    /** True when the launched VM has been started. Access to this
     * variable is controlled by the vmStartedLock object. */
    private boolean vmHasStarted;
    /** Used for notifying waiters that VM has started. */
    private Object vmStartedLock;

    /**
     * Creates a Session object. The session starts off in an inactive
     * state and must be initialized using the <code>init()</code>
     * method.
     *
     * @see #init
     */
    public BasicSession() {
        super();
        vmStartedLock = new Object();
        sessionState = SESSION_NEW;
        // Fetch or create all the objects we need.
        logger().info("creating status log");
        statusLog = new Log();
    }

    public synchronized void activate(VMConnection connection,
                                      boolean showSource,
                                      Object source) {
        if (sessionState != SESSION_INACTIVE) {
            throw new IllegalStateException("must be 'inactive'");
        }
        logger().info("activating the session");
        sessionState = SESSION_ACTIVATING;
        boolean success = false;
        try {
            SessionEvent se = new SessionEvent(this, source, false);
            VMEventManager vmeman = (VMEventManager)
                getManager(VMEventManager.class);
            vmConnection = connection;

            // Become a listener to several key VM events.
            // We want to be high priority to get the events
            // before others, to set the context manager.
            // We listen for the start event so that our event manager
            // does not automatically resume the debuggee.
            vmeman.addListener(VMStartEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);
            vmeman.addListener(VMDisconnectEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);
            vmeman.addListener(StepEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);

            // After we're added as an event listener, we can
            // activate the listeners (including the VMEventManager).
            // (we are 'active' as far as the listeners are concerned)
            sessionState = SESSION_ACTIVE;
            getListeners().activateAll(se);

            if (connection.isRemote()) {
                // In the remote case we can safely assume the VM is ready.
                setStatus(Bundle.getString("Session.vmAttached"));
            } else {
                synchronized (vmStartedLock) {
                    if (!vmHasStarted) {
                        // In the launching case, we need to wait until the
                        // VMStartEvent has been received.
                        try {
                            vmStartedLock.wait();
                        } catch (InterruptedException ie) {
                            // ignored
                        }
                    }
                }
                setStatus(Bundle.getString("Session.vmLoaded"));
            }

            // Let the listeners know if we're running or not.
            if (isSuspended()) {
                getListeners().suspendAll(se);
            } else {
                getListeners().resumeAll(se);
            }

            // Set the default stratum to the user preference, but only if
            // the JDI version is 1.4 or higher.
            VirtualMachine vm = connection.getVM();
            if (vm.canGetSourceDebugExtension()) {
                String stratum = preferences().get(
                        "defaultStratum", Defaults.STRATUM);
                if (stratum != null && stratum.length() > 0) {
                    vm.setDefaultStratum(stratum);
                }
            }

            if (showSource) {
                // Show the source file for the main class.
                String main = connection.getMainClass();
                if (main != null) {
                    showSourceFile(main);
                }
            }
            success = true;
            logger().info("session active");
        } finally {
            if (!success) {
                sessionState = SESSION_INACTIVE;
                logger().info("session still inactive after error");
            }
        }
    }

    public synchronized void close(Object source) {
        if (sessionState != SESSION_INACTIVE) {
            throw new IllegalStateException("must be 'inactive'");
        }
        logger().info("session closing");
        sessionState = SESSION_CLOSING;
        boolean success = false;
        try {
            // Close all the listeners.
            SessionEvent se = new SessionEvent(this, source, false);
            getListeners().closeAll(se);
            // Clear all the lists and references.
            getListeners().clear();
            removeAllManagers();
            success = true;
        } finally {
            if (success) {
                sessionState = SESSION_CLOSED;
            } else {
                sessionState = SESSION_INACTIVE;
            }
        }
        logger().info("session closed");
    }

    public synchronized void deactivate(boolean forceExit, Object source) {
        if (sessionState != SESSION_ACTIVE) {
            throw new IllegalStateException("must be 'active'");
        }
        logger().info("deactivating the session");
        sessionState = SESSION_DEACTIVATING;
        boolean success = false;
        try {
            // Shutdown the running VM before stopping the event handler,
            // so other objects can get the disconnect events.
            VirtualMachine vm = vmConnection.getVM();
            if (vm != null) {
                try {
                    if (vm.process() == null && !forceExit) {
                        // We attached to the VM, so simply detach.
                        logger().info("disposing of debuggee VM");
                        vm.dispose();
                        logger().info("debuggee VM disposed");
                        setStatus(Bundle.getString("Session.vmDetached"));
                    } else {
                        // We launched the VM or the caller wants us to
                        // forcibly kill the debuggee, so bring it down.
                        logger().info("forcing debuggee VM to exit");
                        vm.exit(1);
                        logger().info("debuggee VM terminated");
                        setStatus(Bundle.getString("Session.vmClosed"));
                    }
                } catch (VMDisconnectedException vmde) {
                    // This can happen.
                    logger().info("vm disconnected in deactivate()");
                }

            } else {
                if (vmConnection.isRemote()) {
                    setStatus(Bundle.getString("Session.vmDetached"));
                } else {
                    setStatus(Bundle.getString("Session.vmClosed"));
                }
            }

            disconnected(source);
            success = true;
            logger().info("session inactive");
            synchronized (vmStartedLock) {
                vmHasStarted = false;
            }
        } finally {
            if (!success) {
                sessionState = SESSION_ACTIVE;
                logger().info("session still active after error");
            }
        }
    }

    public void disconnected(Object source) {
        vmConnection.disconnect();
        // Remove ourselves as an event listener.
        VMEventManager vmeman = (VMEventManager)
            getManager(VMEventManager.class);
        vmeman.removeListener(StepEvent.class, this);
        vmeman.removeListener(VMDisconnectEvent.class, this);
        vmeman.removeListener(VMStartEvent.class, this);

        // Notify the listeners that the debugging session has ended.
        // (we are 'inactive' as far as the listeners are concerned)
        sessionState = SESSION_INACTIVE;
        SessionEvent se = new SessionEvent(this, source, false);
        getListeners().deactivateAll(se);
    }

    public boolean eventOccurred(Event e) {
        if (e instanceof VMStartEvent) {
            synchronized (vmStartedLock) {
                vmHasStarted = true;
                vmStartedLock.notify();
            }
        } else if (e instanceof VMDisconnectEvent) {
            // The debuggee VM disconnected; close up shop.
            // When the VM is already disconnected, simply set our
            // reference to null. There's nothing else we can do.
            vmConnection.disconnect();
            setStatus(Bundle.getString("Session.vmDisconnected"));
            if (isActive()) {
                // Call this only if we're still active.
                deactivate(false, this);
            }

        } else if (e instanceof StepEvent) {
            handleDebugEvent((LocatableEvent) e);
        }

        // Most of the time we don't resume the debuggee VM.
        return false;
    }

    public VMConnection getConnection() {
        return vmConnection;
    }

    public Log getStatusLog() {
        return statusLog;
    }

    public VirtualMachine getVM() {
        return vmConnection == null ? null : vmConnection.getVM();
    }

    public UIAdapter getUIAdapter() {
        return interfaceAdapter;
    } // getUIAdapter

    public void handleDebugEvent(Event event) {
        if (!isActive()) {
            // We're not active, there's nothing to do.
            return;
        }

        // Have the interface adapter bring the debugger forward.
        if (preferences().getBoolean("raiseWindow", Defaults.RAISE_WINDOW)) {
            interfaceAdapter.bringForward();
        }

        if (event instanceof LocatableEvent) {
            LocatableEvent le = (LocatableEvent) event;
            // Show where the event occurred.
            showEventLocation(le);

            // Show the source code for this location.
            showSourceFile(le.location());

            // Set the debugger context.
            ContextManager contextManager = (ContextManager)
                getManager(ContextManager.class);
            contextManager.setCurrentLocation(le, false);
        }

        // Treat such events as suspending the debuggee.
        // Must do this after setting the context.
        SessionEvent se = new SessionEvent(this, this, false);
        // We assume here that the event has suspended some threads in
        // the debuggee. If we were unsure, we should check the suspend
        // policy of the event request for this event.
        getListeners().suspendAll(se);
    }

    public synchronized void init(UIAdapter uiadapter) {
        if (sessionState != SESSION_NEW) {
            // We are no longer new, we can't initialize again.
            throw new IllegalStateException("must be 'new'");
        }
        sessionState = SESSION_INACTIVE;
        interfaceAdapter = uiadapter;
    }

    public void initComplete() {
        // Some managers need to be loaded at startup, but only after
        // the UI adapter is completely ready.
        String managerList = Bundle.getString("startupManagers");
        String[] managers = Strings.tokenize(managerList);
        for (int ii = 0; ii < managers.length; ii++) {
            try {
                getManager(Class.forName(managers[ii]));
            } catch (ClassNotFoundException cnfe) {
                String msg = MessageFormat.format(
                    Bundle.getString("Session.mgrClassNotFound"),
                    new Object[] { managers[ii] });
                getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING, msg);
            }
        }
    }

    public boolean isActive() {
        return sessionState == SESSION_ACTIVE;
    }

    /**
     * Determines if the debuggee VM has any running threads or not.
     *
     * @return  true if all threads are suspended, false otherwise.
     */
    private boolean isSuspended() {
        Iterator iter = vmConnection.getVM().allThreads().iterator();
        // Assume they are all suspended.
        boolean allSuspended = true;
        while (iter.hasNext()) {
            ThreadReference thread = (ThreadReference) iter.next();
            try {
                // Checks if the thread was suspended by the debugger.
                if (!thread.isSuspended()) {
                    allSuspended = false;
                    break;
                }
            } catch (ObjectCollectedException oce) {
                // We can ignore that thread then.
            }
        }
        return allSuspended;
    }

    public void preferenceChange(PreferenceChangeEvent evt) {
        super.preferenceChange(evt);
        VMConnection conn = getConnection();
        if (conn != null) {
            VirtualMachine vm = conn.getVM();
            if (vm != null && vm.canGetSourceDebugExtension()) {
                String stratum = preferences().get(
                        "defaultStratum", Defaults.STRATUM);
                if (stratum.length() == 0) {
                    stratum = null;
                }
                vm.setDefaultStratum(stratum);
            }
        }
    }

    public void resumeVM(Object source, boolean brief, boolean quiet) {
        if (!isActive()) {
            logger().severe("session not active, cannot resume");
            throw new IllegalStateException("session not active");
        }
        SessionEvent se = new SessionEvent(this, source, brief);
        // Reset both the current thread and location.
        ContextManager contextManager = (ContextManager)
            getManager(ContextManager.class);
        contextManager.setCurrentLocation(null, brief);
        getListeners().resumeAll(se);
        if (!quiet) {
            setStatus(Bundle.getString("Session.vmRunning"));
        }
        // Call this after resetting the current location. Otherwise a
        // breakpoint may be hit and the event may fire off
        // asynchronously and we'll then return and clear the current
        // location, just after the breakpoint event had set the current
        // location. Fixes bugs 1 and 2.
        logger().info("resuming debuggee VM");
        vmConnection.getVM().resume();
        logger().info("debuggee VM resumed");
    }

    /**
     * Set the status indicator using a short message. This will call
     * into the interface adapter's <code>showMessage()</code> method.
     *
     * @param  status  short message indicating program status.
     */
    private void setStatus(String status) {
        interfaceAdapter.showMessage(UIAdapter.MESSAGE_NOTICE, status);
    }

    /**
     * Shows the location of the event, including the class type, method
     * name, and possibly the source file name and line number (if
     * available).
     *
     * @param  le  LocatableEvent whose location will be displayed.
     */
    protected void showEventLocation(LocatableEvent le) {
        // Show the current thread name.
        StringBuffer buf = new StringBuffer(80);
        buf.append('[');
        buf.append(le.thread().name());
        buf.append("] ");

        Location loc = le.location();
        // Show the class type and method name.
        String tname = loc.declaringType().name();
        tname = Names.justTheName(tname);
        buf.append(tname);
        buf.append('.');
        buf.append(loc.method().name());
        try {
            // Try to show the source file name and line number.
            String source = loc.sourceName();
            buf.append(" (");
            buf.append(source);
            buf.append(':');
            buf.append(loc.lineNumber());
            buf.append(')');
        } catch (AbsentInformationException aie) {
            // Oh well, no source name then.
        }
        // Do not show this as a notice message.
        statusLog.writeln(buf.toString());
    }

    /**
     * Show the source file for the given class.
     *
     * @param  loc  location for which to show the source.
     */
    protected void showSourceFile(Location loc) {
        if (!interfaceAdapter.canShowFile()) {
            // Can't show source files, so don't bother.
            return;
        }

        // Try to ensure that the source file is opened.
        PathManager pathman = (PathManager)
            getManager(PathManager.class);
        // This will return the already mapped File, if one exists.
        try {
            SourceSource src = pathman.mapSource(loc);
            if (src != null && src.exists()) {
                interfaceAdapter.showFile(src, loc.lineNumber(), 0);
            } else {
                interfaceAdapter.showMessage(
                    UIAdapter.MESSAGE_WARNING,
                    Bundle.getString("Session.couldntMapSrcFile")
                    + " (Method: " + loc.method() + ')');
            }
        } catch (IOException ioe) {
            interfaceAdapter.showMessage(
                UIAdapter.MESSAGE_WARNING,
                Bundle.getString("Session.showFileError") + ": " + ioe);
        }
    }

    /**
     * Show the source file for the given class.
     *
     * @param  classname  Fully-qualified name of class.
     */
    protected void showSourceFile(String classname) {
        if (!interfaceAdapter.canShowFile()) {
            // Can't show source files, so don't bother.
            return;
        }

        // Try to ensure that the source file is opened.
        PathManager pathman = (PathManager)
            getManager(PathManager.class);
        // This will return the already mapped File, if one exists.
        try {
            SourceSource src = pathman.mapSource(classname);
            if (src != null && src.exists()) {
                interfaceAdapter.showFile(src, 0, 0);
            } else {
                interfaceAdapter.showMessage(
                    UIAdapter.MESSAGE_WARNING,
                    Bundle.getString("Session.couldntMapSrcFile")
                    + " (Class: " + classname + ')');
            }
        } catch (IOException ioe) {
            interfaceAdapter.showMessage(
                UIAdapter.MESSAGE_WARNING,
                Bundle.getString("Session.showFileError") + ": " + ioe);
        }
    }

    public void suspendVM(Object source) {
        if (!isActive()) {
            logger().severe("session not active, cannot suspend");
            throw new IllegalStateException("session not active");
        }
        // See if there are any running threads.
        if (!isSuspended()) {
            // There are still running threads, let us suspend them.
            logger().info("suspending debuggee VM");
            vmConnection.getVM().suspend();
            logger().info("debuggee VM suspended");
            setStatus(Bundle.getString("Session.vmSuspended"));
            SessionEvent se = new SessionEvent(this, source, false);
            getListeners().suspendAll(se);
        }
    }
}
