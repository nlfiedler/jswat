/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        methodsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/16/99        Initial version
 *      nf      09/03/01        Improved missing args message
 *      nf      09/06/01        Fixed bug #228
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'methods' command.
 *
 * $Id: methodsCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'methods' command.
 *
 * @author  Nathan Fiedler
 */
public class methodsCommand extends JSwatCommand {

    /**
     * Perform the 'methods' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
	if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        // Make sure we got the class name argument.
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }
        String idClass = args.restTrim();

        // Find all matching classes.
        try {
            List classes = findClassesByPattern(session, idClass);
            if ((classes != null) && (classes.size() > 0)) {
                // For each matching class, print its methods.
                StringBuffer buf = new StringBuffer(256);
                Iterator iter = classes.iterator();
                while (iter.hasNext()) {
                    printMethods((ReferenceType) iter.next(), buf);
                    if (iter.hasNext()) {
                        // Print a separator between the classes.
                        buf.append("-----------------------------------");
                        buf.append('\n');
                    }
                }
                out.write(buf.toString());
            } else {
                out.write(swat.getResourceString("classNotFound"));
                out.write(" ");
                out.writeln(idClass);
            }
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform

    /**
     * Print the methods of the given class type.
     *
     * @param  clazz  ReferenceType
     * @param  buf    sink to write to.
     */
    protected void printMethods(ReferenceType clazz, StringBuffer buf) {
        // Display the class name first.
        buf.append("Class ");
        buf.append(clazz.name());
        buf.append(":");
        buf.append('\n');
        List methods = clazz.allMethods();
        for (int i = 0; i < methods.size(); i++) {
            Method method = (Method)methods.get(i);
            // Show the return type and method name.
            String returnType = method.returnTypeName();
            returnType = returnType.substring
                (returnType.lastIndexOf('.') + 1);
            buf.append(returnType);
            buf.append(' ');
            buf.append(method.name());
            buf.append('(');
            // For each parameter, show the parameter type.
            Iterator iter = method.argumentTypeNames().iterator();
            while (iter.hasNext()) {
                buf.append((String) iter.next());
                if (iter.hasNext()) {
                    buf.append(", ");
                }
            }
            buf.append(')');
            buf.append('\n');
        }
    } // printMethods
} // methodsCommand
