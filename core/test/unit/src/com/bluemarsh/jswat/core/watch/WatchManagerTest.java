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
 * $Id: WatchManagerTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.bluemarsh.jswat.core.session.DummySession;
import com.bluemarsh.jswat.core.session.Session;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the WatchManager class.
 */
public class WatchManagerTest extends TestCase {

    public WatchManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(WatchManagerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_WatchManager_addRemoveListener() {
        Session session = new DummySession();
        session.setIdentifier("unitTest1");
        session.init();
        WatchManager sm = WatchProvider.getWatchManager(session);
        TestListener tl = new TestListener();
        sm.addWatchListener(tl);
        Watch watch1 = new DummyWatch();
        sm.addWatch(watch1);
        Watch watch2 = new DummyWatch();
        sm.addWatch(watch2);
        sm.removeWatch(watch1);
        assertTrue("missed add event", tl.added == 2);
        assertTrue("missed remove event", tl.removed == 1);
        sm.removeWatchListener(tl);
    }

    public void test_WatchManager_iterate() {
        Session session = new DummySession();
        session.setIdentifier("unitTest1");
        session.init();
        WatchManager sm = WatchProvider.getWatchManager(session);
        Watch watch1 = new DummyWatch();
        sm.addWatch(watch1);
        Watch watch2 = new DummyWatch();
        sm.addWatch(watch2);
        Iterator<Watch> iter = sm.watchIterator();
        boolean found1 = false;
        boolean found2 = false;
        while (iter.hasNext()) {
            Watch s = iter.next();
            if (s.equals(watch1)) {
                found1 = true;
            } else if (s.equals(watch2)) {
                found2 = true;
            }
        }
        assertTrue("watch 1 missing", found1);
        assertTrue("watch 2 missing", found2);
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
