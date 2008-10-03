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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PopFramesAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.stack;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.IncompatibleThreadStateException;
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

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            if (session.isSuspended()) {
                DebuggingContext dc = ContextProvider.getContext(session);
                if (dc.getThread() != null) {
                    VirtualMachine vm = session.getConnection().getVM();
                    return vm.canPopFrames() && vm.canBeModified();
                }
            }
        }
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(PopFramesAction.class,
                "LBL_StackView_PopFramesAction");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            Node n = activatedNodes[0];
            if (n instanceof StackFrameNode) {
                StackFrameNode fn = (StackFrameNode) n;
                int index = fn.getFrameIndex();
                Session session = SessionProvider.getCurrentSession();
                DebuggingContext dc = ContextProvider.getContext(session);
                ThreadReference thread = dc.getThread();
                try {
                    StackFrame frame = thread.frame(index);
                    thread.popFrames(frame);
                    // Cause the context to be reset.
                    dc.setThread(thread, false);
                }  catch (NativeMethodException nme) {
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                            NbBundle.getMessage(PopFramesAction.class,
                            "ERR_StackView_PoppingNativeMethod"),
                            NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }  catch (IncompatibleThreadStateException itse) {
                    // view should have been cleared already
                    ErrorManager.getDefault().notify(itse);
                }
            }
        }
    }
}
