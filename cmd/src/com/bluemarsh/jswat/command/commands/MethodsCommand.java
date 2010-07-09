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
 * are Copyright (C) 2005-2009. All Rights Reserved.
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
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays the methods for a class.
 *
 * @author Nathan Fiedler
 */
public class MethodsCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "methods";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();
        String cname = arguments.nextToken();

        // Find all matching classes.
        List<ReferenceType> classes =  Classes.findClasses(vm, cname);
        if (classes == null || classes.isEmpty()) {
            classes = resolveReference(context, cname);
        }

        if (classes != null && classes.size() > 0) {
            // For each matching class, print its methods.
            StringBuilder sb = new StringBuilder(256);
            Iterator<ReferenceType> iter = classes.iterator();
            while (iter.hasNext()) {
                printMethods(iter.next(), sb);
                if (iter.hasNext()) {
                    // Print a separator between the classes.
                    sb.append("---");
                    sb.append('\n');
                }
            }
            writer.print(sb.toString());
        } else {
            throw new CommandException(NbBundle.getMessage(
                    MethodsCommand.class, "ERR_ClassNotFound", cname));
        }
    }

    /**
     * Call the evaluator to figure out whether this is a reference to some
     * object.  If so, return its type (as a list, for caller compatibility).
     * @param context command context
     * @param expr an expression to evaluate in the current scope
     * @return a reference type or null if we couldn't resolve the expression
     */
    List<ReferenceType> resolveReference(CommandContext context, String expr) {
        try {
            Session session = context.getSession();
            DebuggingContext dc = context.getDebuggingContext();
            ThreadReference thread = dc.getThread();
            Evaluator evaluator = new Evaluator(expr);
            Object value = evaluator.evaluate(thread, dc.getFrame());
            if (value instanceof ObjectReference) {
                ObjectReference object = (ObjectReference) value;
                ReferenceType rtype = object.referenceType();
                List<ReferenceType> result = new ArrayList<ReferenceType>();
                result.add(rtype);
                return result;
            }
        } catch (Exception x) {
            // Doing nothing here is no worse than the default behavior of
            // the "methods" command before we implemented resolveReference().
            // Could be better, but at least it's a start.
        }
        return null;
    }

    /**
     * Print the methods of the given class type.
     *
     * @param  clazz  class to be displayed.
     * @param  sb     sink to write to.
     */
    protected void printMethods(ReferenceType clazz, StringBuilder sb) {
        // Display the class name first.
        sb.append("Class ");
        sb.append(clazz.name());
        sb.append(":\n");
        List<Method> methods = clazz.allMethods();
        methods = new ArrayList<Method>(methods);
        Collections.sort(methods, new MethodComparator());
        for (Method method : methods) {
            // First print the method name for easy reading.
            sb.append(method.name());
            sb.append('(');
            // For each parameter, show the parameter type.
            Iterator<String> iter = method.argumentTypeNames().iterator();
            while (iter.hasNext()) {
                sb.append(iter.next());
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(") : ");
            // Show the return type and method name.
            String returnType = method.returnTypeName();
            returnType = returnType.substring(returnType.lastIndexOf('.') + 1);
            sb.append(returnType);
            sb.append('\n');
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

    /**
     * A Comparator for Method objects.
     *
     * @author  Nathan Fiedler
     */
    private static class MethodComparator implements Comparator<Method> {

        @Override
        public int compare(Method o1, Method o2) {
            String n1 = o1.name();
            String n2 = o2.name();
            int result = n1.compareTo(n2);
            if (result == 0) {
                // If the names are the same, use the number of arguments
                // to determine order (fewer arguments sort first).
                int c1 = o1.argumentTypeNames().size();
                int c2 = o2.argumentTypeNames().size();
                return c1 - c2;
            } else {
                return result;
            }
        }
    }
}
