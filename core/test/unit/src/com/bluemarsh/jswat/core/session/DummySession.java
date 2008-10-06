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
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DummySession.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.session;

import com.sun.jdi.event.Event;
import com.bluemarsh.jswat.core.connect.JvmConnection;

/**
 * A Session implementation that is only good for testing.
 *
 * @author  Nathan Fiedler
 */
public class DummySession extends AbstractSession {

    public DummySession() {
        super();
    }

    public void connect(JvmConnection connection) {
    }

    public void close() {
    }

    public void disconnect(boolean forceExit) {
    }

    public void disconnected() {
    }

    public JvmConnection getConnection() {
        return null;
    }

    public void init() {
    }

    public boolean isConnected() {
        return false;
    }

    public boolean isSuspended() {
        return false;
    }

    public void resumeVM() {
    }

    public void suspended(Event e) {
    }

    public void suspendVM() {
    }
}
