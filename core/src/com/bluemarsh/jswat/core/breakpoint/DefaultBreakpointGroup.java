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
 * $Id: DefaultBreakpointGroup.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.event.Event;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.openide.util.NbBundle;

/**
 * DefaultBreakpointGroup provides a default implementation of the
 * BreakpointGroup interface.
 *
 * @author  Nathan Fiedler
 */
public class DefaultBreakpointGroup extends AbstractBreakpointGroup {
    /** List of breakpoint groups in this group. */
    private List<BreakpointGroup> groupList;
    /** List of breakpoints in this group. */
    private List<Breakpoint> breakpointList;
    /** List of conditions this breakpoint evaluates when it stops. */
    private List<Condition> conditionList;
    /** List of monitors this breakpoint executes when it stops. */
    private List<Monitor> monitorList;

    /**
     * Creates a BreakpointGroup with the default parameters.
     */
    public DefaultBreakpointGroup() {
        groupList = new LinkedList<BreakpointGroup>();
        breakpointList = new LinkedList<Breakpoint>();
        conditionList = new LinkedList<Condition>();
        monitorList = new LinkedList<Monitor>();
    }

    public void addBreakpoint(Breakpoint bp) {
        bp.setBreakpointGroup(this);
        synchronized (breakpointList) {
            // This method gets called too often in some cases, so check if
            // the breakpoint is already in our list before adding it.
            if (!breakpointList.contains(bp)) {
                breakpointList.add(bp);
            }
        }
    }

    public void addBreakpointGroup(BreakpointGroup bg) {
        bg.setParent(this);
        synchronized (groupList) {
            groupList.add(bg);
        }
    }

    public void addCondition(Condition condition) {
        synchronized (conditionList) {
            conditionList.add(condition);
        }
    }

    public void addMonitor(Monitor monitor) {
        if (monitor.requiresThread()) {
            throw new IllegalArgumentException("monitor must not require thread");
        }
        synchronized (monitorList) {
            monitorList.add(monitor);
        }
    }

    public int breakpointCount(boolean recurse) {
        // First get our own count of breakpoints.
        int count = breakpointList.size();
        if (recurse) {
            // Count the breakpoints in each of our subgroups.
            for (BreakpointGroup bg : groupList) {
                // This will get all subgroup counts as well.
                count += bg.breakpointCount(recurse);
            }
        }
        return count;
    }

    public Iterator<Breakpoint> breakpoints(boolean recurse) {
        if (recurse) {
            return new BreakpointIterator(this);
        } else {
            return breakpointList.iterator();
        }
    }

    public ListIterator<Condition> conditions() {
        return conditionList.listIterator();
    }

    public boolean conditionsSatisfied(Breakpoint bp, Event event) {
        // Check that the conditions are all satisfied.
        // We start by assuming they are satisfied.
        boolean satisfied = true;
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        for (Condition condition : conditionList) {
            try {
                if (!condition.isSatisfied(bp, event)) {
                    satisfied = false;
                    break;
                }
            } catch (Exception exc) {
                BreakpointManager brkman =
                        BreakpointProvider.getBreakpointManager(this);
                brkman.fireEvent(new BreakpointGroupEvent(this, exc));
            }
        }

        // Must check this because root BreakpointGroup has no parent.
        BreakpointGroup parent = getParent();
        if (satisfied && parent != null) {
            return parent.conditionsSatisfied(bp, event);
        } else {
            return satisfied;
        }
    }

    public String getDescription() {
        return NbBundle.getMessage(DefaultBreakpointGroup.class,
                "CTL_BreakpointGroup_description", getName());
    }

    public int groupCount(boolean recurse) {
        // First get our own count of groups.
        int count = groupList.size();
        if (recurse) {
            // Count the groups in each of our subgroups.
            for (BreakpointGroup bg : groupList) {
                // This will get all subgroup counts as well.
                count += bg.groupCount(recurse);
            }
        }
        return count;
    }

    public Iterator<BreakpointGroup> groups(boolean recurse) {
        if (recurse) {
            return new GroupIterator(this);
        } else {
            return groupList.iterator();
        }
    }

    public ListIterator<Monitor> monitors() {
        return monitorList.listIterator();
    }

    public void removeBreakpoint(Breakpoint bp) {
        // Do not clear the parent of the breakpoint, otherwise the
        // Breakpoint.setBreakpointGroup() method won't work too well.
        synchronized (breakpointList) {
            breakpointList.remove(bp);
        }
    }

    public void removeBreakpointGroup(BreakpointGroup bg) {
        // Do not clear the parent of the group, otherwise the
        // BreakpointGroup.setParent() method won't work too well.
        synchronized (groupList) {
            groupList.remove(bg);
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
    }

    public void reset() {
        for (Breakpoint bp : breakpointList) {
            bp.reset();
        }
    }

    public void runMonitors(BreakpointEvent event) {
        // We are not expecting multiple threads to modify this list,
        // but if it does happen, an exception will be thrown.
        for (Monitor monitor : monitorList) {
            try {
                monitor.perform(event);
            } catch (Exception exc) {
                BreakpointManager brkman =
                        BreakpointProvider.getBreakpointManager(this);
                brkman.fireEvent(new BreakpointGroupEvent(this, exc));
            }
        }

        // Must check this because root BreakpointGroup has no parent.
        BreakpointGroup parent = getParent();
        if (parent != null) {
            parent.runMonitors(event);
        }
    }

    public String toString() {
        String name = getName();
        return name == null ? "<no-name>" : name;
    }

    /**
     * Iterator of Breakpoints. Supports recursing through breakpoint
     * groups. It traverses the breakpoint groups in a breadth-first
     * manner.
     */
    static class BreakpointIterator implements Iterator<Breakpoint> {
        /** Current breakpoint group to iterate through. */
        protected BreakpointGroup group;
        /** Iterator of the breakpoints in the current group. */
        protected Iterator<Breakpoint> breakpointIterator;
        /** Queue of breakpoint groups to be iterated over. */
        protected Queue<BreakpointGroup> groupQueue;

        /**
         * Constructs a BreakpointIterator with an initial breakpoint
         * group to iterate.
         *
         * @param  group  group to iterate.
         */
        BreakpointIterator(BreakpointGroup group) {
            this.group = group;
            breakpointIterator = group.breakpoints(false);
            groupQueue = new LinkedList<BreakpointGroup>();
            fillGroupQueue();
        }

        /**
         * Puts all of the current <code>group</code>'s groups onto
         * the group queue.
         */
        private void fillGroupQueue() {
            Iterator<BreakpointGroup> groups = group.groups(false);
            boolean offered = true;
            while (groups.hasNext() && offered) {
                offered = groupQueue.offer(groups.next());
            }
        }

        public boolean hasNext() {
            if (breakpointIterator.hasNext()) {
                return true;
            }

            // Search for a group that contains breakpoints.
            while (!groupQueue.isEmpty()) {
                group = groupQueue.remove();
                fillGroupQueue();
                breakpointIterator = group.breakpoints(false);
                if (breakpointIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        public Breakpoint next() {
            if (breakpointIterator.hasNext()) {
                return breakpointIterator.next();
            }

            // Search for a group that contains breakpoints.
            while (groupQueue.size() > 0) {
                group = groupQueue.remove();
                fillGroupQueue();
                breakpointIterator = group.breakpoints(false);
                if (breakpointIterator.hasNext()) {
                    return breakpointIterator.next();
                }
            }
            throw new NoSuchElementException("no more elements");
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }

    /**
     * Iterator of BreakpointGroups. Supports recursing through breakpoint
     * groups. It traverses the breakpoint groups in a breadth-first
     * manner.
     */
    static class GroupIterator implements Iterator<BreakpointGroup> {
        /** Queue of breakpoint groups to be iterated over. */
        protected Queue<BreakpointGroup> groupQueue;

        /**
         * Constructs a BreakpointIterator with an initial breakpoint
         * group to iterate.
         *
         * @param  group  group to iterate.
         */
        GroupIterator(BreakpointGroup group) {
            groupQueue = new LinkedList<BreakpointGroup>();
            groupQueue.add(group);
            fillGroupQueue(group);
        }

        /**
         * Puts all of the current <code>group</code>'s groups onto
         * the group queue.
         *
         * @param  group  breakpoint group.
         */
        private void fillGroupQueue(BreakpointGroup group) {
            Iterator groups = group.groups(false);
            boolean offered = true;
            while (groups.hasNext() && offered) {
                offered = groupQueue.offer((BreakpointGroup) groups.next());
            }
        }

        public boolean hasNext() {
            return groupQueue.size() > 0;
        }

        public BreakpointGroup next() {
            // Search for more groups.
            if (!groupQueue.isEmpty()) {
                BreakpointGroup group = groupQueue.remove();
                fillGroupQueue(group);
                return group;
            }
            throw new NoSuchElementException("no more elements");
        }

        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
    }
}
