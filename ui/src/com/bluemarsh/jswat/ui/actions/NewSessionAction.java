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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NewSessionAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionFactory;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.components.SessionPropertiesPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Creates a new session and opens the settings dialog for that session.
 *
 * @author Nathan Fiedler
 */
public class NewSessionAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if this action can be invoked on any thread.
     *
     * @return  true if asynchronous, false otherwise.
     */
    protected boolean asynchronous() {
        // performAction() should run in event thread
        return false;
    }

    /**
     * Get a help context for the action.
     *
     * @return  help context.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_NewSessionAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        // First create a new session.
        SessionManager sm = SessionProvider.getSessionManager();
        String id = sm.generateIdentifier();
        SessionFactory sf = SessionProvider.getSessionFactory();
        Session session = sf.createSession(id);
        sm.add(session);
        // Now show the settings dialog to let the user customize it.
        SessionPropertiesPanel spp = new SessionPropertiesPanel();
        spp.loadParameters(session);
        // Display dialog and get the user response.
        if (spp.display()) {
            // Save the changes to the session.
            spp.saveParameters(session);
            sm.setCurrent(session);
        }
    }
}
