/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      Skip List
 * FILE:        SkipList.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/26/01        Initial version
 *      nf      12/30/01        Added searchNextLarger()
 *      nf      01/05/02        Implemented contains, containsAll, and
 *                              iterator.
 *
 * DESCRIPTION:
 *      Implements a skip list, as described in the paper by professor
 *      William Pugh, using int primitives as the keys.
 *
 * $Id: SkipList.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.adt;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Class SkipList implements a skip list that uses int primitives for
 * the element keys. The modification methods defined by <code>Set</code>
 * are not supported. Instead, methods with keys are required. Note that
 * the keys have a many to one relationship with the elements (that is,
 * a range of key values will map to a single element).
 *
 * <p><b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a set concurrently, and at least one of the threads modifies
 * the set, it <i>must</i> be synchronized externally. This is typically
 * accomplished by synchronizing on some object that naturally encapsulates
 * the set. If no such object exists, the set should be "wrapped" using the
 * <code>Collections.synchronizedSet</code> method. This is best done at
 * creation time, to prevent accidental unsynchronized access to the set:</p>
 *<pre>
 *     Set s = Collections.synchronizedSet(new SkipList(...));
 *</pre>
 *
 * <p>The Iterators returned by this class's <tt>iterator</tt> method are
 * <i>fail-fast</i>: if the set is modified at any time after the iterator is
 * created, in any way except through the iterator's own <tt>remove</tt>
 * method, the iterator will throw a <tt>ConcurrentModificationException</tt>.
 * Thus, in the face of concurrent modification, the iterator fails quickly
 * and cleanly, rather than risking arbitrary, non-deterministic behavior at
 * an undetermined time in the future.</p>
 *
 * @author  Nathan Fiedler
 */
public class SkipList implements Set {
    /** Optimal probability of most skip lists. */
    public static final double OPTIMAL_P = 0.25;
    // MaxLevel = L(N) (where N is an upper bound on the number of
    // elements in a skip list). If p = 0.5, using MaxLevel = 16 is
    // appropriate for data structures containing up to 2^16 elements.
    /** Maximum level of any SkipList instance. */
    protected final int MAX_LEVEL;
    /** Probability value for this skip list. */
    protected final double P;
    /** Tail of this skip list. */
    protected final Element NIL;
    /** The level of this skip list. */
    protected int listLevel;
    /** Header is an element with no data. */
    protected Element listHeader;
    /** Number of elements in this skip list. */
    protected int elementCount;
    /** Increments each time the list changes. */
    protected int modCount;

    /**
     * Constructs an empty SkipList using the default probability
     * and maximum element size.
     */
    public SkipList() {
        this(OPTIMAL_P,
             (int) Math.ceil(Math.log(Integer.MAX_VALUE) /
                             Math.log(1 / OPTIMAL_P)) - 1);
    } // SkipList

    /**
     * Constructs an empty SkipList object using the given probability
     * and maximum level.
     *
     * @param  probability  skip list probability value.
     * @param  maxLevel     maximum skip list level.
     */
    public SkipList(double probability, int maxLevel) {
        P = probability;
        MAX_LEVEL = maxLevel;
        // Header is the root of our skip list.
        listHeader = new Element(MAX_LEVEL, Integer.MIN_VALUE, null);
        // Allocate NIL with a key greater than any valid key;
        // all levels of skip lists terminate on NIL.
        NIL = new Element(0, Integer.MAX_VALUE, null);
        clear();
    } // SkipList

    /**
     * Appends the specified element to the end of this list (optional
     * operation).
     *
     * @param  o  element to be appended to this list.
     * @return  <tt>true</tt> if this collection changed as a result of
     *          the call.
     * @see #insert
     * @exception  ClassCastException
     *             Thrown if the class of the specified element prevents
     *             it from being added to this list.
     * @exception  IllegalArgumentException
     *             Thrown if some aspect of this element prevents it from
     *             being added to this collection.
     */
    public boolean add(Object o) {
        insert(o.hashCode(), o);
        return true;
    } // add

    /**
     * Appends all of the elements in the specified collection to the end
     * of this list, in the order that they are returned by the specified
     * collection's iterator (optional operation). The behavior of this
     * operation is unspecified if the specified collection is modified
     * while the operation is in progress. (Note that this will occur if
     * the specified collection is this list, and it's nonempty.)
     *
     * @param  c  collection whose elements are to be added to this list.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @see #insert
     * @exception  ClassCastException
     *             Thrown if the class of an element in the specified
     *             collection prevents it from being added to this list.
     * @exception  IllegalArgumentException
     *             Thrown if some aspect of an element in the specified
     *             collection prevents it from being added to this list.
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        Iterator iter = c.iterator();
        boolean added = false;
        while (iter.hasNext()) {
            added |= add(iter.next());
        }
        return added;
    } // addAll

    /**
     * Removes all of the elements from this list (optional operation).
     * This list will be empty after this call returns (unless it throws
     * an exception).
     */
    public void clear() {
        // List level is started at one.
        listLevel = 1;
        // All forward pointers of list's header point to NIL.
        for (int i = listHeader.forward.length - 1; i >= 0; i--) {
            listHeader.forward[i] = NIL;
        }
        // Reset element count.
        elementCount = 0;
        modCount++;
    } // clear

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
        boolean found = false;
        Element x = listHeader;
        while (!found && x.forward[0] != NIL) {
            if (x.value == o || x.value != null && x.value.equals(o)) {
                found = true;
                break;
            }
            x = x.forward[0];
        }
        return found;
    } // contains

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of
     * the specified collection.
     *
     * @param  c  collection to be checked for containment in this list.
     * @return  <tt>true</tt> if this list contains all of the elements of
     *          the specified collection.
     * @see #contains(Object)
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
     * Removes the element with the given key from the list.
     *
     * @param  searchKey  key of element to remove.
     * @return  <code>true</code> if element was found and removed.
     */
    public boolean delete(int searchKey) {
        // local update[1..MaxLevel]
        Element[] update = new Element[MAX_LEVEL];
        // x := list->header
        Element x = listHeader;
        // for i := list->level downto 1 do
        for (int i = listLevel - 1; i >= 0; i--) {
            // while x->forward[i]->key < searchKey do
            while (x.forward[i].key < searchKey) {
                // x := x->forward[i]
                x = x.forward[i];
            }
            // update[i] := x
            update[i] = x;
        }
        // x := x->forward[1]
        x = x.forward[0];
        // if x->key = searchKey then
        if (x.key == searchKey) {
            // for i := 1 to list->level do
            for (int i = 0; i < listLevel; i++) {
                // if update[i]->forward[i] != x then break
                if (update[i].forward[i] != x) {
                    break;
                }
                // update[i]->forward[i] := x->forward[i]
                update[i].forward[i] = x.forward[i];
            }
            // free(x) - free, what's that?
            // while list->level > 1 and
            //       list->header->forward[list->level] = NIL do
            while (listLevel > 0 && listHeader.forward[listLevel] == NIL) {
                // list->level := list->level - 1
                listLevel--;
            }
            elementCount--;
            modCount++;
            return true;
        }
        return false;
    } // delete

    /**
     * Compares the specified object with this list for equality.
     * Returns <tt>true</tt> if and only if the specified object is
     * also a list, both lists have the same size, and all corresponding
     * pairs of elements in the two lists are <i>equal</i>. (Two elements
     * <tt>e1</tt> and <tt>e2</tt> are <i>equal</i> if <tt>(e1==null ?
     * e2==null : e1.equals(e2))</tt>.) In other words, two lists are
     * defined to be equal if they contain the same elements in the same
     * order. This definition ensures that the equals method works
     * properly across different implementations of the <tt>List</tt>
     * interface.
     *
     * @param  o  the object to be compared for equality with this list.
     * @return  <tt>true</tt> if the specified object is equal to this list.
     */
    public boolean equals(Object o) {
        return o == this;
    } // equals

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
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return System.identityHashCode(this);
    } // hashCode

    /**
     * Inserts the element using the given search key. If an element
     * with the same key already exists in the skip lists, its value
     * will be replaced with <code>newValue</code>.
     *
     * @param  searchKey  key for element.
     * @param  newValue   new element to insert.
     */
    public void insert(int searchKey, Object newValue) {
        // local update[1..MaxLevel]
        Element[] update = new Element[MAX_LEVEL];
        // x := list->header
        Element x = listHeader;
        // for i := list->level downto 1 do
        for (int i = listLevel - 1; i >= 0; i--) {
            // while x->forward[i]->key < searchKey do
            while (x.forward[i].key < searchKey) {
                // x := x->forward[i]
                x = x.forward[i];
            }
            // -- x->key < searchKey <= x->forward[i]->key
            // update[i] := x
            update[i] = x;
        }
        // x := x->forward[1]
        x = x.forward[0];
        // if x->key = searchKey then x->value := newValue
        if (x.key == searchKey) {
            x.value = newValue;
        } else {
            // lvl := randomLevel()
            int lvl = randomLevel();
            // if lvl > list->level then
            if (lvl > listLevel) {
                // for i := list->level + 1 to lvl do
                for (int i = listLevel; i <= lvl; i++) {
                    // update[i] := list->header
                    update[i] = listHeader;
                }
                // list->level := lvl
                listLevel = lvl;
            }
            // x := madeNode(lvl, searchKey, value)
            x = new Element(lvl, searchKey, newValue);
            // for i := 1 to lvl do
            for (int i = 0; i < lvl; i++) {
                // x->forward[i] := update[i]->forward[i]
                x.forward[i] = update[i].forward[i];
                // update[i]->forward[i] := x
                update[i].forward[i] = x;
            }
        }
        elementCount++;
        modCount++;
    } // insert

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements.
     */
    public boolean isEmpty() {
        return elementCount == 0;
    } // isEmpty

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return new Iter();
    } // iterator

    /**
     * Return a random level.
     *
     * @return  level selected randomly.
     */
    protected int randomLevel() {
        int lvl = 1;
        while (lvl < MAX_LEVEL && Math.random() < P) {
            lvl++;
        }
        return lvl;
    } // randomLevel

    /**
     * Removes the first occurrence in this list of the specified element.
     * If this list does not contain the element, it is unchanged. More
     * formally, removes the element with the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
     * element exists).
     *
     * @param  o  element to be removed from this list, if present.
     * @return <tt>true</tt> if this list contained the specified element.
     */
    public boolean remove(Object o) {
        return delete(o.hashCode());
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
     * specified collection (optional operation). In other words, removes
     * from this list all the elements that are not contained in the
     * specified collection.
     *
     * @param  c  collection that defines which elements this set will retain.
     * @return  <tt>true</tt> if this list changed as a result of the call.
     * @exception  UnsupportedOperationException
     *             Thrown if the <tt>retainAll</tt> method is not supported
     *             by this list.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    } // retainAll

    /**
     * Searches for the element with the given key.
     *
     * @param  searchKey  key to look for.
     * @return  element if found, null if not found. Note that you may
     *          not want to store nulls in this list as it would then
     *          be difficult to know the difference.
     */
    public Object search(int searchKey) {
        // x := list->header
        Element x = listHeader;
        // -- loop invariant: x->key < searchKey
        // for i := list->level downto 1 do
        for (int i = listLevel - 1; i >= 0; i--) {
            // while x->forward[i]->key < searchKey do
            while (x.forward[i].key < searchKey) {
                // x := x->forward[i]
                x = x.forward[i];
            }
        }
        // -- x->key < searchKey <= x->forward[1]->key
        // x := x->forward[1]
        x = x.forward[0];
        // if x->key = searchKey then return x->value
        if (x.key == searchKey) {
            return x.value;
        }
        // else return failure
        return null;
    } // search

    /**
     * Searches for the element with a key that is the least smaller
     * value of the given key.
     *
     * @param  searchKey  key to look for.
     * @return  element if found, null if not found.
     */
    public Object searchLeastSmaller(int searchKey) {
        // x := list->header
        Element x = listHeader;
        // -- loop invariant: x->key < searchKey
        // for i := list->level downto 1 do
        for (int i = listLevel - 1; i >= 0; i--) {
            // while x->forward[i]->key < searchKey do
            while (x.forward[i].key < searchKey) {
                // x := x->forward[i]
                x = x.forward[i];
            }
        }
        // -- x->key < searchKey <= x->forward[1]->key

        // Check next one in case of an exact match.
        if (x.forward[0].key == searchKey) {
            return x.forward[0].value;
        } else {
            return x.value;
        }
    } // searchLeastSmaller

    /**
     * Searches for the element just after the one found using the
     * given key (where the key value may be the least smaller of
     * the given key).
     *
     * @param  searchKey  key to look for.
     * @return  next element if found, null if not found.
     */
    public Object searchNextLarger(int searchKey) {
        // x := list->header
        Element x = listHeader;
        // -- loop invariant: x->key < searchKey
        // for i := list->level downto 1 do
        for (int i = listLevel - 1; i >= 0; i--) {
            // while x->forward[i]->key < searchKey do
            while (x.forward[i].key < searchKey) {
                // x := x->forward[i]
                x = x.forward[i];
            }
        }
        // -- x->key < searchKey <= x->forward[1]->key

        // Check next one in case of an exact match.
        Element t = null;
        if (x.forward[0].key == searchKey) {
            t = x.forward[0];
        } else {
            t = x;
        }
        // Return the next element in the list.
        if (t.forward[0] == NIL) {
            return null;
        } else {
            return t.forward[0].value;
        }
    } // searchNextLarger

    /**
     * Returns the number of elements in this list. If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return elementCount;
    } // size

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence. Obeys the general contract of the
     * <tt>Collection.toArray()</tt> method.
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence.
     * @see Arrays#asList(Object[])
     */
    public Object[] toArray() {
        Object[] result = new Object[elementCount];
        Element x = listHeader;
        for (int i = 0; i < elementCount; i++) {
            result[i] = x.forward[0].value;
            x = x.forward[0];
        }
        return result;
    } // toArray

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence; the runtime type of the returned array is that
     * of the specified array. Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     *
     * @param  a  the array into which the elements of this list are to
     *            be stored, if it is big enough; otherwise, a new array
     *            of the same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     * @exception  ArrayStoreException
     *             Throw if the runtime type of the specified array is not
     *             a supertype of the runtime type of every element in this
     *             list.
     */
    public Object[] toArray(Object a[]) {
        // Get the elements into an array.
        Object[] result = toArray();

        // Copy the elements into the array of the desired type.
        int size = size();
        if (a.length < size) {
            a = (Object[]) Array.newInstance(
                a.getClass().getComponentType(), size);
        }
        for (int ii = 0; ii < size; ii++) {
            a[ii] = result[ii];
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    } // toArray

    /**
     * Class Element represents an element of a skip list.
     */
    protected class Element {
        /** Key of element. */
        int key;
        /** Value of element. */
        Object value;
        /** List of forward pointers. */
        Element[] forward;

        /**
         * Constructs an Element for the given key and value.
         *
         * @param  level  level for this node (number of forward pointers).
         * @param  key    key for element.
         * @param  value  value for element.
         */
        public Element(int level, int key, Object value) {
            this.key = key;
            this.value = value;
            forward = new Element[level];
        } // Element
    } // Element

    /**
     * An iterator over a skip list.
     */
    protected class Iter implements Iterator {
        /** Index into the skip list. */
        protected int index;
        /** The modCount of the list at the time we were instantiated. */
        protected int modCount;
        /** Current element being examined. */
        protected Element elem;

        /**
         * Constructs a skip list iterator.
         */
        public Iter() {
            this.modCount = SkipList.this.modCount;
            elem = listHeader;
        } // Iter

        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In
         * other words, returns <tt>true</tt> if <tt>next</tt> would return
         * an element rather than throwing an exception.)
         *
         * @return  <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            if (this.modCount != SkipList.this.modCount) {
                throw new ConcurrentModificationException();
            }
            return elem.forward[0] != NIL;
        } // hasNext

        /**
         * Returns the next element in the iteration.
         *
         * @return  the next element in the iteration.
         * @exception  NoSuchElementException
         *             iteration has no more elements.
         */
        public Object next() {
            if (this.modCount != SkipList.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (hasNext()) {
                elem = elem.forward[0];
                return elem.value;
            } else {
                throw new NoSuchElementException();
            }
        } // next

        /**
         * Removes from the underlying collection the last element returned
         * by the iterator (optional operation).
         *
         * <p>Throws <code>UnsupportedOperationException</code>.</p>
         *
         * @exception  UnsupportedOperationException
         *             always thrown.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        } // remove
    } // Iter
} // SkipList
