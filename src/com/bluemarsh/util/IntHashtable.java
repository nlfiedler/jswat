/*********************************************************************
 *
 *	Copyright (C) 1999 Nathan Fiedler
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
 * PROJECT:     Utilities
 * MODULE:      int-key'd Hashtable
 * FILE:        IntHashtable.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      NF      9/17/98         Initial version
 *
 * DESCRIPTION:
 *      This class implements a hashtable that uses integer values
 *      as the key for each object stored in the table.
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class implements a hashtable, which maps keys to values. Any 
 * non-null object can be used as a value. The keys must be integers.
 * This is advantegous over java.util.Hashtable in that it saves the
 * user from creating key objects when all they are good for is
 * providing a hashcode value.
 * <p>
 * An instance of IntHashtable has two parameters that affect its
 * efficiency: its <em>capacity</em> and its <em>load factor</em>. The
 * load factor should be between 0.0 and 1.0. When the number of entries
 * in the hashtable exceeds the product of the load factor and the current
 * capacity, the capacity is increased by calling the <code>rehash</code>
 * method. Larger load factors use memory more efficiently, at the expense
 * of larger expected time per access.
 * <p>
 * If many entries are to be made into a IntHashtable, creating it with a
 * sufficiently large capacity may allow the entries to be inserted more
 * efficiently than letting it perform automatic rehashing as needed to
 * grow the table. 
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/17/98
 */
public class IntHashtable implements Cloneable {
    /**
     * The hash table data. This is a coalesced chain of entries.
     */
    protected Entry table[];
    /**
     * The total number of entries in the hash table.
     */
    protected int count;
    /**
     * The table is rehashed when its size exceeds this threshold. (The
     * value of this field is (int)(capacity * loadFactor).)
     */
    protected int threshold;
    /**
     * The load factor for the hashtable.
     */
    protected float loadFactor;

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor, which is 0.75. 
     */
    public IntHashtable() {
        // Use a capacity that is not a power of 2 or 10, to
        // avoid hashing problems later.
	this(101, 0.75f);
    } // IntHashtable

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity and default load factor, which is 0.75.
     *
     * @param  initialCapacity  the initial capacity of the hashtable
     * @exception IllegalArgumentException if the initial capacity is
     *            less than zero
     */
    public IntHashtable(int initialCapacity) {
	this(initialCapacity, 0.75f);
    } // IntHashtable

    /**
     * Constructs a new, empty hashtable with the specified initial 
     * capacity and the specified load factor.
     *
     * @param  initialCapacity  the initial capacity of the hashtable
     * @param  loadFactor       a number between 0.0 and 1.0
     * @exception  IllegalArgumentException  if the initial capacity is
     *             less than zero, if the load factor is less than or
     *             equal to zero, or if the load factor is greater than
     *             one.
     */
    public IntHashtable(int initialCapacity, float loadFactor) {
	if (initialCapacity < 0) {
	    throw new IllegalArgumentException("Illegal Capacity: " +
                                               initialCapacity);
        }
        if ((loadFactor > 1) || (loadFactor <= 0)) {
            throw new IllegalArgumentException("Illegal Load: " +
                                               loadFactor);
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
	this.loadFactor = loadFactor;
	table = new Entry[initialCapacity];
	threshold = (int)(initialCapacity * loadFactor);
    } // IntHashtable

    /**
     * Clears this hashtable so that it contains no keys or values.
     */
    public synchronized void clear() {
	Entry tab[] = table;
	for (int i = tab.length - 1; i >= 0; i--) {
	    tab[i] = null;
        }
	count = 0;
    } // clear

    /**
     * Creates a shallow copy of this hashtable. All the structure of the 
     * hashtable itself is copied, but the values are not cloned. 
     * This is a relatively expensive operation.
     *
     * @return  a clone of the hashtable, or null if clone not supported
     */
    public synchronized Object clone() {
        IntHashtable ht = null;
        try {
            ht = (IntHashtable)super.clone();
            ht.table = new Entry[table.length];
            for (int i = table.length - 1; i >= 0; i--) {
                ht.table[i] = (table[i] != null) 
                    ? (Entry)table[i].clone() : null;
            }
        }
        catch (CloneNotSupportedException cnse) {
            // what's the likelihood of this?
        }
        return ht;
    } // clone

    /**
     * Returns an enumeration of the values in this hashtable.
     * Use the Enumeration methods on the returned object to
     * fetch the elements sequentially.
     *
     * @return  An enumeration of the values in this hashtable.
     */
    public Enumeration elements() {
        return new Enumerator();
    } // elements

    /**
     * Returns the value to which the specified key is mapped in this
     * hashtable.
     *
     * @param   key   a key in the hashtable
     * @return  the value to which the key is mapped in this hashtable
     *          or null if the key is not mapped to any value
     * @see     #put
     */
    public synchronized Object get(int key) {
	Entry tab[] = table;
	int index = hash(key, tab.length);
	for (Entry e = tab[index]; e != null ; e = e.next) {
	    if (e.hash == key) {
		return e.value;
	    }
	}
	return null;
    } // get

    /**
     * Returns the first key in this hashtable. Since the order
     * of elements in the table is non-deterministic, this method
     * is only useful for getting a random key out of the table.
     *
     * @return  Random key from this hashtable,
     *          or 0 if the table is empty.
     * @see #get
     */
    public synchronized int getFirstKey() {
	Entry tab[] = table;
	for (int i = tab.length - 1; i >= 0; i--) {
            if (tab[i] != null) {
                return tab[i].hash;
            }
	}
	return 0;
    } // getFirstKey

    /**
     * Calculate the hash value given the key value and the
     * size of the hashtable. This uses the multiplication
     * method for hashing, as given in "Introduction To
     * Algorithms", Chapter 12, page 228.
     *
     * @param  k  key value to hash
     * @param  m  number of slots in hashtable
     */
    protected int hash(int k, int m) {
        // Use the Knuth value for A.
        double p = k * 0.6180339887;
        return (int)(m * (p - Math.floor(p)));
    } // hash

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return count == 0;
    } // isEmpty

    /**
     * Maps the specified key to the specified value in this hashtable.
     * Neither the key nor the value can be null.
     * <p>
     * The value can be retrieved by calling the <code>get</code> method 
     * with a key that is equal to the original key. 
     *
     * @param  key    the hashtable key
     * @param  value  the value
     * @return  the previous value of the specified key in this hashtable,
     *          or null if it did not have one
     * @see     #get
     */
    public synchronized Object put(int key, Object value) {
	// Make sure the value is not null.
	if (value == null) {
	    throw new NullPointerException("Null value not allowed");
	}

	// Makes sure the key is not already in the hashtable.
	Entry tab[] = table;
	int index = hash(key, tab.length);
	for (Entry e = tab[index]; e != null ; e = e.next) {
	    if (e.hash == key) {
                // Save the new value and return the old one.
		Object old = e.value;
		e.value = value;
		return old;
	    }
	}

        // Rehash the table if the threshold is exceeded.
	if (count >= threshold) {
	    rehash();
            tab = table;
            index = hash(key, tab.length);
	} 

	// Creates the new Entry.
	Entry e = new Entry(key, value, tab[index]);
	tab[index] = e;
	count++;
	return null;
    } // put

    /**
     * Increases the capacity of and internally reorganizes this 
     * hashtable, in order to accommodate and access its entries more 
     * efficiently. This method is called automatically when the 
     * number of keys in the hashtable exceeds this hashtable's capacity 
     * and load factor. 
     */
    protected void rehash() {
	int oldCapacity = table.length;
	Entry oldTable[] = table;

        // Increase the size of the table by a factor of two.
	int newCapacity = oldCapacity * 2 + 1;
	Entry newTable[] = new Entry[newCapacity];
	threshold = (int)(newCapacity * loadFactor);
	table = newTable;

        // For each entry in the old table, rehash it into the new
        // table. This includes rehashing the chained entries.
	for (int i = oldCapacity - 1; i >= 0; i--) {
            Entry old = oldTable[i];
	    while (old != null) {
		Entry e = old;
		old = old.next;
		int index = hash(e.hash, newCapacity);
		e.next = newTable[index];
		newTable[index] = e;
	    }
	}
    } // rehash

    /**
     * Removes the key and its corresponding value from this 
     * hashtable. This method does nothing if the key is not
     * in the hashtable.
     *
     * @param   key  the key that needs to be removed
     * @return  the value to which the key had been mapped in this hashtable,
     *          or null if the key did not have a mapping
     */
    public synchronized Object remove(int key) {
	Entry tab[] = table;
	int index = hash(key, tab.length);
        // Find the matching entry in the entry chain.
	for (Entry e = tab[index], prev = null; e != null;
             prev = e, e = e.next) {
	    if (e.hash == key) {
		if (prev != null) {
                    // Link around the entry being removed.
		    prev.next = e.next;
		} else {
                    // Move the second entry to the front.
		    tab[index] = e.next;
		}
		count--;
                // Return the entry's value object.
		Object oldValue = e.value;
		e.value = null;
		return oldValue;
	    }
	}
	return null;
    } // remove

    /**
     * Removes the first key and its corresponding value from this 
     * hashtable. Since the order of elements in the table is
     * non-deterministic, this method is only useful for getting a
     * random key out of the table.
     *
     * @return  The first value found in this hashtable,
     *          or null if the table is empty.
     */
    public synchronized Object removeFirst() {
	Entry tab[] = table;
	for (int i = tab.length; i-- >= 0;) {
            Entry e = tab[i];
            if (e != null) {

                // Move the second entry to the front.
                tab[i] = e.next;
                count--;
                // Return the entry's value object.
                Object oldValue = e.value;
                e.value = null;
                return oldValue;
            }
	}
	return null;
    } // removeFirst

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */
    public int size() {
	return count;
    } // size

    /**
     * Returns the string representation of this hashtable.
     * The key/value pairs are inserted into the returned
     * string in no particular order.
     *
     * @return  string value of this table
     */
    public String toString() {
        StringBuffer buff = new StringBuffer("IntHashtable=[");
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                buff.append(table[i] + ", ");
            }
        }
        buff.append("]");
        return buff.toString();
    } // toString

    /**
     * Hashtable entry class. This implements the unit that
     * is stored in the hastable. It holds the hash value
     * and the object being stored. Each unit links to the
     * next one in the chain.
     *
     * @author  Nathan Fiedler
     * @version 1.0 9/17/98
     */
    protected static class Entry {
        /** The hash value for this entry. */
	protected int hash;
        /** The value object stored here. */
	protected Object value;
        /** The reference to the next entry in the list. */
	protected Entry next;

        /**
         * Creates a hash entry object with the given hash value,
         * value object, and next pointer.
         *
         * @param  hash   hash value for this entry
         * @param  value  value stored in this entry
         * @param  next   next entry in list
         */
	protected Entry(int hash, Object value, Entry next) {
	    this.hash = hash;
	    this.value = value;
	    this.next = next;
	} // Entry

        /**
         * Creates a copy of this object.
         *
         * @return  new copy of this entry
         */
	protected Object clone() {
            // The IntHashtable.clone() method doesn't scan for
            // the chained entries, so we must clone them here.
	    return new Entry(hash, value,
                                 (next == null ?
                                  null : (Entry)next.clone()));
	} // clone

        /**
         * Returns the value stored in this entry.
         *
         * @return  value from this entry
         */
	public Object getValue() {
	    return value;
	} // getValue

        /**
         * Returns the hash code value for this entry. This is the
         * same as the hash code of the value stored here, or'd
         * with the hash code of the value object. Since we can
         * create an entry that has a null value, the return value
         * might be just the initial hash code given when creating
         * the entry.
         *
         * @return  entry's hashcode value
         */
	public int hashCode() {
	    return hash ^ (value == null ? 0 : value.hashCode());
	} // hashCode

        /**
         * Sets the value object for this entry.
         *
         * @param  value  value to be stored here
         * @return  value previously stored here, if any
         */
	public Object setValue(Object value) {
	    if (value == null) {
		throw new NullPointerException("Null value not allowed");
            }
	    Object oldValue = this.value;
	    this.value = value;
	    return oldValue;
	} // setValue

        /**
         * Returns the string representation of this entry. This
         * includes the hash and value elements, but ignores the
         * next pointer.
         *
         * @return  string value of this entry
         */
        public String toString() {
            return new String("(" + hash + "=" + value + ")");
        } // toString
    } // Entry

    /**
     * A hashtable enumerator class. This class implements the
     * Enumeration interface.
     *
     * @author  Nathan Fiedler
     * @version 1.0  10/24/99
     */
    protected class Enumerator implements Enumeration {
        /** Copy of the hashtable. */
	protected Entry[] table = IntHashtable.this.table;
        /** Current value being enumerated. */
	protected int index = table.length;
        /** Current entry being enumerated. */
	protected Entry entry = null;

        /**
         * Tests if there are more elements to be enumerated.
         *
         * @return  True if more elements exist.
         */
	public boolean hasMoreElements() {
	    while ((entry == null) && (index > 0)) {
		entry = table[--index];
            }
	    return entry != null;
	} // hasMoreElements

        /**
         * Returns the next element in the hashtable.
         *
         * @return  Next value object.
         */
	public Object nextElement() {
	    while ((entry == null) && (index > 0)) {
		entry = table[--index];
            }
	    if (entry != null) {
		Entry e = entry;
		entry = e.next;
		return e.value;
	    }
	    throw new NoSuchElementException("Hashtable Enumerator");
	} // nextElement
    } // Enumerator
} // IntHashtable
