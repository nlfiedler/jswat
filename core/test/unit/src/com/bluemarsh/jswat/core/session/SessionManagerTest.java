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
 * are Copyright (C) 2004-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.session;

import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the SessionManager class.
 */
public class SessionManagerTest extends TestCase {

    public SessionManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionManagerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_SessionManager_addRemoveListener() {
        SessionManager sm = SessionProvider.getSessionManager();
        TestListener tl = new TestListener();
        sm.addSessionManagerListener(tl);
        Session session1 = new DummySession();
        session1.setIdentifier("unitTest1");
        sm.add(session1);
        Session session2 = new DummySession();
        session2.setIdentifier("unitTest2");
        sm.add(session2);
        sm.setCurrent(session2);
        sm.remove(session1);
        assertTrue("missed add event", tl.wasAdded);
        assertTrue("missed remove event", tl.wasRemoved);
        assertTrue("missed set-current event", tl.wasSetCurrent);
        sm.removeSessionManagerListener(tl);
        // Do not call SessionManager.close() as other tests will need it.
    }

    public void test_SessionManager_iterate() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session1 = new DummySession();
        session1.setIdentifier("unitTest1");
        sm.add(session1);
        Session session2 = new DummySession();
        session2.setIdentifier("unitTest2");
        sm.add(session2);
        Iterator<Session> iter = sm.iterateSessions();
        boolean found1 = false;
        boolean found2 = false;
        // There may be other sessions opened, so just check the ones we know.
        while (iter.hasNext()) {
            Session s = iter.next();
            if (s.equals(session1)) {
                found1 = true;
            } else if (s.equals(session2)) {
                found2 = true;
            }
        }
        assertTrue("session 1 missing", found1);
        assertTrue("session 2 missing", found2);
        // Do not call SessionManager.close() as other tests will need it.
    }

    public void test_SessionManager_properties() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session1 = new DummySession();
        session1.setIdentifier("unitTest1");
        sm.add(session1);
        // Need to set the current since this is a tainted manager instance.
        sm.setCurrent(session1);
        assertEquals(session1, sm.getCurrent());
        session1.setProperty("test1", "value1");
        Session session2 = sm.copy(session1, "unitTest2");
        assertNotSame(session1, session2);
        sm.setCurrent(session2);
        assertEquals(session2, sm.getCurrent());
        assertEquals("value1", session2.getProperty("test1"));
        // Do not call SessionManager.close() as other tests will need it.
    }

    public class TestListener implements SessionManagerListener {
        public boolean wasAdded;
        public boolean wasRemoved;
        public boolean wasSetCurrent;
        
        public void sessionAdded(SessionManagerEvent e) {
            wasAdded = true;
        }
        
        public void sessionRemoved(SessionManagerEvent e) {
            wasRemoved = true;
        }
        
        public void sessionSetCurrent(SessionManagerEvent e) {
            wasSetCurrent = true;
        }
    }
}
