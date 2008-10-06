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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PropertiesCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.openide.util.NbBundle;

/**
 * Displays the elements in a collection in the debuggee.
 *
 * @author Nathan Fiedler
 */
public class PropertiesCommand extends AbstractCommand {
    /** Emtpy list of Value objects. */
    private static List<Value> EMTPY_ARGUMENTS = new LinkedList<Value>();

    public String getName() {
        return "properties";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter writer = context.getWriter();
        // Get the current thread.
        DebuggingContext dc = context.getDebuggingContext();
        ThreadReference thread = dc.getThread();

        Session session = context.getSession();
        VirtualMachine vm = session.getConnection().getVM();
        List<ReferenceType> classes = vm.classesByName("java.lang.System");
        // We assume it exists and is a real class.
        ClassType type = (ClassType) classes.get(0);
        List<Method> methods = type.methodsByName("getProperties");
        if (methods.size() == 0) {
            // KVM does not have a System.getProperties() method.
            writer.println(NbBundle.getMessage(PropertiesCommand.class,
                    "CTL_properties_NoPropertiesMethod"));
            return;
        }
        Method method = methods.get(0);
        try {
            ObjectReference props = (ObjectReference) Classes.invokeMethod(
                    null, type, thread, method, EMTPY_ARGUMENTS);

            // Get the property names enumerator.
            type = (ClassType) props.referenceType();
            methods = type.methodsByName("propertyNames", "()Ljava/util/Enumeration;");
            if (methods.size() == 0) {
                throw new IllegalArgumentException("no propertyNames() method");
            }
            method = methods.get(0);
            ObjectReference iter = (ObjectReference) Classes.invokeMethod(
                props, type, thread, method, EMTPY_ARGUMENTS);

            ClassType iterType = (ClassType) iter.referenceType();
            methods = iterType.methodsByName("hasMoreElements", "()Z");
            if (methods.size() == 0) {
                throw new IllegalArgumentException("no hasMoreElements() method");
            }
            Method hasNextMeth = methods.get(0);

            methods = iterType.methodsByName("nextElement", "()Ljava/lang/Object;");
            if (methods.size() == 0) {
                throw new IllegalArgumentException("no nextElement() method");
            }
            Method nextMeth = methods.get(0);

            BooleanValue bool = (BooleanValue) Classes.invokeMethod(
                iter, iterType, thread, hasNextMeth, EMTPY_ARGUMENTS);

            // Enumerate the property names, and then sort them.
            List<String> propNames = new LinkedList<String>();
            while (bool != null && bool.value()) {
                StringReference sr = (StringReference) Classes.invokeMethod(
                        iter, iterType, thread, nextMeth, EMTPY_ARGUMENTS);
                propNames.add(sr.value());
                bool = (BooleanValue) Classes.invokeMethod(
                        iter, iterType, thread, hasNextMeth, EMTPY_ARGUMENTS);
            }
            Collections.sort(propNames);

            // Display the property values.
            type = (ClassType) props.referenceType();
            methods = type.methodsByName("getProperty",
                    "(Ljava/lang/String;)Ljava/lang/String;");
            if (methods.size() == 0) {
                throw new IllegalArgumentException("no getProperty() method");
            }
            method = methods.get(0);
            List<Value> args = new ArrayList<Value>(1);
            args.add(vm.mirrorOf("dummy"));
            for (String prop : propNames) {
                args.set(0, vm.mirrorOf(prop));
                StringReference sr = (StringReference) Classes.invokeMethod(
                        props, type, thread, method, args);
                writer.print(prop);
                writer.print(" = ");
                writer.println(sr.value());
            }
        } catch (ExecutionException ee) {
            Throwable t = ee.getCause();
            if (t instanceof IncompatibleThreadStateException) {
                throw new CommandException(NbBundle.getMessage(PropertiesCommand.class,
                        "ERR_IncompatibleThread"), ee);
            } else {
                throw new CommandException(t.toString(), t);
            }
        } catch (Exception e) {
            throw new CommandException(e.toString(), e);
        }
    }

    public boolean requiresDebuggee() {
        return true;
    }

    public boolean requiresThread() {
        return true;
    }
}
