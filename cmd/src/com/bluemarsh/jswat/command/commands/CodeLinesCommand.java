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
 * $Id: CodeLinesCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.Location;
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
 * Displays the line numbers for the ones that have executable code.
 *
 * @author Nathan Fiedler
 */
public class CodeLinesCommand extends AbstractCommand {

    public String getName() {
        return "lines";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        // Get name of class, and possibly method name and/or loader ID.
        String className = arguments.nextToken();
        String methodName = arguments.hasMoreTokens() ? arguments.nextToken() : null;
        String loaderId = arguments.hasMoreTokens() ? arguments.nextToken() : null;
        long lid = -1;
        if (loaderId != null) {
            try {
                lid = Long.parseLong(loaderId);
            } catch (NumberFormatException nfe) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_lines_InvalidLoader"));
            }
        } else if (methodName != null) {
            // See if the second argument is a method name or loader ID.
            try {
                lid = Long.parseLong(methodName);
                // It is a class loader ID.
                loaderId = methodName;
                methodName = null;
            } catch (NumberFormatException nfe) {
                // It is a method name.
            }
        }

        List<ReferenceType> classes = vm.classesByName(className);
        if (classes != null && classes.size() > 0) {
            // Print out line number info for all matching classes.
            for (ReferenceType clazz : classes) {
                if (loaderId != null) {
                    ClassLoaderReference clr = clazz.classLoader();
                    if (clr != null && clr.uniqueID() != lid) {
                        // Class loader does not match what user provided.
                        continue;
                    }
                }
                try {
                    printLines(clazz, methodName, writer);
                } catch (AbsentInformationException aie) {
                    writer.println(NbBundle.getMessage(getClass(),
                            "ERR_lines_AbsentInfo", clazz.name()));
                }
            }
        } else {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_ClassNotFound", className));
        }
    }

    /**
     * Print the code line numbers for the given class and method.
     * If method is null, prints out lines for all methods.
     *
     * @param  clazz       class to operate on.
     * @param  methodName  name of method, or null to for all.
     * @param  writer      where to write line information.
     * @throws  AbsentInformationException
     *          if class was not compiled with debugging information.
     */
    protected void printLines(ReferenceType clazz, String methodName,
            PrintWriter writer) throws AbsentInformationException, CommandException {
        List<Location> lines = null;
        if (methodName == null) {
            lines = clazz.allLineLocations();
            // Need to make it modifiable so it can be sorted.
            lines = new ArrayList<Location>(lines);
        } else {
            lines = new ArrayList<Location>();
            List<Method> methods = clazz.allMethods();
            Iterator<Method> iter = methods.iterator();
            while (iter.hasNext()) {
                Method method = iter.next();
                if (method.name().equals(methodName)) {
                    lines.addAll(method.allLineLocations());
                }
            }

            if (lines.size() == 0) {
                throw new CommandException(NbBundle.getMessage(getClass(),
                        "ERR_lines_InvalidMethod", methodName));
            }
        }

        Collections.sort(lines, new LineComparator());

        StringBuilder sb = new StringBuilder(256);
        ClassLoaderReference clr = clazz.classLoader();
        if (clr != null) {
            sb.append(clr.referenceType().name());
            sb.append(" (");
            sb.append(clr.uniqueID());
            sb.append(")\n");
        }
        for (Location line : lines) {
            sb.append(line.lineNumber());
            sb.append(": ");
            sb.append(line.method().name());
            sb.append('(');
            List<String> types = line.method().argumentTypeNames();
            if (types != null && types.size() > 0) {
                sb.append(Strings.listToString(types));
            }
            sb.append(')');
            sb.append('\n');
        }
        writer.print(sb.toString());
    }

    public boolean requiresArguments() {
        return true;
    }

    public boolean requiresDebuggee() {
        return true;
    }

    /**
     * Compares Location objects for sorting.
     *
     * @author  Nathan Fiedler
     */
    private static class LineComparator implements Comparator<Location> {

        /**
         * @return  a negative integer, zero, or a positive integer as the
         *          first argument is less than, equal to, or greater than
         *          the second.
         */
        public int compare(Location o1, Location o2) {
            return o1.lineNumber() - o2.lineNumber();
        }
    }
}
