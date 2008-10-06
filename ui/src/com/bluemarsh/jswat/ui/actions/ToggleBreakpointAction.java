/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler,
 *                 Jay Burgess.
 *
 * $Id: ToggleBreakpointAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import java.net.MalformedURLException;
import java.util.Iterator;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Toggles a line breakpoint at a line in the currently focused editor.
 *
 * @author Nathan Fiedler
 */
public class ToggleBreakpointAction extends CallableSystemAction {
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
        return new HelpCtx("jswat-create-breakpoint");
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_ToggleBreakpointAction");
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
            // Determine if a breakpoint already exists.
            Breakpoint bp = null;
            Iterator iter = bm.getDefaultGroup().breakpoints(true);
            while (iter.hasNext()) {
                Breakpoint temp = (Breakpoint) iter.next();
                if (temp instanceof LineBreakpoint) {
                    LineBreakpoint lbp = (LineBreakpoint) temp;
                    if (lbp.getLineNumber() == line && lbp.getURL().equals(url)) {
                        bp = lbp;
                        break;
                    }
                }
            }
            if (bp != null) {
                if (bp.isEnabled()) {
                    bp.setEnabled(false);
                } else {
                    bm.removeBreakpoint(bp);
                }
            } else {
                try {
                    // Attempt to determine the package name.
                    String name = es.getClassName(url, line);
                    name = Names.getPackageName(name);
                    if (name != null && name.length() == 0) {
                        // Empty package names should be treated as null.
                        name = null;
                    }
                    bp = bf.createLineBreakpoint(url, name, line);
                    bm.addBreakpoint(bp);
                } catch (MalformedClassNameException mcne) {
                    ErrorManager.getDefault().notify(mcne);
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify(mue);
                }
            }
        }
    }
}
