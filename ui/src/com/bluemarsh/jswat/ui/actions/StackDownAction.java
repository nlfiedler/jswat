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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: StackDownAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.ActionEnabler;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Action that increases the current frame index (make caller current).
 *
 * @author Nathan Fiedler
 */
public class StackDownAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of StackDownAction.
     */
    public StackDownAction() {
        ActionEnabler ae = ActionEnabler.getDefault();
        ae.registerSuspendedAction(this);
    }

    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(StackDownAction.class, "LBL_StackDownAction");
    }

    public void performAction() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext context = ContextProvider.getContext(session);
        // Increment immediately for easier bounds checking.
        int index = context.getFrame() + 1;
        ThreadReference thread = context.getThread();
        try {
            if (thread != null && thread.frameCount() > index) {
                context.setFrame(index);
            }
        } catch (IndexOutOfBoundsException ioobe) {
            // We already checked the bounds, this should not happen.
            ErrorManager.getDefault().notify(ioobe);
        } catch (IncompatibleThreadStateException itse) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    StackUpAction.class, "ERR_StackAction_ThreadState"));
        }
    }
}
