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
 * $Id$
 */

package com.bluemarsh.jswat.nodes.stack;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of setting a breakpoint at the location
 * for the selected frame.
 *
 * @author  Nathan Fiedler
 */
public class SetBreakpointAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

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

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(SetBreakpointAction.class,
                "LBL_StackView_SetBreakpointAction");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            Node n = activatedNodes[0];
            if (n instanceof StackFrameNode) {
                StackFrameNode fn = (StackFrameNode) n;
                Session session = SessionProvider.getCurrentSession();
                BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
                BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
                DebuggingContext dc = ContextProvider.getContext(session);
                ThreadReference tr = dc.getThread();
                int index = fn.getFrameIndex();
                try {
                    StackFrame frame = tr.frame(index);
                    Location location = frame.location();
                    Breakpoint bp = bf.createLocationBreakpoint(location);
                    bm.addBreakpoint(bp);
                }  catch (IncompatibleThreadStateException itse) {
                    ErrorManager.getDefault().notify(itse);
                }  catch (IndexOutOfBoundsException ioobe) {
                    ErrorManager.getDefault().notify(ioobe);
                }
            }
        }
    }
}
