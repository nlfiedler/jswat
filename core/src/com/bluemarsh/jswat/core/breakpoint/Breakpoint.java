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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: Breakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;
import java.beans.PropertyChangeListener;
import java.util.ListIterator;

/**
 * Interface Breakpoint defines the methods applicable to a breakpoint.
 * Breakpoints have several states, including enabled, disabled,
 * expired, and resolved. Breakpoints have several optional elements.
 * First they have a "skip" count, which tells the breakpoint not to
 * stop until it has been hit a certain number of times. Then
 * breakpoints may expire after a number of hits, in which case they
 * will no longer stop.
 *
 * @author  Nathan Fiedler
 */
public interface Breakpoint {
    /** Name of 'breakpointGroup' property. */
    public static final String PROP_BREAKPOINTGROUP = "breakpointGroup";
    /** Name of 'classFilter' property. */
    public static final String PROP_CLASSFILTER = "classFilter";
    /** Name of 'deleteOnExpire' property. */
    public static final String PROP_DELETEONEXPIRE = "deleteOnExpire";
    /** Name of 'description' property. */
    public static final String PROP_DESCRIPTION = "description";
    /** Name of 'enabled' property. */
    public static final String PROP_ENABLED = "enabled";
    /** Name of 'expireCount' property. */
    public static final String PROP_EXPIRECOUNT = "expireCount";
    /** Name of 'expired' property. */
    public static final String PROP_EXPIRED = "expired";
    /** Name of 'resolved' property. */
    public static final String PROP_RESOLVED = "resolved";
    /** Name of 'skipCount' property. */
    public static final String PROP_SKIPCOUNT = "skipCount";
    /** Name of 'skipping' property. */
    public static final String PROP_SKIPPING = "skipping";
    /** Name of 'suspendPolicy' property. */
    public static final String PROP_SUSPENDPOLICY = "suspendPolicy";
    /** Name of 'threadFilter' property. */
    public static final String PROP_THREADFILTER = "threadFilter";

    /**
     * Add a breakpoint listener to this breakpoint.
     *
     * @param  listener  listener to be added.
     */
    void addBreakpointListener(BreakpointListener listener);

    /**
     * Add the given condition to this breakpoint. That is, when the
     * <code>shouldStop()</code> method is called, this breakpoint
     * should check if this condition is satisfied or not.
     *
     * @param  condition  condition for this breakpoint to stop.
     */
    void addCondition(Condition condition);

    /**
     * Add the given monitor to this breakpoint. That is, when the
     * <code>stopped()</code> method is called, this breakpoint
     * will execute this monitor.
     *
     * @param  monitor  monitor for this breakpoint to execute.
     */
    void addMonitor(Monitor monitor);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Indicates if this type of breakpoint supports filtering events
     * by the name of a class (inclusive only).
     *
     * @return  true if this breakpoint supports class filters.
     */
    boolean canFilterClass();

    /**
     * Indicates if this type of breakpoint supports filtering events
     * by the name or number of a thread (inclusive only).
     *
     * @return  true if this breakpoint supports thread filters.
     */
    boolean canFilterThread();

    /**
     * Returns an iterator of the conditions associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Condition</code> objects.
     */
    ListIterator<Condition> conditions();

    /**
     * Generates a description of this breakpoint suitable for the user,
     * to be displayed when the breakpoint has been hit.
     *
     * @param  e  JDI event.
     * @return  the description of this breakpoint.
     */
    String describe(Event e);

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    void destroy();

    /**
     * Gets the breakpoint group to which this breakpoint belongs.
     *
     * @return  parent breakpoint group, always non-null.
     * @see #setBreakpointGroup
     */
    BreakpointGroup getBreakpointGroup();

    /**
     * Retrieve the class filter, if any. Class filter format is described
     * in the JDI documentation for several of the event request classes.
     *
     * @return  class filter, or null if none.
     */
    String getClassFilter();

    /**
     * Generates a description of this breakpoint suitable for the user,
     * to be displayed in the breakpoint management interface.
     *
     * @return  description of this breakpoint.
     */
    String getDescription();

    /**
     * Return the number of times this breakpoint can be hit before it
     * expires and no longer stops.
     *
     * @return  number of times this breakpoint can be hit
     *          before it expires; zero means it will never expire.
     */
    int getExpireCount();

    /**
     * Returns the property value previously set using the method
     * <code>setProperty()</code>, or null if the property is not found.
     *
     * @param  name  the property name.
     * @return  the value of the property, or null if not found.
     */
    Object getProperty(String name);

    /**
     * Return the number of times this breakpoint can be hit before it
     * starts stopping the debuggee VM. That is, the breakpoint will be
     * hit N times before it stops.
     *
     * @return  number of times this breakpoint will be hit
     *          before it stops; zero means it will not skip.
     */
    int getSkipCount();

    /**
     * Retrieve the suspend policy for this breakpoint. The returned value
     * will be one of the <code>com.sun.jdi.request.EventRequest</code>
     * constants for suspending threads.
     *
     * @return  suspend policy, one of the EventRequest suspend constants.
     * @see  #setSuspendPolicy
     */
    int getSuspendPolicy();

    /**
     * Retrieve the thread filter, if any. A thread filter may be either
     * the name of a thread, or the thread identifier (usually an integer
     * generated at runtime).
     *
     * @return  thread filter, or null if none.
     */
    String getThreadFilter();

    /**
     * Returns true if this breakpoint is to be deleted when it has expired,
     * false if it will be retained.
     *
     * @return  true if deleting, false otherwise.
     */
    boolean isDeleteOnExpire();

    /**
     * Returns true if the breakpoint has expired and will no longer
     * cause execution to halt.
     *
     * @return  true if this breakpoint has expired, false otherwise.
     */
    boolean isExpired();

    /**
     * Returns true if and only if the breakpoint is enabled and the
     * group containing this breakpoint is also enabled.
     *
     * @return  true if this breakpoint is enabled, false otherwise.
     * @see #setEnabled
     */
    boolean isEnabled();

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  true if this breakpoint has resolved, false otherwise.
     */
    boolean isResolved();

    /**
     * Returns true if this breakpoint is currently skipping. That is,
     * the skipCount is greater than zero and the stoppedCount is less
     * than the skipCount.
     *
     * @return  true if the breakpoint is skipping hits.
     */
    boolean isSkipping();

    /**
     * Returns an iterator of the monitors associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Monitor</code> objects.
     */
    ListIterator<Monitor> monitors();

    /**
     * Remove a BreakpointListener from the listener list.
     *
     * @param  listener  listener to be removed.
     */
    void removeBreakpointListener(BreakpointListener listener);

    /**
     * Remove the given condition from this breakpoint. This condition
     * should no longer be associated with this breakpoint. If the
     * condition is not a part of this breakpoint, nothing happens.
     *
     * @param  condition  condition to remove from this breakpoint.
     */
    void removeCondition(Condition condition);

    /**
     * Remove the given monitor from this breakpoint. This monitor
     * should no longer be associated with this breakpoint. If the
     * monitor is not a part of this breakpoint, nothing happens.
     *
     * @param  monitor  monitor to remove from this breakpoint.
     */
    void removeMonitor(Monitor monitor);

    /**
     * Remove a PropertyChangeListener from the listener list.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Resets the breakpoint so it may be re-used on another debuggee.
     */
    void reset();

    /**
     * Sets the breakpoint group to which this breakpoint will belong.
     *
     * @param  group  new parent breakpoint group.
     * @see #getBreakpointGroup
     */
    void setBreakpointGroup(BreakpointGroup group);

    /**
     * Sets the class filter for this breakpoint. See the JDI documentation
     * of the event request classes for the format of the filter.
     *
     * @param  filter  class name filter.
     */
    void setClassFilter(String filter);

    /**
     * Cause this breakpoint to be deleted when it has expired.
     *
     * @param  delete  true to delete at expiration, false to retain.
     */
    void setDeleteOnExpire(boolean delete);

    /**
     * Enables or disables this breakpoint, according to the parameter.
     *
     * @param  enabled  True if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    void setEnabled(boolean enabled);

    /**
     * Set the number of times this breakpoint can be hit before it
     * expires and no longer stops.
     *
     * @param  expireCount  number of times this breakpoint can be hit
     *                      before it expires; zero to never expire
     */
    void setExpireCount(int expireCount);

    /**
     * Stores a value to be associated with the given key in the properties
     * list maintained by this breakpoint instance.
     *
     * @param  name   the property name.
     * @param  value  the property value.
     * @return  the previously set property value, or null if none.
     */
    Object setProperty(String name, Object value);

    /**
     * Set the number of times this breakpoint can be hit before it
     * starts stopping the debuggee VM. That is, the breakpoint will be
     * hit <code>skipCount</code> times before it stops.
     *
     * @param  skipCount  number of times this breakpoint will be hit
     *                    before it stops; zero to disable skipping
     */
    void setSkipCount(int skipCount);

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants
     * for suspending threads.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    void setSuspendPolicy(int policy);

    /**
     * Sets the thread filter for this breakpoint. A thread filter may be
     * either the name of a thread, or the thread identifier (usually an
     * integer generated at runtime).
     *
     * @param  filter  thread filter.
     */
    void setThreadFilter(String filter);
}
