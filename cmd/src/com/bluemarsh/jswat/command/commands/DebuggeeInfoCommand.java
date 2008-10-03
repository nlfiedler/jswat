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
 * $Id: DebuggeeInfoCommand.java 6 2007-05-16 07:14:24Z nfiedler $
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
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays information regarding the connected debuggee.
 *
 * @author Nathan Fiedler
 */
public class DebuggeeInfoCommand extends AbstractCommand {

    public String getName() {
        return "vminfo";
    }

    /**
     * Returns a string comprised of the desired prefix, followed by
     * a newline, and each path element on a separate line.
     *
     * @param  prefix  path display prefix.
     * @param  path    list of Strings to display.
     * @return  resultant string.
     */
    private static String pathToString(String prefix, List path) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append('\n');
        Iterator iter = path.iterator();
        if (iter.hasNext()) {
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append('\n');
                sb.append(iter.next());
            }
        }
        return sb.toString();
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        //
        // Display classpath information.
        //
        if (vm instanceof PathSearchingVirtualMachine) {
            PathSearchingVirtualMachine psvm =
                (PathSearchingVirtualMachine) vm;
            writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_basedir"));
            writer.println();
            writer.println(psvm.baseDirectory());

            writer.println();

            List cpath = psvm.classPath();
            writer.println(pathToString(NbBundle.getMessage(getClass(), "CTL_vminfo_cpath"), cpath));

            writer.println();

            cpath = psvm.bootClassPath();
            writer.println(pathToString(
                            NbBundle.getMessage(getClass(), "CTL_vminfo_bcpath"), cpath));
        }

        //
        // Display the default stratum.
        //
        writer.println();
        writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_stratum"));
        writer.print(" ");
        writer.println(vm.getDefaultStratum());

        //
        // Display debuggee memory sizes.
        //

        // We need the current thread.
        DebuggingContext dc = context.getDebuggingContext();
        ThreadReference thread = dc.getThread();
        if (thread == null) {
            writer.println();
            writer.println(NbBundle.getMessage(getClass(), "ERR_vminfo_nothread"));
            return;
        }

        // We assume this class exists in the debuggee.
        List runtimeTypes = vm.classesByName("java.lang.Runtime");
        ClassType clazz = (ClassType) runtimeTypes.get(0);
        // We assume this class has just one of each of these methods.
        List methods = clazz.methodsByName("getRuntime",
                                            "()Ljava/lang/Runtime;");
        Method method = (Method) methods.get(0);
        List<Value> emptyList = Collections.emptyList();
        try {
            ObjectReference oref = (ObjectReference) Classes.invokeMethod(
                null, clazz, thread, method, emptyList);

            methods = clazz.methodsByName("availableProcessors", "()I");
            method = (Method) methods.get(0);
            Object rval = Classes.invokeMethod(
                oref, clazz, thread, method, emptyList);
            writer.println();
            writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_numprocs"));
            writer.print(" ");
            writer.println(rval.toString());

            methods = clazz.methodsByName("freeMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, clazz, thread, method, emptyList);
            writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_freemem"));
            writer.print(" ");
            writer.println(rval.toString());

            methods = clazz.methodsByName("maxMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, clazz, thread, method, emptyList);
            writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_maxmem"));
            writer.print(" ");
            writer.println(rval.toString());

            methods = clazz.methodsByName("totalMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, clazz, thread, method, emptyList);
            writer.print(NbBundle.getMessage(getClass(), "CTL_vminfo_totalmem"));
            writer.print(" ");
            writer.println(rval.toString());
        } catch (Exception e) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_vminfo_InvocationFailed"), e);
        }
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
