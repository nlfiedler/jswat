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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.ui.ActionEnabler;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import java.net.MalformedURLException;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Sets a line breakpoint at a line in the currently focused editor,
 * then resumes the debuggee. The breakpoint is set to delete on expire
 * and expire after one hit, so it will be deleted automatically.
 *
 * @author Nathan Fiedler
 */
public class RunToCursorAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of RunToCursorAction.
     */
    public RunToCursorAction() {
        ActionEnabler ae = ActionEnabler.getDefault();
        ae.registerSuspendedAction(this);
    }

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
        return new HelpCtx("jswat-create-breakpoint");
    }

    /**
     * Specify the proper resource name for the action's icon.
     *
     * @return  the resource name for the icon.
     */
    protected String iconResource() {
        return NbBundle.getMessage(getClass(), "IMG_RunToCursorAction");
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_RunToCursorAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        EditorSupport es = EditorSupport.getDefault();
        // Get current editor url and line number.
        String url = es.getCurrentURL();
        int line = es.getCurrentLineNumber();
        if (url != null && line > 0) {
            Session session = SessionProvider.getSessionManager().getCurrent();
            BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
            BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
            try {
                // Attempt to determine the package name.
                String name = es.getClassName(url, line);
                name = Names.getPackageName(name);
                if (name != null && name.length() == 0) {
                    // Empty package names should be treated as null.
                    name = null;
                }
                Breakpoint bp = bf.createLineBreakpoint(url, name, line);
                bp.setExpireCount(1);
                bp.setDeleteOnExpire(true);
                bm.addBreakpoint(bp);
                session.resumeVM();
            } catch (MalformedClassNameException mcne) {
                ErrorManager.getDefault().notify(mcne);
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
            }
        }
    }
}
