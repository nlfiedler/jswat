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
 * FILE:        PriorityListTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/23/02        Initial version
 *
 * $Id: PriorityListTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Test the PriorityList class.
 */
public class PriorityListTest extends TestCase {

    public PriorityListTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(PriorityListTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testPriorityListEmptySize() {
        PriorityList list = new PriorityList();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        for (int ii = 0; ii < 1000; ii++) {
            list.add(new Integer(ii), ii);
        }
        assertTrue(!list.isEmpty());
        assertEquals(1000, list.size());
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    public void testPriorityListEquals() {
        PriorityList list = new PriorityList();
        list.add(new Integer(12), 12);
        list.add(new Integer(24), 24);
        list.add(new Integer(36), 36);
        assertTrue(list.equals(list));
        assertTrue(!list.equals(this));
    }

    public void testPriorityListGet() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 1000; ii++) {
            list.add(new Integer(ii), ii);
        }
        for (int i = 0; i < 1000; i++) {
            Integer igr = (Integer) list.get(i);
            if (igr == null || igr.intValue() != (999 - i)) {
                fail("could not find element " + i);
            }
        }
    }

    public void testPriorityListContents() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 1000; ii++) {
            list.add(new Integer(ii), ii);
        }
        Integer igr = new Integer(500);
        int idx = list.indexOf(igr);
        int lidx = list.lastIndexOf(igr);
        assertEquals(499, idx);
        assertEquals(499, lidx);
        assertTrue(list.contains(igr));
        assertTrue(!list.contains(new Integer(12345)));

        List contents = new ArrayList();
        for (int ii = 0; ii < 1000; ii += 10) {
            contents.add(new Integer(ii));
        }
        assertTrue(list.containsAll(contents));
    }

    public void testPriorityListClone() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 100; ii++) {
            list.add(new Integer(ii), ii);
        }
        PriorityList clone = (PriorityList) list.clone();
        assertTrue(!clone.isEmpty());
        assertTrue(!clone.equals(list));
        assertEquals(100, clone.size());
        clone.clear();
        assertTrue(clone.isEmpty());
        assertEquals(0, clone.size());
    }

    public void testPriorityListAddRemove() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 1000; ii++) {
            list.add(new Integer(ii), ii);
        }

        List contents = new ArrayList();
        for (int ii = 0; ii < 1000; ii += 10) {
            contents.add(new Integer(ii));
        }

        list.addAll(contents, 12345);
        assertEquals(1100, list.size());
        list.remove(12345);
        assertEquals(1000, list.size());

        Integer igr = (Integer) list.get(500);
        list.remove(igr);
        assertEquals(999, list.size());
        list.removeAll(contents);
        assertEquals(899, list.size());
    }

    public void testPriorityListSub() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 100; ii++) {
            list.add(new Integer(ii), ii);
        }

        List sub = list.subList(30, 80);
        assertTrue(!sub.isEmpty());
        assertEquals(50, sub.size());

        for (int ii = sub.size() - 1, jj = 20; ii >= 0; ii--, jj++) {
            Integer igr = (Integer) sub.get(ii);
            if (igr == null || igr.intValue() != jj) {
                fail("subList element incorrect: expected " +
                     jj + " at " + ii + ", got " + igr);
            }
        }
    }

    public void testPriorityListToArray() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 1000; ii++) {
            list.add(new Integer(ii), ii);
        }

        Object[] arr = list.toArray();
        for (int ii = 0; ii < arr.length; ii++) {
            if (((Integer) arr[ii]).intValue() != (arr.length - ii - 1)) {
                fail("array element incorrect " + arr[ii] + " at " + ii);
            }
        }

        Integer[] irr = (Integer[]) list.toArray(new Integer[100]);
        for (int ii = 0; ii < irr.length; ii++) {
            if (irr[ii].intValue() != (arr.length - ii - 1)) {
                fail("irray element incorrect " + irr[ii] + " at " + ii);
            }
        }
    }

    public void testPriorityListIterator() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 100; ii++) {
            list.add(new Integer(ii), ii);
        }

        Iterator iter = list.iterator();
        int jj = 99;
        while (iter.hasNext()) {
            Integer igr = (Integer) iter.next();
            if (igr.intValue() != jj) {
                fail("iterator element incorrect at " + jj + ", got" +
                     igr.intValue());
            }
            jj--;
        }
    }

    public void testPriorityListListIterator() {
        PriorityList list = new PriorityList();
        for (int ii = 0; ii < 100; ii++) {
            list.add(new Integer(ii), ii);
        }

        ListIterator liter = list.listIterator();
        int jj = 0;
        while (liter.hasNext()) {
            if (liter.nextIndex() != jj) {
                fail("listIterator nextIndex incorrect at " +
                     jj + ", got " + liter.nextIndex());
            }
            Integer igr = (Integer) liter.next();
            if (igr.intValue() != (99 - jj)) {
                fail("listIterator next element incorrect at " + jj);
            }
            jj++;
        }

        while (liter.hasPrevious()) {
            jj--;
            if (liter.previousIndex() != jj) {
                fail("listIterator previousIndex incorrect at " + jj);
            }
            Integer igr = (Integer) liter.previous();
            if (igr.intValue() != (99 - jj)) {
                fail("listIterator previous element incorrect at " + jj);
            }
        }

        liter = list.listIterator(10);
        assertEquals(10, liter.nextIndex());
        liter.next();
        liter.remove();
        assertEquals(99, list.size());

        Integer igr = (Integer) list.set(30, new Integer(321));
        Integer nigr = (Integer) list.get(30);
        if (igr == null || igr.intValue() != 68 ||
            nigr == null || nigr.intValue() != 321) {
            fail("set() failed: igr = " + igr + ", nigr = " + nigr);
        }
    }
}
