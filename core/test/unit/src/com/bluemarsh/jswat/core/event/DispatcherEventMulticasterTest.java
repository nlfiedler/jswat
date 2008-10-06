/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DispatcherEventMulticasterTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.event;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the DispatcherEventMulticaster class.
 */
public class DispatcherEventMulticasterTest extends TestCase {
    protected DispatcherListener sl;

    public DispatcherEventMulticasterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(DispatcherEventMulticasterTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_DispatcherEventMulticaster() {
        sl = DispatcherEventMulticaster.add(null, null);
        assertEquals(sl, null);

        sl = DispatcherEventMulticaster.remove(null, null);
        assertEquals(sl, null);

        TestListener l1 = new TestListener(false);
        sl = DispatcherEventMulticaster.add(sl, l1);
        TestListener l2 = new TestListener(true);
        sl = DispatcherEventMulticaster.add(sl, l2);

        assertEquals(0, l1.occurred);

        DispatcherEvent je = new DispatcherEvent(this, null, null);
        sl.eventOccurred(je);
        assertEquals(1, l1.occurred);
        assertEquals(1, l2.occurred);

        // With only two methods to notify, there's not a lot of testing we
        // can do, especially with mutation of the list.
    }

    protected class TestListener implements DispatcherListener {
        public TestListener listener;
        public int occurred;

        public TestListener(boolean mutate) {
            if (mutate) {
                listener = new TestListener(false);
            }
        }

        public boolean eventOccurred(DispatcherEvent sevt) {
            if (listener != null) {
                // Need to hack the listener list ourselves.
                sl = DispatcherEventMulticaster.add(sl, listener);
            }
            occurred++;
            return true;
        }

        public Iterator<Class> eventTypes() {
            // This will never be called.
            return null;
        }
    }
}
