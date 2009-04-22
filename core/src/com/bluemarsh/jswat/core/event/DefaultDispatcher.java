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

package com.bluemarsh.jswat.core.event;

import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;
import org.openide.ErrorManager;

/**
 * Class DefaultDispatcher is a concrete implementation of the Dispatcher
 * interface. Each event is delivered to the listener which was registered
 * with the event request which resulted in the event.
 *
 * @author  Nathan Fiedler
 */
public class DefaultDispatcher implements Dispatcher, Runnable {
    /** EventRequest property for the DispatcherListener. */
    private static final String PROP_LISTENER = "listener";
    /** VM event queue. */
    private EventQueue eventQueue;
    /** Invoked when VMStartEvent is received. */
    private DispatcherListener startedCallback;
    /** Invoked when debuggee dies or is disconnected. */
    private Runnable stoppedCallback;
    /** Invoked if the debuggee is suspended by an event. */
    private DispatcherListener suspendedCallback;

    /**
     * Constructs a new instance of DefaultDispatcher.
     */
    public DefaultDispatcher() {
    }

    @Override
    public void register(DispatcherListener listener, EventRequest request) {
        request.putProperty(PROP_LISTENER, listener);
    }

    @Override
    public void run() {
        // Run until we get interrupted or the VM disconnects.
        boolean stop = false;
        while (!stop) {
            try {
                // Wait for JDI events to occur.
                EventSet set = eventQueue.remove();
                EventIterator iter = set.eventIterator();
                boolean resume = true;
                Event suspendEvent = null;
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();

                    // Notify the appropriate listeners of the event.
                    if (event instanceof VMDisconnectEvent) {
                        // This is the final event, must stop now.
                        stop = true;
                    } else if (event instanceof VMStartEvent) {
                        if (startedCallback != null) {
                            resume &= startedCallback.eventOccurred(event);
                        }
                    } else {
                        EventRequest request = event.request();
                        if (request != null) {
                            DispatcherListener listener = (DispatcherListener)
                                    request.getProperty(PROP_LISTENER);
                            if (listener != null) {
                                resume &= listener.eventOccurred(event);
                                if (!resume && suspendEvent == null) {
                                    suspendEvent = event;
                                }
                            }
                        }
                    }
                }
                if (resume) {
                    // Resume only if everyone said it was okay to do so.
                    set.resume();
                } else if (suspendedCallback != null) {
                    suspendedCallback.eventOccurred(suspendEvent);
                }
            } catch (InterruptedException ie) {
                // Nothing left to do but leave.
                break;
            } catch (VMDisconnectedException vmde) {
                // Exit the loop and call the stopped callback.
                break;
            } catch (Exception e) {
                // We do not care what FindBugs says about this catch.
                // We absolutely cannot terminate this loop -- catch the
                // exception, report it, and continue reading events.
                ErrorManager.getDefault().notify(e);
            }
        }
        if (stoppedCallback != null) {
            stoppedCallback.run();
        }
    }

    @Override
    public void start(EventQueue queue, DispatcherListener started,
            Runnable stopped, DispatcherListener suspended) {
        startedCallback = started;
        stoppedCallback = stopped;
        suspendedCallback = suspended;
        eventQueue = queue;
        Threads.getThreadPool().submit(this);
    }

    @Override
    public void unregister(EventRequest request) {
        request.putProperty(PROP_LISTENER, null);
    }
}
