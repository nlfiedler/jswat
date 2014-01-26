/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * MODULE:      Utilities
 * FILE:        ThreadGroupIterator.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      01/12/02        Initial version from threadgroups
 *
 * DESCRIPTION:
 *      This file defines the thread group iterator.
 *
 * $Id: ThreadGroupIterator.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import com.sun.jdi.ThreadGroupReference;

/**
 * Class ThreadGroupIterator has special functionality for iterating
 * over a list of thread group references. Since thread groups are
 * often assembled in trees, this iterator uses a stack to traverse
 * that tree in depth-first order.
 *
 * @author  Nathan Fiedler
 */
public class ThreadGroupIterator implements Iterator {
    /** Stack of thread group iterators. */
    protected Stack stack = new Stack();

    /**
     * Constructs a new ThreadGroupIterator with an initial set
     * of thread group iterators.
     *
     * @param  groups  ThreadGroup list.
     */
    public ThreadGroupIterator(List groups) {
        push(groups);
    } // ThreadGroupIterator

    /**
     * Constructs a new ThreadGroupIterator with an initial set
     * of thread groups.
     *
     * @param  group  ThreadGroup
     */
    public ThreadGroupIterator(ThreadGroupReference group) {
        List groups = new ArrayList();
        groups.add(group);
        push(groups);
    } // ThreadGroupIterator

    /**
     * Returns true if there are more iterators to be examined.
     *
     * @return  True if there are more iterators.
     */
    public boolean hasNext() {
        return !stack.isEmpty();
    } // hasNext

    /**
     * Returns the next element in the interation.
     *
     * @return  Next object in iteration.
     */
    public Object next() {
        // Ask the top iterator for the next thread group reference.
        ThreadGroupReference group = (ThreadGroupReference) peek().next();
        // If this group has more groups, add them to the stack.
        push(group.threadGroups());
        // Return the thread group.
        return group;
    } // next

    /**
     * Looks at the object at the top of this stack without removing
     * it from the stack.
     *
     * @return  First iterator on the stack, or null if none.
     */
    protected Iterator peek() {
        try {
            return (Iterator) stack.peek();
        } catch (EmptyStackException ese) {
            return null;
        }
    } // peek

    /**
     * Push the given list of thread group iterators onto the stack.
     *
     * @param  groups  List of ThreadGroup iterators.
     */
    protected void push(List groups) {
        // Add this list's iterator to the stack.
        stack.push(groups.iterator());
        // While the top iterator is empty, pop it off the stack.
        // This ensures that the top iterator has something to iterate.
        while (!stack.isEmpty() && !peek().hasNext()) {
            stack.pop();
        }
    } // push

    /**
     * Remove is not supported on this iterator.
     *
     * @exception  UnsupportedOperationException
     *             Thrown always.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    } // remove
} // ThreadGroupIterator
