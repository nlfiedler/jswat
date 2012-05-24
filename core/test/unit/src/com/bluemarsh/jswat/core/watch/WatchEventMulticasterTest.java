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
 * are Copyright (C) 2006-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.watch;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the WatchEventMulticaster class.
 */
public class WatchEventMulticasterTest {

    @Test
    public void test_WatchEventMulticaster() {
        WatchEventMulticaster wem = new WatchEventMulticaster();
        Assert.assertNotNull(wem);

        // nothing should happen
        wem.add(null);
        wem.remove(null);

        TestListener l1 = new TestListener();
        wem.add(l1);
        TestListener l2 = new TestListener();
        wem.add(l2);

        Assert.assertEquals(0, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(0, l2.added);
        Assert.assertEquals(0, l2.removed);

        Watch watch = new DummyWatch();
        WatchEvent sevt = new WatchEvent(watch, WatchEventType.ADDED);
        sevt.getType().fireEvent(sevt, wem);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(0, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(0, l2.removed);

        sevt = new WatchEvent(watch, WatchEventType.REMOVED);
        sevt.getType().fireEvent(sevt, wem);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(1, l2.added);
        Assert.assertEquals(1, l2.removed);

        wem.remove(l1);
        sevt = new WatchEvent(watch, WatchEventType.ADDED);
        sevt.getType().fireEvent(sevt, wem);
        Assert.assertEquals(1, l1.added);
        Assert.assertEquals(1, l1.removed);
        Assert.assertEquals(2, l2.added);
        Assert.assertEquals(1, l2.removed);
    }

    private static class TestListener implements WatchListener {

        public int added;
        public int removed;

        @Override
        public void watchAdded(WatchEvent event) {
            added++;
        }

        @Override
        public void watchRemoved(WatchEvent event) {
            removed++;
        }
    }
}
