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
 * $Id: linesCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Classes;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines the class that handles the 'lines' command.
 *
 * @author  Nathan Fiedler
 */
public class linesCommand extends JSwatCommand {

    /**
     * Perform the 'lines' command.
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

        // Get the name of the class, and possibly method name.
        String idClass = args.nextToken();
        String idMethod = args.hasMoreTokens() ? args.nextToken() : null;
        String loaderId = args.hasMoreTokens() ? args.nextToken() : null;
        long lid = -1;
        if (loaderId != null) {
            try {
                lid = Long.parseLong(loaderId);
            } catch (NumberFormatException nfe) {
                throw new CommandException(
                    Bundle.getString("lines.invalidLoaderId"));
            }
        } else if (idMethod != null) {
            // See if the second argument is a method name or loader ID.
            try {
                lid = Long.parseLong(idMethod);
                // It is a class loader ID.
                loaderId = idMethod;
                idMethod = null;
            } catch (NumberFormatException nfe) {
                // It is a method name.
            }
        }

        VirtualMachine vm = session.getConnection().getVM();
        List classes =  Classes.findClassesByPattern(vm, idClass);
        try {
            if (classes != null && classes.size() > 0) {
                // Print out line number info for all matching classes.
                int size = classes.size();
                int size1 = size - 1;
                for (int ii = 0; ii < size; ii++) {
                    boolean doPrint = true;
                    ReferenceType refType = (ReferenceType) classes.get(ii);
                    if (loaderId != null) {
                        // Only print this class if it's loader matches.
                        ClassLoaderReference clr = refType.classLoader();
                        if (clr != null && clr.uniqueID() != lid) {
                            doPrint = false;
                        }
                    }
                    if (doPrint) {
                        printLines(refType, idMethod, out);
                        if (ii < size1) {
                            out.writeln("");
                        }
                    }
                }
            } else {
                throw new CommandException(
                    Bundle.getString("classNotFound") + ' ' + idClass);
            }
        } catch (AbsentInformationException aie) {
            throw new CommandException(
                Bundle.getString("lines.noLineNumberInfo"));
        }
    } // perform

    /**
     * Print the lines out for the given class and method. If method is
     * null, prints out lines for entire class.
     *
     * @param  clazz     Class to find lines for.
     * @param  idMethod  Method to find lines for (can be null).
     * @param  out       Output to write lines to.
     * @throws  AbsentInformationException
     *          if class was not compiled with debugging information.
     */
    protected void printLines(ReferenceType clazz, String idMethod, Log out)
        throws AbsentInformationException {
        List lines = null;
        if (idMethod == null) {
            // No method, get all lines of class.
            lines = clazz.allLineLocations();
        } else {
            lines = new LinkedList();

            // Find the matching methods.
            List methods = clazz.allMethods();
            Iterator iter = methods.iterator();
            while (iter.hasNext()) {
                Method method = (Method) iter.next();
                if (method.name().equals(idMethod)) {
                    lines.addAll(method.allLineLocations());
                }
            }

            if (lines.size() == 0) {
                throw new CommandException(Bundle.getString("invalidMethod")
                                           + ": " + idMethod);
            }
        }

        StringBuffer buf = new StringBuffer(256);
        ClassLoaderReference clr = clazz.classLoader();
        if (clr != null) {
            buf.append(clr.referenceType().name());
            buf.append(" (");
            buf.append(clr.uniqueID());
            buf.append(")\n");
        }
        Iterator iter = lines.iterator();
        while (iter.hasNext()) {
            Location line = (Location) iter.next();
            buf.append(line.toString());
            buf.append('\n');
        }
        out.write(buf.toString());
    } // printLines
} // linesCommand
