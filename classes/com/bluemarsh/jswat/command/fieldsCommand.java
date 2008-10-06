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
 * FILE:        fieldsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/02/99        Initial version
 *      nf      07/13/02        Implemented RFE 430
 *
 * $Id: fieldsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the class that handles the 'fields' command.
 *
 * @author  Nathan Fiedler
 */
public class fieldsCommand extends JSwatCommand {

    /**
     * Perform the 'fields' command.
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

        VirtualMachine vm = session.getVM();
        vm.suspend();
        List classes = vm.allClasses();
        vm.resume();

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

        boolean found = false;
        StringBuffer buf = new StringBuffer(256);
        if (classes != null && classes.size() > 0) {
            // For each matching class, print its methods.
            Iterator iter = classes.iterator();
            while (iter.hasNext()) {
                found = true;
                printFields((ReferenceType) iter.next(), buf);
                if (iter.hasNext()) {
                    // Print a separator between the classes.
                    buf.append("------------------------------");
                    buf.append("------------------------------");
                    buf.append('\n');
                }
            }
        } else {
            buf.append(Bundle.getString("classNotFound"));
            buf.append(' ');
            buf.append(regex);
            buf.append('\n');
        }
        if (!found) {
            throw new CommandException(Bundle.getString("fields.nomatch"));
        }
        out.write(buf.toString());
    } // perform

    /**
     * Print the fields of the given class type.
     *
     * @param  clazz  ReferenceType to display.
     * @param  buf    sink to write to.
     */
    protected void printFields(ReferenceType clazz, StringBuffer buf) {
        List fields = clazz.allFields();
        List visible = clazz.visibleFields();
        buf.append("Class: ");
        buf.append(clazz.name());
        buf.append('\n');
        for (int i = 0; i < fields.size(); i++) {
            // This looks a lot like tty/Commands.commandFields().
            Field field = (Field) fields.get(i);
            buf.append(field.typeName());
            buf.append(' ');
            buf.append(field.name());
            if (!visible.contains(field)) {
                buf.append(" (hidden)");
            } else if (!field.declaringType().equals(clazz)) {
                buf.append(" (inherited from ");
                buf.append(field.declaringType().name());
                buf.append(')');
            }
            buf.append('\n');
        }
    } // printFields
} // fieldsCommand
