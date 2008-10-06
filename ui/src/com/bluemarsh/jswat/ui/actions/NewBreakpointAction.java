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
 * $Id: NewBreakpointAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.breakpoint.CreatorPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Displays a dialog to allow the user to create a new breakpoint.
 *
 * @author Nathan Fiedler
 */
public class NewBreakpointAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if this action can be invoked on any thread.
     *
     * @return  true if asynchronous, false otherwise.
     */
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Returns the help context for this action.
     *
     * @return  help context.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-create-breakpoint");
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_NewBreakpointAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        CreatorPanel cp = new CreatorPanel();
        if (cp.display()) {
            // First, create the breakpoint.
            Breakpoint bp = cp.createBreakpoint();
            if (bp == null) {
                String msg = NbBundle.getMessage(getClass(),
                        "LBL_NewBreakpoint_Failed");
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                return;
            }
            // Next, add the breakpoint to the manager.
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            bm.addBreakpoint(bp);
            // Save the additional attributes to the breakpoint.
            cp.saveParameters(bp);
        }
    }
}
