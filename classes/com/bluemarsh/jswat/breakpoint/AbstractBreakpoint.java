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
 * FILE:        AbstractBreakpoint.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      08/16/01        Removed the colors
 *      nf      09/06/01        Change to support request 236
 *      nf      07/29/02        Fixed bug 602
 *      nf      11/22/03        Renamed
 *
 * $Id: AbstractBreakpoint.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.breakpoint.ui.BreakpointUI;
import com.bluemarsh.jswat.event.VMEventListener;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Defines the default breakpoint class. This class implements most of
 * the default behavior for all breakpoints.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractBreakpoint
    implements Breakpoint, VMEventListener {
    /** Logger. */
    protected static Logger logger;
    /** The thread suspension policy for this request. Must be one of
     * the <code>com.sun.jdi.request.EventRequest</code> suspend constants.
     * Defaults to <code>SUSPEND_ALL</code>. */
    protected int suspendPolicy = EventRequest.SUSPEND_ALL;
    /** True if this breakpoint is enabled. */
    protected boolean isEnabled;
    /** Breakpoint group that contains us (always non-null). */
    protected BreakpointGroup breakpointGroup;
    /** The number of this breakpoint, unique among all breakpoints. */
    protected int breakpointNumber;
    /** Number of times this breakpoint has stopped. */
    protected int stoppedCount;
    /** Number of times this breakpoint can be hit before it expires.
     * If value is zero, breakpoint will not expire. */
    protected int expireCount;
    /** Number of times this breakpoint will be hit before it stops.
     * If value is zero, breakpoint will not skip. */
    protected int skipCount;
    /** List of conditions this breakpoint depends on. */
    protected List conditionList;
    /** List of monitors this breakpoint executes when it stops. */
    protected List monitorList;
    /** List of class filters separated by commas, appropriate for
     * JDI event requests. */
    protected String classFilters;
    /** List of thread filters separated by commas, appropriate for
     * JDI event requests. */
    protected String threadFilters;
    /** True if the breakpoint should be deleted on expiration. */
    protected boolean deleteOnExpire;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.breakpoint");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Creates a AbstractBreakpoint with the default parameters.
     */
    public AbstractBreakpoint() {
        conditionList = new LinkedList();
        monitorList = new LinkedList();
        isEnabled = true;
    } // AbstractBreakpoint

    /**
     * Add the given condition to this breakpoint. That is, when the
     * <code>shouldStop()</code> method is called, this breakpoint
     * should check if this condition is satisfied or not.
     *
     * @param  condition  condition for this breakpoint to stop.
     */
    public void addCondition(Condition condition) {
        synchronized (conditionList) {
            conditionList.add(condition);
        }
    } // addCondition

    /**
     * Add the given monitor to this breakpoint. That is, when the
     * <code>stopped()</code> method is called, this breakpoint
     * will execute this monitor.
     *
     * @param  monitor  monitor for this breakpoint to execute.
     */
    public void addMonitor(Monitor monitor) {
        synchronized (monitorList) {
            monitorList.add(monitor);
        }
    } // addMonitor

    /**
     * Returns an iterator of the conditions associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Condition</code> objects.
     */
    public ListIterator conditions() {
        return conditionList.listIterator();
    } // conditions

    /**
     * Ensures that this breakpoint will be deleted when it has expired.
     */
    public void deleteOnExpire() {
        deleteOnExpire = true;
    } // deleteOnExpire

    /**
     * Tear down this breakpoint in preparation for deletion.
     */
    public void destroy() {
        conditionList.clear();
        monitorList.clear();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("destroyed " + this);
        }
    } // destroy

    /**
     * Invoked when a VM event has occurred. This implements the basic
     * behavior that all breakpoints generally exhibit when an event
     * has occurred. Some breakpoints may override this method to take
     * different actions.
     *
     * @param  e  VM event.
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(Event e) {
        // Does this event belong to the request we created?
        if (logger.isLoggable(Level.INFO)) {
            logger.info("handling event for " + this);
        }
        EventRequest er = e.request();
        Object o = er.getProperty("breakpoint");
        boolean shouldResume = true;
        if (o == this) {
            // Yes, this is our event.
            if (logger.isLoggable(Level.INFO)) {
                logger.info("evaluating " + this);
            }
            // Need to increment this for things to work.
            incrementStoppedCount();
            shouldResume = shouldResume(e);
            if (!shouldResume) {
                // Seems we should execute the stop code.
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("performing stop for " + this);
                }
                shouldResume = performStop(e);
            }
        }
        return shouldResume;
    } // eventOccurred

    /**
     * Notify breakpoint listeners that this breakpoint has changed.
     */
    protected void fireChange() {
        BreakpointManager brkman = getBreakpointManager();
        brkman.fireChange(this, BreakpointEvent.TYPE_MODIFIED);
    } // fireChange

    /**
     * Gets the breakpoint group to which this breakpoint belongs.
     *
     * @return  parent breakpoint group, always non-null.
     * @see #setBreakpointGroup
     */
    public BreakpointGroup getBreakpointGroup() {
        return breakpointGroup;
    } // getBreakpointGroup

    /**
     * Acquires the breakpoint manager instance.
     *
     * @return  the breakpoint manager instance for this session.
     */
    protected BreakpointManager getBreakpointManager() {
        Session session = getBreakpointGroup().getSession();
        BreakpointManager brkman = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        return brkman;
    } // getBreakpointManager

    /**
     * Retrieve the comma-separated list of class filters, if any.
     * Class filter format is described in the JDI documentation
     * for several of the event request classes.
     *
     * @return  class filters or null if none.
     */
    public String getClassFilters() {
        return classFilters;
    } // getClassFilters

    /**
     * Return the number of times this breakpoint can be hit before it
     * expires and no longer stops.
     *
     * @return  number of times this breakpoint can be hit
     *          before it expires; zero means it will never expire.
     */
    public int getExpireCount() {
        return expireCount;
    } // getExpireCount

    /**
     * Retrieves the number of this breakpoint, or zero if not set.
     *
     * @return  breakpoint number, or zero if not set.
     */
    public int getNumber() {
        return breakpointNumber;
    } // getNumber

    /**
     * Return the number of times this breakpoint can be hit before it
     * starts stopping the debuggee VM. That is, the breakpoint will be
     * hit N times before it stops.
     *
     * @return  number of times this breakpoint will be hit
     *          before it stops; zero means it will not skip.
     */
    public int getSkipCount() {
        return skipCount;
    } // getSkipCount

    /**
     * Retrieve the suspend policy for this breakpoint. The returned value
     * will be one of the <code>com.sun.jdi.request.EventRequest</code>
     * constants for suspending threads.
     *
     * @return  suspend policy, one of the EventRequest suspend constants.
     * @see  #setSuspendPolicy
     */
    public int getSuspendPolicy() {
        return suspendPolicy;
    } // getSuspendPolicy

    /**
     * Retrieve the comma-separated list of thread filters, if any.
     *
     * @return  thread filters or null if none.
     */
    public String getThreadFilters() {
        return threadFilters;
    } // getThreadFilters

    /**
     * Returns the user interface widget for customizing this breakpoint.
     *
     * @return  Breakpoint user interface adapter.
     */
    public abstract BreakpointUI getUIAdapter();

    /**
     * Returns true if the breakpoint has expired and will no longer
     * cause execution to halt.
     *
     * @return  true if this breakpoint has expired, false otherwise.
     */
    public boolean hasExpired() {
        return (expireCount > 0) && (stoppedCount > expireCount);
    } // hasExpired

    /**
     * Increments the <code>stoppedCount</code> value and sends out
     * notifications that the breakpoint has been modified, if
     * appropriate (i.e. if the breakpoint is set to skip or expire).
     * This is important for breakpoint listeners so they will notice
     * if the breakpoint stops skipping or starts being expired.
     */
    protected void incrementStoppedCount() {
        stoppedCount++;
        if (expireCount > 0 || skipCount > 0) {
            fireChange();
        }
    } // incrementStoppedCount

    /**
     * Initialize the breakpoint so it may operate normally.
     */
    public void init() {
    } // init

    /**
     * Returns true if and only if the breakpoint is enabled and the
     * group containing this breakpoint is also enabled.
     *
     * @return  true if this breakpoint is enabled, false otherwise.
     * @see #setEnabled
     */
    public boolean isEnabled() {
        if (breakpointGroup.isEnabled()) {
            return isEnabled;
        } else {
            // Our group is disable, thus we are, too.
            return false;
        }
    } // isEnabled

    /**
     * Returns true if the breakpoint has been resolved against the
     * intended object in the debuggee VM. How a breakpoint resolves
     * itself depends on the type of the breakpoint.
     *
     * @return  true if this breakpoint has resolved, false otherwise.
     */
    public abstract boolean isResolved();

    /**
     * Returns true if this breakpoint is currently skipping. That is,
     * the skipCount is greater than zero and the stoppedCount is less
     * than the skipCount.
     *
     * @return  true if the breakpoint is skipping hits.
     */
    public boolean isSkipping() {
        return (skipCount > 0) && (skipCount >= stoppedCount);
    } // isSkipping

    /**
     * Returns an iterator of the monitors associated with this
     * breakpoint.
     *
     * @return  ListIterator of <code>Monitor</code> objects.
     */
    public ListIterator monitors() {
        return monitorList.listIterator();
    } // monitors

    /**
     * This breakpoint has caused the debuggee VM to stop. Execute all
     * monitors associated with this breakpoint. Send the event to the
     * Session to be handled in the usual way.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        // Show which breakpoint was just hit.
        return performStop(e, Bundle.getString("breakpointHit") + ' '
                           + getNumber());
    } // performStop

    /**
     * This breakpoint has caused the debuggee VM to stop. Execute all
     * monitors associated with this breakpoint. Send the event to the
     * Session to be handled in the usual way.
     *
     * @param  e    Event for which we are stopping.
     * @param  msg  message to be displayed, or null for none.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e, String msg) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("performing stop for " + this);
        }
        Session session = getBreakpointGroup().getSession();
        if (msg != null) {
            session.getUIAdapter().showMessage(UIAdapter.MESSAGE_NOTICE, msg);
        }

        // Call the Session to do the usual event stuff.
        session.handleDebugEvent(e);

        // Get the monitor list iterator.
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        Iterator iter = monitorList.iterator();
        if (logger.isLoggable(Level.INFO) && iter.hasNext()) {
            logger.info("running monitors for " + this);
        }
        while (iter.hasNext()) {
            Monitor monitor = (Monitor) iter.next();
            try {
                monitor.perform(session);
            } catch (Exception exc) {
                session.getStatusLog().writeStackTrace(exc);
            }
        }

        if (deleteOnExpire && (expireCount > 0)
            && (stoppedCount >= expireCount)) {
            // Breakpoint will expire after this hit, so remove it now.
            BreakpointManager brkman = getBreakpointManager();
            brkman.removeBreakpoint(this);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("deleting expired " + this);
            }
        }
        return false;
    } // performStop

    /**
     * Reads the breakpoint properties from the given preferences node.
     *
     * @param  prefs  Preferences node from which to initialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean readObject(Preferences prefs) {
        isEnabled = prefs.getBoolean("isEnabled", true);
        deleteOnExpire = prefs.getBoolean("deleteOnExpire", false);
        breakpointNumber = prefs.getInt("breakpointNumber", 0);
        // Just hope this constant never changes.
        suspendPolicy = prefs.getInt("suspendPolicy",
                                     EventRequest.SUSPEND_ALL);
        stoppedCount = prefs.getInt("stoppedCount", 0);
        expireCount = prefs.getInt("expireCount", 0);
        skipCount = prefs.getInt("skipCount", 0);
        classFilters = prefs.get("classFilters", null);
        threadFilters = prefs.get("threadFilters", null);

        // Look for persisted conditions.
        Session session = getBreakpointGroup().getSession();
        try {
            int n = 1;
            String subname = "condition" + n;
            while (prefs.nodeExists(subname)) {
                // We have conditions.
                Preferences subprefs = prefs.node(subname);
                String cname = subprefs.get("class", null);
                if (cname != null) {
                    Class bclass = Class.forName(cname);
                    Condition c = (Condition) bclass.newInstance();
                    addCondition(c);
                    if (!c.readObject(subprefs)) {
                        String msg = MessageFormat.format(
                            Bundle.getString("error.readCondition"),
                            new Object[] { String.valueOf(n) });
                        session.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING, msg);
                        removeCondition(c);
                    }
                } else {
                    String msg = MessageFormat.format(
                        Bundle.getString("error.conditionNoClass"),
                        new Object[] { String.valueOf(n) });
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING, msg);
                }
                n++;
                subname = "condition" + n;
            }

        } catch (ClassNotFoundException cnfe) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readCondition"),
                new Object[] { cnfe });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (IllegalAccessException iae) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readCondition"),
                new Object[] { iae });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (InstantiationException ie) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readCondition"),
                new Object[] { ie });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (BackingStoreException bse) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readCondition"),
                new Object[] { bse });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        }

        // Look for persisted monitors.
        try {
            int n = 1;
            String subname = "monitor" + n;
            while (prefs.nodeExists(subname)) {
                // We have monitors.
                Preferences subprefs = prefs.node(subname);
                String cname = subprefs.get("class", null);
                if (cname != null) {
                    Class bclass = Class.forName(cname);
                    Monitor m = (Monitor) bclass.newInstance();
                    addMonitor(m);
                    if (!m.readObject(subprefs)) {
                        String msg = MessageFormat.format(
                            Bundle.getString("error.readMonitor"),
                            new Object[] { String.valueOf(n) });
                        session.getUIAdapter().showMessage(
                            UIAdapter.MESSAGE_WARNING, msg);
                        removeMonitor(m);
                    }
                } else {
                    String msg = MessageFormat.format(
                        Bundle.getString("error.monitorNoClass"),
                        new Object[] { String.valueOf(n) });
                    session.getUIAdapter().showMessage(
                        UIAdapter.MESSAGE_WARNING, msg);
                }
                n++;
                subname = "monitor" + n;
            }

        } catch (ClassNotFoundException cnfe) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readMonitor"), new Object[] { cnfe });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (IllegalAccessException iae) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readMonitor"), new Object[] { iae });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (InstantiationException ie) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readMonitor"), new Object[] { ie });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        } catch (BackingStoreException bse) {
            String msg = MessageFormat.format(
                Bundle.getString("error.readMonitor"), new Object[] { bse });
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_WARNING, msg);
            return false;
        }

        return true;
    } // readObject

    /**
     * Remove the given condition from this breakpoint. This condition
     * should no longer be associated with this breakpoint. If the
     * condition is not a part of this breakpoint, nothing happens.
     *
     * @param  condition  condition to remove from this breakpoint.
     */
    public void removeCondition(Condition condition) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing condition " + condition + " for " + this);
        }
        synchronized (conditionList) {
            conditionList.remove(condition);
        }
    } // removeCondition

    /**
     * Remove the given monitor from this breakpoint. This monitor
     * should no longer be associated with this breakpoint. If the
     * monitor is not a part of this breakpoint, nothing happens.
     *
     * @param  monitor  monitor to remove from this breakpoint.
     */
    public void removeMonitor(Monitor monitor) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("removing monitor " + monitor + " for " + this);
        }
        synchronized (monitorList) {
            monitorList.remove(monitor);
        }
    } // removeMonitor

    /**
     * Reset the stopped count to zero and clear any other attributes
     * such that this breakpoint can be used again for a new session.
     * This does not change the enabled-ness of the breakpoint.
     */
    public void reset() {
        stoppedCount = 0;
        // Let the listeners know that we have changed.
        fireChange();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("reset " + this);
        }
    } // reset

    /**
     * Sets the breakpoint group to which this breakpoint will belong.
     *
     * @param  group  new parent breakpoint group.
     * @see #getBreakpointGroup
     */
    public void setBreakpointGroup(BreakpointGroup group) {
        breakpointGroup = group;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setting group " + group + " for " + this);
        }
    } // setBreakpointGroup

    /**
     * Sets the class filters for this breakpoint. Multiple filters
     * are separated by commas. See the JDI documentation for the event
     * request classes for the format of the filters. The breakpoint
     * must be disabled before calling this method.
     *
     * @param  filters  class filters.
     */
    public void setClassFilters(String filters) {
        classFilters = filters;
    } // setClassFilters

    /**
     * Enables or disables this breakpoint, according to the parameter.
     * This only affects the breakpoint itself. If the breakpoint group
     * containing this breakpoint is disabled, this breakpoint will
     * remain effectively disabled.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     * @see #isEnabled
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        if (logger.isLoggable(Level.INFO)) {
            if (enabled) {
                logger.info("enabling breakpoint: " + this);
            } else {
                logger.info("disabling breakpoint: " + this);
            }
        }
        fireChange();
    } // setEnabled

    /**
     * Set the number of times this breakpoint can be hit before it
     * expires and no longer stops.
     *
     * @param  expireCount  number of times this breakpoint can be hit
     *                      before it expires; zero to never expire
     */
    public void setExpireCount(int expireCount) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setting expire " + expireCount + " for " + this);
        }
        boolean wereExpired = hasExpired();
        this.expireCount = expireCount;
        boolean areExpired = hasExpired();
        if (wereExpired != areExpired) {
            // The expiration status has changed, notify the listeners.
            fireChange();
        }
    } // setExpireCount

    /**
     * Sets the number for this breakpoint. The number is used to
     * uniquely identify the breakpoint among all breakpoints.
     * This method is only called by the BreakpointManager.
     *
     * @param  n  new number for this breakpoint.
     */
    public void setNumber(int n) {
        breakpointNumber = n;
    } // setNumber

    /**
     * Set the number of times this breakpoint can be hit before it
     * starts stopping the debuggee VM. That is, the breakpoint will be
     * hit <code>skipCount</code> times before it stops.
     *
     * @param  skipCount  number of times this breakpoint will be hit
     *                    before it stops; zero to disable skipping
     */
    public void setSkipCount(int skipCount) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setting skip " + skipCount + " for " + this);
        }
        boolean wereSkipping = isSkipping();
        this.skipCount = skipCount;
        boolean areSkipping = isSkipping();
        if (wereSkipping != areSkipping) {
            // The skipping status has changed, notify the listeners.
            fireChange();
        }
    } // setSkipCount

    /**
     * Set the suspend policy for the request. Use one of the
     * <code>com.sun.jdi.request.EventRequest</code> constants
     * for suspending threads. The breakpoint must be disabled
     * before calling this method.
     *
     * @param  policy  one of the EventRequest suspend constants.
     * @see  #getSuspendPolicy
     */
    public void setSuspendPolicy(int policy) {
        if ((policy != EventRequest.SUSPEND_ALL)
            && (policy != EventRequest.SUSPEND_EVENT_THREAD)
            && (policy != EventRequest.SUSPEND_NONE)) {
            throw new IllegalArgumentException("not a valid suspend policy");
        }
        suspendPolicy = policy;
    } // setSuspendPolicy

    /**
     * Sets the thread filters for this breakpoint. Multiple filters
     * are separated by commas. The breakpoint must be disabled before
     * calling this method.
     *
     * @param  filters  thread filters.
     */
    public void setThreadFilters(String filters) {
        threadFilters = filters;
    } // setThreadFilters

    /**
     * Determines if this breakpoint is to halt execution. Technically
     * execution has already stopped. This method simply indicates
     * whether the debuggee VM should be resumed or not. This method must
     * take into consideration whether this breakpoint is enabled and
     * has not already expired.
     *
     * @param  event  JDI Event that brought us here.
     * @return  true if debuggee VM should resume, false otherwise.
     */
    protected boolean shouldResume(Event event) {
        // Check if we're disabled, have expired, or have not skipped
        // enough hits yet.
        if (!isEnabled() || hasExpired() || isSkipping()) {
            return true;
        }

        // Check that the conditions are all satisfied.
        // We start by assuming they are satisfied.
        boolean satisfied = true;
        // Get the condition list iterator.
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        Iterator iter = conditionList.iterator();
        while (iter.hasNext()) {
            Condition condition = (Condition) iter.next();
            try {
                if (!condition.isSatisfied(event)) {
                    // A condition was not satisfied.
                    satisfied = false;
                    break;
                }
            } catch (Exception e) {
                // Ugh, condition had a problem.
                Session session = getBreakpointGroup().getSession();
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING,
                    Bundle.getString("conditionSatisFailed") + ' '
                    + condition + ": " + e);
            }
        }
        return !satisfied;
    } // shouldResume

    /**
     * Writes the breakpoint properties to the given preferences node.
     * It is assumed that the preferences node is completely empty.
     *
     * @param  prefs  Preferences node to which to serialize this
     *                breakpoint.
     * @return  true if successful, false otherwise.
     */
    public boolean writeObject(Preferences prefs) {
        prefs.put("class", this.getClass().getName());
        prefs.putBoolean("isEnabled", isEnabled);
        prefs.putBoolean("deleteOnExpire", deleteOnExpire);
        prefs.putInt("breakpointNumber", breakpointNumber);
        prefs.putInt("suspendPolicy", suspendPolicy);
        prefs.putInt("stoppedCount", stoppedCount);
        prefs.putInt("expireCount", expireCount);
        prefs.putInt("skipCount", skipCount);
        if (classFilters != null) {
            prefs.put("classFilters", classFilters);
        }
        if (threadFilters != null) {
            prefs.put("threadFilters", threadFilters);
        }

        // Save out the conditions.
        int n = 1;
        Iterator iter = conditionList.iterator();
        while (iter.hasNext()) {
            Condition c = (Condition) iter.next();
            c.writeObject(prefs.node("condition" + n));
            n++;
        }

        // Save out the monitors.
        n = 1;
        iter = monitorList.iterator();
        while (iter.hasNext()) {
            Monitor m = (Monitor) iter.next();
            m.writeObject(prefs.node("monitor" + n));
            n++;
        }

        return true;
    } // writeObject
} // AbstractBreakpoint
