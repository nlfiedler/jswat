/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MethodsCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
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

    public String getName() {
        return "methods";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();
        String cname = arguments.nextToken();

        // Find all matching classes.
        List<ReferenceType> classes =  Classes.findClasses(vm, cname);
        if (classes != null && classes.size() > 0) {
            // For each matching class, print its methods.
            StringBuilder sb = new StringBuilder(256);
            Iterator iter = classes.iterator();
            while (iter.hasNext()) {
                printMethods((ReferenceType) iter.next(), sb);
                if (iter.hasNext()) {
                    // Print a separator between the classes.
                    sb.append("---");
                    sb.append('\n');
                }
            }
            writer.print(sb.toString());
        } else {
            throw new CommandException(NbBundle.getMessage(getClass(),
                "ERR_ClassNotFound", cname));
        }
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
            Iterator iter = method.argumentTypeNames().iterator();
            while (iter.hasNext()) {
                sb.append((String) iter.next());
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

    public boolean requiresArguments() {
        return true;
    }

    public boolean requiresDebuggee() {
        return true;
    }

    /**
     * A Comparator for Method objects.
     *
     * @author  Nathan Fiedler
     */
    private static class MethodComparator implements Comparator<Method> {

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than,
         * equal to, or greater than the second.
         *
         * @param  o1  first Method.
         * @param  o2  second Method.
         * @return  zero if equal, -1 if o1 less than o2, otherwise +1.
         */
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
