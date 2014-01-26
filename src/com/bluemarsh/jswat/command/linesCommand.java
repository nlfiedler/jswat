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
 * FILE:        linesCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/05/99        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'lines' command.
 *
 * $Id: linesCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Defines the class that handles the 'lines' command.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/5/99
 */
public class linesCommand extends JSwatCommand {

    /**
     * Perform the 'lines' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Check for active session.
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }
        // Check for missing arguments.
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }

        // Get the name of the class, and possibly method name.
        String idClass = args.nextToken();
        String idMethod = args.hasMoreTokens() ? args.nextToken() : null;
        try {
            List classes = findClassesByPattern(session, idClass);
            if ((classes != null) && (classes.size() > 0)) {
                // Print out line number info for all matching classes.
                for (int i = 0; i < classes.size(); i++) {
                    ReferenceType refType = (ReferenceType)classes.get(i);
                    printLines(refType, idMethod, out);
                }
            } else {
                out.writeln(swat.getResourceString("classNotFound") +
                            ' ' + idClass);
            }
        } catch (AbsentInformationException aie) {
            out.writeln(swat.getResourceString("noLineNumberInfo"));
        } catch (NotActiveException nse) {
            out.writeln(swat.getResourceString("noActiveSession"));
        }
    } // perform

    /**
     * Print the lines out for the given class and method. If
     * method is null, prints out lines for entire class.
     *
     * @param  clazz     Class to find lines for.
     * @param  idMethod  Method to find lines for (can be null).
     * @param  out       Output to write lines to.
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
                Method method = (Method)iter.next();
                if (method.name().equals(idMethod)) {
                    lines.addAll(method.allLineLocations());
                }
            }

            if (lines.size() == 0) {
                out.writeln(Bundle.getString("invalidMethod") +
                            ": " + idMethod);
                return;
            }
        }

        StringBuffer buf = new StringBuffer(256);
        Iterator iter = lines.iterator();
        while (iter.hasNext()) {
            Location line = (Location)iter.next();
            buf.append(line.toString());
            buf.append('\n');
        }
        out.write(buf.toString());
    } // printLines
} // linesCommand
