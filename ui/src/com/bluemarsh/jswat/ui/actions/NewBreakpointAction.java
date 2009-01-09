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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
//import org.openide.src.ClassElement;
//import org.openide.src.ConstructorElement;
//import org.openide.src.Element;
//import org.openide.src.Identifier;
//import org.openide.src.InitializerElement;
//import org.openide.src.MethodParameter;
//import org.openide.src.Type;
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
        return NbBundle.getMessage(getClass(), "LBL_NewBreakpointAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        // Try to create a breakpoint based on the caret position.
        EditorSupport es = EditorSupport.getDefault();
        String url = es.getCurrentURL();
        int line = es.getCurrentLineNumber();
        Breakpoint bp = null;
        Session session = SessionProvider.getCurrentSession();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
// XXX: org.openide.src is gone
//        if (url != null && line > 0) {
//            Element element = es.getElement(url, line);
//            if (element != null) {
//                BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
//                if (element instanceof ConstructorElement) {
//                    // MethodElement subclasses ConstructorElement.
//                    ConstructorElement ce = (ConstructorElement) element;
//                    String className = ce.getDeclaringClass().getName().
//                            getFullName();
//                    String methodName = ce.getName().getFullName();
//                    MethodParameter[] params = ce.getParameters();
//                    List<String> methodParams = new ArrayList<String>(
//                            params.length);
//                    for (MethodParameter param : params) {
//                        // If the parameter type is a class and is not
//                        // fully-qualified, and not an inner class, then
//                        // it will be replaced with "*" because we cannot
//                        // feasibly resolve the name otherwise.
//                        Type ptype = param.getType();
//                        String pname = null;
//                        if (ptype.isArray()) {
//                            ptype = ptype.getElementType();
//                            pname = resolveClass(ce.getDeclaringClass(),
//                                    ptype.getTypeIdentifier());
//                            if (!pname.equals("*")) {
//                                pname += "[]";
//                            }
//                        } else if (ptype.isClass()) {
//                            pname = resolveClass(ce.getDeclaringClass(),
//                                    ptype.getTypeIdentifier());
//                        } else {
//                            pname = ptype.getFullString();
//                        }
//                        methodParams.add(pname);
//                    }
//                    try {
//                        bp = bf.createMethodBreakpoint(className, methodName,
//                                methodParams);
//                    } catch (MalformedClassNameException mcne) {
//                        // Fall through as if caret position was invalid.
//                    } catch (MalformedMemberNameException mmne) {
//                        // Fall through as if caret position was invalid.
//                    }
// For some reason, NB never returns field elements.
//                } else if (element instanceof FieldElement) {
//                    FieldElement fe = (FieldElement) element;
//                    String className = fe.getDeclaringClass().getName().
//                            getFullName();
//                    String fieldName = fe.getName().getFullName();
//                    try {
//                        bp = bf.createWatchBreakpoint(className, fieldName,
//                                true, true);
//                    } catch (MalformedClassNameException mcne) {
//                        // Fall through as if caret position was invalid.
//                    } catch (MalformedMemberNameException mmne) {
//                        // Fall through as if caret position was invalid.
//                    }
//                } else if (element instanceof InitializerElement) {
//                    InitializerElement ie = (InitializerElement) element;
//                    String className = ie.getDeclaringClass().getName().
//                            getFullName();
//                    String methodName = ie.isStatic() ? "<clinit>" : "<init>";
//                    List<String> methodParams = Collections.emptyList();
//                    try {
//                        bp = bf.createMethodBreakpoint(className, methodName,
//                                methodParams);
//                    } catch (MalformedClassNameException mcne) {
//                        // Fall through as if caret position was invalid.
//                    } catch (MalformedMemberNameException mmne) {
//                        // Fall through as if caret position was invalid.
//                    }
//                }
//            }
//        }

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

    /**
     * Attempt to resolve the class name to either a fully-qualified name,
     * or to the "*" wildcard for method parameters.
     *
     * @param  declaringType  the type containing the identifier.
     * @param  identifier     the identifier to resolve.
     * @return resolved identifier, or "*".
     */
//    private static String resolveClass(ClassElement declaringType,
//            Identifier identifier) {
//        // 0. Identifier is already fully-qualified.
//        String fullName = identifier.getFullName();
//        if (fullName.indexOf('.') > 0) {
//            return fullName;
//        }
//        // 1. Class is an inner class of declaringType.
//        ClassElement subc = declaringType.getClass(identifier);
//        if (subc != null) {
//            return fullName;
//        }
//        // 2. Class is in same package as declaringType.
//        // 3. Class is in the java.lang package.
//        // 4. It is in the default package.
//        return "*";
//    }
}
