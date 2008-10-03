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
 * $Id: CopySessionAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.sessions;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of copying the selected session.
 *
 * @author  Nathan Fiedler
 */
public class CopySessionAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length > 0;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(CopySessionAction.class,
                "LBL_CopySessionAction");
    }

    protected void performAction(Node[] activatedNodes) {
        SessionManager sm = SessionProvider.getSessionManager();
        for (Node n : activatedNodes) {
            if (n instanceof SessionNode) {
                SessionNode sessionNode = (SessionNode) n;
                Session session = sessionNode.getSession();
                // Copy the session, giving it a generated name.
                Session copy = sm.copy(session, null);
                // Copy the source/class paths to the copy.
                PathManager pm = PathProvider.getPathManager(session);
                List<String> cpath = pm.getClassPath();
                List<FileObject> spath = pm.getSourcePath();
                pm = PathProvider.getPathManager(copy);
                pm.setClassPath(cpath);
                pm.setSourcePath(spath);
            }
        }
    }
}
