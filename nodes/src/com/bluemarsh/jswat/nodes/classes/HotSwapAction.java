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
 * $Id: HotSwapAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.classes;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Implements the action of redefining the source code for a class.
 *
 * @author  Nathan Fiedler
 */
public class HotSwapAction extends NodeAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null &&
                activatedNodes.length == 1 &&
                activatedNodes[0] instanceof ClassNode) {
            return true;
        }
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(HotSwapAction.class,
                "LBL_HotSwapAction_Name");
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null &&
                activatedNodes.length == 1 &&
                activatedNodes[0] instanceof ClassNode) {
            ClassNode cn = (ClassNode) activatedNodes[0];
            ReferenceType rt = cn.getReferenceType();
            Session session = SessionProvider.getCurrentSession();
            PathManager pm = PathProvider.getPathManager(session);
            StatusDisplayer sd = StatusDisplayer.getDefault();

            // Try to find the .class file.
            FileObject fo = pm.findByteCode(rt);
            if (fo == null) {
                sd.setStatusText(NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_ClassFileNotFound"));
                return;
            }

            // Do the actual hotswap operation.
            String errorMsg = null;
            InputStream is = null;
            VirtualMachine vm = session.getConnection().getVM();
            try {
                is = fo.getInputStream();
                Classes.hotswap(rt, is, vm);
            }  catch (UnsupportedOperationException uoe) {
                if (!vm.canRedefineClasses()) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_HotSwapAction_CannotHotSwap");
                } else if (!vm.canAddMethod()) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_HotSwapAction_CannotAddMethod");
                } else if (!vm.canUnrestrictedlyRedefineClasses()) {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_HotSwapAction_NotUnrestricted");
                } else {
                    errorMsg = NbBundle.getMessage(HotSwapAction.class,
                            "ERR_HotSwapAction_Unsupported");
                }
            }  catch (IOException ioe) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_IOException", ioe);
            }  catch (NoClassDefFoundError ncdfe) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_WrongClass");
            }  catch (VerifyError ve) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_VerifyError", ve);
            }  catch (UnsupportedClassVersionError ucve) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_VersionError", ucve);
            }  catch (ClassFormatError cfe) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_FormatError", cfe);
            }  catch (ClassCircularityError cce) {
                errorMsg = NbBundle.getMessage(HotSwapAction.class,
                        "ERR_HotSwapAction_Circularity", cce);
            }  finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        // ignored
                    }
                }
            }

            // Display the results.
            if (errorMsg != null) {
                sd.setStatusText(errorMsg);
            } else {
                sd.setStatusText(NbBundle.getMessage(HotSwapAction.class,
                        "CTL_HotSwapAction_Success"));
            }
        }
    }
}
