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
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.nodes.stack;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.IncompatibleThreadStateException;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of setting the current frame.
 *
 * @author  Nathan Fiedler
 */
public class SetCurrentAction extends NodeAction {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            Session session = SessionProvider.getCurrentSession();
            if (session.isSuspended()) {
                DebuggingContext dc = ContextProvider.getContext(session);
                return dc.getThread() != null;
            }
        }
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SetCurrentAction.class,
                "LBL_StackView_SetCurrentFrameAction");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            GetFrameCookie gfc = activatedNodes[0].getLookup().lookup(
                    GetFrameCookie.class);
            if (gfc != null) {
                int frame = gfc.getFrameIndex();
                Session session = SessionProvider.getCurrentSession();
                DebuggingContext dc = ContextProvider.getContext(session);
                try {
                    dc.setFrame(frame);
                } catch (IncompatibleThreadStateException itse) {
                    // eek, view should have been cleared already
                    ErrorManager.getDefault().notify(itse);
                }
            }
        }
    }
}
