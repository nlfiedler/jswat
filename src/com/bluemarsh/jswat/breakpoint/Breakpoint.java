/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        Breakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      08/16/01        Removed the colors
 *
 * $Id: Breakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.sun.jdi.request.EventRequest;
import java.util.ListIterator;
import java.util.prefs.Preferences;

/**
 * Interface Breakpoint defines the methods applicable to a breakpoint.
 * Breakpoints have several states, including enabled, disabled,
 * expired, and resolved. Breakpoints have several optional elements.
 * First they have a "skip" count, which tells the breakpoint not to
 * stop until it has been hit a certain number of times. Then
 * breakpoints may expire after a number of hits, in which case they
 * will no longer stop.
 *
 * <p>All concrete breakpoint implementations should set a "breakpoint"
 * property in their <code>com.sun.jdi.request.EventRequest</code>
 * object. This is used by the breakpoint manager to retrieve the
 * breakpoint object from the event request object.</p>
 *
 * @author  Nathan Fiedler
 */
public interface Breakpoint {

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
     * Returns an iterator of the conditions associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Condition</code> objects.
     */
    ListIterator conditions();

    /**
     * Ensures that this breakpoint will be deleted when it has expired.
     */
    void deleteOnExpire();

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
     * Retrieve the comma-separated list of class filters, if any.
     * Class filter format is described in the JDI documentation
     * for several of the event request classes.
     *
     * @return  class filters or null if none.
     */
    String getClassFilters();

    /**
     * Return the number of times this breakpoint can be hit before it
     * expires and no longer stops.
     *
     * @return  number of times this breakpoint can be hit
     *          before it expires; zero means it will never expire.
     */
    int getExpireCount();

    /**
     * Retrieves the number of this breakpoint, or zero if not set.
     *
     * @return  breakpoint number, or zero if not set.
     */
    int getNumber();

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
     * Retrieve the comma-separated list of thread filters, if any.
     *
     * @return  thread filters or null if none.
     */
    String getThreadFilters();

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    BreakpointUI getUIAdapter();

    /**
     * Returns true if the breakpoint has expired and will no longer
     * cause execution to halt.
     *
     * @return  true if this breakpoint has expired, false otherwise.
     */
    boolean hasExpired();

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    void init();

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
    ListIterator monitors();

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    boolean readObject(Preferences prefs);

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
     * Reset the stopped count to zero and clear any other attributes
     * such that this breakpoint can be used again for a new session.
     * This does not change the enabled-ness of the breakpoint.
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
     * Sets the class filters for this breakpoint. Multiple filters
     * are separated by commas. See the JDI documentation for the event
     * request classes for the format of the filters. The breakpoint
     * must be disabled before calling this method.
     *
     * @param  filters  class filters.
     */
    void setClassFilters(String filters);

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
     * Sets the number for this breakpoint. The number is used to
     * uniquely identify the breakpoint among all breakpoints.
     * This method is only called by the BreakpointManager.
     *
     * @param  n  new number for this breakpoint.
     */
    void setNumber(int n);

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
     * for suspending threads. The breakpoint must be disabled
     * before calling this method.
     *
     * @param  policy  one of the EventRequest suspend constants.
     */
    void setSuspendPolicy(int policy);

    /**
     * Sets the thread filters for this breakpoint. Multiple filters
     * are separated by commas. The breakpoint must be disabled before
     * calling this method.
     *
     * @param  filters  thread filters.
     */
    void setThreadFilters(String filters);

    /**
     * Returns a String representation of this.
     *
     * @param  terse  true to keep the description terse.
     * @return  string of this.
     */
    String toString(boolean terse);

    /**
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    boolean writeObject(Preferences prefs);
} // Breakpoint
