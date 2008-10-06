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
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultDispatcher.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import com.bluemarsh.jswat.core.event.AbstractDispatcher.Entry;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import org.openide.ErrorManager;

/**
 * This class is responsible for maintaining a list of all the objects
 * interested in events sent from the back-end of the JPDA debugger.
 * Listeners registered for VM events are listed according to there
 * class. Breakpoints get JDI events first, then Session instances,
 * and finally all other types of objects.
 *
 * @author  Nathan Fiedler
 */
public class DefaultDispatcher extends AbstractDispatcher
        implements Runnable, SessionListener {
    /** VM event queue. */
    private EventQueue eventQueue;
    /** The Session instance we belong to. */
    private Session owningSession;

    /**
     * Constructs a new instance of DefaultDispatcher.
     */
    public DefaultDispatcher() {
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
    }

    public void disconnected(SessionEvent sevt) {
    }

    public void opened(Session session) {
        owningSession = session;
    }

    /**
     * Send the given event to the listener.
     *
     * @param  event     event to process.
     * @param  listener  listener to handle event.
     * @return  true to resume VM, false to suspend VM.
     */
    protected boolean processEvent(Event event, DispatcherListener listener) {
        DispatcherEvent de = new DispatcherEvent(this, owningSession, event);
        // Let the listener know about the event.
        try {
            // Listener will indicate if we should resume the debuggee VM.
            return listener.eventOccurred(de);
        } catch (VMDisconnectedException vmde) {
            throw vmde;
        } catch (Exception e) {
            // Anything can happen, must catch all types of exceptions.
            ErrorManager.getDefault().notify(e);
            // Assume that we should stop after processing the listeners.
            return false;
        }
    }

    public void resuming(SessionEvent sevt) {
    }

    public void run() {
        // Run until we get interrupted or the VM disconnects.
        while (true) {
            try {
                // Wait for JDI events to occur.
                EventSet set = eventQueue.remove();
                EventIterator iter = set.eventIterator();
                boolean shouldResume = true;
                Event suspendEvent = null;
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();

                    // Notify the appropriate listeners of the event.
                    Entry entry = findEntry(event);
                    if (entry != null) {
                        // processEvent() returns true if we should resume.
                        shouldResume &= processEvent(event, entry);
                        if (!shouldResume && suspendEvent == null) {
                            suspendEvent = event;
                        }
                    }
                }
                if (shouldResume) {
                    // Resume only if everyone said it was okay to go.
                    set.resume();
                } else {
                    // Notify the session that the debuggee suspended.
                    owningSession.suspended(suspendEvent);
                }
            } catch (InterruptedException ie) {
                // Nothing left to do but leave.
                break;
            } catch (VMDisconnectedException vmde) {
                // Notify the session and break out.
                if (owningSession.isConnected()) {
                    owningSession.disconnected();
                }
                break;
            } catch (Exception e) {
                // We do not care what FindBugs says about this catch.
                // We absolutely cannot terminate this loop -- catch the
                // exception, report it, and continue reading events.
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public void start() {
        eventQueue = owningSession.getConnection().getVM().eventQueue();
        Threads.getThreadPool().submit(this);
    }

    public void suspended(SessionEvent sevt) {
    }
}
