/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 1999-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultSession.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.event.DispatcherProvider;
import com.bluemarsh.jswat.core.event.DispatcherEvent;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A basic implementation of a Session.
 *
 * @author  Nathan Fiedler
 */
public class DefaultSession extends AbstractSession implements DispatcherListener {
    /** List of event types we listen for. */
    private static List<Class> jdiEventTypes;
    /** JvmConnection for connecting to the debuggee VM. */
    private JvmConnection vmConnection;
    /** Used to synchronize the starting and resuming of the debuggee. */
    private Semaphore vmStartedLock;
    /** Flag to allow us to prevent re-initialization of the session. */
    private boolean initialized;

    static {
        jdiEventTypes = new ArrayList<Class>();
        // We listen for these only to cause the debuggee to wait for us.
        jdiEventTypes.add(VMStartEvent.class);
        jdiEventTypes.add(StepEvent.class);
        // This one we need to know about to clean up.
        jdiEventTypes.add(VMDisconnectEvent.class);
    }

    /**
     * Creates a new instance of DefaultSession. The session starts off in an
     * inactive state and must be initialized using the <code>init()</code>
     * method.
     */
    public DefaultSession() {
        super();
        vmStartedLock = new Semaphore(0);
    }

    public void close() {
        if (isConnected()) {
            throw new IllegalStateException("session not disconnected");
        }

        // Remove ourselves as an event listener.
        DispatcherProvider.getDispatcher(this).removeListener(this);

        // Close all the listeners.
        fireEvent(new SessionEvent(this, SessionEvent.Type.CLOSING));
    }

    public void connect(JvmConnection connection) {
        if (isConnected()) {
            throw new IllegalStateException("session not disconnected");
        }
        vmConnection = connection;

        // We are basically connected already, so notify the listeners.
        fireEvent(new SessionEvent(this, SessionEvent.Type.CONNECTED));

        // Drain all of the permits from the semaphore first so that later
        // we are forced to wait until a permit has been released.
        vmStartedLock.drainPermits();
        // Start the JDI event handler after the listeners are notified
        // and before we attempt to acquire the semaphore.
        DispatcherProvider.getDispatcher(this).start();
        if (!connection.isRemote()) {
            // The launching connector requires that we wait until the
            // VMStartEvent is received, in order for the VM to be stable.
            try {
                vmStartedLock.acquire();
            } catch (InterruptedException ie) {
                // ignored
            }
        }
        // The remote VM may have already started so the VMStartEvent is
        // not dispatched, although it may seem like it on some platforms.

        // Should not reset the context at this point, as it has already
        // been set when the suspend event was dispatched.
    }

    public void disconnect(boolean forceExit) {
        if (!isConnected()) {
            throw new IllegalStateException("session not connected");
        }
        VirtualMachine vm = vmConnection.getVM();
        try {
            // Determine how to say goodbye to the debuggee.
            if (vm.process() == null && !forceExit) {
                vm.dispose();
            } else {
                vm.exit(0);
            }
        } catch (VMDisconnectedException vmde) {
            // At this point, it doesn't matter.
        }
        disconnected();
    }

    public void disconnected() {
        vmConnection.disconnect();
        // Notify listeners in case we disconnected abruptly during launch.
        vmStartedLock.release();
        // Reset the context (without sending events).
        ContextProvider.getContext(this).reset();
        // Notify the listeners that the debugging session has ended.
        fireEvent(new SessionEvent(this, SessionEvent.Type.DISCONNECTED));
    }

    public boolean eventOccurred(DispatcherEvent de) {
        Event e = de.getEvent();
        if (e instanceof VMStartEvent) {
            // Let the connect thread know that the VM has started.
            vmStartedLock.release();            
        } else if (e instanceof VMDisconnectEvent) {
            // Perform the post-disconnect cleanup.
            disconnected();
        }
        return false;
    }

    public Iterator<Class> eventTypes() {
        return jdiEventTypes.iterator();
    }

    public JvmConnection getConnection() {
        return vmConnection;
    }

    public synchronized void init() {
        if (initialized) {
            throw new IllegalStateException("cannot initialize twice");
        }
        initialized = true;

        // Initialize some key objects so they are ready to work.
        ContextProvider.getContext(this);
        BreakpointProvider.getBreakpointManager(this);

        // We must listen for events so everything works.
        DispatcherProvider.getDispatcher(this).addListener(this);
    }

    public boolean isConnected() {
        if (vmConnection != null) {
            return vmConnection.isConnected();
        }
        return false;
    }

    public boolean isSuspended() {
        if (!isConnected()) {
            throw new IllegalStateException("session not connected");
        }
        List<ThreadReference> threads = vmConnection.getVM().allThreads();
        for (ThreadReference thread : threads) {
            try {
                if (thread.isSuspended()) {
                    return true;
                }
            } catch (ObjectCollectedException oce) {
                // Ignore dead threads.
            }
        }
        return false;
    }

    public void resumeVM() {
        if (!isConnected()) {
            throw new IllegalStateException("session not connected");
        }
        //
        // Always resume the VM, regardless if any threads have been
        // suspended or not, because the VM suspend count is tracked
        // separately from the individual threads.
        //
        // Reset the context (without sending events).
        ContextProvider.getContext(this).reset();
        // Avoid race conditions by resuming after clearing the context.
        fireEvent(new SessionEvent(this, SessionEvent.Type.RESUMING));
        // Once the listeners have been notified, resume the debuggee.
        vmConnection.getVM().resume();
    }

    public void suspended(Event e) {
        if (!isConnected()) {
            // Silently ignore events between the time the VM disconnects
            // and when we receive the disconnect event.
            return;
        }
        if (e instanceof LocatableEvent) {
            DebuggingContext dc = ContextProvider.getContext(this);
            dc.setLocation((LocatableEvent) e, true);
        } else if (e instanceof ClassPrepareEvent) {
            // Special case for these, which at least have a thread.
            ThreadReference thread = ((ClassPrepareEvent) e).thread();
            DebuggingContext dc = ContextProvider.getContext(this);
            dc.setThread(thread, true);
        } else if (e instanceof ThreadStartEvent) {
            // Special case for these, which at least have a thread.
            ThreadReference thread = ((ThreadStartEvent) e).thread();
            DebuggingContext dc = ContextProvider.getContext(this);
            dc.setThread(thread, true);
        } else if (e instanceof ThreadDeathEvent) {
            // Special case for these, which at least have a thread.
            ThreadReference thread = ((ThreadDeathEvent) e).thread();
            DebuggingContext dc = ContextProvider.getContext(this);
            dc.setThread(thread, true);
        }
        fireEvent(new SessionEvent(this, SessionEvent.Type.SUSPENDED, e));
    }

    public void suspendVM() {
        if (!isConnected()) {
            throw new IllegalStateException("session not connected");
        }
        // Always suspend the VM, regardless if any threads are already
        // suspended or not, because the VM suspend count is tracked
        // separately from the individual threads.
        vmConnection.getVM().suspend();
        // Once the debuggee is suspended, notify the listeners.
        fireEvent(new SessionEvent(this, SessionEvent.Type.SUSPENDED));
    }
}
