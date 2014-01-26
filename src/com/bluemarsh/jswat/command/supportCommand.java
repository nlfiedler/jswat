/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      JSwat Commands
 * FILE:        supportCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/06/03        Initial version
 *
 * $Id: supportCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.VirtualMachine;

/**
 * Defines the class that handles the 'support' command.
 *
 * @author  Nathan Fiedler
 */
public class supportCommand extends JSwatCommand {

    /**
     * Perform the 'support' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        VirtualMachine vm = session.getVM();
        if (vm.canAddMethod()) {
            out.writeln(Bundle.getString("support.canAddMethod"));
        } else {
            out.writeln(Bundle.getString("support.cannotAddMethod"));
        }

        if (vm.canGetBytecodes()) {
            out.writeln(Bundle.getString("support.canGetBytecodes"));
        } else {
            out.writeln(Bundle.getString("support.cannotGetBytecodes"));
        }

        if (vm.canGetCurrentContendedMonitor()) {
            out.writeln(Bundle.getString(
                            "support.canGetCurrentContendedMonitor"));
        } else {
            out.writeln(Bundle.getString(
                            "support.cannotGetCurrentContendedMonitor"));
        }

        if (vm.canGetMonitorInfo()) {
            out.writeln(Bundle.getString("support.canGetMonitorInfo"));
        } else {
            out.writeln(Bundle.getString("support.cannotGetMonitorInfo"));
        }

        if (vm.canGetOwnedMonitorInfo()) {
            out.writeln(Bundle.getString("support.canGetOwnedMonitorInfo"));
        } else {
            out.writeln(Bundle.getString("support.cannotGetOwnedMonitorInfo"));
        }

        if (vm.canGetSourceDebugExtension()) {
            out.writeln(Bundle.getString(
                            "support.canGetSourceDebugExtension"));
        } else {
            out.writeln(Bundle.getString(
                            "support.cannotGetSourceDebugExtension"));
        }

        if (vm.canGetSyntheticAttribute()) {
            out.writeln(Bundle.getString("support.canGetSyntheticAttribute"));
        } else {
            out.writeln(Bundle.getString(
                            "support.cannotGetSyntheticAttribute"));
        }

        if (vm.canPopFrames()) {
            out.writeln(Bundle.getString("support.canPopFrames"));
        } else {
            out.writeln(Bundle.getString("support.cannotPopFrames"));
        }

        if (vm.canRedefineClasses()) {
            out.writeln(Bundle.getString("support.canRedefineClasses"));
        } else {
            out.writeln(Bundle.getString("support.cannotRedefineClasses"));
        }

        if (vm.canRequestVMDeathEvent()) {
            out.writeln(Bundle.getString("support.canRequestVMDeathEvent"));
        } else {
            out.writeln(Bundle.getString("support.cannotRequestVMDeathEvent"));
        }

        if (vm.canUnrestrictedlyRedefineClasses()) {
            out.writeln(Bundle.getString(
                            "support.canUnrestrictedlyRedefineClasses"));
        } else {
            out.writeln(Bundle.getString(
                            "support.cannotUnrestrictedlyRedefineClasses"));
        }

        if (vm.canUseInstanceFilters()) {
            out.writeln(Bundle.getString("support.canUseInstanceFilters"));
        } else {
            out.writeln(Bundle.getString("support.cannotUseInstanceFilters"));
        }

        if (vm.canWatchFieldAccess()) {
            out.writeln(Bundle.getString("support.canWatchFieldAccess"));
        } else {
            out.writeln(Bundle.getString("support.cannotWatchFieldAccess"));
        }

        if (vm.canWatchFieldModification()) {
            out.writeln(Bundle.getString("support.canWatchFieldModification"));
        } else {
            out.writeln(Bundle.getString(
                            "support.cannotWatchFieldModification"));
        }
    } // perform
} // supportCommand
