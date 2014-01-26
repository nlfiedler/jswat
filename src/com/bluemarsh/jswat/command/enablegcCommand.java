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
 * FILE:        enablegcCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/05/99        Initial version
 *      nf      07/09/01        Corrected the absent info message.
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'enablegc' command.
 *
 * $Id: enablegcCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.VariableUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;

/**
 * Defines the class that handles the 'enablegc' command.
 *
 * @author  Nathan Fiedler
 */
public class enablegcCommand extends JSwatCommand {

    /**
     * Perform the 'enablegc' command.
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
        // Check for enough arguments.
        if (!args.hasMoreTokens()) {
            missingArgs(out);
            return;
        }
        // Get the current thread.
        ThreadReference thread = session.getCurrentThread();
        if (thread == null) {
            out.writeln(Bundle.getString("noCurrentThread"));
            return;
        }

        try {
            String expr = args.nextToken();
            ContextManager ctxtMgr = (ContextManager)
                session.getManager(ContextManager.class);
            Value val = VariableUtils.getValue(expr, thread,
                                               ctxtMgr.getCurrentFrame());
            if (val instanceof ObjectReference) {
                ObjectReference obj = (ObjectReference)val;
                obj.enableCollection();
                out.writeln(swat.getResourceString("collectionEnabled"));
            } else {
                out.writeln(Bundle.getString("fieldNotObject"));
            }
        } catch (AbsentInformationException aie) {
            out.writeln(Bundle.getString("noVariableInfo1") + '\n' +
                        Bundle.getString("noVariableInfo2"));
        } catch (ClassNotPreparedException cnpe) {
	    out.writeln(swat.getResourceString("classNotPrepared"));
        } catch (FieldNotObjectException fnoe) {
            out.writeln(Bundle.getString("fieldNotObject"));
            out.writeln(fnoe.toString());
        } catch (IllegalThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotRunning"));
            out.writeln(itse.toString());
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
        } catch (IndexOutOfBoundsException ioobe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
        } catch (InvalidStackFrameException isfe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
        } catch (NativeMethodException nme) {
            out.writeln(Bundle.getString("nativeMethod"));
        } catch (NoSuchFieldException nsfe) {
            out.writeln(swat.getResourceString("fieldNotFound") + ": " +
                        nsfe.getMessage());
        } catch (ObjectCollectedException oce) {
	    out.writeln(swat.getResourceString("objectCollected"));
        }
    } // perform
} // enablegcCommand
