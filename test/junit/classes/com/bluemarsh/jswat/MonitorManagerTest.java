/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * FILE:        MonitorManagerTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/26/02        Initial version
 *
 * $Id: MonitorManagerTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.breakpoint.AbstractMonitor;
import com.bluemarsh.jswat.breakpoint.Monitor;
import com.bluemarsh.jswat.breakpoint.ui.MonitorUI;
import com.bluemarsh.jswat.event.SessionEvent;
import java.util.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the MonitorManager class.
 */
public class MonitorManagerTest extends TestCase {

    public MonitorManagerTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(MonitorManagerTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testMMBasic() {
        // testing without a Session instance

        List list = new ArrayList();
        MonitorManager mm = new MonitorManager();
        // don't bother calling its init() method
        for (int ii = 0; ii < 100; ii++) {
            Monitor mon = new TestMonitor("stdout " + ii);
            list.add(mon);
            mm.addMonitor(mon);
        }
        ListIterator liter = mm.monitors();
        int ii = 0;
        while (liter.hasNext()) {
            Monitor mon = (Monitor) liter.next();
            assertEquals(list.get(ii), mon);
            ii++;
        }

        // monitor numbers are assigned in add(), starting with 1
        for (ii = 0; ii < 100; ii++) {
            int n = (int) Math.random() * 100 + 1;
            Monitor mon = (Monitor) mm.getMonitor(n);
            assertNotNull(mon);
            assertEquals(n, mon.getNumber());
            String s = "stdout " + (n - 1);
            assertEquals(s, mon.description());
        }

        while (list.size() > 0) {
            Monitor mon = (Monitor) list.remove(
                (int) (Math.random() * list.size()));
            mm.removeMonitor(mon);
        }
        liter = mm.monitors();
        assertTrue(!liter.hasNext());

        // don't bother calling the close() method
    }

    public void testMMPerforming() {
        TestMonitor mon = new TestMonitor("stdout blah");
        MonitorManager mm = new MonitorManager();
        mm.addMonitor(mon);

        // call suspended() immediately
        Session session = SessionManager.beginSession();
        SessionEvent se = new SessionEvent(session, this, false);
        mm.suspended(se);
        assertTrue(!mon.wasRun);

        // call resuming() followed by suspended() again
        mm.resuming(se);
        mm.suspended(se);
        assertTrue(mon.wasRun);
    }

    protected class TestMonitor extends AbstractMonitor {
        public String desc;
        public boolean wasRun;

        public TestMonitor(String desc) {
            this.desc = desc;
        }

        public String description() {
            return desc;
        }

        public MonitorUI getUIAdapter() {
            return null;
        }

        public void perform(Session session) {
            wasRun = true;
        }
    }
}
