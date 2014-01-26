/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:        classesCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/16/99        Initial version
 *      nf      09/03/01        Use list iterator
 *      nf      01/12/02        Implemented request 50
 *      nf      07/13/02        Implemented RFE 430
 *      nf      12/23/02        Include class loader info
 *
 * $Id: classesCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the class that handles the 'classes' command.
 *
 * @author  Nathan Fiedler
 */
public class classesCommand extends JSwatCommand {

    /**
     * Perform the 'classes' command.
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
        vm.suspend();
        List classes = vm.allClasses();
        vm.resume();
        if (args.hasMoreTokens()) {
            // Their list is unmodifiable for some reason.
            classes = new ArrayList(classes);
            String regex = args.nextToken();
            Pattern patt = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            ListIterator liter = classes.listIterator();
            while (liter.hasNext()) {
                ReferenceType clazz = (ReferenceType) liter.next();
                Matcher matcher = patt.matcher(clazz.name());
                if (!matcher.find()) {
                    liter.remove();
                }
            }
        }

        Iterator iter = classes.iterator();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                ReferenceType clazz = (ReferenceType) iter.next();
                out.write(clazz.name());
                out.write(" [");
                ClassLoaderReference clr = clazz.classLoader();
                if (clr != null) {
                    out.write(clr.referenceType().name());
                    out.write(" (");
                    out.write(String.valueOf(clr.uniqueID()));
                    out.write(")");
                } else {
                    out.write(Bundle.getString("classes.noClassLoader"));
                }
                out.writeln("]");
            }
        } else {
            throw new CommandException(Bundle.getString("classes.noneLoaded"));
        }
    } // perform
} // classesCommand
