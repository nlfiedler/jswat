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
 * $Id: Session.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.event.VMEventManager;
import com.bluemarsh.jswat.report.Category;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * This class is responsible for maintaining references to all of
 * the objects pertaining to an active debugging session. Session
 * is used to perform some basic operations on the debugging session.
 * Many of the useful objects in the debugging session are accessible
 * from this Session class.
 *
 * @author  Nathan Fiedler
 */
public class Session implements VMEventListener {
    /** Session is brand new. */
    protected static final int SESSION_NEW = 1;
    /** Session is active. */
    protected static final int SESSION_ACTIVE = 2;
    /** Session is inactive. */
    protected static final int SESSION_INACTIVE = 3;
    /** Session is presently activating. */
    protected static final int SESSION_ACTIVATING = 4;
    /** Session is presently deactivating. */
    protected static final int SESSION_DEACTIVATING = 5;
    /** Session is presently closing. */
    protected static final int SESSION_CLOSING = 6;
    /** Session is permanently closed. */
    protected static final int SESSION_CLOSED = 7;
    /** Reporting category. */
    protected static Category logCategory = Category.instanceOf("session");
    /** Our very own user interface adapter. */
    protected UIAdapter interfaceAdapter;
    /** Status log to which messages are written. */
    protected Log statusLog;
    /** VMConnection for connecting to the debuggee VM. */
    protected VMConnection vmConnection;
    /** Table of Manager objects keyed by their Class. Used to
     * retrieve managers from the Session. */
    protected Hashtable managerTable;
    /** List of SessionListener objects. */
    protected SessionListenerList listenerList;
    /** Session properties. */
    protected Properties sessionProperties;
    /** State of the Session at this time. */
    protected int sessionState;

    /**
     * Creates a Session object. The session starts off in an
     * inactive state and must be initialized using the
     * <code>init()</code> method.
     *
     * @see #init
     */
    public Session() {
        sessionState = SESSION_NEW;
        // Fetch or create all the objects we need.
        logCategory.report("creating log, manager table, and listener list");
        statusLog = new Log();  
        sessionProperties = new Properties();
        managerTable = new Hashtable();
        listenerList = new SessionListenerList(this);
    } // Session

    /**
     * Activates the Session now that a connection has been made. If
     * the new connection is different from the previous connection,
     * and there was a previous conneciton defined, the session
     * properties will be erased.
     *
     * @param  connection  VMConnection used to represent the debuggee
     *                     VM connection.
     * @exception  IllegalStateException
     *             Thrown if the session state is not 'inactive'.
     */
    public synchronized void activate(VMConnection connection) {
        if (sessionState != SESSION_INACTIVE) {
            throw new IllegalStateException("must be 'inactive'");
        }
        sessionState = SESSION_ACTIVATING;
        boolean success = false;
        try {
            logCategory.report("activating the session");

            vmConnection = connection;

            // Become a listener to several key VM events.
            // We want to be high priority to get the events
            // before others, to set the context manager.
            VMEventManager vmeman = (VMEventManager)
                getManager(VMEventManager.class);
            vmeman.addListener(VMStartEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);
            vmeman.addListener(VMDisconnectEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);
            vmeman.addListener(StepEvent.class, this,
                               VMEventListener.PRIORITY_SESSION);

            // After we're added as an event listener, we can
            // activate the listeners (including the VMEventManager).
            logCategory.report("activating session listeners");
            // (we are 'active' as far as the listeners are concerned)
            sessionState = SESSION_ACTIVE;
            listenerList.activateAll(this);
            success = true;
        } finally {
            if (!success) {
                sessionState = SESSION_INACTIVE;
            }
        }
        logCategory.report("session active");

        // Catch all uncaught exceptions.
        // As a by-product, initializes the breakpiont manager just
        // in time to create and resolve any persisted breakpoints.
        BreakpointManager brkman = (BreakpointManager)
            getManager(BreakpointManager.class);
        brkman.createExceptionCatch();

        Connector conn = connection.getConnector();
        if (conn instanceof LaunchingConnector) {
            setStatus(Bundle.getString("Session.vmLoaded"));
        } else if ((conn instanceof AttachingConnector) ||
                   (conn instanceof ListeningConnector)) {
            setStatus(Bundle.getString("Session.vmAttached"));
        }

        // Show the source file for the main class.
        String main = connection.getMainClass();
        if (main != null) {
            showSourceFile(main);
        }
    } // activate

    /**
     * Adds a SessionListener to this session. This method
     * calls the <code>init()</code> method on the listener.
     * It may also call the <code>activate()</code> method if
     * the Session is already active.
     *
     * @param  listener  SessionListener to add to this session.
     */
    public void addListener(SessionListener listener) {
        listenerList.add(listener, this, isActive());
        logCategory.report("adding session listener: " +
                           listener.getClass().getName());
    } // addListener

    /**
     * Shutdown the session permanently. This will save the session
     * properties to disk, close all the listeners, clear all lists
     * and set all references to null.
     *
     * @exception  IllegalStateException
     *             Thrown if session state is not 'inactive'.
     */
    public synchronized void close() {
        if (sessionState != SESSION_INACTIVE) {
            throw new IllegalStateException("must be 'inactive'");
        }
        sessionState = SESSION_CLOSING;
        boolean success = false;
        try {
            logCategory.report("session closing");

            saveProperties();

            // Close all the listeners.
            logCategory.report("closing session listeners");
            listenerList.closeAll(this);
            // Clear all the lists and references.
            listenerList.clear();
            listenerList = null;
            managerTable.clear();
            managerTable = null;
            statusLog = null;
            vmConnection = null;
            success = true;
        } finally {
            if (success) {
                sessionState = SESSION_CLOSED;
            } else {
                sessionState = SESSION_INACTIVE;
            }
        }
        logCategory.report("session closed");
    } // close

    /**
     * Set this session inactive, clearing all the objects associated
     * with this session. If the debuggee VM is active, it is brought down.
     * If the debuggee was remote, the session disconnects but leaves
     * the remote debuggee running, unless the 'forceExit' parameter is
     * set to true.
     *
     * @param  forceExit  true to close debuggee VM forcibly in all
     *                    cases; false to leave remote debuggee running.
     * @exception  IllegalStateException
     *             Thrown if the session state is not 'active'.
     * @see #activate
     */
    public synchronized void deactivate(boolean forceExit) {
        if (sessionState != SESSION_ACTIVE) {
            throw new IllegalStateException("must be 'active'");
        }
        sessionState = SESSION_DEACTIVATING;
        boolean success = false;
        try {
            logCategory.report("deactivating the session");

            // Shutdown the running VM before stopping the event handler,
            // so other objects can get the disconnect events.
            VirtualMachine vm = vmConnection.getVM();
            if (vm != null) {
                try {
                    if ((vm.process() == null) && !forceExit) {
                        // We attached to the VM, so simply detach.
                        logCategory.report("disposing of debuggee VM");
                        vm.dispose();
                        logCategory.report("debuggee VM disposed");
                        setStatus(Bundle.getString("Session.vmDetached"));
                    } else {
                        // We launched the VM or the caller wants us to
                        // forcibly kill the debuggee, so bring it down.
                        logCategory.report("forcing debuggee VM to exit");
                        vm.exit(1);
                        logCategory.report("debuggee VM terminated");
                        setStatus(Bundle.getString("Session.vmClosed"));
                    }
                } catch (VMDisconnectedException vmde) {
                    // This can happen.
                    logCategory.report("vm disconnected in deactivate()");
                }

            } else {
                Connector conn = vmConnection.getConnector();
                if (conn instanceof LaunchingConnector) {
                    setStatus(Bundle.getString("Session.vmClosed"));
                    statusLog.writeln(Bundle.getString(
                        "Session.checkInputOutput"));
                } else if ((conn instanceof AttachingConnector) ||
                           (conn instanceof ListeningConnector)) {
                    setStatus(Bundle.getString("Session.vmDetached"));
                }
            }

            disconnected();
            success = true;
        } finally {
            if (!success) {
                sessionState = SESSION_ACTIVE;
            }
        }
        logCategory.report("session inactive");
    } // deactivate

    /**
     * The debuggee has unexpectedly disconnected without sending a
     * proper VMDisconnectEvent. This method is to be invoked by the
     * event dispatcher when a VMDisconnectedException occurs.
     */
    public void disconnected() {
        vmConnection.setVM(null);
        // Remove ourselves as an event listener.
        VMEventManager vmeman = (VMEventManager)
            getManager(VMEventManager.class);
        vmeman.removeListener(StepEvent.class, this);
        vmeman.removeListener(VMDisconnectEvent.class, this);
        vmeman.removeListener(VMStartEvent.class, this);

        // Notify the listeners that the debugging session has ended.
        logCategory.report("deactivating session listeners");
        // We are inactive as far as the listeners are concerned.
        sessionState = SESSION_INACTIVE;
        listenerList.deactivateAll(this);
    }

    /**
     * Invoked when a VM event has occurred. The Session object
     * listens for several VM events in order to keep up-to-date
     * with the state of the running debuggee VM.
     *
     * @param  e  VM event
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        // Note that we will receive VMStartEvents but we do nothing with
        // them. We need to catch them so we can ensure the VM does not
        // resume (which is the default for our event manager).

        if (e instanceof VMDisconnectEvent) {
            // The debuggee VM disconnected; close up shop.
            // When the VM is already disconnected, simply set our
            // reference to null. There's nothing else we can do.
            vmConnection.setVM(null);
            setStatus(Bundle.getString("Session.vmDisconnected"));
            statusLog.writeln(Bundle.getString("Session.deactivating"));
            if (isActive()) {
                // Call this only if we're still active.
                deactivate(false);
            }

        } else if (e instanceof StepEvent) {
            // Only show the message here instead of printing
            // it to the log.
            interfaceAdapter.showStatus(Bundle.getString("Session.stepping"));
            handleLocatableEvent((LocatableEvent) e);
        }

        // Most of the time we don't resume the debuggee VM.
        return false;
    } // eventOccurred

    /**
     * Returns the VMConnection object for this Session, if any.
     *
     * @return  VMConnection, or null if none has been made yet.
     */
    public VMConnection getConnection() {
        return vmConnection;
    } // getConnection

    /**
     * Returns the ThreadReference to the current thread object.
     *
     * @return  Current thread reference.
     */
    public ThreadReference getCurrentThread() {
        ContextManager contextManager = (ContextManager)
            getManager(ContextManager.class);
        return contextManager.getCurrentThread();
    } // getCurrentThread

    /**
     * Retrieve an instance of a manager of the given class. If one
     * does not exist, an instance will be created.
     *
     * @param  managerClass  Class of Manager to retrieve.
     * @return  Manager object, or null if unlikely things happened.
     */
    public Manager getManager(Class managerClass) {
        Manager manager = (Manager) managerTable.get(managerClass);
        if (manager == null) {
            try {
                // Create the Manager object.
                logCategory.report("creating manager: " +
                                   managerClass.getName());
                manager = (Manager) managerClass.newInstance();
                // Put the Manager in the table.
                managerTable.put(managerClass, manager);
                // Add the Manager as a SessionListener.
                addListener(manager);
            } catch (Exception e) {
                statusLog.writeStackTrace(e);
                // Return the null Manager.
            }
        }
        return manager;
    } // getManager

    /**
     * Searches for the property with the specified key in this property
     * list. The method returns null if the property is not found.
     *
     * @param  key  the property key.
     * @return  the value in this property list with the specified key value.
     */
    public String getProperty(String key) {
        return sessionProperties.getProperty(key);
    } // getProperty

    /**
     * Returns a reference to the Log object that receives messages.
     *
     * @return  Log for writing messages to.
     */
    public Log getStatusLog() {
        return statusLog;
    } // getStatusLog

    /**
     * Returns a reference to the virtual machine associated with
     * this session. If the Session is not active, this will return
     * null.
     *
     * @return  Java virtual machine for this session, or null if
     *          not currently connected.
     */
    public VirtualMachine getVM() {
        return vmConnection == null ? null : vmConnection.getVM();
    } // getVM

    /**
     * Returns a reference to the interface adapter associated with
     * this session.
     *
     * @return  interface adapter for this session.
     */
    public UIAdapter getUIAdapter() {
        return interfaceAdapter;
    } // getUIAdapter

    /**
     * Do the usual thing when a locatable event has occurred.
     * This involves displaying an indication of the type of event
     * that occurred, such as setting the status indicator (title bar).
     * Shows the location of the event that occurred.
     * Sets the current context of the event in the ContextManager.
     * Tries to show the source code for the event location.
     *
     * @param  event  locatable event.
     */
    public void handleLocatableEvent(LocatableEvent event) {
        // Show where the event occurred.
        showEventLocation(event);

        // Set the debugger context.
        ContextManager contextManager = (ContextManager)
            getManager(ContextManager.class);
        contextManager.setCurrentLocation(event);

        // Show the source code for this location.
        showSourceFile(event.location());
    } // handleLocatableEvent

    /**
     * Initialize the Session by loading the session properties
     * from disk.
     *
     * @param  uiadapter  interface adapter.
     * @exception  IllegalStateException
     *             Thrown if the session state is not 'new'.
     */
    public synchronized void init(UIAdapter uiadapter) {
        if (sessionState != SESSION_NEW) {
            // We are no longer new, we can't initialize twice.
            throw new IllegalStateException("must be 'new'");
        }
        sessionState = SESSION_INACTIVE;

        interfaceAdapter = uiadapter;

        // Load the Session properties.
        loadProperties();
    } // init

    /**
     * Returns true if this session is active and attached to
     * a virtual machine. When the session is not active, many
     * objects will be not have been created yet.
     *
     * @return  true if active, false otherwise
     * @see #activate
     * @see #deactivate
     */
    public boolean isActive() {
        return sessionState == SESSION_ACTIVE;
    } // isActive

    /**
     * Load the session properties from the user's home directory.
     */
    protected void loadProperties() {
        loadProperties(new File(System.getProperty("user.home") +
                                File.separator + ".jswat", "session"));
    } // loadProperties

    /**
     * Load the session properties from the given file.
     *
     * @param  propsFile  properties file.
     */
    public void loadProperties(File propsFile) {
        sessionProperties.clear();
        if (propsFile.exists()) {
            if (propsFile.canRead()) {
                try {
                    FileInputStream fis = new FileInputStream(propsFile);
                    logCategory.report("loading session properties");
                    sessionProperties.load(fis);
                    fis.close();
                } catch (IOException ioe) {
                    StringBuffer buf = new StringBuffer(
                        Bundle.getString("Session.sessionFileReadError"));
                    buf.append(' ');
                    buf.append(ioe.toString());
                    statusLog.writeln(buf.toString());
                }
            } else {
                statusLog.writeln(Bundle.getString(
                    "Session.sessionFileCannotBeRead"));
            }
        }
    } // loadProperties

    /**
     * Removes a Session listener from this session. This calls the
     * <code>close()</code> method of the listener.
     *
     * @param  listener  SessionListener to remove from this session.
     */
    public void removeListener(SessionListener listener) {
        listenerList.remove(listener, this);
        if (listener instanceof Manager) {
            // Managers must be removed from the manager table.
            logCategory.report("removing session listener: " +
                               listener.getClass().getName());
            managerTable.remove(listener);
        }
    } // removeListener

    /**
     * Resume execution of the debuggee VM, from a suspended state.
     *
     * @exception  NotActiveException
     *             Thrown if session is not actively debugging a VM.
     */
    public void resumeVM() throws NotActiveException {
        if (!isActive()) {
            throw new NotActiveException();
        }
        // Reset both the current thread and location.
        ContextManager contextManager = (ContextManager)
            getManager(ContextManager.class);
        contextManager.setCurrentLocation(null);
        // This will refresh many of the panels twice, but that's okay
        // since they have nothing to show and will return immediately.
        interfaceAdapter.refreshDisplay();
        // Call this after resetting the current location.
        // Otherwise a breakpoint may be hit and the event may
        // fire off asynchronously and we'll then return and
        // clear the current location, just after the breakpoint
        // event had set the current location. Fixes bugs #1 and 2.
        logCategory.report("resuming debuggee VM");
        vmConnection.getVM().resume();
        setStatus(Bundle.getString("Session.vmRunning"));
    } // resumeVM

    /**
     * Save the session properties to a file in the user's home directory.
     *
     * @return  True if saved successfully, false if error.
     */
    protected boolean saveProperties() {
        return saveProperties(new File(System.getProperty("user.home") +
                                       File.separator + ".jswat", "session"));
    } // saveProperties

    /**
     * Save the session properties to the given file.
     *
     * @param  propsFile  properties file.
     * @return  true if saved successfully, false if error.
     */
    public boolean saveProperties(File propsFile) {
        try {
            FileOutputStream fos = new FileOutputStream(propsFile);
            String header = Bundle.getString("Session.sessionFileHeader");
            logCategory.report("saving session properties");
            sessionProperties.store(fos, header);
            fos.close();
            return true;
        } catch (IOException ioe) {
            StringBuffer buf = new StringBuffer
                (Bundle.getString("Session.sessionFileWriteError"));
            buf.append(' ');
            buf.append(ioe.toString());
            statusLog.writeln(buf.toString());
            return false;
        }
    } // saveProperties

    /**
     * Stores the given value in the properties list with the given
     * key as a reference. If the value is null, then the key and
     * value will be removed from the properties.
     *
     * @param  key    the key to be placed into this property list.
     * @param  value  the value corresponding to key, or null to remove
     *                the key and value from the properties.
     * @return  Previous value stored using this key.
     */
    public Object setProperty(String key, String value) {
        if (value == null) {
            return sessionProperties.remove(key);
        } else {
            return sessionProperties.setProperty(key, value);
        }
    } // setProperty

    /**
     * Set the status indicator using a short message. This will
     * call into the interface adapter's <code>showStatus()</code>
     * method. In addition, the message will be printed to the log.
     *
     * @param  status  short message indicating program status.
     */
    public void setStatus(String status) {
        interfaceAdapter.showStatus(status);
        statusLog.writeln(status);
    } // setStatus

    /**
     * Shows the location of the event, including the class type,
     * method name, and possibly the source file name and line
     * number (if available).
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
        tname = ClassUtils.justTheName(tname);
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
        statusLog.writeln(buf.toString());
    } // showEventLocation

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
            SourceSource src = pathman.mapSource(loc.declaringType());
            if (src != null) {
                interfaceAdapter.showFile(src, loc.lineNumber(), 0);
            } else {
                JSwat swat = JSwat.instanceOf();
                statusLog.writeln(swat.getResourceString("couldntMapSrcFile") +
                                  " (Method: " + loc.method() + ')');
            }
        } catch (IOException ioe) {
            // I don't expect this to ever happen.
            ioe.printStackTrace();
        }
    } // showSourceFile

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
            if (src != null) {
                interfaceAdapter.showFile(src, 0, 0);
            } else {
                JSwat swat = JSwat.instanceOf();
                statusLog.writeln(swat.getResourceString("couldntMapSrcFile") +
                                  " (Class: " + classname + " )");
            }
        } catch (IOException ioe) {
            // I don't expect this to ever happen.
            ioe.printStackTrace();
        }
    } // showSourceFile

    /**
     * Suspend execution of the debuggee VM.
     *
     * @exception  NotActiveException
     *             Thrown if session is not actively debugging a VM.
     */
    public void suspendVM() throws NotActiveException {
        if (!isActive()) {
            throw new NotActiveException();
        }
        // See if there are any running threads.
        Iterator iter = vmConnection.getVM().allThreads().iterator();
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
        if (!allSuspended) {
            // There are still running threads, let us suspend them.
            logCategory.report("suspending debuggee VM");
            vmConnection.getVM().suspend();
            setStatus(Bundle.getString("Session.vmSuspended"));
            interfaceAdapter.refreshDisplay();
        }
    } // suspendVM
} // Session
