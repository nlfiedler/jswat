/*********************************************************************
 *
 *      Copyright (C) 1999-2003 Nathan Fiedler
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
 * FILE:        threadgroupsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/16/99        Initial version
 *      nf      01/12/02        Yanked ThreadGroupIterator into new file.
 *      nf      04/26/02        Changed from hex to decimal
 *
 * $Id: threadgroupsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'threadgroups' command.
 *
 * @author  Nathan Fiedler
 */
public class threadgroupsCommand extends JSwatCommand {

    /**
     * Perform the 'threadgroups' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        // Iterate the top-level groups, traversing those recursively.
        List topGroups = session.getVM().topLevelThreadGroups();
        Iterator iter = topGroups.iterator();
        while (iter.hasNext()) {
            ThreadGroupReference group = (ThreadGroupReference) iter.next();
            printGroup(group, out, "");
        }
    } // perform

    /**
     * Print the thread group to the output with each line prefixed
     * by the given string.
     *
     * @param  group   thread group to print.
     * @param  out     place to print to.
     * @param  prefix  string to display before each line.
     */
    protected void printGroup(ThreadGroupReference group, Log out,
                              String prefix) {
        // See if the thread group has a class type.
        ReferenceType clazz = group.referenceType();
        String id = String.valueOf(group.uniqueID());
        if (clazz == null) {
            // No class type, show just the ID and group name.
            out.writeln(prefix + id + ' ' + group.name());
        } else {
            // Show ID, group name, and class name.
            out.writeln(prefix + id + ' ' + group.name()
                        + " (" + clazz.name() + ')');
        }

        // Now traverse this group's subgroups.
        List groups = group.threadGroups();
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            ThreadGroupReference subgrp = (ThreadGroupReference) iter.next();
            printGroup(subgrp, out, prefix + "  ");
        }
    } // printGroup
} // threadgroupsCommand
