/*********************************************************************
 *
 *      Copyright (C) 2005 Nathan Fiedler
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
 * $Id: sourceCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the class that handles the 'source' command.
 *
 * @author  Nathan Fiedler
 */
public class sourceCommand extends JSwatCommand {

    /**
     * Perform the 'source' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // Get the arguments.
        String regex = args.nextToken();

        // Prepare for the search.
        Pattern patt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        VirtualMachine vm = session.getVM();
        vm.suspend();
        List classes = vm.allClasses();
        vm.resume();

        // Perform the search.
        Iterator iter = classes.iterator();
        boolean found = false;
        StringBuffer buf = new StringBuffer(512);
        while (iter.hasNext()) {
            ReferenceType clazz = (ReferenceType) iter.next();
            Matcher matcher = patt.matcher(clazz.name());
            if (matcher.find()) {
                found = true;
                // For each matching class, print the source information.
                printClass(clazz, buf);
                if (iter.hasNext()) {
                    buf.append('\n');
                }
            }
        }
        if (!found) {
            throw new CommandException(Bundle.getString("class.nomatch"));
        }
        out.write(buf.toString());
    }

    /**
     * Print information about the sources for a class.
     *
     * @param  type  ReferenceType to display.
     * @param  buf   sink for output.
     */
    protected void printClass(ReferenceType type, StringBuffer buf) {
        buf.append("Source name: ");
        try {
            buf.append(type.sourceName());
        } catch (AbsentInformationException aie) {
            buf.append("<absent information>");
        }
        buf.append('\n');
        VirtualMachine vm = type.virtualMachine();
        if (vm.canGetSourceDebugExtension()) {
            buf.append("Default stratum: ");
            buf.append(type.defaultStratum());
            buf.append('\n');
            buf.append("Source debug extension: ");
            try {
                buf.append(type.sourceDebugExtension());
            } catch (AbsentInformationException aie) {
                buf.append("<absent information>");
            }
            buf.append('\n');
            buf.append("Source paths:\n");
            try {
                List paths = type.sourcePaths(null);
                Iterator iter = paths.iterator();
                while (iter.hasNext()) {
                    buf.append("   ");
                    buf.append(iter.next().toString());
                    buf.append('\n');
                }
            } catch (AbsentInformationException aie) {
                buf.append("<absent information>\n");
            }
        }
    }
}
