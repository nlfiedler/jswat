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
 * FILE:        classCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/30/99        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'class' command.
 *
 * $Id: classCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.ReferenceType;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'class' command.
 *
 * @author  Nathan Fiedler
 */
public class classCommand extends JSwatCommand {

    /**
     * Perform the 'class' command.
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
                boolean showAll = false;
                if (idClass.equals("all")) {
                    showAll = true;
                    if (!args.hasMoreTokens()) {
                        missingArgs(out);
                        return;
                    }
                    idClass = args.nextToken();
                }
                StringBuffer buf = new StringBuffer(256);
                List classes = findClassesByPattern(session, idClass);
                if ((classes != null) && (classes.size() > 0)) {
                    // For each matching class, print its methods.
                    Iterator iter = classes.iterator();
                    while (iter.hasNext()) {
                        printClass((ReferenceType) iter.next(), showAll, buf);
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
     * Print information about a class, interface, or array.
     *
     * @param  type     ReferenceType to display.
     * @param  showAll  True to show all superclasses.
     * @param  buf      sink for output.
     */
    protected void printClass(ReferenceType type, boolean showAll,
                              StringBuffer buf) {
        if (type instanceof ArrayType) {
            buf.append("Array: ");
            buf.append(type.name());
            buf.append('\n');
            return;
        }

        // This code is pretty much from the early JPDA example.
        if (type instanceof ClassType) {
            ClassType clazz = (ClassType) type;
            buf.append("Class: ");
            buf.append(clazz.name());
            buf.append('\n');
            ClassType superclass = clazz.superclass();
            while (superclass != null) {
                buf.append("extends: ");
                buf.append(superclass.name());
                buf.append('\n');
                superclass = showAll ? superclass.superclass() : null;
            }
            List interfaces = showAll ? clazz.allInterfaces() 
                                      : clazz.interfaces();
            Iterator iter = interfaces.iterator();
            while (iter.hasNext()) {
                InterfaceType interfaze = (InterfaceType) iter.next();
                buf.append("implements: ");
                buf.append(interfaze.name());
                buf.append('\n');
            }
            List subs = clazz.subclasses();
            iter = subs.iterator();
            while (iter.hasNext()) {
                ClassType sub = (ClassType) iter.next();
                buf.append("subclass: ");
                buf.append(sub.name());
                buf.append('\n');
            }

            // Display any inner classes of this type.
            List nested = clazz.nestedTypes();
            iter = nested.iterator();
            while (iter.hasNext()) {
                ReferenceType nest = (ReferenceType) iter.next();
                buf.append("nested: ");
                buf.append(nest.name());
                buf.append('\n');
            }

        } else if (type instanceof InterfaceType) {
            InterfaceType interfaze = (InterfaceType) type;
            buf.append("Interface: ");
            buf.append(interfaze.name());
            buf.append('\n');
            List supers = interfaze.superinterfaces();
            Iterator iter = supers.iterator();
            while (iter.hasNext()) {
                InterfaceType superinterface = (InterfaceType) iter.next();
                buf.append("extends: ");
                buf.append(superinterface.name());
                buf.append('\n');
            }
            List subs = interfaze.subinterfaces();
            iter = subs.iterator();
            while (iter.hasNext()) {
                InterfaceType sub = (InterfaceType) iter.next();
                buf.append("subinterface: ");
                buf.append(sub.name());
                buf.append('\n');
            }
            List implementors = interfaze.implementors();
            iter = implementors.iterator();
            while (iter.hasNext()) {
                ClassType implementor = (ClassType) iter.next();
                buf.append("implementor: ");
                buf.append(implementor.name());
                buf.append('\n');
            }

            // Display any inner classes of this type.
            List nested = interfaze.nestedTypes();
            iter = nested.iterator();
            while (iter.hasNext()) {
                ReferenceType nest = (ReferenceType) iter.next();
                buf.append("nested: ");
                buf.append(nest.name());
                buf.append('\n');
            }
        }
    } // printClass
} // classCommand
