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
 * $Id: DebuggeeSupportCommand.java 15 2007-06-03 00:01:17Z nfiedler $
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
 * Displays the JDI options that the debuggee supports. This is useful for
 * debugging the debugger.
 *
 * @author Nathan Fiedler
 */
public class DebuggeeSupportCommand extends AbstractCommand {

    public String getName() {
        return "support";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        if (vm.canAddMethod()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canAddMethod"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotAddMethod"));
        }

        if (vm.canGetBytecodes()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetBytecodes"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetBytecodes"));
        }

        if (vm.canGetCurrentContendedMonitor()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetCurrentContendedMonitor"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetCurrentContendedMonitor"));
        }

        if (vm.canGetMonitorInfo()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetMonitorInfo"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetMonitorInfo"));
        }

        if (vm.canGetOwnedMonitorInfo()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetOwnedMonitorInfo"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetOwnedMonitorInfo"));
        }

        if (vm.canGetSourceDebugExtension()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetSourceDebugExtension"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetSourceDebugExtension"));
        }

        if (vm.canGetSyntheticAttribute()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canGetSyntheticAttribute"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotGetSyntheticAttribute"));
        }

        if (vm.canPopFrames()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canPopFrames"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotPopFrames"));
        }

        if (vm.canRedefineClasses()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canRedefineClasses"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotRedefineClasses"));
        }

        if (vm.canRequestVMDeathEvent()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canRequestVMDeathEvent"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotRequestVMDeathEvent"));
        }

        if (vm.canUnrestrictedlyRedefineClasses()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canUnrestrictedlyRedefineClasses"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotUnrestrictedlyRedefineClasses"));
        }

        if (vm.canUseInstanceFilters()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canUseInstanceFilters"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotUseInstanceFilters"));
        }

        if (vm.canWatchFieldAccess()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canWatchFieldAccess"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotWatchFieldAccess"));
        }

        if (vm.canWatchFieldModification()) {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_canWatchFieldModification"));
        } else {
            writer.println(NbBundle.getMessage(getClass(), "CTL_support_cannotWatchFieldModification"));
        }
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
