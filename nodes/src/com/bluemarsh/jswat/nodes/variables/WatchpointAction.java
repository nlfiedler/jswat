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
 * are Copyright (C) 2005-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.nodes.variables;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.VirtualMachine;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of creating a watchpoint for the selected fields.
 *
 * @author  Nathan Fiedler
 */
public class WatchpointAction extends NodeAction {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            for (Node n : activatedNodes) {
                GetVariableCookie gvc = n.getCookie(GetVariableCookie.class);
                if (gvc == null) {
                    return false;
                } else {
                    Field f = gvc.getField();
                    if (f == null) {
                        return false;
                    } else {
                        VirtualMachine vm = f.virtualMachine();
                        if (!vm.canWatchFieldModification()
                                || gvc.getObjectReference() != null
                                && !vm.canUseInstanceFilters()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WatchpointAction.class,
                "LBL_BreakpointAction_Name");
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Session session = SessionProvider.getCurrentSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        for (Node n : activatedNodes) {
            GetVariableCookie gvc = n.getCookie(GetVariableCookie.class);
            if (gvc != null) {
                Field field = gvc.getField();
                if (field != null) {
                    ObjectReference obj = gvc.getObjectReference();
                    Breakpoint bp = null;
                    if (obj != null) {
                        bp = bf.createWatchBreakpoint(
                                field, obj, false, true);
                    } else {
                        try {
                            bp = bf.createWatchBreakpoint(
                                    field.declaringType().name(),
                                    field.name(), false, true);
                        } catch (MalformedClassNameException mcne) {
                            // This can't happen.
                        } catch (MalformedMemberNameException mmne) {
                            // This can't happen.
                        }
                    }
                    if (bp != null) {
                        bm.addBreakpoint(bp);
                    }
                }
            }
        }
    }
}
