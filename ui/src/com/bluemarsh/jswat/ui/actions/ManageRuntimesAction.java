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
 * $Id: ManageRuntimesAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.ui.components.RuntimeManagerPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Provides a means of managing the Java runtimes used in launching.
 *
 * @author Nathan Fiedler
 */
public class ManageRuntimesAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-runtime-manager");
    }

    public String getName() {
        return NbBundle.getMessage(ManageRuntimesAction.class,
                "LBL_ManageRuntimesAction");
    }

    protected String iconResource() {
        return NbBundle.getMessage(ManageRuntimesAction.class,
                "IMG_ManageRuntimesAction");
    }

    public void performAction() {
        new RuntimeManagerPanel().display();
    }
}
