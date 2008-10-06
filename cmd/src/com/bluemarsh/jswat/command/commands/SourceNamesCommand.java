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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SourceNamesCommand.java 15 2007-06-03 00:01:17Z nfiedler $
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
 * Displays source file related information for a class.
 *
 * @author Nathan Fiedler
 */
public class SourceNamesCommand extends AbstractCommand {

    public String getName() {
        return "source";
    }

    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        Session session = context.getSession();
        PrintWriter writer = context.getWriter();
        VirtualMachine vm = session.getConnection().getVM();

        // Prepare for the search.
        String cname = arguments.nextToken();
        List<ReferenceType> classes = vm.allClasses();

        // Perform the search.
        Iterator<ReferenceType> iter = classes.iterator();
        boolean found = false;
        StringBuilder sb = new StringBuilder(512);
        while (iter.hasNext()) {
            ReferenceType clazz = iter.next();
            if (cname.equals(clazz.name())) {
                found = true;
                printClass(clazz, sb);
                break;
            }
        }
        if (!found) {
            throw new CommandException(NbBundle.getMessage(getClass(),
                    "ERR_source_NoMatch", cname));
        }
        writer.write(sb.toString());
    }

    /**
     * Print information about the sources for a class.
     *
     * @param  type  ReferenceType to display.
     * @param  sb    sink for output.
     */
    private static void printClass(ReferenceType type, StringBuilder sb) {
        String absent = NbBundle.getMessage(SourceNamesCommand.class,
                "CTL_source_AbsentInfo");
        String val;
        try {
            val = type.sourceName();
        } catch (AbsentInformationException aie) {
            val = absent;
        }
        sb.append(NbBundle.getMessage(SourceNamesCommand.class,
                "CTL_source_SourceName", val));
        sb.append('\n');
        VirtualMachine vm = type.virtualMachine();
        if (vm.canGetSourceDebugExtension()) {
            sb.append(NbBundle.getMessage(SourceNamesCommand.class,
                "CTL_source_DefaultStratum", type.defaultStratum()));
            sb.append('\n');
            try {
                val = type.sourceDebugExtension();
            } catch (AbsentInformationException aie) {
                val = absent;
            }
            sb.append(NbBundle.getMessage(SourceNamesCommand.class,
                    "CTL_source_DebugExt", val));
            sb.append('\n');
            sb.append(NbBundle.getMessage(SourceNamesCommand.class,
                    "CTL_source_SourcePaths"));
            try {
                List paths = type.sourcePaths(null);
                Iterator iter = paths.iterator();
                while (iter.hasNext()) {
                    sb.append("   ");
                    sb.append(iter.next().toString());
                    sb.append('\n');
                }
            } catch (AbsentInformationException aie) {
                sb.append(absent);
                sb.append('\n');
            }
        } else {
            sb.append(NbBundle.getMessage(SourceNamesCommand.class,
                "CTL_source_cannotGetSourceExtension"));
            sb.append('\n');
        }
    }

    public boolean requiresArguments() {
        return true;
    }

    public boolean requiresDebuggee() {
        return true;
    }
}
