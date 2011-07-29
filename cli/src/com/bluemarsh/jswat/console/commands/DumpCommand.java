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
 * $Id: DownCommand.java 284 2010-11-20 22:35:20Z nathanfiedler $
 */

package com.bluemarsh.jswat.console.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Implements the JDB "dump" command.
 *
 * @author Steve Yegge
 */
public class DumpCommand extends AbstractCommand {

    private StringBuilder sb;

    @Override
    public String getName() {
        return "dump";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        PrintWriter writer = context.getWriter();
        String expr = arguments.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, dc.getFrame());
            String result = dump(expr, o);
            writer.print(result);
        } catch (Exception x) {
            throw new CommandException(x);
        }
    }

    /**
     * Dumps an object reference.
     * @param expr the string expression that was evaluated
     * @param val the object evaluated from {@code expr} (may be {@code null})
     * @return the printed/formatted result
     */
    public String dump(String expr, Object val) {
        if (val == null) {
            return NbBundle.getMessage(getClass(), "CTL_expr_is_null", expr);
        }
        if (!(val instanceof ObjectReference)) {
            return NbBundle.getMessage(getClass(), "CTL_expr_is_value",
                                       expr, String.valueOf(expr));
        }
        sb = new StringBuilder();
        sb.append(NbBundle.getMessage(getClass(), "CTL_expr_is_value",
                                      expr, "{"));
        ObjectReference obj = (ObjectReference) val;
        ReferenceType refType = obj.referenceType();
        dump(obj, refType, refType);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Dumps out the fields of {@code obj} and their rendered values.
     * @param obj an object, or a field of an object being dumped
     * @param refType the type of {@code obj}
     * @param refTypeBase the original type being dumped
     */
    private void dump(ObjectReference obj,
                      ReferenceType refType,
                      ReferenceType refTypeBase) {
        for (Iterator it = refType.fields().iterator(); it.hasNext(); ) {
            Field field = (Field)it.next();
            sb.append("    ");
            if (!refType.equals(refTypeBase)) {
                sb.append(refType.name());
                sb.append(".");
            }
            sb.append(field.name());
            sb.append(": ");
            sb.append(obj.getValue(field));
            sb.append("\n");
        }
        if (refType instanceof ClassType) {
            ClassType sup = ((ClassType)refType).superclass();
            if (sup != null) {
                dump(obj, sup, refTypeBase);
            }
        } else if (refType instanceof InterfaceType) {
            List sups = ((InterfaceType)refType).superinterfaces();
            for (Iterator it = sups.iterator(); it.hasNext(); ) {
                dump(obj, (ReferenceType)it.next(), refTypeBase);
            }
        } else {
            /* else refType is an instanceof ArrayType */
            if (obj instanceof ArrayReference) {
                for (Iterator it = ((ArrayReference)obj).getValues().iterator();
                     it.hasNext(); ) {
                    sb.append(it.next().toString());
                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }
                sb.append("\n");
            }
        }
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }
}
