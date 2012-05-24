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
 * are Copyright (C) 2001-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.sun.jdi.event.Event;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;

/**
 * Class AbstractBreakpointManager provides an abstract implementation of a
 * BreakpointManager for the concrete implementations to subclass. It takes
 * care of basic functionality such as breakpoint properties and listeners.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractBreakpointManager
        implements BreakpointManager, BreakpointListener,
        BreakpointGroupListener, SessionListener, SessionManagerListener {

    /** List of breakpoint listeners. */
    private BreakpointEventMulticaster bpMulticaster;
    /** List of group listeners. */
    private BreakpointGroupEventMulticaster groupMulticaster;
    /** The Session instance with which we are associated. */
    private Session ourSession;

    protected AbstractBreakpointManager() {
        bpMulticaster = new BreakpointEventMulticaster();
        groupMulticaster = new BreakpointGroupEventMulticaster();
    }

    @Override
    public void addBreakpoint(Breakpoint bp) {
        bp.addBreakpointListener(this);
    }

    @Override
    public void addBreakpointListener(BreakpointListener listener) {
        if (listener != null) {
            bpMulticaster.add(listener);
        }
    }

    @Override
    public void addBreakpointGroup(BreakpointGroup group, BreakpointGroup parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent must not be null");
        }
        group.addBreakpointGroupListener(this);
    }

    @Override
    public void addBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            groupMulticaster.add(listener);
        }
    }

    @Override
    public void breakpointAdded(BreakpointEvent event) {
        fireEvent(event);
    }

    @Override
    public void breakpointRemoved(BreakpointEvent event) {
        // The individual breakpoint is firing this event, which indicates
        // that it wants to be deleted, which it is not generally able to
        // do for itself. We take care of that, and forward the event to
        // our own set of listeners.
        Breakpoint bp = event.getBreakpoint();
        removeBreakpoint(bp);
        fireEvent(event);
    }

    @Override
    public void breakpointStopped(BreakpointEvent event) {
        fireEvent(event);
    }

    @Override
    public void closing(SessionEvent sevt) {
        saveBreakpoints(sevt.getSession());
    }

    @Override
    public void connected(SessionEvent sevt) {
        // Have to enable the default group so new breakpoints will be enabled.
        getDefaultGroup().setEnabled(true);
    }

    /**
     * Delete the breakpoint groups and their constituent breakpoints from the
     * persistent store. Because breakpoints are associated with a Session,
     * use the given Session instance to locate the breakpoints appropriately.
     *
     * @param  session  Session associated with the breakpoints.
     */
    protected abstract void deleteBreakpoints(Session session);

    @Override
    public void disconnected(SessionEvent sevt) {
        // Reset all of the breakpoint groups, which in turn reset their
        // constituent breakpoints. This is called when the session disconnects
        // so that the breakpoint counters are reset to be ready for the next
        // session connection.
        Iterator<BreakpointGroup> iter = getDefaultGroup().groups(true);
        while (iter.hasNext()) {
            BreakpointGroup group = iter.next();
            group.reset();
        }
    }

    @Override
    public void errorOccurred(BreakpointEvent event) {
        fireEvent(event);
    }

    @Override
    public void errorOccurred(BreakpointGroupEvent event) {
        fireEvent(event);
    }

    @Override
    public void fireEvent(Breakpoint b, BreakpointEventType t, Event e) {
        fireEvent(new BreakpointEvent(b, t, e));
    }

    /**
     * Let the breakpoint listeners know of a recent event in the breakpoints.
     *
     * @param  e   the breakpoint event.
     */
    private void fireEvent(BreakpointEvent e) {
        e.getType().fireEvent(e, bpMulticaster);
    }

    @Override
    public void fireEvent(BreakpointGroupEvent e) {
        e.getType().fireEvent(e, groupMulticaster);
    }

    /**
     * Return the Session instance with which we are associated.
     *
     * @return  Session instance.
     */
    protected Session getSession() {
        return ourSession;
    }

    @Override
    public void groupAdded(BreakpointGroupEvent event) {
        fireEvent(event);
    }

    @Override
    public void groupRemoved(BreakpointGroupEvent event) {
        fireEvent(event);
    }

    /**
     * Load the breakpoint groups and their constituent breakpoints from the
     * persistent store. Because breakpoints are associated with a Session,
     * use the given Session instance to restore the breakpoints appropriately.
     *
     * @param  session  Session associated with the breakpoints.
     */
    protected abstract void loadBreakpoints(Session session);

    @Override
    public void opened(Session session) {
        ourSession = session;
        loadBreakpoints(session);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Object src = event.getSource();
        if (src instanceof Breakpoint) {
            bpMulticaster.propertyChange(event);
        } else if (src instanceof BreakpointGroup) {
            groupMulticaster.propertyChange(event);
        }
    }

    @Override
    public void removeBreakpoint(Breakpoint bp) {
        bp.removeBreakpointListener(this);
    }

    @Override
    public void removeBreakpointListener(BreakpointListener listener) {
        if (listener != null) {
            bpMulticaster.remove(listener);
        }
    }

    @Override
    public void removeBreakpointGroup(BreakpointGroup group) {
        group.removeBreakpointGroupListener(this);
    }

    @Override
    public void removeBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            groupMulticaster.remove(listener);
        }
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    /**
     * Save the breakpoint groups and their constituent breakpoints to a
     * persistent store. Because breakpoints are associated with a Session,
     * use the given Session instance to store the breakpoints appropriately.
     *
     * @param  session  Session associated with the breakpoints.
     */
    protected abstract void saveBreakpoints(Session session);

    @Override

    public void sessionAdded(SessionManagerEvent e) {
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        deleteBreakpoints(e.getSession());
    }

    @Override
    public void suspended(SessionEvent sevt) {
    }
}
