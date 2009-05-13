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
 * are Copyright (C) 2006-2009. All Rights Reserved.
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
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import org.openide.util.NbBundle;

/**
 * Allows setting the JDI VirtualMachine debug tracing mode, useful for
 * debugging the debugger.
 *
 * @author Nathan Fiedler
 */
public class EventTraceCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "trace";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        String smode = arguments.nextToken();
        int imode = -1;
        if (smode.equals("all")) {
            imode = VirtualMachine.TRACE_ALL;
        } else if (smode.equals("events")) {
            imode = VirtualMachine.TRACE_EVENTS;
        } else if (smode.equals("none")) {
            imode = VirtualMachine.TRACE_NONE;
        } else if (smode.equals("objrefs")) {
            imode = VirtualMachine.TRACE_OBJREFS;
        } else if (smode.equals("receive")) {
            imode = VirtualMachine.TRACE_RECEIVES;
        } else if (smode.equals("reftypes")) {
            imode = VirtualMachine.TRACE_REFTYPES;
        } else if (smode.equals("sends")) {
            imode = VirtualMachine.TRACE_SENDS;
        } else {
            throw new CommandException(NbBundle.getMessage(
                    EventTraceCommand.class, "ERR_trace_UnknownMode", smode));
        }
        vm.setDebugTraceMode(imode);
        writer.write(NbBundle.getMessage(
                EventTraceCommand.class, "CTL_trace_ModeSet", smode));
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
