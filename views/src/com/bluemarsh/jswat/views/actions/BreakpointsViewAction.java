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
 * are Copyright (C) 2004-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.views.actions;

import com.bluemarsh.jswat.ui.views.BreakpointsView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Displays the breakpoints top component.
 *
 * @author Nathan Fiedler
 */
public class BreakpointsViewAction extends CallableSystemAction {
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
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_BreakpointsViewAction");
    }

    /**
     * Specify the proper resource name for the action's icon.
     *
     * @return  the resource name for the icon.
     */
    protected String iconResource() {
        return NbBundle.getMessage(getClass(), "IMG_BreakpointsViewAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        WindowManager wm = WindowManager.getDefault();
        TopComponent tc = wm.findTopComponent("breakpoints");
        if (tc == null) {
            tc = new BreakpointsView();
        }
        tc.open();
        tc.requestActive();
    }
}
