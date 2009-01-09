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
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
