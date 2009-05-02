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
import com.bluemarsh.jswat.core.util.Threads;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.VirtualMachine;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Displays all of the thread groups in the debuggee.
 *
 * @author Nathan Fiedler
 */
public class ThreadGroupsCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "threadgroups";
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        if (arguments.hasMoreTokens()) {
            long tid = -1;
            String name = arguments.nextToken();
            // Is it a thread group name or unique ID?
            try {
                tid = Long.parseLong(name);
            } catch (NumberFormatException nfe) {
                // It's not a thread id then.
            }

            List<ThreadGroupReference> groupsList = null;
            // Look for a thread group with this name or id.
            Iterator<ThreadGroupReference> iter =
                    Threads.iterateGroups(vm.topLevelThreadGroups());
            if (tid > -1) {
                while (iter.hasNext()) {
                    ThreadGroupReference group = iter.next();
                    if (group.uniqueID() == tid) {
                        groupsList = group.threadGroups();
                        break;
                    }
                }

            } else {
                groupsList = new ArrayList<ThreadGroupReference>();
                Pattern patt = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
                while (iter.hasNext()) {
                    ThreadGroupReference group = iter.next();
                    Matcher matcher = patt.matcher(group.name());
                    if (matcher.find()) {
                        groupsList.addAll(group.threadGroups());
                    } else {
                        String idstr = String.valueOf(group.uniqueID());
                        matcher = patt.matcher(idstr);
                        if (matcher.find()) {
                            groupsList.addAll(group.threadGroups());
                        }
                    }
                }
            }

            if (groupsList == null || groupsList.size() == 0) {
                writer.println(NbBundle.getMessage(getClass(),
                        "CTL_threadgroups_NoGroupsInGroup"));
            } else if (groupsList.size() > 0) {
                iter = groupsList.iterator();
                while (iter.hasNext()) {
                    ThreadGroupReference group = iter.next();
                    printGroup(group, writer, "");
                }
            }

        } else {
            // Print all of the thread groups and their threads.
            List topGroups = vm.topLevelThreadGroups();
            if (topGroups == null || topGroups.size() == 0) {
                writer.println(NbBundle.getMessage(getClass(),
                        "CTL_threadgroups_NoGroups"));
            } else if (topGroups.size() > 0) {
                Iterator iter = topGroups.iterator();
                while (iter.hasNext()) {
                    ThreadGroupReference group =
                        (ThreadGroupReference) iter.next();
                    printGroup(group, writer, "");
                }
            }
        }
    }


    /**
     * Print the thread group to the output with each line prefixed
     * by the given string.
     *
     * @param  group    thread group to print.
     * @param  current  current thread.
     * @param  writer   writer to print to.
     * @param  prefix   string to display before each line.
     */
    private void printGroup(ThreadGroupReference group, PrintWriter writer,
            String prefix) {
        ReferenceType clazz = group.referenceType();
        String id = String.valueOf(group.uniqueID());
        if (clazz == null) {
            writer.println(prefix + id + ' ' + group.name());
        } else {
            writer.println(prefix + id + ' ' + group.name() + " (" + clazz.name() + ')');
        }

        // Now traverse this group's subgroups.
        List<ThreadGroupReference> groups = group.threadGroups();
        Iterator<ThreadGroupReference> iter = groups.iterator();
        while (iter.hasNext()) {
            ThreadGroupReference subgrp = iter.next();
            printGroup(subgrp, writer, prefix + "  ");
        }
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }
}
