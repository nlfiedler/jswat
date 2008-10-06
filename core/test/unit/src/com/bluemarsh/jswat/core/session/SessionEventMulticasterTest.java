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
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionEventMulticasterTest.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the SessionEventMulticaster class.
 */
public class SessionEventMulticasterTest extends TestCase {

    public SessionEventMulticasterTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionEventMulticasterTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_SessionEventMulticaster() {
        SessionListener sl = SessionEventMulticaster.add(null, null);
        assertEquals(sl, null);

        sl = SessionEventMulticaster.remove(null, null);
        assertEquals(sl, null);

        TestListener l1 = new TestListener(false);
        sl = SessionEventMulticaster.add(sl, l1);
        TestListener l2 = new TestListener(true);
        sl = SessionEventMulticaster.add(sl, l2);

        assertEquals(0, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(0, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        Session session = new DummySession();
        session.setIdentifier("eventUnitTest1");
        SessionEvent sevt = new SessionEvent(session, SessionEvent.Type.OPENED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(0, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.CONNECTED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        assertEquals(0, l2.listener.activated);
        assertEquals(0, l2.listener.closing);
        assertEquals(0, l2.listener.deactivated);
        assertEquals(1, l2.listener.opened);
        assertEquals(0, l2.listener.resuming);
        assertEquals(0, l2.listener.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.RESUMING);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(1, l1.resuming);
        assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.SUSPENDED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(1, l1.resuming);
        assertEquals(1, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.DISCONNECTED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(1, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(1, l1.resuming);
        assertEquals(1, l1.suspended);

        assertEquals(0, l2.listener.activated);
        assertEquals(1, l2.listener.closing);
        assertEquals(0, l2.listener.deactivated);
        assertEquals(1, l2.listener.opened);
        assertEquals(0, l2.listener.resuming);
        assertEquals(0, l2.listener.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.CLOSING);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(1, l1.activated);
        assertEquals(1, l1.closing);
        assertEquals(1, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(1, l1.resuming);
        assertEquals(1, l1.suspended);

        l1 = new TestListener(false);
        sl = SessionEventMulticaster.add(sl, l1);
        l2 = new TestListener(true);
        sl = SessionEventMulticaster.add(sl, l2);

        assertEquals(0, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(0, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.OPENED);
        sevt.getType().fireEvent(sevt, sl);
        assertEquals(0, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.CONNECTED);
        sevt.getType().fireEvent(sevt, sl);
        sl = SessionEventMulticaster.remove(sl, l2);
        assertEquals(1, l1.activated);
        assertEquals(0, l1.closing);
        assertEquals(0, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEvent.Type.DISCONNECTED);
        sevt.getType().fireEvent(sevt, sl);
        sevt = new SessionEvent(session, SessionEvent.Type.CLOSING);
        sevt.getType().fireEvent(sevt, sl);
        sl = SessionEventMulticaster.remove(sl, l1);
        assertEquals(1, l1.activated);
        assertEquals(1, l1.closing);
        assertEquals(1, l1.deactivated);
        assertEquals(1, l1.opened);
        assertEquals(0, l1.resuming);
        assertEquals(0, l1.suspended);
    }

    protected class TestListener implements SessionListener {
        public TestListener listener;
        public int activated;
        public int closing;
        public int deactivated;
        public int opened;
        public int resuming;
        public int suspended;

        public TestListener(boolean mutate) {
            if (mutate) {
                listener = new TestListener(false);
            }
        }

        public void connected(SessionEvent sevt) {
            if (listener != null) {
                sevt.getSession().addSessionListener(listener);
            }
            activated++;
        }

        public void closing(SessionEvent sevt) {
            closing++;
        }

        public void disconnected(SessionEvent sevt) {
            if (listener != null) {
                sevt.getSession().removeSessionListener(listener);
            }
            deactivated++;
        }

        public void opened(Session session) {
            opened++;
        }

        public void resuming(SessionEvent sevt) {
            resuming++;
        }

        public void suspended(SessionEvent sevt) {
            suspended++;
        }
    }
}
