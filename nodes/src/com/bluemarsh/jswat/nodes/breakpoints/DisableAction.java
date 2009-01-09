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
 * are Copyright (C) 2004-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.breakpoints;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of disabling a breakpoint.
 *
 * @author  Nathan Fiedler
 */
public class DisableAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null) {
            if (activatedNodes.length == 1) {
                Node n = activatedNodes[0];
                if (n instanceof BreakpointNode) {
                    Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                    return bp.isEnabled();
                } else if (n instanceof BreakpointGroupNode) {
                    BreakpointGroup bg = ((BreakpointGroupNode) n).getBreakpointGroup();
                    return bg.isEnabled();
                }
            } else if (activatedNodes.length > 1) {
                // For multiple selections, always enable.
                return true;
            }
        }
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(DisableAction.class,
                "LBL_DisableAction_Name");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                if (n instanceof BreakpointNode) {
                    Breakpoint bp = ((BreakpointNode) n).getBreakpoint();
                    if (bp.isEnabled()) {
                        bp.setEnabled(false);
                    }
                } else if (n instanceof BreakpointGroupNode) {
                    BreakpointGroup bg = ((BreakpointGroupNode) n).getBreakpointGroup();
                    if (bg.isEnabled()) {
                        bg.setEnabled(false);
                    }
                }
            }
        }
    }
}
