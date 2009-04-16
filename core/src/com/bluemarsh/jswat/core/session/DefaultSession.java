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
 * are Copyright (C) 1999-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.event.DispatcherProvider;
import com.bluemarsh.jswat.core.event.DispatcherListener;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Class DefaultSession is a concrete implementation of a Session.
 *
 * @author  Nathan Fiedler
 */
public class DefaultSession extends AbstractSession {
    /** JvmConnection for connecting to the debuggee VM. */
    private JvmConnection vmConnection;
    /** Used to synchronize the starting and resuming of the debuggee. */
    private Semaphore vmStartedLock;

    /**
     * Creates a new instance of DefaultSession. The session starts off in an
     * inactive state and must be initialized using the <code>init()</code>
     * method.
     */
    public DefaultSession() {
        super();
        vmStartedLock = new Semaphore(0);
    }

    @Override
    public void close() {
        if (isConnected()) {
            throw new IllegalStateException("session not disconnected");
        }
        fireEvent(new SessionEvent(this, SessionEvent.Type.CLOSING));
    }

    @Override
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
        DispatcherProvider.getDispatcher(this).start(
                connection.getVM().eventQueue(), new DispatcherListener() {
            @Override
            public boolean eventOccurred(Event event) {
                // Let the connect thread know that the VM has started.
                vmStartedLock.release();
                return false;
            }
        }, new Runnable() {
            @Override
            public void run() {
                // Perform the post-disconnect cleanup (ignore the
                // connected state, since chances are we lost the
                // connnection with the debuggee already).
                disconnected();
            }
        }, new DispatcherListener() {
            @Override
            public boolean eventOccurred(Event event) {
                if (!isConnected()) {
                    // Silently ignore events between the time the VM
                    // disconnects and when we receive the disconnect event.
                    return true;
                }
                DebuggingContext dc = ContextProvider.getContext(
                            DefaultSession.this);
                if (event instanceof LocatableEvent) {
                    dc.setLocation((LocatableEvent) event, true);
                } else if (event instanceof ClassPrepareEvent) {
                    ThreadReference th = ((ClassPrepareEvent) event).thread();
                    dc.setThread(th, true);
                } else if (event instanceof ThreadStartEvent) {
                    ThreadReference th = ((ThreadStartEvent) event).thread();
                    dc.setThread(th, true);
                } else if (event instanceof ThreadDeathEvent) {
                    ThreadReference th = ((ThreadDeathEvent) event).thread();
                    dc.setThread(th, true);
                }
                fireEvent(new SessionEvent(DefaultSession.this,
                        SessionEvent.Type.SUSPENDED, event));
                return false;
            }
        });

        if (!connection.isRemote()) {
            // The launching connector requires that we wait until the
            // VMStartEvent is received, in order for the VM to be stable.
            try {
                vmStartedLock.acquire();
            } catch (InterruptedException ie) {
                // ignored
            }
        }

        //
        // The remote VM may have already started so the VMStartEvent is
        // not dispatched, although it may seem like it on some platforms.
        //
        // Should not reset the context at this point, as it has already
        // been set when the suspend event was dispatched.
        //
    }

    @Override
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

    /**
     * The debuggee has been disconnected, perform cleanup.
     */
    private void disconnected() {
        vmConnection.disconnect();
        // Notify listeners in case we disconnected abruptly during launch.
        vmStartedLock.release();
        // Reset the context (without sending events).
        ContextProvider.getContext(this).reset();
        fireEvent(new SessionEvent(this, SessionEvent.Type.DISCONNECTED));
    }

    @Override
    public JvmConnection getConnection() {
        return vmConnection;
    }

    @Override
    public boolean isConnected() {
        if (vmConnection != null) {
            return vmConnection.isConnected();
        }
        return false;
    }

    @Override
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

    @Override
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

    @Override
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
