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
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionManagerEventMulticasterTest.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the SessionManagerEventMulticaster class.
 */
public class SessionManagerEventMulticasterTest extends TestCase {

    public SessionManagerEventMulticasterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionManagerEventMulticasterTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_SessionManagerEventMulticaster() {
        SessionManagerListener sl = SessionManagerEventMulticaster.add(null, null);
        assertEquals(sl, null);

        sl = SessionManagerEventMulticaster.remove(null, null);
        assertEquals(sl, null);

        TestListener l1 = new TestListener();
        sl = SessionManagerEventMulticaster.add(sl, l1);
        assertTrue(sl != null);

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
    }

    protected class TestListener implements SessionManagerListener {
        public int added;
        public int removed;
        public int setCurrent;

        public void sessionAdded(SessionManagerEvent e) {
            added++;
        }

        public void sessionRemoved(SessionManagerEvent e) {
            removed++;
        }

        public void sessionSetCurrent(SessionManagerEvent e) {
            setCurrent++;
        }
    }
}
