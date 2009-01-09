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

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import com.bluemarsh.jswat.ui.components.SessionPropertiesPanel;

/**
 * Provides a means for changing the properties of the current session.
 *
 * @author Nathan Fiedler
 */
public class SessionPropertiesAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-session-properties");
    }

    public String getName() {
        return NbBundle.getMessage(SessionPropertiesAction.class,
                "LBL_SessionPropertiesAction");
    }

    public void performAction() {
        SessionPropertiesPanel spp = new SessionPropertiesPanel();
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        spp.loadParameters(session);
        if (spp.display()) {
            spp.saveParameters(session);
        }
    }
}
