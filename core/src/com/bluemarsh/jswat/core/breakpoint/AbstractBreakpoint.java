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
 * $Id: AbstractBreakpoint.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.event.DispatcherEvent;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Class AbstractBreakpoint is an abstract implementation of the Breakpoint
 * interface. It implements most of the basic behavior of breakpoints.
 *
 * @author  Nathan Fiedler
 */
public abstract class AbstractBreakpoint implements Breakpoint {
    /** The thread suspension policy requested by the user. Must be one of
     * the <code>com.sun.jdi.request.EventRequest</code> suspend constants.
     * Defaults to <code>SUSPEND_ALL</code>. */
    private int suspendPolicy = EventRequest.SUSPEND_ALL;
    /** If true, force the debuggee to suspend, regardless of the suspend
     * policy chosen by the user. This is to accomodate monitors that
     * require a suspended debuggee in order to perform. */
    private boolean forceSuspend;
    /** True if this breakpoint is enabled. */
    private boolean isEnabled;
    /** Breakpoint group that contains us (always non-null). */
    private BreakpointGroup breakpointGroup;
    /** Number of times this breakpoint has stopped. */
    private int stoppedCount;
    /** Number of times this breakpoint can be hit before it expires.
     * If value is zero, breakpoint will not expire. */
    private int expireCount;
    /** Number of times this breakpoint will be hit before it stops.
     * If value is zero, breakpoint will not skip. */
    private int skipCount;
    /** List of conditions this breakpoint depends on. */
    private List<Condition> conditionList;
    /** List of monitors this breakpoint executes when it stops. */
    private List<Monitor> monitorList;
    /** Class filter, appropriate for JDI event requests. */
    private String classFilter;
    /** Thread filter, appropriate for JDI event requests. */
    private String threadFilter;
    /** True if the breakpoint should be deleted on expiration. */
    private boolean deleteOnExpire;
    /** Handles property change listeners and sending events. */
    protected PropertyChangeSupport propSupport;
    /** Map of the properties set in this breakpoint. */
    private Map<String, Object> propertiesMap;
    /** List of breakpoint listeners. */
    private BreakpointListener listeners;

    /**
     * Creates a AbstractBreakpoint with the default parameters.
     */
    public AbstractBreakpoint() {
        conditionList = new LinkedList<Condition>();
        monitorList = new LinkedList<Monitor>();
        isEnabled = true;
        propSupport = new PropertyChangeSupport(this);
        propertiesMap = new HashMap<String, Object>();
    }

    public void addBreakpointListener(BreakpointListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointEventMulticaster.add(listeners, listener);
            }
            propSupport.addPropertyChangeListener(listener);
        }
    }

    public void addCondition(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("null condition not permitted");
        }
        synchronized (conditionList) {
            conditionList.add(condition);
        }
    }

    public void addMonitor(Monitor monitor) {
        if (monitor == null) {
            throw new IllegalArgumentException("null monitor not permitted");
        }
        synchronized (monitorList) {
            monitorList.add(monitor);
        }
        // Update the suspend policy of the breakpoint requests.
        setSuspendPolicy(getSuspendPolicy());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Applies the effective suspend policy of this breakpoint to the given
     * JDI event request. This takes into account any monitors that require
     * the debuggee to be suspended in order to perform.
     *
     * @param  request  event request to apply suspend policy.
     */
    protected void applySuspendPolicy(EventRequest request) {
        request.setSuspendPolicy(forceSuspend ? EventRequest.SUSPEND_ALL :
            suspendPolicy);
    }

    public ListIterator<Condition> conditions() {
        return conditionList.listIterator();
    }

    /**
     * Delete the event requests created by this breakpoint. Called by the
     * destroy() method, when the event requests are no longer needed.
     */
    protected abstract void deleteRequests();

    public void destroy() {
        deleteRequests();
        conditionList.clear();
        monitorList.clear();
    }

    /**
     * Invoked when a VM event has occurred. This implements the basic
     * behavior that all breakpoints generally exhibit when an event
     * has occurred. Some breakpoints may override this method to take
     * different actions.
     *
     * @param  e  debugging event.
     * @return  true if debuggee VM should be resumed, false otherwise.
     */
    public boolean eventOccurred(DispatcherEvent e) {
        //
        // This method exists here because the logic of evaluating the event
        // and processing it is common for all of the breakpoint types. Each
        // breakpoint type creates its requests and sets the 'breakpoint'
        // property on the request. When the event occurs, this method then
        // determines if the event belongs to 'this' breakpoint and does the
        // standard processing as the case may be.
        //
        Event ev = e.getEvent();
        EventRequest er = ev.request();
        Object o = er.getProperty("breakpoint");
        boolean shouldResume = true;
        // Is it ours, are we enabled, and are we not expired?
        if (o == this && isEnabled() && !isExpired()) {
            // Check the filters and conditions.
            shouldResume = shouldResume(ev);
            if (!shouldResume) {
                // Good so far, now count this as a 'stop'.
                incrementStoppedCount();
                // Then check if we are skipping.
                if (isSkipping()) {
                    shouldResume = true;
                } else {
                    // Everything is good, it's a real stop.
                    shouldResume = performStop(ev);
                }
                // Do nothing else now that we may be expired and deleted.
            }
        }
        return shouldResume;
    }

    /**
     * Notify breakpoint listeners that this breakpoint has changed.
     *
     * @param  type  type of change.
     */
    protected void fireChange(BreakpointEvent.Type type) {
        fireEvent(new BreakpointEvent(this, type, null));
    }

    /**
     * Notify breakpoint listeners that this breakpoint experienced
     * an exceptional event.
     *
     * @param  exc  exception that occurred.
     */
    protected void fireError(Exception exc) {
        fireEvent(new BreakpointEvent(this, exc));
    }

    /**
     * Let the breakpoint listeners know of an event in this breakpoint.
     *
     * @param  e  the breakpoint event.
     */
    private void fireEvent(BreakpointEvent e) {
        BreakpointListener bl;
        synchronized (this) {
            bl = listeners;
        }
        if (bl != null) {
            e.getType().fireEvent(e, bl);
        }
    }

    public BreakpointGroup getBreakpointGroup() {
        return breakpointGroup;
    }

    public String getClassFilter() {
        return classFilter;
    }

    public int getExpireCount() {
        return expireCount;
    }

    public Object getProperty(String name) {
        return propertiesMap.get(name);
    }

    public int getSkipCount() {
        return skipCount;
    }

    public int getSuspendPolicy() {
        // Return the suspend policy selected by the user, regardless
        // of the attached monitors and their requirements.
        return suspendPolicy;
    }

    public String getThreadFilter() {
        return threadFilter;
    }

    public boolean isDeleteOnExpire() {
        return deleteOnExpire;
    }

    public boolean isExpired() {
        return (expireCount > 0) && (stoppedCount >= expireCount);
    }

    /**
     * Increments the <code>stoppedCount</code> value and possibly fires
     * an event that the breakpoint has been modified. That is, an event
     * is fired if the expireCount or skipCount are non-zero.
     */
    private void incrementStoppedCount() {
        boolean wasExpired = isExpired();
        boolean wasSkipping = isSkipping();
        stoppedCount++;
        boolean isExpired = isExpired();
        boolean isSkipping = isSkipping();
        if (wasExpired != isExpired) {
            propSupport.firePropertyChange(PROP_EXPIRED, wasExpired, isExpired);
        }
        if (wasSkipping != isSkipping) {
            propSupport.firePropertyChange(PROP_SKIPPING, wasSkipping, isSkipping);
        }
    }

    public boolean isEnabled() {
        BreakpointGroup parent = getBreakpointGroup();
        if (parent != null) {
            return parent.isEnabled() ? isEnabled : false;
        } else {
            return isEnabled;
        }
    }

    public abstract boolean isResolved();

    public boolean isSkipping() {
        return (skipCount > 0) && (stoppedCount <= skipCount);
    }

    public ListIterator<Monitor> monitors() {
        return monitorList.listIterator();
    }

    /**
     * This breakpoint has caused the debuggee VM to stop. Execute all
     * monitors associated with this breakpoint.
     *
     * @param  e  Event for which we are stopping.
     * @return  true if VM should resume, false otherwise.
     */
    protected boolean performStop(Event e) {
        BreakpointGroup group = getBreakpointGroup();
        BreakpointEvent be = new BreakpointEvent(this,
                BreakpointEvent.Type.STOPPED, e);
        fireEvent(be);
        runMonitors(be);
        if (isDeleteOnExpire() && isExpired()) {
            // Let listeners know we should be deleted. Hopefully one of
            // them (e.g. breakpoint manager) will actually remove us.
            fireChange(BreakpointEvent.Type.REMOVED);
        }
        // Return true if our policy is to not suspend any threads.
        return suspendPolicy == EventRequest.SUSPEND_NONE;
    }

    public void removeBreakpointListener(BreakpointListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointEventMulticaster.remove(listeners, listener);
            }
            propSupport.removePropertyChangeListener(listener);
        }
    }

    public void removeCondition(Condition condition) {
        synchronized (conditionList) {
            conditionList.remove(condition);
        }
    }

    public void removeMonitor(Monitor monitor) {
        synchronized (monitorList) {
            monitorList.remove(monitor);
        }
        // Update the suspend policy of the breakpoint requests.
        setSuspendPolicy(getSuspendPolicy());
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    public void reset() {
        boolean wasExpired = isExpired();
        boolean wasSkipping = isSkipping();
        stoppedCount = 0;
        boolean isExpired = isExpired();
        boolean isSkipping = isSkipping();
        if (wasExpired != isExpired) {
            propSupport.firePropertyChange(PROP_EXPIRED, wasExpired, isExpired);
        }
        if (wasSkipping != isSkipping) {
            propSupport.firePropertyChange(PROP_SKIPPING, wasSkipping, isSkipping);
        }
    }

    /**
     * Run the monitors associated with this breakpoint and its group.
     *
     * @param  event  breakpoint event.
     */
    protected void runMonitors(BreakpointEvent event) {
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        for (Monitor monitor : monitorList) {
            try {
                monitor.perform(event);
            } catch (Exception e) {
                fireError(e);
            }
        }
        getBreakpointGroup().runMonitors(event);
    }

    public void setBreakpointGroup(BreakpointGroup group) {
        BreakpointGroup old = breakpointGroup;
        breakpointGroup = group;
        propSupport.firePropertyChange(PROP_BREAKPOINTGROUP, old, group);
    }

    public void setClassFilter(String filter) {
        if (!canFilterClass() && filter != null && filter.length() > 0) {
            throw new IllegalArgumentException(
                    "breakpoint does not support class filters");
        }
        String old = classFilter;
        if (filter != null && filter.length() == 0) {
            // Property editor doesn't let user delete, so use blank
            // as the indication to delete the filter.
            filter = null;
        }
        classFilter = filter;
        propSupport.firePropertyChange(PROP_CLASSFILTER, old, filter);
    }

    public void setDeleteOnExpire(boolean delete) {
        boolean old = deleteOnExpire;
        deleteOnExpire = delete;
        propSupport.firePropertyChange(PROP_DELETEONEXPIRE, old, delete);
    }

    public void setEnabled(boolean enabled) {
        boolean old = isEnabled;
        isEnabled = enabled;
        propSupport.firePropertyChange(PROP_ENABLED, old, enabled);
    }

    public void setExpireCount(int expireCount) {
        int old = this.expireCount;
        boolean wasExpired = isExpired();
        this.expireCount = expireCount;
        boolean isExpired = isExpired();
        if (wasExpired != isExpired) {
            // The expiration status has changed, notify the listeners.
            propSupport.firePropertyChange(PROP_EXPIRED, wasExpired, isExpired);
        }
        propSupport.firePropertyChange(PROP_EXPIRECOUNT, old, expireCount);
    }

    public Object setProperty(String name, Object value) {
        Object rv = propertiesMap.put(name, value);
        propSupport.firePropertyChange(name, rv, value);
        return rv;
    }

    public void setSkipCount(int skipCount) {
        int old = this.skipCount;
        boolean wasSkipping = isSkipping();
        this.skipCount = skipCount;
        boolean isSkipping = isSkipping();
        if (wasSkipping != isSkipping) {
            // The skipping status has changed, notify the listeners.
            propSupport.firePropertyChange(PROP_SKIPPING, wasSkipping, isSkipping);
        }
        propSupport.firePropertyChange(PROP_SKIPCOUNT, old, skipCount);
    }

    public void setSuspendPolicy(int policy) {
        if ((policy != EventRequest.SUSPEND_ALL) &&
                (policy != EventRequest.SUSPEND_EVENT_THREAD) &&
                (policy != EventRequest.SUSPEND_NONE)) {
            throw new IllegalArgumentException("invalid suspend policy: " + policy);
        }
        int old = suspendPolicy;
        suspendPolicy = policy;
        // Determine if we require the debuggee to always suspend.
        forceSuspend = false;
        for (Monitor monitor : monitorList) {
            if (monitor.requiresThread()) {
                // Found a monitor that requires a suspended debuggee.
                forceSuspend = true;
                break;
            }
        }
        propSupport.firePropertyChange(PROP_SUSPENDPOLICY, old, policy);
    }

    public void setThreadFilter(String filter) {
        if (!canFilterThread() && filter != null && filter.length() > 0) {
            throw new IllegalArgumentException(
                    "breakpoint does not support thread filters");
        }
        String old = threadFilter;
        if (filter != null && filter.length() == 0) {
            // Property editor doesn't let user delete, so use blank
            // as the indication to delete the filter.
            filter = null;
        }
        threadFilter = filter;
        propSupport.firePropertyChange(PROP_THREADFILTER, old, filter);
    }

    /**
     * Determines if this breakpoint is to halt execution. Technically
     * execution has already stopped. This method simply indicates
     * whether the debuggee VM should be resumed or not. This method checks
     * if a thread filter is in effect and if there is a match, as well as
     * consulting any registered conditions to ensure they are satisfied.
     * The conditions of the parent group, and it's parent and so on, are
     * also considered prior to this method returning.
     *
     * @param  event  JDI Event that brought us here.
     * @return  true if debuggee VM should resume, false otherwise.
     */
    protected boolean shouldResume(Event event) {
        // Check the thread filter to see if there is a match.
        if (event instanceof LocatableEvent) {
            String filter = getThreadFilter();
            if (filter != null && filter.length() > 0) {
                LocatableEvent le = (LocatableEvent) event;
                ThreadReference thread = le.thread();
                if (!filter.equals(thread.name())) {
                    // Not a match, resume the debuggee.
                    return true;
                }
            }
        }

        // Check that the conditions are all satisfied.
        // We start by assuming they are satisfied.
        boolean satisfied = true;
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        for (Condition condition : conditionList) {
            try {
                if (!condition.isSatisfied(this, event)) {
                    satisfied = false;
                    break;
                }
            } catch (Exception e) {
                fireError(e);
            }
        }

        // Check the parent group to see if its conditions are satisfied.
        // Note the reversal of the boolean, since we are determining if
        // the debuggee should resume or not.
        if (satisfied) {
            return !breakpointGroup.conditionsSatisfied(this, event);
        } else {
            return true;
        }
    }

    public String toString() {
        return getDescription();
    }
}
