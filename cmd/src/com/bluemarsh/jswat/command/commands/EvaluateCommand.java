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
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
import com.sun.jdi.ThreadReference;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Evalutes a Java-like expression and displays the result.
 *
 * @author Nathan Fiedler
 */
public class EvaluateCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "evaluate";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        Session session = context.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        arguments.returnAsIs(true);
        String expr = arguments.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, dc.getFrame());
            String s = Evaluator.prettyPrint(o, thread);
            writer.println(s);
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

    @Override
    public boolean requiresArguments() {
        return true;
    }
}
