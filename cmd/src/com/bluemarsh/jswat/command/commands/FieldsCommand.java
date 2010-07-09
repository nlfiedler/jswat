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
import com.sun.jdi.Field;
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
 * Displays the fields for a class.
 *
 * @author Nathan Fiedler
 */
public class FieldsCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "fields";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();
        String cname = arguments.nextToken();

        // Find all matching classes.
        List<ReferenceType> classes = Classes.findClasses(vm, cname);
        if (classes == null || classes.isEmpty()) {
            classes = new MethodsCommand().resolveReference(context, cname);
        }

        if (classes != null && classes.size() > 0) {
            // For each matching class, print its fields.
            StringBuilder sb = new StringBuilder(256);
            Iterator<ReferenceType> iter = classes.iterator();
            while (iter.hasNext()) {
                ReferenceType clazz = iter.next();
                // Get the fields and sort them by name.
                sb.append(NbBundle.getMessage(FieldsCommand.class, "CTL_fields_Header",
                        clazz.name()));
                List<Field> fields = clazz.allFields();
                List<Field> visible = clazz.visibleFields();
                fields = new ArrayList<Field>(fields);
                Collections.sort(fields, new Comparator<Field>() {

                    @Override
                    public int compare(Field o1, Field o2) {
                        return o1.name().compareTo(o2.name());
                    }
                });
                // Display the fields for the class.
                for (Field field : fields) {
                    sb.append(field.typeName());
                    sb.append(' ');
                    sb.append(field.name());
                    if (!visible.contains(field)) {
                        sb.append(' ');
                        sb.append(NbBundle.getMessage(FieldsCommand.class,
                                "CTL_fields_Hidden"));
                    } else if (!field.declaringType().equals(clazz)) {
                        sb.append(' ');
                        sb.append(NbBundle.getMessage(FieldsCommand.class,
                                "CTL_fields_Inherited", field.declaringType().name()));
                    }
                    sb.append('\n');
                }
                if (iter.hasNext()) {
                    // Print a separator between the classes.
                    sb.append("---");
                    sb.append('\n');
                }
            }
            writer.print(sb.toString());
        } else {
            throw new CommandException(NbBundle.getMessage(
                    FieldsCommand.class, "ERR_ClassNotFound", cname));
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
