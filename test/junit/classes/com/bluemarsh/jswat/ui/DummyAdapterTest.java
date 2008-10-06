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
 * $Id: DummyAdapterTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Main;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import java.io.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests some features that rely on optional features of the UI adapter.
 */
public class DummyAdapterTest extends TestCase {

    public DummyAdapterTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(com.bluemarsh.jswat.command.listTest.suite());
        suite.addTest(com.bluemarsh.jswat.command.viewTest.suite());
        return new DummyAdapterSetup(suite);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

class DummyAdapterSetup extends TestSetup {
    protected Class oldAdapter;

    public DummyAdapterSetup(Test test) {
        super(test);
    }

    protected void setUp() {
        try {
            super.setUp();
        } catch (Exception e) { }
        oldAdapter = SessionManager.setUIAdapter(DummyUIAdapter.class);
        SessionManager.beginSession();
    }

    protected void tearDown() {
        SessionManager.endSession();
        SessionManager.setUIAdapter(oldAdapter);
        try {
            super.tearDown();
        } catch (Exception e) { }
    }
}
