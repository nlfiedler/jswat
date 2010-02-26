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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the SessionManagerEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class SessionManagerEventMulticasterTest {

    @Test
    public void testMulticaster() {
        SessionManagerListener sl = SessionManagerEventMulticaster.add(null, null);
        assertNull(sl);

        sl = SessionManagerEventMulticaster.remove(null, null);
        assertNull(sl);

        TestListener l1 = new TestListener();
        sl = SessionManagerEventMulticaster.add(sl, l1);
        assertNotNull(sl);
        sl = SessionManagerEventMulticaster.add(sl, null);
        assertNotNull(sl);
        sl = SessionManagerEventMulticaster.add(null, sl);
        assertNotNull(sl);

        assertEquals(0, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(0, l1.setCurrent);

        TestListener l2 = new TestListener();
        sl = SessionManagerEventMulticaster.add(sl, l2);
        sl.sessionAdded(null);
        assertEquals(1, l1.added);
        assertEquals(0, l1.removed);
        assertEquals(0, l1.setCurrent);
        assertEquals(1, l2.added);
        assertEquals(0, l2.removed);
        assertEquals(0, l2.setCurrent);

        sl = SessionManagerEventMulticaster.remove(sl, l2);
        sl.sessionRemoved(null);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(0, l1.setCurrent);
        assertEquals(1, l2.added);
        assertEquals(0, l2.removed);
        assertEquals(0, l2.setCurrent);

        sl = SessionManagerEventMulticaster.add(sl, l2);
        sl.sessionSetCurrent(null);
        assertEquals(1, l1.added);
        assertEquals(1, l1.removed);
        assertEquals(1, l1.setCurrent);
        assertEquals(1, l2.added);
        assertEquals(0, l2.removed);
        assertEquals(1, l2.setCurrent);

        sl.sessionRemoved(null);
        assertEquals(1, l1.added);
        assertEquals(2, l1.removed);
        assertEquals(1, l1.setCurrent);
        assertEquals(1, l2.added);
        assertEquals(1, l2.removed);
        assertEquals(1, l2.setCurrent);

        sl = SessionManagerEventMulticaster.remove(sl, null);
        assertNotNull(sl);
        assertNull(SessionManagerEventMulticaster.remove(null, sl));
    }

    @Test
    public void testManyListeners() {
        List<TestListener> list = new ArrayList<TestListener>();
        SessionManagerListener sl = null;
        for (int ii = 0; ii < 100; ii++) {
            TestListener l = new TestListener();
            sl = SessionManagerEventMulticaster.add(sl, l);
            list.add(l);
        }

        sl.sessionAdded(null);
        assertEquals(100, list.size());
        for (TestListener l : list) {
            assertEquals(1, l.added);
            assertEquals(0, l.removed);
            assertEquals(0, l.setCurrent);
        }

        Collections.shuffle(list);
        for (TestListener l : list) {
            sl = SessionManagerEventMulticaster.remove(sl, l);
        }
        assertNull(sl);
    }

    private static class TestListener implements SessionManagerListener {
        public int added;
        public int removed;
        public int setCurrent;

        @Override
        public void sessionAdded(SessionManagerEvent e) {
            added++;
        }

        @Override
        public void sessionRemoved(SessionManagerEvent e) {
            removed++;
        }

        @Override
        public void sessionSetCurrent(SessionManagerEvent e) {
            setCurrent++;
        }
    }
}
