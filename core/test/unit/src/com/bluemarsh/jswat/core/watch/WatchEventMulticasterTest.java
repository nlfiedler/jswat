/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchEventMulticasterTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the WatchEventMulticaster class.
 */
public class WatchEventMulticasterTest extends TestCase {

    public WatchEventMulticasterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(WatchEventMulticasterTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_WatchEventMulticaster() {
        WatchListener sl = WatchEventMulticaster.add(null, null);
        assertEquals(sl, null);

        sl = WatchEventMulticaster.remove(null, null);
        assertEquals(sl, null);

        TestListener l1 = new TestListener();
        sl = WatchEventMulticaster.add(sl, l1);
        TestListener l2 = new TestListener();
        sl = WatchEventMulticaster.add(sl, l2);

        assertEquals(0, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(0, l2.added);
        assertEquals(0, l2.removed);

        Watch watch = new DummyWatch();
        WatchEvent sevt = new WatchEvent(watch, WatchEvent.Type.ADDED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(1, l2.added);
        assertEquals(0, l2.removed);

        sevt = new WatchEvent(watch, WatchEvent.Type.REMOVED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(1, l2.added);
        assertEquals(1, l2.removed);

        sl = WatchEventMulticaster.remove(sl, l1);
        sevt = new WatchEvent(watch, WatchEvent.Type.ADDED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(2, l2.added);
        assertEquals(1, l2.removed);
    }

    protected class TestListener implements WatchListener {
        public int added;
        public int removed;

        public void watchAdded(WatchEvent event) {
            added++;
        }

        public void watchRemoved(WatchEvent event) {
            removed++;
        }
    }
}
