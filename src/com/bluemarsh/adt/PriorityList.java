/********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     Abstract Data Types
 * MODULE:      Priority List
 * FILE:        PriorityList.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/29/99        Initial version
 *      nf      12/09/01        Added toArray() methods
 *      nf      01/05/02        Implemented List interface
 *
 * DESCRIPTION:
 *      Defines the PriorityList class. This class keeps a list
 *      of elements, sorted by their priorities.
 *
 * $Id: PriorityList.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 *******************************************************************/

package com.bluemarsh.adt;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Class PriorityList maintains a sorted list of elements. The
 * elements are sorted based on their priority, where a higher
 * value is sorted before lower values.
 *
 * <p>This is implemented using an <code>java.util.ArrayList</code>
 * with the addition of sorting in the <code>add</code> method.</p>
 *
 * <p><b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a list concurrently, and at least one of the threads
 * modifies the list structurally, it <i>must</i> be synchronized
 * externally. (A structural modification is any operation that adds or
 * deletes one or more elements; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list. If no
 * such object exists, the list should be "wrapped" using the
 * Collections.synchronizedList method. This is best done at creation time,
 * to prevent accidental unsynchronized access to the list:</p>
 * <pre>
 *     List list = Collections.synchronizedList(new PriorityList(...));
 * </pre>
 *
 * <p>The iterators returned by the this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the Iterator's own <tt>remove</tt> or <tt>add</tt> methods,
 * the iterator will throw a <tt>ConcurrentModificationException</tt>. Thus,
 * in the face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behavior at an
 * undetermined time in the future.</p>
 *
 * @author  Nathan Fiedler
 */
public class PriorityList implements Cloneable, List {
    /** Sorted list of elements */
    protected List sortedList;

    /**
     * Constructs a new PriorityList object.
     */
    public PriorityList() {
        sortedList = new ArrayList();
    } // PriorityList

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * <p>Throws <code>UnsupportedOperationException</code>.</p>
     *
     * @param  o  element to be appended to this list.
     * @return  <tt>true</tt> (as per the general contract of the
     *          <tt>Collection.add</tt> method).
     * @exception  UnsupportedOperationException
     *             always thrown.
     */
    public boolean add(Object o) {
        throw new UnsupportedOperationException();
    } // add

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).
     *
     * <p>Throws <code>UnsupportedOperationException</code>.</p>
     *
     * @param  index    index at which the specified element is to be inserted.
     * @param  element  element to be inserted.
     * @exception  UnsupportedOperationException
     *             always thrown.
     */
    public void add(int index, Object element) {
        throw new UnsupportedOperationException();
    } // add

    /**
     * Adds the specified element to this list, using the specified priority
     * value as a sorting aid. Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * <p>Has O(n) running time.</p>
     *
     * @param  element   element to be inserted.
     * @param  priority  priority value for the element.
     * @return  <tt>true</tt> (as per the general contract of the
     *          <tt>Collection.add</tt> method).
     * @exception  ClassCastException
     *             if the class of the specified element prevents it from
     *             being added to this list.
     * @exception  IllegalArgumentException
     *             if some aspect of the specified element prevents it from
     *             being added to this list.
     * @exception  IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 ||
     *             index &gt; size()).
     */
    public boolean add(Object element, int priority) {
        if (sortedList.size() == 0) {
            // Special case, list of zero size.
            sortedList.add(new Entry(priority, element));
        } else {
            // General case, add element in appropriate position.
            int size = sortedList.size();
            for (int i = 0; i < size; i++) {
                Entry entry = (Entry) sortedList.get(i);
                if (entry.priority < priority) {
                    // Insert in front of this entry.
                    sortedList.add(i, new Entry(priority, element));
                    return true;
                }
            }
            // Add the element at the end of the list.
            sortedList.add(new Entry(priority, element));
        }
        return true;
    } // add

    /**
     * Appends all of the elements in the specified collection to the end
     * of this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).
     *
     * <p>Throws <code>UnsupportedOperationException</code>.</p>
     *
     * @param  c  collection whose elements are to be added to this list.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @exception  UnsupportedOperationException
     *             if the <tt>addAll</tt> method is not supported by this list.
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    } // addAll

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position (optional operation).
     *
     * <p>Throws <code>UnsupportedOperationException</code>.</p>
     *
     * @param  index  index at which to insert first element from the
     *	              specified collection.
     * @param  c      elements to be inserted into this list.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @exception  UnsupportedOperationException
     *		   always thrown.
     */
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException();
    } // addAll

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position.
     *
     * @param  c         elements to be inserted into this list.
     * @param  priority  priority value for the elements.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     */
    public boolean addAll(Collection c, int priority) {
        int oldsize = size();
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            add(iter.next(), priority);
        }
        return oldsize != size();
    } // addAll

    /**
     * Removes all of the elements from this list (optional operation).
     * This list will be empty after this call returns (unless it throws
     * an exception).
     */
    public void clear() {
        sortedList.clear();
    } // clear

    /**
     * Creates a shallow copy of the list. The elements themselves are
     * not cloned.
     *
     * @return  clone of this list.
     */
    public Object clone() {
        PriorityList newList = new PriorityList();
        newList.sortedList.addAll(sortedList);
        return newList;
    } // clone

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param  o  element whose presence in this list is to be tested.
     * @return  <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        return indexOf(o) > -1;
    } // contains

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of
     * the specified collection.
     *
     * @param  c  collection to be checked for containment in this list.
     * @return <tt>true</tt> if this list contains all of the elements of the
     *         specified collection.
     * @exception  NullPointerException
     *             if the specified collection is <tt>null</tt>.
     * @see  #contains(Object)
     */
    public boolean containsAll(Collection c) {
        Iterator iter = c.iterator();
        while (iter.hasNext()) {
            if (!contains(iter.next())) {
                return false;
            }
        }
        return true;
    } // containsAll

    /**
     * Compares the specified object with this list for equality. Returns
     * <tt>true</tt> if and only if the specified object is also a list, both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are <i>equal</i>. (Two elements <tt>e1</tt> and
     * <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ? e2==null :
     * e1.equals(e2))</tt>.) In other words, two lists are defined to be
     * equal if they contain the same elements in the same order. This
     * definition ensures that the equals method works properly across
     * different implementations of the <tt>List</tt> interface.
     *
     * @param  o  the object to be compared for equality with this list.
     * @return  <tt>true</tt> if the specified object is equal to this list.
     */
    public boolean equals(Object o) {
        return o == this;
    } // equals

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index  index of element to return.
     * @return  the element at the specified position in this list.
     * 
     * @exception IndexOutOfBoundsException
     *            if the index is out of range (index &lt; 0 ||
     *            index &gt;= size()).
     */
    public Object get(int index) throws IndexOutOfBoundsException {
        Entry entry = (Entry) sortedList.get(index);
        return entry.element;
    } // get

    /**
     * Returns the hash code value for this list. The hash code of a list
     * is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator i = list.iterator();
     *  while (i.hasNext()) {
     *      Object obj = i.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     * This ensures that <tt>list1.equals(list2)</tt> implies that
     * <tt>list1.hashCode()==list2.hashCode()</tt> for any two lists,
     * <tt>list1</tt> and <tt>list2</tt>, as required by the general
     * contract of <tt>Object.hashCode</tt>.
     *
     * @return  the hash code value for this list.
     * @see  Object#hashCode()
     * @see  Object#equals(Object)
     * @see  #equals(Object)
     */
    public int hashCode() {
        return System.identityHashCode(this);
    } // hashCode

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element. More
     * formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there
     * is no such index.
     *
     * @param  o  element to search for.
     * @return  the index in this list of the first occurrence of the specified
     *          element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object o) {
        int size = sortedList.size();
        for (int ii = 0; ii < size; ii++) {
            Entry entry = (Entry) sortedList.get(ii);
            if (entry.element.equals(o)) {
                return ii;
            }            
        }
        return -1;
    } // indexOf

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return  <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return size() == 0;
    } // isEmpty

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return  an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return new Iter(sortedList.iterator());
    } // iterator

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element. More
     * formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there
     * is no such index.
     *
     * @param  o  element to search for.
     * @return  the index in this list of the last occurrence of the specified
     *          element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        for (int ii = sortedList.size() - 1; ii >= 0; ii--) {
            Entry entry = (Entry) sortedList.get(ii);
            if (entry.element.equals(o)) {
                return ii;
            }            
        }
        return -1;
    } // lastIndexOf

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     *
     * @return  a list iterator of the elements in this list (in proper
     *          sequence).
     */
    public ListIterator listIterator() {
        return new ListIter(sortedList.listIterator());
    } //  listIterator

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list. The
     * specified index indicates the first element that would be returned
     * by an initial call to the <tt>next</tt> method. An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.
     *
     * @param  index  index of first element to be returned from the
     *               list iterator (by a call to the <tt>next</tt> method).
     * @return  a list iterator of the elements in this list (in proper
     *          sequence), starting at the specified position in this list.
     * @exception  IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 ||
     *             index &gt; size()).
     */
    public ListIterator listIterator(int index) {
        return new ListIter(sortedList.listIterator(index));
    } // listIterator

    /**
     * Removes the element at the specified position in this list. Note that
     * the position is really a priority value, not a literal offset in the
     * list. Shifts any subsequent elements to the left (subtracts one from
     * their indices). Returns the element that was removed from the list.
     *
     * @param  priority  priority of elements to remove.
     * @return  always returns null.
     * @exception  IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 ||
     *             index &gt;= size()).
     */
    public Object remove(int priority) {
        int size = sortedList.size();
        int i = 0;
        while (i < size) {
            Entry entry = (Entry) sortedList.get(i);
            if (entry.priority == priority) {
                sortedList.remove(i);
                // Size goes down by one.
                size--;
            } else {
                // Only increment if we didn't remove an element.
                i++;
            }
        }
        return null;
    } // remove

    /**
     * Removes the first occurrence in this list of the specified element.
     * If this list does not contain the element, it is unchanged. More
     * formally, removes the element with the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
     * element exists).
     *
     * @param  o  element to be removed from this list, if present.
     * @return  <tt>true</tt> if this list contained the specified element.
     */
    public boolean remove(Object element) {
        int size = sortedList.size();
        for (int i = 0; i < size; i++) {
            Entry entry = (Entry) sortedList.get(i);
            if (entry.element.equals(element)) {
                sortedList.remove(i);
                return true;
            }            
        }
        return false;
    } // remove

    /**
     * Removes from this list all the elements that are contained in the
     * specified collection (optional operation).
     *
     * @param  c  collection that defines which elements will be removed
     *            from this list.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        Iterator iter = c.iterator();
        boolean removed = false;
        while (iter.hasNext()) {
            removed |= remove(iter.next());
        }
        return removed;
    } // removeAll

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).
     *
     * <p>Throws <code>UnsupportedOperationException</code>.</p>
     *
     * @param  c  collection that defines which elements this set will retain.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @exception  UnsupportedOperationException
     *             always thrown.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    } // retainAll

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param  index    index of element to replace.
     * @param  element  element to be stored at the specified position.
     * @return  the element previously at the specified position.
     * @exception  ClassCastException
     *             if the class of the specified element prevents it from
     *             being added to this list.
     * @exception  IllegalArgumentException
     *             if some aspect of the specified element prevents it from
     *             being added to this list.
     * @exception  IndexOutOfBoundsException
     *             if the index is out of range (index &lt; 0 ||
     *             index &gt;= size()).
     */
    public Object set(int index, Object element) {
        Entry n = (Entry) sortedList.get(index);
        sortedList.set(index, new Entry(n.priority, element));
        return n.element;
    } // set

    /**
     * Returns the number of elements in this list. If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
        return sortedList.size();
    } // size

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations supported
     * by this list.<p>
     *
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     *
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param  fromIndex  low endpoint (inclusive) of the subList.
     * @param  toIndex    high endpoint (exclusive) of the subList.
     * @return  a view of the specified range within this list.
     * @exception  IndexOutOfBoundsException
     *             for an illegal endpoint index value (fromIndex &lt; 0 ||
     *             toIndex &gt; size || fromIndex &gt; toIndex).
     */
    public List subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > sortedList.size() ||
            toIndex < 0 || toIndex > sortedList.size()) {
            throw new IndexOutOfBoundsException();
        }
        List sub = new ArrayList();
        for (int ii = fromIndex; ii < toIndex; ii++) {
            Entry entry = (Entry) sortedList.get(ii);
            sub.add(entry.element);
        }
        return sub;
    } // subList

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence. Obeys the general contract of the <tt>Collection.toArray</tt>
     * method.
     *
     * @return  an array containing all of the elements in this list in proper
     *          sequence.
     * @see  Arrays#asList(Object[])
     */
    public Object[] toArray() {
        return toArray(new Object[size()]);
    } // toArray

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array. Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     *
     * <p>If the list fits in the specified array with room to spare
     * (i.e., the array has more elements than the list), the element
     * in the array immediately following the end of the  collection is
     * set to null. This is useful in determining the length of the list
     * <em>only</em> if the caller knows that the list does not contain
     * any null elements.</p>
     *
     * @param  a  the array into which the elements of this list are to
     *            be stored, if it is big enough; otherwise, a new array of
     *            the same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     * @exception  ArrayStoreException
     *             if the runtime type of the specified array is not a
     *             supertype of the runtime type of every element in this list.
     * @exception  NullPointerException
     *             if the specified array is <tt>null</tt>.
     */
    public Object[] toArray(Object a[]) {
        int size = size();
        if (a.length < size) {
            a = (Object[]) Array.newInstance(
                a.getClass().getComponentType(), size);
        }
        for (int ii = 0; ii < size; ii++) {
            Entry entry = (Entry) sortedList.get(ii);
            a[ii] = entry.element;
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    } // toArray

    /**
     * A priority list entry.
     */
    protected class Entry {
        /** Priority value for this element. */
        int priority;
        /** Object stored in this entry (the element itself). */
        Object element;

        /**
         * Constructs a new Entry object with the given priority
         * and element values.
         *
         * @param  priority  Priority value.
         * @param  element   Object.
         */
        Entry(int priority, Object element) {
            this.priority = priority;
            this.element = element;
        } // Entry
    } // Entry

    /**
     * A priority list iterator.
     */
    protected class Iter implements Iterator {
        /** The sorted list iterator. */
        Iterator iter;

        /**
         * Constructs an iterator from the given iterator.
         *
         * @param  iter  list iterator.
         */
        Iter(Iterator iter) {
            this.iter = iter;
        } // Iter

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In
         * other words, returns <tt>true</tt> if <tt>next</tt> would return
         * an element rather than throwing an exception.)
         *
         * @return  <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            return iter.hasNext();
        } // hasNext

        /**
         * Returns the next element in the iteration.
         *
         * @return  the next element in the iteration.
         * @exception  NoSuchElementException
         *             iteration has no more elements.
         */
        public Object next() {
            return ((Entry) iter.next()).element;
        } // next

        /**
         * Removes from the underlying collection the last element returned
         * by the iterator. This method can be called only once per call to
         * <tt>next</tt>. The behavior of an iterator is unspecified if the
         * underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @exception  IllegalStateException
         *             if the <tt>next</tt> method has not yet been called,
         *             or the <tt>remove</tt> method has already been called
         *             after the last call to the <tt>next</tt> method.
         */
        public void remove() {
            iter.remove();
        } // remove
    } // Iter

    /**
     * A priority list list iterator.
     */
    protected class ListIter extends Iter implements ListIterator {
        /** List iterator of the sorted list. */
        protected ListIterator liter;

        /**
         * Constructs an iterator from the given iterator.
         *
         * @param  iter  list iterator.
         */
        ListIter(ListIterator iter) {
            super(iter);
            liter = iter;
        } // ListIter

        /**
         * This method is not supported.
         *
         * <em>
         * Inserts the specified element into the list (optional
         * operation). The element is inserted immediately before the
         * next element that would be returned by <tt>next</tt>, if
         * any, and after the next element that would be returned by
         * <tt>previous</tt>, if any. (If the list contains no
         * elements, the new element becomes the sole element on the
         * list.) The new element is inserted before the implicit
         * cursor: a subsequent call to <tt>next</tt> would be
         * unaffected, and a subsequent call to <tt>previous</tt>
         * would return the new element. (This call increases by one
         * the value that would be returned by a call to
         * <tt>nextIndex</tt> or <tt>previousIndex</tt>.)
         * </em>
         *
         * @param  o  the element to insert.
         * @exception  UnsupportedOperationException
         *             if the <tt>add</tt> method is not supported by this
         *             list iterator.
         * @exception  ClassCastException
         *             if the class of the specified element prevents it
         *             from being added to this list.
         * @exception  IllegalArgumentException
         *             if some aspect of this element prevents it from
         *             being added to this list.
         */
        public void add(Object o) {
            throw new UnsupportedOperationException();
        } // add

        /**
         * Returns <tt>true</tt> if this list iterator has more elements when
         * traversing the list in the reverse direction. (In other words,
         * returns <tt>true</tt> if <tt>previous</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return  <tt>true</tt> if the list iterator has more elements when
         *          traversing the list in the reverse direction.
         */
        public boolean hasPrevious() {
            return liter.hasPrevious();
        } // hasPrevious

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to <tt>next</tt>. (Returns list size if the list
         * iterator is at the end of the list.)
         *
         * @return  the index of the element that would be returned by a
         *          subsequent call to <tt>next</tt>, or list size if list
         *          iterator is at end of list.
         */
        public int nextIndex() {
            return liter.nextIndex();
        } // nextIndex

        /**
         * Returns the previous element in the list. This method may be called
         * repeatedly to iterate through the list backwards, or intermixed with
         * calls to <tt>next</tt> to go back and forth. (Note that alternating
         * calls to <tt>next</tt> and <tt>previous</tt> will return the same
         * element repeatedly.)
         *
         * @return  the previous element in the list.
         * @exception  NoSuchElementException
         *             if the iteration has no previous element.
         */
        public Object previous() {
            return ((Entry) liter.previous()).element;
        } // previous

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to <tt>previous</tt>. (Returns -1 if the list
         * iterator is at the beginning of the list.)
         *
         * @return  the index of the element that would be returned by a
         *          subsequent call to <tt>previous</tt>, or -1 if list
         *          iterator is at beginning of list.
         */ 
        public int previousIndex() {
            return liter.previousIndex();
        } // previousIndex

        /**
         * Replaces the last element returned by <tt>next</tt> or
         * <tt>previous</tt> with the specified element (optional operation).
         *
         * <p>Throws <code>UnsupportedOperationException</code>.</p>
         *
         * @param  o  the element with which to replace the last element
         *            returned by <tt>next</tt> or <tt>previous</tt>.
         * @exception  UnsupportedOperationException
         *             always thrown.
         */
        public void set(Object o) {
            throw new UnsupportedOperationException();
        } // set
    } // ListIter
} // PriorityList
