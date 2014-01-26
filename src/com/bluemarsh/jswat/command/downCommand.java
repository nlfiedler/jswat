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
 * FILE:        downCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/16/99        Initial version
 *      nf      09/03/01        Handle error cases
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'down' command.
 *
 * $Id: downCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.IncompatibleThreadStateException;

/**
 * Defines the class that handles the 'down' command.
 *
 * @author  Nathan Fiedler
 */
public class downCommand extends JSwatCommand {

    /**
     * Perform the 'down' command.
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

        // Default number # of frames to move down.
        int count = 1;
        // Check for optional arguments.
        if (args.hasMoreTokens()) {
            // Get the value of the stack frame index.
            try {
                count = Integer.parseInt(args.nextToken());
            } catch (NumberFormatException nfe) {
                out.writeln(Bundle.getString("invalidStackFrame"));
                return;
            }
        }

        // Try to set the new current frame index.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        if (ctxtMgr.getCurrentThread() == null) {
            out.writeln(Bundle.getString("noCurrentThread"));
            return;
        }
        try {
            ctxtMgr.setCurrentFrame(ctxtMgr.getCurrentFrame() - count);
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
        } catch (IndexOutOfBoundsException ioobe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
        }
    } // perform
} // downCommand
