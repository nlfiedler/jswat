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
 * FILE:        upCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/16/99        Initial version
 *      nf      09/03/01        Improved error messages
 *      nf      04/25/02        Fixed bug #485
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'up' command.
 *
 * $Id: upCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.IncompatibleThreadStateException;

/**
 * Defines the class that handles the 'up' command.
 *
 * @author  Nathan Fiedler
 */
public class upCommand extends JSwatCommand {

    /**
     * Perform the 'up' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        // Default number # of frames to move down.
        int count = 1;
        // Check for optional arguments.
        if (args.hasMoreTokens()) {
            // Get the value of the stack frame index.
            try {
                count = Integer.parseInt(args.nextToken());
            } catch (NumberFormatException nfe) {
                throw new CommandException(
                    Bundle.getString("invalidStackFrame"));
            }
        }

        // Try to set the new current frame index.
        ContextManager ctxtMgr = (ContextManager)
            session.getManager(ContextManager.class);
        if (ctxtMgr.getCurrentThread() == null) {
            throw new CommandException(Bundle.getString("noCurrentThread"));
        }
        try {
            ctxtMgr.setCurrentFrame(ctxtMgr.getCurrentFrame() + count);
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(Bundle.getString("threadNotSuspended"));
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"));
        }
    } // perform
} // upCommand
