/*********************************************************************
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
 * PROJECT:     Utilities
 * MODULE:      Soft HashMap
 * FILE:        CacheMap.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/17/98        Initial version of IntHashtable
 *      nf      10/19/99        Made into CacheTable
 *      nf      10/24/99        Rewrote to use java.util.Hashtable
 *      nf      01/12/02        Changed to CacheMap, uses HashMap
 *
 * DESCRIPTION:
 *      This class implements a Map that uses soft references for each
 *      key and value stored in the map. This allows the objects to
 *      be garbage-collected if the JVM runs low on memory.
 *
 * $Id: CacheMap.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class implements a <code>Map</code>, which maps keys to values.
 * Any non-null object can be used as a key or as a value. The twist with
 * this map is that the value objects are stored in this map using a
 * <code>SoftReference</code>. This allows the value objects to be
 * garbage-collected if the Java VM is running out of memory. The caller
 * will not know when the value object has been collected until the
 * <code>get()</code> method is called to retrieve that object, in which
 * case <code>null</code> will be returned.
 *
 * <p>This class may be useful for storing objects while enough memory is
 * available. As memory in the JVM runs low, the value objects will
 * be discarded. Thus, the name "CacheMap", meaning it holds values
 * so long as memory is not a problem.</p>
 *
 * <p>This class has the ability to keep only the N most recently stored
 * key-value mappings. This is useful to prevent the map from growing
 * without bound when it is not necessary to keep all mappings. See the
 * <code>setMaximumSize(int)</code> method.</p>
 *
 * <p>An instance of CacheMap has two parameters that affect its
 * efficiency: its <em>capacity</em> and its <em>load factor</em>. The
 * load factor should be between 0.0 and 1.0. When the number of entries
 * in the map exceeds the product of the load factor and the current
 * capacity, the capacity is increased by calling the <code>rehash</code>
 * method. Larger load factors use memory more efficiently, at the expense
 * of larger expected time per access.</p>
 *
 * <p>If many entries are to be made into a CacheMap, creating it with a
 * sufficiently large capacity may allow the entries to be inserted more
 * efficiently than letting it perform automatic rehashing as needed to
 * grow the map.</p>
 *
 * @author  Nathan Fiedler
 */
public class CacheMap implements Cloneable, Map {
    /** The hash map data. This is a coalesced chain of entries. */
    protected HashMap cache;
    /** Circular buffer of the most-recently-put keys. */
    protected Object[] mrpKeys;
    /** Indicates where the next key will be placed in the circular
     * buffer. */
    protected int mrpIndex;

    /**
     * Constructs a new, empty hash map with a default capacity of 16
     * and load factor of 0.75.
     */
    public CacheMap() {
        this(new HashMap());
    } // CacheMap

    /**
     * Constructs a new, empty hash map with the specified initial
     * capacity and default load factor of 0.75.
     *
     * @param  initialCapacity  the initial capacity of the hash map.
     * @exception  IllegalArgumentException
     *             if the initial capacity is less than zero.
     */
    public CacheMap(int initialCapacity) {
        this(new HashMap(initialCapacity));
    } // CacheMap

    /**
     * Constructs a new, empty hash map with the specified initial 
     * capacity and the specified load factor.
     *
     * @param  initialCapacity  the initial capacity of the hash map.
     * @param  loadFactor       a number between 0.0 and 1.0.
     * @exception  IllegalArgumentException
     *             if the initial capacity is less than zero, if the
     *             load factor is less than or equal to zero, or if the
     *             load factor is greater than one.
     */
    public CacheMap(int initialCapacity, float loadFactor) {
        this(new HashMap(initialCapacity, loadFactor));
    } // CacheMap

    /**
     * Constructs a CacheMap from the given map of values.
     *
     * @param  map  map from which to populate this map.
     */
    protected CacheMap(HashMap map) {
        cache = map;
    } // CacheMap

    /**
     * Clears this map so that it contains no keys or values.
     */
    public void clear() {
        cache.clear();
    } // clear

    /**
     * Creates a shallow copy of this map. All the structure of the 
     * map itself is copied, but the values are not cloned. This is
     * a relatively expensive operation.
     *
     * @return  a clone of the map, or null if clone not supported
     */
    public Object clone() {
        return new CacheMap(new HashMap(cache));
    } // clone

    /**
     * Returns <code>true</code> if this map contains a mapping for the
     * specified key.
     *
     * @param  key  key whose presence in this map is to be tested.
     * @return  <code>true</code> if this map contains a mapping for the
     *          specified key.
     * @exception  ClassCastException
     *             if the key is of an inappropriate type for this map.
     * @exception  NullPointerException
     *             if the key is <code>null</code> and this map does not not
     *             permit <code>null</code> keys.
     */
    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    } // containsKey

    /**
     * Returns <code>true</code> if this map maps one or more keys to the
     * specified value.  More formally, returns <code>true</code> if and only
     * if this map contains at least one mapping to a value <code>v</code>
     * such that <code>(value==null ? v==null : value.equals(v))</code>.
     * This operation will probably require time linear in the map size
     * for most implementations of the <code>Map</code> interface.
     *
     * @param  value  value whose presence in this map is to be tested.
     * @return  <code>true</code> if this map maps one or more keys to the
     *          specified value.
     */
    public boolean containsValue(Object value) {
        Collection coll = values();
        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            SoftReference sr = (SoftReference) iter.next();
            Object o = sr.get();
            if (o != null && o.equals(value)) {
                return true;
            }
        }
        return false;
    } // containsValue

    /**
     * Returns a set view of the mappings contained in this map. Each
     * element in the returned set is a <code>Map.Entry</code>. The
     * set is backed by the map, so changes to the map are reflected
     * in the set, and vice-versa.  If the map is modified while an
     * iteration over the set is in progress, the results of the
     * iteration are undefined. The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <code>Iterator.remove</code>, <code>Set.remove</code>,
     * <code>removeAll</code>, <code>retainAll</code> and
     * <code>clear</code> operations. It does not support the
     * <code>add</code> or <code>addAll</code> operations.
     *
     * @return  a set view of the mappings contained in this map.
     *          Note that the set contains the SoftReference objects;
     *          the actual values are inside those references.
     */
    public Set entrySet() {
        return cache.entrySet();
    } // entrySet

    /**
     * Compares the specified object with this map for equality. Returns
     * <code>true</code> if the given object is also a map and the two
     * Maps represent the same mappings. More formally, two maps
     * <code>t1</code> and <code>t2</code> represent the same mappings if
     * <code>t1.entrySet().equals(t2.entrySet())</code>. This ensures
     * that the <code>equals</code> method works properly across
     * different implementations of the <code>Map</code> interface.
     *
     * @param  o  object to be compared for equality with this map.
     * @return  <code>true</code> if the specified object is equal to
     *          this map.
     */
    public boolean equals(Object o) {
        return o == this;
    } // equals

    /**
     * Returns the value to which the specified key is mapped in this
     * map. It is possible the value object has been garbage collected
     * and this may return null, in that case.
     *
     * @param   key   a key in the map.
     * @return  the value to which the key is mapped in this map
     *          or null if the key is not mapped to any value
     * @see     #put
     */
    public Object get(Object key) {
        Object o = cache.get(key);
        if (o != null && o instanceof SoftReference) {
            return ((SoftReference) o).get();
        }
        return o;
    } // get

    /**
     * Returns the hash code value for this map. The hash code of a map
     * is defined to be the sum of the hashCodes of each entry in the map's
     * entrySet view. This ensures that <code>t1.equals(t2)</code> implies
     * that <code>t1.hashCode()==t2.hashCode()</code> for any two maps
     * <code>t1</code> and <code>t2</code>, as required by the general
     * contract of <code>Object.hashCode()</code>.
     *
     * @return the hash code value for this map.
     * @see Map.Entry#hashCode()
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
    public int hashCode() {
        return System.identityHashCode(this);
    } // hashCode

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    } // isEmpty

    /**
     * Returns a set view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set,
     * and vice-versa. If the map is modified while an iteration over the
     * set is in progress, the results of the iteration are undefined. The
     * set supports element removal, which removes the corresponding mapping
     * from the map, via the <code>Iterator.remove</code>,
     * <code>Set.remove</code>, <code>removeAll</code>,
     * <code>retainAll</code>, and <code>clear</code> operations. It does
     * not support the <code>add</code> or <code>addAll</code> operations.
     *
     * @return  a set view of the keys contained in this map.
     */
    public Set keySet() {
        return cache.keySet();
    } // keySet

    /**
     * Maps the specified key to the specified value in this map.
     * Neither the key nor the value can be null.
     * <p>
     * The value can be retrieved by calling the <code>get</code> method 
     * with a key that is equal to the original key. 
     *
     * @param  key    the map key.
     * @param  value  the value.
     * @return  the previous value of the specified key in this map,
     *          or null if it did not have one.
     * @see     #get
     */
    public Object put(Object key, Object value) {
        if (mrpKeys != null) {
            // Update the most-recent buffer.
            if (mrpKeys[mrpIndex] != null) {
                cache.remove(mrpKeys[mrpIndex]);
            }
            mrpKeys[mrpIndex] = key;
            mrpIndex++;
            if (mrpIndex >= mrpKeys.length) {
                mrpIndex = 0;
            }
        }
        Object o = cache.put(key, new SoftReference(value));
        if (o != null && o instanceof SoftReference) {
            return ((SoftReference) o).get();
        }
        return o;
    } // put

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param  t  Mappings to be stored in this map.
     * @exception  UnsupportedOperationException
     *             if the <code>putAll</code> method is not supported by
     *             this map.
     * @exception  ClassCastException
     *             if the class of a key or value in the specified map
     *             prevents it from being stored in this map.
     * @exception  IllegalArgumentException
     *             some aspect of a key or value in the specified map
     *             prevents it from being stored in this map.
     * @exception  NullPointerException
     *             the specified map is <code>null</code>, or if this map
     *             does not permit <code>null</code> keys or values, and
     *             the specified map contains <code>null</code> keys or
     *             values.
     */
    public void putAll(Map t) {
        Set entries = t.entrySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            put(entry.getKey(), new SoftReference(entry.getValue()));
        }
    } // putAll

    /**
     * Removes the key and its corresponding value from this map. This
     * method does nothing if the key is not in the map.
     *
     * @param   key  the key that needs to be removed.
     * @return  the value to which the key had been mapped in this map,
     *          or null if the key did not have a mapping.
     */
    public Object remove(Object key) {
        Object o = cache.remove(key);
        if (o != null && o instanceof SoftReference) {
            return ((SoftReference) o).get();
        }
        return o;
    } // remove

    /**
     * Sets the maximum size of this map. That is, the number of
     * elements stored in this map will never exceed <code>size</code>.
     * As new elements are put into the map, the oldest entries will be
     * removed. Only the <code>size</code> most recently put entries
     * will exist in the map.
     *
     * @param  size  number of elements to keep.
     */
    public void setMaximumSize(int size) {
        Object[] newmrp = new Object[size];
        if (mrpKeys != null) {
            // Copy over the existing buffer.
            int c = Math.min(size, mrpKeys.length);
            System.arraycopy(mrpKeys, 0, newmrp, 0, c);
        }
        if (mrpIndex >= size) {
            mrpIndex = 0;
        }
        mrpKeys = newmrp;
    } // setMaximumSize

    /**
     * Returns the number of key-value mappings in this map. If the
     * map contains more than <code>Integer.MAX_VALUE</code> elements,
     * returns <code>Integer.MAX_VALUE</code>.
     *
     * @return  the number of key-value mappings in this map.
     */
    public int size() {
        return cache.size();
    } // size

    /**
     * Returns the string representation of this map. The key/value pairs
     * are inserted into the returned string in no particular order.
     *
     * @return  string value of this map.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer("CacheMap=[");
        buff.append(cache.toString());
        buff.append("]");
        return buff.toString();
    } // toString

    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. If the map is
     * modified while an iteration over the collection is in progress,
     * the results of the iteration are undefined. The collection
     * supports element removal, which removes the corresponding mapping
     * from the map, via the <code>Iterator.remove</code>,
     * <code>Collection.remove</code>, <code>removeAll</code>,
     * <code>retainAll</code> and <code>clear</code> operations.
     * It does not support the <code>add</code> or <code>addAll</code>
     * operations.
     *
     * @return  a collection view of the values contained in this map.
     *          Note that the collection contains the SoftReference
     *          objects; the actual values are inside those references.
     */
    public Collection values() {
        return cache.values();
    } // values
} // CacheMap
