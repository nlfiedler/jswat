/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: SessionSetup.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import junit.extensions.*;
import junit.framework.*;

/**
 * Starts and stops the Session instance once for all of the tests.
 *
 * @author  Nathan Fiedler
 */
public class SessionSetup extends TestSetup {
    /** True to have the Session activated in setUp(). */
    protected boolean activate;

    /**
     * Constructs a SessionSetup instance for the given test.
     *
     * @param  test  test to run.
     */
    public SessionSetup(Test test) {
        this(test, false);
    } // SessionSetup

    /**
     * Constructs a SessionSetup instance for the given test.
     *
     * @param  test  test to run.
     * @param  active  true to active Session on setUp().
     */
    public SessionSetup(Test test, boolean active) {
        super(test);
        activate = active;
    } // SessionSetup

    /**
     * Set up the test. This starts a single Session instance.
     */
    protected void setUp() {
        try {
            super.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SessionManager.beginSession();
        if (activate) {
            // REQUIRES: locals class to be in the classpath
            SessionManager.launchSimple("locals");
        }
    } // setup

    /**
     * Tear down the test. This stops the single Session instance.
     */
    protected void tearDown() {
        if (activate && SessionManager.isActive()) {
            SessionManager.deactivate(true);
        }
        SessionManager.endSession();
        try {
            super.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // tearDown
} // SessionSetup
