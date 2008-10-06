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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointManager.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;

/**
 * A BreakpointManager is responsible for managing groups of breakpoints.
 * It does not contain any breakpoints directly, but rather contains a
 * single group which contains the breakpoints.
 *
 * <p>The BreakpointManager contains a BreakpointGroup called "Default".
 * This default breakpoint group takes all the new breakpoints that are
 * not associated with any other group. This group also represents the root
 * of the breakpoint/group heirarchy.</p>
 *
 * <p>All BreakpointManager implementations are to be event listeners for
 * their constituent breakpoints and groups, and forward those events to
 * the listeners of the BreakpointManager itself. In this way, the groups
 * and breakpoints can easily notify all interested parties of changes.
 * Listeners can choose whether to listen to individual breakpoints or
 * all of them, simply by adding themselves as a listener to either the
 * breakpoint, or the breakpoint manager.</p>
 *
 * <p>A concrete implementation of this interface can be acquired from the
 * <code>BreakpointProvider</code> class.</p>
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointManager {

    /**
     * Add a breakpoint listener to this manager object. Listeners are
     * notified of events to all breakpoints managed by this manager.
     *
     * @param  listener  listener to be added.
     */
    void addBreakpointListener(BreakpointListener listener);

    /**
     * Add a breakpoint group listener to this manager object. Listeners
     * are notified of events to all groups managed by this manager.
     *
     * @param  listener  listener to be added.
     */
    void addBreakpointGroupListener(BreakpointGroupListener listener);

    /**
     * Adds the given breakpoint to the default breakpoint group. If the
     * breakpoint implements the SessionListener interface, it will be
     * added to the session associated with this manager. If the breakpoint
     * implements the JdiEventListener interface, it will be added to the
     * event handler associated with the session.
     *
     * @param  bp  breakpoint to add.
     */
    void addBreakpoint(Breakpoint bp);

    /**
     * Adds the given breakpoint group to the given breakpoint group. If
     * the parent group is not provided, then the group is added to the
     * default group.
     *
     * @param  group   new breakpoint group.
     * @param  parent  parent breakpoint group.
     */
    void addBreakpointGroup(BreakpointGroup group, BreakpointGroup parent);

    /**
     * Let all the breakpoint listeners know of a new event concerning the
     * breakpoints. This creates a BreakpointEvent object and sends it to
     * the listeners.
     *
     * @param  b  breakpoint affected by the event.
     * @param  t  breakpoint event type.
     * @param  e  associated JDI event, or null if none.
     */
    void fireEvent(Breakpoint b, BreakpointEvent.Type t, Event e);

    /**
     * Let all the group listeners know of a recent event in the groups.
     *
     * @param  e   breakpoint group event.
     */
    void fireEvent(BreakpointGroupEvent e);

    /**
     * Returns the default breakpoint group, under which all other groups
     * and breakpoints are contained.
     *
     * @return  "default" breakpoint group.
     */
    BreakpointGroup getDefaultGroup();

    /**
     * Remove a BreakpointListener from the listener list.
     *
     * @param  listener  listener to be removed.
     */
    void removeBreakpointListener(BreakpointListener listener);

    /**
     * Removes the given breakpoint from this breakpoint manager. This
     * results in the breakpoint being effectively unreachable, as well
     * as disabled. Fires a breakpoint removed event to all the listeners.
     *
     * @param  bp  breakpoint to remove.
     */
    void removeBreakpoint(Breakpoint bp);

    /**
     * Removes the given breakpoint group from this breakpoint manager.
     * This results in all of the child groups and breakpoints contained
     * therein to be removed as well. Fires breakpoint removed events
     * for all affected breakpoints.
     *
     * @param  group  breakpoint group to remove.
     */
    void removeBreakpointGroup(BreakpointGroup group);

    /**
     * Remove a BreakpointGroupListener from the listener list.
     *
     * @param  listener  listener to be removed.
     */
    void removeBreakpointGroupListener(BreakpointGroupListener listener);
}
