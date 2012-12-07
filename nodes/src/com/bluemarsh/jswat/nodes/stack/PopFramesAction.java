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
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of popping frames from the stack.
 *
 * @author  Nathan Fiedler
 */
public class PopFramesAction extends NodeAction {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            try {
                if (session.isSuspended()) {
                    DebuggingContext dc = ContextProvider.getContext(session);
                    if (dc.getThread() != null) {
                        GetFrameCookie gfc = nodes[0].getLookup().lookup(GetFrameCookie.class);
                        if (gfc != null) {
                            // Add one for easy comparison to check if this is
                            // the earliest frame, which can never be popped.
                            int frame = gfc.getFrameIndex() + 1;
                            if (frame < dc.getThread().frameCount()) {
                                VirtualMachine vm = session.getConnection().getVM();
                                return vm.canPopFrames() && vm.canBeModified();
                            }
                        }
                    }
                }
            } catch (IncompatibleThreadStateException itse) {
                ErrorManager.getDefault().notify(itse);
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
        return NbBundle.getMessage(PopFramesAction.class,
                "LBL_StackView_PopFramesAction");
    }

    @Override
    protected void performAction(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            GetFrameCookie gfc = nodes[0].getLookup().lookup(GetFrameCookie.class);
            if (gfc != null) {
                int index = gfc.getFrameIndex();
                Session session = SessionProvider.getCurrentSession();
                DebuggingContext dc = ContextProvider.getContext(session);
                ThreadReference thread = dc.getThread();
                try {
                    StackFrame frame = thread.frame(index);
                    thread.popFrames(frame);
                } catch (NativeMethodException nme) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                            NbBundle.getMessage(PopFramesAction.class,
                            "ERR_StackView_PoppingNativeMethod"),
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } catch (IncompatibleThreadStateException itse) {
                    // view should have been cleared already
                    ErrorManager.getDefault().notify(itse);
                } catch (InvalidStackFrameException isfe) {
                    // This happens if user tries to pop all of the frames.
                    ErrorManager.getDefault().notify(isfe);
                } finally {
                    // Cause the context to be reset no matter what happens.
                    dc.setThread(thread, false);
                }
            }
        }
    }
}
