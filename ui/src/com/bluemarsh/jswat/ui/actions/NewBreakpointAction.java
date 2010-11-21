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
package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.breakpoint.CreatorPanel;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Displays a dialog to allow the user to create a new breakpoint.
 *
 * @author Nathan Fiedler
 */
public class NewBreakpointAction extends CallableSystemAction {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-create-breakpoint");
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_NewBreakpointAction");
    }

    @Override
    public void performAction() {
        // Try to create a breakpoint based on the caret position.
        EditorSupport es = EditorSupport.getDefault();
        String url = es.getCurrentURL();
        int line = es.getCurrentLineNumber();
        Breakpoint bp = null;
        Session session = SessionProvider.getCurrentSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        if (url != null && line > 0) {
            ExecutableElement element = es.getElement(url, line);
            if (element != null) {
                BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
                // This includes methods, constructors, and initializers.
                TypeElement te = es.getEnclosingType(element);
                String className = te.getQualifiedName().toString();
                String methodName = element.getSimpleName().toString();
                List<String> methodParams = new ArrayList<String>(element.getParameters().size());
                // While it would be great if we could determine the types
                // of the parameters, with the information we have it is
                // quite difficult to get anything reasonably accurate.
                Collections.fill(methodParams, "*");
                try {
                    bp = bf.createMethodBreakpoint(className, methodName,
                            methodParams);
                } catch (MalformedClassNameException mcne) {
                    // Fall through as if caret position was invalid.
                } catch (MalformedMemberNameException mmne) {
                    // Fall through as if caret position was invalid.
                }
                // It would be great to handle fields as well, to create
                // watch breakpoints, but the code is too fragile to get
                // reliable results, and the effort outweighs the benefit.
            }
        }

        CreatorPanel cp = new CreatorPanel();
        if (bp != null) {
            cp.loadParameters(bp);
        }
        if (cp.display()) {
            if (bp == null) {
                bp = cp.createBreakpoint();
            }
            if (bp != null) {
                bm.addBreakpoint(bp);
                cp.saveParameters(bp);
            } else {
                String msg = NbBundle.getMessage(getClass(),
                        "LBL_NewBreakpoint_Failed");
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
}
