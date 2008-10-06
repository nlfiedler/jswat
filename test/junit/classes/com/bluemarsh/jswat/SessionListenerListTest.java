/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      Unit Tests
 * FILE:        SessionListenerListTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/25/02        Initial version
 *      nf      11/09/03        Increased thread test list size
 *      nf      11/11/03        Removed threaded test
 *
 * $Id: SessionListenerListTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import java.util.ArrayList;
import java.util.List;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the SessionListenerList class.
 */
public class SessionListenerListTest extends TestCase {
    private static final int THREADED_TEST_SIZE = 1000;

    //
    // Multi-threaded tests, in which the thread-safe aspects of the
    // SessionListenerList are exercised, are literally impossible to
    // implement. The JVM does not guarantee reasonable thread
    // scheduling and thus it is impossible to write a reliable test.
    // So, for now, we have only the basic list test.
    //

    public SessionListenerListTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(SessionListenerListTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testSLLBasic() {
        SessionListenerList sll = new SessionListenerList();
        assertNull(sll.acquireListener(-1));
        assertNull(sll.acquireListener(0));
        assertNull(sll.acquireListener(1));
        sll.activateAll(null);
        sll.closeAll(null);
        sll.deactivateAll(null);
        sll.resumeAll(null);
        sll.suspendAll(null);
        assertEquals(0, sll.size());

        try {
            sll.add(null, null, false);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // this is expected
        }

        try {
            sll.remove(null, null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            // this is expected
        }

        TestListener l1 = new TestListener();
        sll.add(l1, null, false);
        assertTrue(l1.open);
        assertTrue(!l1.active);
        assertTrue(!l1.running);

        sll.activateAll(null);
        assertTrue(l1.open);
        assertTrue(l1.active);
        assertTrue(!l1.running);

        sll.resumeAll(null);
        assertTrue(l1.open);
        assertTrue(l1.active);
        assertTrue(l1.running);

        sll.suspendAll(null);
        assertTrue(l1.open);
        assertTrue(l1.active);
        assertTrue(!l1.running);

        sll.deactivateAll(null);
        assertTrue(l1.open);
        assertTrue(!l1.active);
        assertTrue(!l1.running);

        sll.closeAll(null);
        assertTrue(!l1.open);
        assertTrue(!l1.active);
        assertTrue(!l1.running);
    }

    protected class TestListener implements SessionListener {
        public boolean open;
        public boolean active;
        public boolean running;

        public void activated(SessionEvent sevt) {
            active = true;
        }

        public void closing(SessionEvent sevt) {
            open = false;
        }

        public void deactivated(SessionEvent sevt) {
            active = false;
        }

        public void opened(Session session) {
            open = true;
        }

        public void resuming(SessionEvent sevt) {
            running = true;
        }

        public void suspended(SessionEvent sevt) {
            running = false;
        }
    }
}
