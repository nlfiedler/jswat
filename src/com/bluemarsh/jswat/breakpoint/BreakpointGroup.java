/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        BreakpointGroup.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/24/01        Initial version
 *      nf      06/11/01        Added group and breakpoint iterators
 *
 * DESCRIPTION:
 *      Defines the breakpoint group class.
 *
 * $Id: BreakpointGroup.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.Session;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class BreakpointGroup acts as a container for breakpoints.
 * In addition to breakpoints, a group may hold other breakpoint groups.
 *
 * @author  Nathan Fiedler
 */
public class BreakpointGroup implements Serializable {
    /** Name of our breakpoint group. Used for display. */
    protected String groupName;
    /** True if this breakpoint group is enabled. */
    protected boolean isEnabled;
    /** List of breakpoint groups in this group. */
    protected List groupList;
    /** List of breakpoints in this group. */
    protected List breakpointList;
    /** The breakpoint group to which we belong (always non-null). */
    protected BreakpointGroup parentGroup;
    /** serial version */
    static final long serialVersionUID = -4595947731582413285L;

    /**
     * Creates a BreakpointGroup with the default parameters.
     */
    public BreakpointGroup() {
        groupList = new LinkedList();
        breakpointList = new LinkedList();
        isEnabled = true;
    } // BreakpointGroup

    /**
     * Creates a BreakpointGroup with the default parameters.
     *
     * @param  name  group name.
     */
    public BreakpointGroup(String name) {
        this();
        groupName = name;
    } // BreakpointGroup

    /**
     * Adds the given breakpoint to this breakpoint group.
     *
     * @param  bp  breakpoint to add to this group.
     */
    public void addBreakpoint(Breakpoint bp) {
        bp.setBreakpointGroup(this);
        synchronized (breakpointList) {
            breakpointList.add(bp);
        }
    } // addBreakpoint

    /**
     * Adds the given breakpoint group to this breakpoint group.
     *
     * @param  bg  breakpoint group to add to this group.
     */
    public void addBreakpointGroup(BreakpointGroup bg) {
        bg.setParent(this);
        synchronized (groupList) {
            groupList.add(bg);
        }
    } // addBreakpointGroup

    /**
     * Returns a count of the breakpoints in this group, including
     * the counts from this group's subgroups.
     *
     * @return  number of breakpoints in this group and its subgroups.
     * @see #breakpointCount(boolean)
     */ 
    public int breakpointCount() {
        // By default, get our count plus counts from subgroups.
        return breakpointCount(true);
    } // breakpointCount

    /**
     * Returns a count of the breakpoints in this group.
     *
     * @param  recurse  true to include subgroup counts, false to ignore
     *                  this group's subgroups.
     * @return  number of breakpoints in this group.
     * @see #breakpointCount()
     */ 
    public int breakpointCount(boolean recurse) {
        // First get our own count of breakpoints.
        int count = breakpointList.size();
        if (recurse) {
            // Count the breakpoints in each of our subgroups.
            Iterator iter = groupList.iterator();
            while (iter.hasNext()) {
                BreakpointGroup bg = (BreakpointGroup) iter.next();
                // This will get all subgroup counts as well.
                count += bg.breakpointCount(recurse);
            }
        }
        return count;
    } // breakpointCount

    /**
     * Returns an iterator over the set of breakpoints in this group
     * (not including subgroups).
     *
     * @return  Iterator over the breakpoints.
     */
    public Iterator breakpoints() {
        return breakpoints(false);
    } // breakpoints

    /**
     * Returns an iterator over the set of breakpoints in this group.
     *
     * @param  recurse  true to recurse through all the groups.
     * @return  Iterator over the breakpoints.
     */
    public Iterator breakpoints(boolean recurse) {
        if (recurse) {
            return new BreakpointIterator(this);
        } else {
            return breakpointList.iterator();
        }
    } // breakpoints

    /**
     * Returns the name of this breakpoint group.
     *
     * @return  name of this breakpoint group.
     */
    public String getName() {
        return groupName;
    } // getName

    /**
     * Gets the breakpoint group that is the parent of this group.
     *
     * @return  parent of this breakpoint group.
     */
    public BreakpointGroup getParent() {
        return parentGroup;
    } // getParent

    /**
     * Gets the Session that owns this group.
     *
     * @return  owning Session.
     */
    public Session getSession() {
        return parentGroup.getSession();
    } // getSession

    /**
     * Returns a count of the breakpoint groups in this group, including
     * the counts from this group's subgroups.
     *
     * @return  number of breakpoint groups in this group.
     */ 
    public int groupCount() {
        return groupCount(true);
    } // groupCount

    /**
     * Returns a count of the groups in this group.
     *
     * @param  recurse  true to include subgroup counts, false to ignore
     *                  this group's subgroups.
     * @return  number of groups in this group.
     * @see #groupCount()
     */ 
    public int groupCount(boolean recurse) {
        // First get our own count of groups.
        int count = groupList.size();
        if (recurse) {
            // Count the groups in each of our subgroups.
            Iterator iter = groupList.iterator();
            while (iter.hasNext()) {
                BreakpointGroup bg = (BreakpointGroup) iter.next();
                // This will get all subgroup counts as well.
                count += bg.groupCount(recurse);
            }
        }
        return count;
    } // groupCount

    /**
     * Returns an iterator over the set of groups in this group
     * (not including subgroups).
     *
     * @return  Iterator over the groups.
     */
    public Iterator groups() {
        return groups(false);
    } // groups

    /**
     * Returns an iterator over the set of groups in this group.
     *
     * @param  recurse  true to iterate over all subgroups.
     * @return  Iterator over the groups.
     */
    public Iterator groups(boolean recurse) {
        if (recurse) {
            return new GroupIterator(this);
        } else {
            return groupList.iterator();
        }
    } // groups

    /**
     * Returns true if this breakpoint group and all of its ancestors
     * are enabled.
     *
     * @return  true if this group and all of its ancestors are enabled,
     *          false otherwise.
     */
    public boolean isEnabled() {
        BreakpointGroup parent = getParent();
        if (parent == null) {
            return isEnabled;
        } else {
            return parent.isEnabled() && isEnabled;
        }
    } // isEnabled

    /**
     * Removes the given breakpoint from this breakpoint group.
     *
     * @param  bp  breakpoint to remove from this group.
     */
    public void removeBreakpoint(Breakpoint bp) {
        synchronized (breakpointList) {
            breakpointList.remove(bp);
        }
    } // removeBreakpoint

    /**
     * Removes the given breakpoint group from this breakpoint group.
     *
     * @param  bg  breakpoint group to remove from this group.
     */
    public void removeBreakpointGroup(BreakpointGroup bg) {
        bg.setParent(null);
        synchronized (groupList) {
            groupList.remove(bg);
        }
    } // removeBreakpointGroup

    /**
     * Resets all the breakpoints in this group.
     */
    public void reset() {
        Iterator iter = breakpointList.iterator();
        while (iter.hasNext()) {
            Breakpoint bp = (Breakpoint) iter.next();
            bp.reset();
        }
    } // reset

    /**
     * Enables or disables this breakpoint group, according to the parameter.
     *
     * @param  enabled  true if breakpoint should be enabled, false
     *                  if breakpoint should be disabled.
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    } // setEnabled

    /**
     * Changes the name of this breakpoint group to that which is given.
     *
     * @param  name  new name for this breakpoint group.
     */
    public void setName(String name) {
        groupName = name;
    } // setName

    /**
     * Sets the breakpoint group that is to be the parent of this group.
     *
     * @param  parent  new parent of this group.
     */
    public void setParent(BreakpointGroup parent) {
        parentGroup = parent;
    } // setParent

    /**
     * Iterator of Breakpoints. Supports recursing through breakpoint
     * groups. It traverses the breakpoint groups in a breadth-first
     * manner.
     */
    class BreakpointIterator implements Iterator {
        /** Current breakpoint group to iterate through. */
        protected BreakpointGroup group;
        /** Iterator of the breakpoints in the current group. */
        protected Iterator breakpointIterator;
        /** Queue of breakpoint groups to be iterated over. */
        protected LinkedList groupQueue;

        /**
         * Constructs a BreakpointIterator with an initial breakpoint
         * group to iterate.
         *
         * @param  group  group to iterate.
         */
        BreakpointIterator(BreakpointGroup group) {
            this.group = group;
            breakpointIterator = group.breakpoints();
            groupQueue = new LinkedList();
            fillGroupQueue();
        } // BreakpointIterator

        /**
         * Puts all of the current <code>group</code>'s groups onto
         * the group queue.
         */
        protected void fillGroupQueue() {
            Iterator groups = group.groups();
            while (groups.hasNext()) {
                groupQueue.addLast(groups.next());
            }
        } // fillGroupQueue

        /**
         * Returns <code>true</code> if the iteration has more elements.
         * (In other words, returns true if next would return an element
         * rather than throwing an exception.)
         *
         * @return  <code>true</code> if the iterator has more elements.
         */
        public boolean hasNext() {
            if (breakpointIterator.hasNext()) {
                return true;
            }

            // Search for a group that contains breakpoints.
            while (groupQueue.size() > 0) {
                group = (BreakpointGroup) groupQueue.removeFirst();
                fillGroupQueue();
                breakpointIterator = group.breakpoints();
                if (breakpointIterator.hasNext()) {
                    return true;
                }
            }
            return false;
        } // hasNext

        /**
         * Returns the next element in the interation.
         *
         * @return  the next element in the iteration.
         * @exception  NoSuchElementException
         *             iteration has no more elements.
         */
        public Object next() {
            if (breakpointIterator.hasNext()) {
                return breakpointIterator.next();
            }

            // Search for a group that contains breakpoints.
            while (groupQueue.size() > 0) {
                group = (BreakpointGroup) groupQueue.removeFirst();
                fillGroupQueue();
                breakpointIterator = group.breakpoints();
                if (breakpointIterator.hasNext()) {
                    return breakpointIterator.next();
                }
            }
            throw new NoSuchElementException("no more elements");
        } // next

        /**
         * Throws an <code>UnsupportedOperationException</code> since
         * remove is not supported by this iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        } // remove
    } // BreakpointIterator

    /**
     * Iterator of BreakpointGroups. Supports recursing through breakpoint
     * groups. It traverses the breakpoint groups in a breadth-first
     * manner.
     */
    class GroupIterator implements Iterator {
        /** Queue of breakpoint groups to be iterated over. */
        protected LinkedList groupQueue;

        /**
         * Constructs a BreakpointIterator with an initial breakpoint
         * group to iterate.
         *
         * @param  group  group to iterate.
         */
        GroupIterator(BreakpointGroup group) {
            groupQueue = new LinkedList();
            groupQueue.add(group);
            fillGroupQueue(group);
        } // GroupIterator

        /**
         * Puts all of the current <code>group</code>'s groups onto
         * the group queue.
         */
        protected void fillGroupQueue(BreakpointGroup group) {
            Iterator groups = group.groups();
            while (groups.hasNext()) {
                groupQueue.addLast(groups.next());
            }
        } // fillGroupQueue

        /**
         * Returns <code>true</code> if the iteration has more elements.
         * (In other words, returns true if next would return an element
         * rather than throwing an exception.)
         *
         * @return  <code>true</code> if the iterator has more elements.
         */
        public boolean hasNext() {
            return groupQueue.size() > 0;
        } // hasNext

        /**
         * Returns the next element in the interation.
         *
         * @return  the next element in the iteration.
         * @exception  NoSuchElementException
         *             iteration has no more elements.
         */
        public Object next() {
            // Search for more groups.
            if (groupQueue.size() > 0) {
                BreakpointGroup group = (BreakpointGroup)
                    groupQueue.removeFirst();
                fillGroupQueue(group);
                return group;
            }
            throw new NoSuchElementException("no more elements");
        } // next

        /**
         * Throws an <code>UnsupportedOperationException</code> since
         * remove is not supported by this iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        } // remove
    } // GroupIterator
} // BreakpointGroup
