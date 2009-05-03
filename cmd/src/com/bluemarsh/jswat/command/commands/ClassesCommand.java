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
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Displays the list of classes in the debuggee.
 *
 * @author Nathan Fiedler
 */
public class ClassesCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "classes";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        List<ReferenceType> classes;
        if (arguments.hasMoreTokens()) {
            String cname = arguments.nextToken();
            classes = Classes.findClasses(vm, cname);
        } else {
            classes = vm.allClasses();
        }
        Iterator<ReferenceType> iter = classes.iterator();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                ReferenceType clazz = iter.next();
                writer.print(clazz.name());
                writer.print(" [");
                ClassLoaderReference clr = clazz.classLoader();
                if (clr != null) {
                    writer.print(clr.referenceType().name());
                    writer.print(" (");
                    writer.print(String.valueOf(clr.uniqueID()));
                    writer.print(")");
                } else {
                    writer.print(NbBundle.getMessage(ClassesCommand.class,
                            "CTL_classes_NoClassLoader"));
                }
                writer.println("]");
            }
        } else {
            throw new CommandException(NbBundle.getMessage(
                    ClassesCommand.class, "CTL_classes_NoneLoaded"));
        }
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }
}
