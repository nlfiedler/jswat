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
 * $Id: BreakpointGroup.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A BreakpointGroup acts as a container for breakpoints. In addition to
 * breakpoints, a group may hold other breakpoint groups. Groups may have
 * conditions and monitors attached to them, which apply to all contained
 * breakpoints.
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointGroup {    
    /** Name of 'enabled' property. */
    public static final String PROP_ENABLED = "enabled";
    /** Name of 'name' property. */
    public static final String PROP_NAME = "name";
    /** Name of 'parent' property. */
    public static final String PROP_PARENT = "parent";

    /**
     * Adds the given breakpoint to this breakpoint group.
     *
     * @param  bp  breakpoint to add to this group.
     */
    void addBreakpoint(Breakpoint bp);

    /**
     * Adds the given breakpoint group to this breakpoint group.
     *
     * @param  bg  breakpoint group to add to this group.
     */
    void addBreakpointGroup(BreakpointGroup bg);

    /**
     * Add a breakpoint group listener to this breakpoint group.
     *
     * @param  listener  listener to be added.
     */
    void addBreakpointGroupListener(BreakpointGroupListener listener);

    /**
     * Add the given condition to this group.
     *
     * @param  condition  condition to add to this group.
     */
    void addCondition(Condition condition);

    /**
     * Add the given monitor to this group. That is, when the
     * <code>runMonitors()</code> method is called, this group will execute
     * this monitor.
     *
     * <p><em>Note to implementors: should disallow adding monitors that
     * require a suspended debuggee, as there is no mechanism for dealing
     * with this in breakpoint groups.</em></p>
     *
     * @param  monitor  monitor for this group to execute.
     */
    void addMonitor(Monitor monitor);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns a count of the breakpoints in this group.
     *
     * @param  recurse  true to include subgroup counts, false to ignore
     *                  this group's subgroups.
     * @return  number of breakpoints in this group.
     */
    int breakpointCount(boolean recurse);

    /**
     * Returns an iterator over the set of breakpoints in this group.
     *
     * @param  recurse  true to recurse through all the groups.
     * @return  Iterator over the breakpoints.
     */
    Iterator<Breakpoint> breakpoints(boolean recurse);

    /**
     * Returns an iterator of the conditions associated with this group.
     *
     * @return  ListIterator of <code>Condition</code> objects.
     */
    ListIterator<Condition> conditions();

    /**
     * Evaluate the conditions associated with this group. If there are
     * none, or they are all satisfied, this method will delegate to the
     * parent of this group, until all are satisfied. If at any point a
     * condition is not met, false is returned.
     *
     * @param  bp     breakpoint for which this condition is being evaluated.
     * @param  event  JDI event that caused the breakpoint to suspend.
     * @return  true if all conditions affecting this group are satisfied,
     *          false otherwise.
     */
    boolean conditionsSatisfied(Breakpoint bp, Event event);

    /**
     * Returns a description of this breakpoint group.
     *
     * @return  description of this breakpoint group.
     */
    String getDescription();

    /**
     * Returns the name of this breakpoint group.
     *
     * @return  name of this breakpoint group.
     */
    String getName();

    /**
     * Gets the breakpoint group that is the parent of this group.
     *
     * @return  parent of this breakpoint group.
     */
    BreakpointGroup getParent();

    /**
     * Returns a count of the groups in this group.
     *
     * @param  recurse  true to include subgroup counts, false to ignore
     *                  this group's subgroups.
     * @return  number of groups in this group.
     */
    int groupCount(boolean recurse);

    /**
     * Returns an iterator over the set of groups in this group.
     *
     * @param  recurse  true to iterate over all subgroups.
     * @return  Iterator over the groups.
     */
    Iterator<BreakpointGroup> groups(boolean recurse);

    /**
     * Returns true if this breakpoint group and all of its ancestors
     * are enabled.
     *
     * @return  true if this group and all of its ancestors are enabled,
     *          false otherwise.
     */
    boolean isEnabled();

    /**
     * Returns an iterator of the monitors associated with this group.
     *
     * @return  ListIterator of <code>Monitor</code> objects.
     */
    ListIterator<Monitor> monitors();

    /**
     * Removes the given breakpoint from this breakpoint group.
     *
     * @param  bp  breakpoint to remove from this group.
     */
    void removeBreakpoint(Breakpoint bp);

    /**
     * Removes the given breakpoint group from this breakpoint group.
     *
     * @param  bg  breakpoint group to remove from this group.
     */
    void removeBreakpointGroup(BreakpointGroup bg);

    /**
     * Remove a BreakpointGroupListener from the listener list.
     *
     * @param  listener  listener to be removed.
     */
    void removeBreakpointGroupListener(BreakpointGroupListener listener);

    /**
     * Remove the given condition from this group. If the condition is
     * not a part of this group, nothing happens.
     *
     * @param  condition  condition to remove from this group.
     */
    void removeCondition(Condition condition);

    /**
     * Remove the given monitor from this group. This monitor
     * should no longer be associated with this group. If the
     * monitor is not a part of this group, nothing happens.
     *
     * @param  monitor  monitor to remove from this group.
     */
    void removeMonitor(Monitor monitor);

    /**
     * Remove a PropertyChangeListener from the listener list.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Resets all the breakpoints in this group.
     */
    void reset();

    /**
     * Runs the monitors associated with this breakpoint group, then runs
     * the monitors of the parent group.
     *
     * @param  event  breakpoint event.
     */
    void runMonitors(BreakpointEvent event);

    /**
     * Enables or disables this breakpoint group, according to the parameter.
     * This method will dispatch breakpoint events for all affected
     * breakpoints.
     *
     * @param  enabled  true to enable, false to disable.
     */
    void setEnabled(boolean enabled);

    /**
     * Changes the name of this breakpoint group to that which is given.
     *
     * @param  name  new name for this breakpoint group.
     */
    void setName(String name);

    /**
     * Sets the breakpoint group that is to be the parent of this group.
     *
     * @param  parent  new parent of this group.
     */
    void setParent(BreakpointGroup parent);
}
