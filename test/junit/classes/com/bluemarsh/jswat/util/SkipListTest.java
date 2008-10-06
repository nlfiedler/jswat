/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      Unit Tests
 * FILE:        SkipListTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/23/02        Initial version
 *
 * $Id: SkipListTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Test the SkipList class.
 */
public class SkipListTest extends TestCase {

    public SkipListTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SkipListTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testSkipListEmptySize() {
        SkipList list = new SkipList();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        list.insert(12, new Integer(12));
        list.insert(24, new Integer(24));
        list.insert(36, new Integer(36));
        assertTrue(!list.isEmpty());
        assertEquals(3, list.size());
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    public void testSkipListEquals() {
        SkipList list = new SkipList();
        list.insert(12, new Integer(12));
        list.insert(24, new Integer(24));
        list.insert(36, new Integer(36));
        assertTrue(list.equals(list));
        assertTrue(!list.equals(this));
    }

    public void testSkipListDelete() {
        SkipList list = new SkipList();
        list.insert(12, new Integer(12));
        list.insert(24, new Integer(24));
        list.insert(36, new Integer(36));
        assertTrue(list.delete(24));
        assertTrue(!list.delete(24));
        assertEquals(2, list.size());
        assertTrue(!list.isEmpty());
    }

    public void testSkipListConsistent() {
        // populate the list
        SkipList list = new SkipList();
        for (int ii = 1; ii <= 1000; ii++) {
            list.insert(ii, new Integer(ii));
        }

        // search for every element
        for (int ii = 1; ii <= 1000; ii++) {
            Integer igr = (Integer) list.search(ii);
            if (igr == null || igr.intValue() != ii) {
                fail("could not find element " + ii);
            }
        }

        assertTrue(list.contains(new Integer(5)));
        assertTrue(list.contains(new Integer(10)));
        assertTrue(list.contains(new Integer(55)));
        assertTrue(list.contains(new Integer(100)));
        assertTrue(list.contains(new Integer(555)));
        assertTrue(list.contains(new Integer(999)));

        List contents = new ArrayList();
        for (int ii = 1; ii <= 1000; ii += 10) {
            contents.add(new Integer(ii));
        }
        assertTrue(list.containsAll(contents));

        Iterator iter = list.iterator();
        int jj = 1;
        while (iter.hasNext()) {
            Integer igr = (Integer) iter.next();
            if (igr == null || igr.intValue() != jj) {
                fail("wrong iterator element at " + jj + ", got " + igr);
            }
            jj++;
        }
        assertEquals(1001, jj);
    }

    public void testSkipListToArray() {
        SkipList list = new SkipList();
        for (int i = 1; i <= 20; i++) {
            list.insert(i, new Integer(i));
        }
        Object[] arr = list.toArray();
        assertEquals(20, arr.length);
        for (int ii = 0; ii < arr.length; ii++) {
            Integer in = (Integer) arr[ii];
            assertEquals(ii + 1, in.intValue());
        }

        Integer[] intarr = (Integer[]) list.toArray(new Integer[0]);
        assertEquals(20, arr.length);
        for (int ii = 0; ii < arr.length; ii++) {
            Integer in = (Integer) arr[ii];
            assertEquals(ii + 1, in.intValue());
        }
    }

    public void testSkipListSearch() {
        SkipList list = new SkipList();
        for (int ii = 0; ii <= 1000; ii += 10) {
            list.insert(ii, new Integer(ii));
        }

        // test searchLeastSmaller
        for (int i = 0; i < 100; i++) {
            int k = (int) (Math.random() * 100);
            Integer o = (Integer) list.searchLeastSmaller(k);
            if (o == null ||
                o.intValue() % 10 != 0 ||
                o.intValue() - k >= 10) {
                fail("got wrong thing: " + o + " for " + k);
            }
        }

        // special case of exact match for searchLeastSmaller
        Integer o = (Integer) list.searchLeastSmaller(20);
        if (o == null || o.intValue() != 20) {
            fail("got wrong thing: " + o + " for 20");
        }

        // test searchNextLarger
        for (int i = 0; i < 10; i++) {
            int k = (int) (Math.random() * 100);
            o = (Integer) list.searchNextLarger(k);
            if (o == null ||
                o.intValue() % 10 != 0 ||
                k - o.intValue() >= 10) {
                fail("got wrong thing: " + o + " for " + k);
            }
        }

        // special case of exact match for searchNextLarger
        o = (Integer) list.searchNextLarger(20);
        if (o == null || o.intValue() != 30) {
            fail("got wrong thing: " + o + " for 20");
        }
    }
}
