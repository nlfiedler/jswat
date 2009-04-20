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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui;

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

    @Override
    public void restored() {
        SessionManager sessionMgr = SessionProvider.getSessionManager();

        // Create a SessionWatcher to monitor the session status.
        SessionWatcher swatcher = new SessionWatcher();
        sessionMgr.addSessionManagerListener(swatcher);

        // Create an OutputAdapter to display debuggee output.
        OutputAdapter adapter = new OutputAdapter();
        sessionMgr.addSessionManagerListener(adapter);

        // Create the BreakpointWatcher to monitor breakpoints.
        BreakpointWatcher bwatcher = new BreakpointWatcher();
        sessionMgr.addSessionManagerListener(bwatcher);

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
