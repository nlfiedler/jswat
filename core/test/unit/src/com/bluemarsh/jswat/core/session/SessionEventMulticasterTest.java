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
 * are Copyright (C) 2002-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.core.session;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the SessionEventMulticaster class.
 *
 * @author Nathan Fiedler
 */
public class SessionEventMulticasterTest {

    @Test
    public void test_SessionEventMulticaster() {
        SessionEventMulticaster sem = new SessionEventMulticaster();
        Assert.assertNotNull(sem);

        // nothing should happen
        sem.add(null);
        sem.remove(null);

        TestListener l1 = new TestListener(false);
        sem.add(l1);
        TestListener l2 = new TestListener(true);
        sem.add(l2);

        Assert.assertEquals(0, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(0, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        Session session = new DummySession();
        session.setIdentifier("eventUnitTest1");
        SessionEvent sevt = new SessionEvent(session, SessionEventType.OPENED);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(0, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.CONNECTED);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        Assert.assertEquals(0, l2.listener.activated);
        Assert.assertEquals(0, l2.listener.closing);
        Assert.assertEquals(0, l2.listener.deactivated);
        Assert.assertEquals(1, l2.listener.opened);
        Assert.assertEquals(0, l2.listener.resuming);
        Assert.assertEquals(0, l2.listener.suspended);

        sevt = new SessionEvent(session, SessionEventType.RESUMING);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(1, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.SUSPENDED);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(1, l1.resuming);
        Assert.assertEquals(1, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.DISCONNECTED);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(1, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(1, l1.resuming);
        Assert.assertEquals(1, l1.suspended);

        Assert.assertEquals(0, l2.listener.activated);
        Assert.assertEquals(1, l2.listener.closing);
        Assert.assertEquals(0, l2.listener.deactivated);
        Assert.assertEquals(1, l2.listener.opened);
        Assert.assertEquals(0, l2.listener.resuming);
        Assert.assertEquals(0, l2.listener.suspended);

        sevt = new SessionEvent(session, SessionEventType.CLOSING);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(1, l1.closing);
        Assert.assertEquals(1, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(1, l1.resuming);
        Assert.assertEquals(1, l1.suspended);

        l1 = new TestListener(false);
        sem.add(l1);
        l2 = new TestListener(true);
        sem.add(l2);

        Assert.assertEquals(0, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(0, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.OPENED);
        sevt.getType().fireEvent(sevt, sem);
        Assert.assertEquals(0, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.CONNECTED);
        sevt.getType().fireEvent(sevt, sem);
        sem.remove(l2);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(0, l1.closing);
        Assert.assertEquals(0, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);

        sevt = new SessionEvent(session, SessionEventType.DISCONNECTED);
        sevt.getType().fireEvent(sevt, sem);
        sevt = new SessionEvent(session, SessionEventType.CLOSING);
        sevt.getType().fireEvent(sevt, sem);
        sem.remove(l1);
        Assert.assertEquals(1, l1.activated);
        Assert.assertEquals(1, l1.closing);
        Assert.assertEquals(1, l1.deactivated);
        Assert.assertEquals(1, l1.opened);
        Assert.assertEquals(0, l1.resuming);
        Assert.assertEquals(0, l1.suspended);
    }

    private static class TestListener implements SessionListener {

        public TestListener listener;
        public int activated;
        public int closing;
        public int deactivated;
        public int opened;
        public int resuming;
        public int suspended;

        TestListener(boolean mutate) {
            if (mutate) {
                listener = new TestListener(false);
            }
        }

        @Override
        public void connected(SessionEvent sevt) {
            if (listener != null) {
                sevt.getSession().addSessionListener(listener);
            }
            activated++;
        }

        @Override
        public void closing(SessionEvent sevt) {
            closing++;
        }

        @Override
        public void disconnected(SessionEvent sevt) {
            if (listener != null) {
                sevt.getSession().removeSessionListener(listener);
            }
            deactivated++;
        }

        @Override
        public void opened(Session session) {
            opened++;
        }

        @Override
        public void resuming(SessionEvent sevt) {
            resuming++;
        }

        @Override
        public void suspended(SessionEvent sevt) {
            suspended++;
        }
    }
}
