/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: Threads.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.List;

/**
 * Provides utility methods for handling threads.
 *
 * @author  Nathan Fiedler
 */
public class Threads {

    /**
     * Finds a thread in the list of all threads whose unique identifier
     * matches that given.
     *
     * @param  vm   debuggee virtual machine.
     * @param  tid  thread identifier (decimal number or name).
     * @return  ThreadReference, or null if not found.
     */
    public static ThreadReference getThreadByID(VirtualMachine vm,
                                                String tid) {

        List threads = vm.allThreads();
        Iterator iter = threads.iterator();
        ThreadReference thread = null;
        try {
            // Compare the identifier as if it were a number.
            long threadID = Long.parseLong(tid);
            while (iter.hasNext()) {
                ThreadReference th = (ThreadReference) iter.next();
                if (th.uniqueID() == threadID) {
                    thread = th;
                    break;
                }
            }

        } catch (NumberFormatException nfe) {
            // Compare the identifier as if it were a name.
            while (iter.hasNext()) {
                ThreadReference th = (ThreadReference) iter.next();
                if (th.name().equals(tid)) {
                    thread = th;
                    break;
                }
            }
        }
        return thread;
    } // getThreadByID

    /**
     * Return a one-word description of the thread status.
     *
     * @param  thread  thread from which to get status.
     * @return  string description of status.
     */
    public static String threadStatus(ThreadReference thread) {
        String desc;
        switch (thread.status()) {
        case ThreadReference.THREAD_STATUS_MONITOR:
            desc = Bundle.getString("threadStatusMonitor");
            break;
        case ThreadReference.THREAD_STATUS_RUNNING:
            desc = Bundle.getString("threadStatusRunning");
            break;
        case ThreadReference.THREAD_STATUS_SLEEPING:
            desc = Bundle.getString("threadStatusSleeping");
            break;
        case ThreadReference.THREAD_STATUS_WAIT:
            desc = Bundle.getString("threadStatusWait");
            break;
        case ThreadReference.THREAD_STATUS_ZOMBIE:
            desc = Bundle.getString("threadStatusZombie");
            break;
        case ThreadReference.THREAD_STATUS_NOT_STARTED:
            desc = Bundle.getString("threadStatusNotStarted");
            break;
        default:
            desc = Bundle.getString("threadStatusUnknown");
            break;
        }
        return desc;
    } // threadStatus
} // Threads
