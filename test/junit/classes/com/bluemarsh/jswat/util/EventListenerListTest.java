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
 * PROJECT:     Utilities
 * MODULE:      Unit Tests
 * FILE:        EventListenerListTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/23/02        Initial version
 *
 * $Id: EventListenerListTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import java.util.EventListener;
import junit.extensions.*;
import junit.framework.*;

/**
 * Test the EventListenerList class.
 */
public class EventListenerListTest extends TestCase {

    public EventListenerListTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(EventListenerListTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testELListSize() {
        EventListenerList list = new EventListenerList();
        assertEquals(0, list.getListenerCount());
        assertEquals(0, list.getListenerCount(Listener1.class));
    }

    public void testELListContents() {
        EventListenerList list = new EventListenerList();
        list.add(Listener1.class, new Listener1());
        list.add(Listener1.class, new Listener1());
        assertEquals(2, list.getListenerCount());
        assertEquals(2, list.getListenerCount(Listener1.class));
        assertEquals(0, list.getListenerCount(Listener2.class));

        list.add(Listener2.class, new Listener2());
        Listener2 l1 = new Listener2();
        list.add(Listener2.class, l1);
        list.add(Listener2.class, new Listener2());
        assertEquals(5, list.getListenerCount());

        assertEquals(3, list.getListenerCount(Listener2.class));

        Object[] array = list.getListenerList();
        assertEquals(10, array.length);

        EventListener[] arr = list.getListeners(Listener2.class);
        assertEquals(3, arr.length);

        list.remove(Listener2.class, l1);
        assertEquals(4, list.getListenerCount());
        assertEquals(2, list.getListenerCount(Listener2.class));
    }

    protected static class Listener1 implements EventListener {
    }

    protected static class Listener2 implements EventListener {
    }
}
