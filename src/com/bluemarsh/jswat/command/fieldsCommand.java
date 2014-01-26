/*********************************************************************
 *
 *      Copyright (C) 1999 Nathan Fiedler
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
 *      nf      9/2/99          Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'fields' command.
 *
 * $Id: fieldsCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'fields' command.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/2/99
 */
public class fieldsCommand extends JSwatCommand {

    /**
     * Perform the 'fields' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        try {
            if (!args.hasMoreTokens()) {
                missingArgs(out);
            } else {
                String idClass = args.nextToken();
                List classes = findClassesByPattern(session, idClass);
                StringBuffer buf = new StringBuffer(256);
                if ((classes != null) && (classes.size() > 0)) {
                    // For each matching class, print its methods.
                    Iterator iter = classes.iterator();
                    while (iter.hasNext()) {
                        printFields((ReferenceType) iter.next(), buf);
                        if (iter.hasNext()) {
                            // Print a separator between the classes.
                            buf.append("-----------------------------------");
                            buf.append('\n');
                        }
                    }
                } else {
                    buf.append(swat.getResourceString("classNotFound"));
                    buf.append(' ');
                    buf.append(idClass);
                    buf.append('\n');
                }
                out.write(buf.toString());
            }
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform

    /**
     * Print the fields of the given class type.
     *
     * @param  clazz  ReferenceType
     * @param  buf    sink to write to.
     */
    protected void printFields(ReferenceType clazz, StringBuffer buf) {
        List fields = clazz.allFields();
        List visible = clazz.visibleFields();
        for (int i = 0; i < fields.size(); i++) {
            // This looks a lot like tty/Commands.commandFields().
            Field field = (Field)fields.get(i);
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
