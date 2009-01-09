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

package com.bluemarsh.jswat.nodes.threads;

import com.sun.jdi.ThreadReference;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of interrupting the selected threads and groups.
 *
 * @author  Nathan Fiedler
 */
public class InterruptAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                if (!(n instanceof ThreadNode)) {
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
        return NbBundle.getMessage(InterruptAction.class,
                "LBL_InterruptAction_Name");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                if (n instanceof ThreadNode) {
                    ThreadReference tr = ((ThreadNode) n).getThread();
                    tr.interrupt();
                }
            }
        }
    }
}
