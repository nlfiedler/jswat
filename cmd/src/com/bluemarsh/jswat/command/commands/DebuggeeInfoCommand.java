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
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Displays information regarding the connected debuggee.
 *
 * @author Nathan Fiedler
 */
public class DebuggeeInfoCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "vminfo";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();
        StringBuilder sb = new StringBuilder();

        // Display the Java VM version.
        sb.append(NbBundle.getMessage(DebuggeeInfoCommand.class,
                "CTL_vminfo_version"));
        sb.append(' ');
        sb.append(vm.version());
        sb.append("\n\n");

        // Display classpath information.
        if (vm instanceof PathSearchingVirtualMachine) {
            PathSearchingVirtualMachine psvm =
                    (PathSearchingVirtualMachine) vm;
            sb.append(NbBundle.getMessage(DebuggeeInfoCommand.class,
                    "CTL_vminfo_basedir"));
            sb.append('\n');
            sb.append(psvm.baseDirectory());
            sb.append("\n\n");
            sb.append(NbBundle.getMessage(DebuggeeInfoCommand.class,
                    "CTL_vminfo_cpath"));
            sb.append('\n');
            sb.append(Strings.listToString(psvm.classPath(), "\n"));
            sb.append("\n\n");
            sb.append(NbBundle.getMessage(DebuggeeInfoCommand.class,
                    "CTL_vminfo_bcpath"));
            sb.append('\n');
            sb.append(Strings.listToString(psvm.bootClassPath(), "\n"));
            sb.append('\n');
        }

        // Display the default stratum.
        sb.append('\n');
        sb.append(NbBundle.getMessage(DebuggeeInfoCommand.class,
                "CTL_vminfo_stratum"));
        sb.append(' ');
        sb.append(vm.getDefaultStratum());
        writer.println(sb.toString());
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }
}
