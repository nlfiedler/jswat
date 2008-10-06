/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InterfaceInstall.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.util.Iterator;
import org.openide.modules.ModuleInstall;

/**
 * Manages the ui module's lifecycle.
 *
 * @author  Nathan Fiedler
 */
public class InterfaceInstall extends ModuleInstall {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * The IDE is starting up.
     */
    public void restored() {
        // Create a SessionWatcher to monitor the session status.
        SessionWatcher swatcher = new SessionWatcher();

        // Create an OutputAdapter to display debuggee output.
        OutputAdapter adapter = new OutputAdapter();
        SessionManager sessionMgr = SessionProvider.getSessionManager();
        sessionMgr.addSessionManagerListener(adapter);

        // Create the BreakpointWatcher to monitor breakpoints. This
        // class, unlike some of the others, registers itself to the
        // components it is interested in.
        BreakpointWatcher bwatcher = new BreakpointWatcher();

        // Get the ActionEnabler that controls the actions enabled state.
        ActionEnabler ae = ActionEnabler.getDefault();

        // Add the watchers and adapters to the open sessions.
        Iterator iter = sessionMgr.iterateSessions();
        while (iter.hasNext()) {
            Session session = (Session) iter.next();
            session.addSessionListener(ae);
            session.addSessionListener(swatcher);
            session.addSessionListener(adapter);
        }
    }
}
