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
 * are Copyright (C) 2006-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchManagerTest.java 6 2007-05-16 07:14:24Z nfiedler $
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
