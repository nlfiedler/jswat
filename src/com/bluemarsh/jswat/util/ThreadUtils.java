/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      Utilities
 * FILE:        ThreadUtils.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/29/02        Initial version
 *
 * DESCRIPTION:
 *      This file defines a thread utility class.
 *
 * $Id: ThreadUtils.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.NotActiveException;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.ThreadReference;
import java.util.List;

/**
 * Provides utility methods for handling threads.
 *
 * @author  Nathan Fiedler
 */
public class ThreadUtils {

    /**
     * Finds a thread in the list of all threads whose unique identifier
     * matches that given.
     *
     * @param  session     Session in which to look for threads.
     * @param  identifier  thread identifier, either a decimal number
     *                     or a String name.
     * @return  ThreadReference, or null if not found.
     * @exception  NotActiveException
     *             Thrown if the current session is not active.
     */
    public static ThreadReference getThreadByID(Session session,
                                                String identifier)
        throws NotActiveException {

        if (!session.isActive()) {
            throw new NotActiveException();
        }

        List threads = session.getConnection().getVM().allThreads();
        ThreadReference thread = null;
        try {
            // Compare the identifier as if it were a number.
            long threadID = Long.parseLong(identifier);
            for (int i = threads.size() - 1; i >= 0; i--) {
                ThreadReference th = (ThreadReference) threads.get(i);
                if (th.uniqueID() == threadID) {
                    thread = th;
                    break;
                }
            }

        } catch (NumberFormatException nfe) {
            // Compare the identifier as if it were a name.
            for (int i = threads.size() - 1; i >= 0; i--) {
                ThreadReference th = (ThreadReference) threads.get(i);
                if (th.name().equals(identifier)) {
                    thread = th;
                    break;
                }
            }
        }
        return thread;
    } // getThreadByID
} // ThreadUtils
