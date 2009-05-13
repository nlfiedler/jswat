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
import com.bluemarsh.jswat.core.path.PathEntry;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Classes;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Redefines a class in the running debuggee.
 *
 * @author Nathan Fiedler
 */
public class RedefineClassCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "redefine";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        String cname = arguments.nextToken();
        List<ReferenceType> classes = Classes.findClasses(vm, cname);
        if (classes.size() == 0) {
            throw new CommandException(NbBundle.getMessage(
                    RedefineClassCommand.class,
                    "ERR_redefine_MissingClass", cname));
        }
        ReferenceType clazz = classes.get(0);

        InputStream stream = null;
        if (arguments.hasMoreTokens()) {
            String fname = arguments.nextToken();
            try {
                stream = new FileInputStream(fname);
            } catch (FileNotFoundException fnfe) {
                throw new CommandException(NbBundle.getMessage(
                        RedefineClassCommand.class,
                        "ERR_redefine_MissingFile", fname));
            }
        } else {
            // Attempt to locate the .class file using the class itself.
            PathManager pm = PathProvider.getPathManager(session);
            PathEntry pe = pm.findByteCode(clazz);
            if (pe == null) {
                throw new CommandException(NbBundle.getMessage(
                        RedefineClassCommand.class, "ERR_redefine_MissingBytes",
                        cname));
            }
            try {
                stream = pe.getInputStream();
            } catch (IOException ioe) {
                throw new CommandException(NbBundle.getMessage(
                        RedefineClassCommand.class, "ERR_redefine_IOError"), ioe);
            }
        }

        if (stream == null) {
            throw new CommandException(NbBundle.getMessage(
                    RedefineClassCommand.class, "ERR_redefine_MissingBytes",
                    cname));
        }
        try {
            // Perform the class redefinition operation.
            Classes.hotswap(clazz, stream, vm);
        } catch (Throwable t) {
            throw new CommandException(NbBundle.getMessage(
                    RedefineClassCommand.class, "ERR_redefine_Failed",
                    t.toString()), t);
        }
        writer.println(NbBundle.getMessage(RedefineClassCommand.class,
                "CTL_redefine_Redefined"));
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
