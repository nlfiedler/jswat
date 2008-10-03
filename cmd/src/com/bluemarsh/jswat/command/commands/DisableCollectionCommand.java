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
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DisableCollectionCommand.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Disables garbage collection for the specified object.
 *
 * @author Nathan Fiedler
 */
public class DisableCollectionCommand extends AbstractCommand {

    public String getName() {
        return "disablegc";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        arguments.returnAsIs(true);
        String expr = arguments.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, dc.getFrame());
            if (o instanceof ObjectReference) {
                ObjectReference obj = (ObjectReference) o;
                obj.disableCollection();
                PrintWriter writer = context.getWriter();
                writer.println(NbBundle.getMessage(getClass(),
                        "CTL_disablegc_CollectionDisabled"));
            } else {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_ExprNotAnObject"));
            }
        } catch (EvaluationException ee) {
            Throwable t = ee.getCause();
            if (t instanceof ClassNotPreparedException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_ClassNotPrepared"), t);
            } else if (t instanceof IllegalThreadStateException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_ThreadNoStack"), t);
            } else if (t instanceof IndexOutOfBoundsException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_InvalidStackFrame"), t);
            } else if (t instanceof InvalidStackFrameException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_InvalidStackFrame"), t);
            } else if (t instanceof NativeMethodException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_NativeMethod"), t);
            } else if (t instanceof ObjectCollectedException) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_ObjectCollected"), t);
            } else {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_EvaluationError", ee.getMessage()), ee);
            }
        }
    }

    public boolean requiresArguments() {
        return true;
    }

    public boolean requiresDebuggee() {
        return true;
    }

    public boolean requiresThread() {
        return true;
    }
}
