/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchEventMulticasterTest.java 6 2007-05-16 07:14:24Z nfiedler $
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
