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
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'classes' command.
 *
 * $Id: classesCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.List;

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
    public void perform(Session session, StringTokenizer args, Log out) {
	if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        List classes;
        if (args.hasMoreTokens()) {
            String pattern = args.nextToken();
            try {
                classes = findClassesByPattern(session, pattern);
            } catch (NotActiveException nae) {
                // won't happen
                classes = null;
            }
        } else {
            VirtualMachine vm = session.getVM();
            vm.suspend();
            classes = vm.allClasses();
            vm.resume();
        }

        Iterator iter = classes.iterator();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                ReferenceType clazz = (ReferenceType) iter.next();
                out.writeln(clazz.name());
            }
        } else {
            out.writeln(Bundle.getString("classes.noneLoaded"));
        }
    } // perform
} // classesCommand
