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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EventTraceCommand.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Allows setting the JDI VirtualMachine debug tracing mode, useful for
 * debugging the debugger.
 *
 * @author Nathan Fiedler
 */
public class EventTraceCommand extends AbstractCommand {

    public String getName() {
        return "trace";
    }

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

    public boolean requiresArguments() {
        return true;
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
