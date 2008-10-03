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
 * $Id: MethodBreakpointAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import java.util.ArrayList;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of setting a method breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class MethodBreakpointAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                if (!(n instanceof MethodNode)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(TraceAction.class,
                "LBL_MethodBreakpointAction_Name");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            Session session = SessionProvider.getCurrentSession();
            BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            for (Node n : activatedNodes) {
                if (n instanceof MethodNode) {
                    MethodNode mn = (MethodNode) activatedNodes[0];
                    Method method = mn.getMethod();
                    ReferenceType type = method.declaringType();
                    // JDI returns a list that cannot be instantiated by the
                    // XMLEncoder, which is required for our breakpoints.
                    List<String> args = new ArrayList<String>(
                            method.argumentTypeNames());
                    try {
                        Breakpoint bp = bf.createMethodBreakpoint(
                                type.name(), method.name(), args);
                        bm.addBreakpoint(bp);
                    } catch (MalformedClassNameException mcne) {
                        // This cannot happen.
                        ErrorManager.getDefault().notify(mcne);
                    } catch (MalformedMemberNameException mmne) {
                        // This cannot happen.
                        ErrorManager.getDefault().notify(mmne);
                    }
                }
            }
        }
    }
}
