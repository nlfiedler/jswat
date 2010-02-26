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
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the SessionManager class.
 */
public class SessionManagerTest {

    @AfterClass
    public static void tearDownClass() {
        // Remove all of the sessions and create a default for testing
        // by the other unit tests.
        List<Session> sessions = new ArrayList<Session>();
        SessionManager sm = SessionProvider.getSessionManager();
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            sessions.add(iter.next());
        }
        SessionFactory factory = SessionProvider.getSessionFactory();
        Session session = factory.createSession("unit");
        sm.add(session);
        sm.setCurrent(session);
        for (Session s : sessions) {
            sm.remove(s);
        }
        sm.saveSessions(false);
    }

    @Test
    public void addRemoveListener() {
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(null);
        TestListener tl = new TestListener();
        sm.addSessionManagerListener(tl);
        Session session1 = new DummySession();
        session1.setIdentifier("unitTest1");
        sm.add(session1);
        Session found = sm.findById("unitTest1");
        assertEquals(session1, found);
        Session session2 = new DummySession();
        session2.setIdentifier("unitTest2");
        sm.add(session2);
        sm.setCurrent(session2);
        assertTrue(SessionProvider.isCurrentSession(session2));
        assertFalse(SessionProvider.isCurrentSession(session1));
        try {
            sm.remove(session2);
            fail("should have failed");
        } catch (IllegalArgumentException iae) {
            // expected
        }
        sm.remove(session1);
        assertTrue("missed add event", tl.wasAdded);
        assertTrue("missed remove event", tl.wasRemoved);
        assertTrue("missed set-current event", tl.wasSetCurrent);
        sm.removeSessionManagerListener(tl);
        sm.removeSessionManagerListener(null);
        // Do not call SessionManager.close() as other tests will need it.
    }

    @Test
    public void iterate() {
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

    @Test
    public void properties() {
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

    @Test
    public void loadAndSave() {
        SessionManager sm = SessionProvider.getSessionManager();

        // Collect all of the sessions from previous tests.
        List<Session> sessions = new ArrayList<Session>();
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            sessions.add(iter.next());
        }

        Session session1 = new DummySession();
        session1.setIdentifier("unitTest1");
        sm.add(session1);
        sm.setCurrent(session1);

        // Clean out the left-over sessions.
        for (Session s : sessions) {
            sm.remove(s);
        }

        Session session2 = sm.copy(session1, "unitTest2");
        assertNotSame(session1, session2);
        session2.setIdentifier("unitTest2");
        sm.saveSessions(false);
        sm.saveSessions(true);
        Session session3 = new DummySession();
        session3.setIdentifier("foobar");
        sm.add(session3);
        sm.setCurrent(session3);
        sm.remove(session2);
        sm.remove(session1);
        assertNull(sm.findById("unitTest1"));
        assertNull(sm.findById("unitTest2"));
        iter = sm.iterateSessions();
        assertTrue(iter.hasNext());
        Session s = iter.next();
        assertEquals(session3, s);
        assertFalse(iter.hasNext());

        sm.loadSessions();
        assertNotNull(sm.findById("unitTest1"));
        assertNotNull(sm.findById("unitTest2"));
        iter = sm.iterateSessions();
        assertTrue(iter.hasNext());
        iter.next();
        iter.next();
        assertFalse(iter.hasNext());
    }

    private static class TestListener implements SessionManagerListener {
        public boolean wasAdded;
        public boolean wasRemoved;
        public boolean wasSetCurrent;
        
        @Override
        public void sessionAdded(SessionManagerEvent e) {
            wasAdded = true;
        }
        
        @Override
        public void sessionRemoved(SessionManagerEvent e) {
            wasRemoved = true;
        }
        
        @Override
        public void sessionSetCurrent(SessionManagerEvent e) {
            wasSetCurrent = true;
        }
    }
}
