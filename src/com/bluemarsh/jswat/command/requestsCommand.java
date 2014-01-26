/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: requestsCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.sun.jdi.*;
import com.sun.jdi.request.*;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'requests' command.
 *
 * @author  Nathan Fiedler
 */
public class requestsCommand extends JSwatCommand {

    /**
     * Perform the 'requests' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(
                "Must have active session for 'requests'.");
        }
        VirtualMachine vm = session.getVM();
        EventRequestManager erm = vm.eventRequestManager();

        // Access watchpoint requests
        List requests = erm.accessWatchpointRequests();
        Iterator iter = requests.iterator();
        out.writeln("Access watchpoint requests:");
        while (iter.hasNext()) {
            AccessWatchpointRequest awr = (AccessWatchpointRequest) iter.next();
            printBasics(awr, out);
            out.write("\tField: ");
            out.writeln(awr.field().toString());
            out.writeln("");
        }
        out.writeln("");

        // Breakpoint requests
        requests = erm.breakpointRequests();
        iter = requests.iterator();
        out.writeln("Breakpoint requests:");
        while (iter.hasNext()) {
            BreakpointRequest br = (BreakpointRequest) iter.next();
            printBasics(br, out);
            out.write("\tLocation: ");
            out.writeln(br.location().toString());
            out.writeln("");
        }
        out.writeln("");

        // Class prepare requests
        requests = erm.classPrepareRequests();
        iter = requests.iterator();
        out.writeln("Class prepare requests:");
        while (iter.hasNext()) {
            ClassPrepareRequest cpr = (ClassPrepareRequest) iter.next();
            printBasics(cpr, out);
            out.writeln("");
        }
        out.writeln("");

        // Class unload requests
        requests = erm.classUnloadRequests();
        iter = requests.iterator();
        out.writeln("Class unload requests:");
        while (iter.hasNext()) {
            ClassUnloadRequest cur = (ClassUnloadRequest) iter.next();
            printBasics(cur, out);
            out.writeln("");
        }
        out.writeln("");

        // Exception requests
        requests = erm.exceptionRequests();
        iter = requests.iterator();
        out.writeln("Exception requests:");
        while (iter.hasNext()) {
            ExceptionRequest er = (ExceptionRequest) iter.next();
            printBasics(er, out);
            out.write("\tException: ");
            out.writeln(String.valueOf(er.exception()));
            out.write("\tNotify caught: ");
            out.writeln(String.valueOf(er.notifyCaught()));
            out.write("\tNotify uncaught: ");
            out.writeln(String.valueOf(er.notifyUncaught()));
            out.writeln("");
        }
        out.writeln("");

        // Method entry requests
        requests = erm.methodEntryRequests();
        iter = requests.iterator();
        out.writeln("Method entry requests:");
        while (iter.hasNext()) {
            MethodEntryRequest mer = (MethodEntryRequest) iter.next();
            printBasics(mer, out);
            out.writeln("");
        }
        out.writeln("");

        // Method entry requests
        requests = erm.methodExitRequests();
        iter = requests.iterator();
        out.writeln("Method exit requests:");
        while (iter.hasNext()) {
            MethodExitRequest mer = (MethodExitRequest) iter.next();
            printBasics(mer, out);
            out.writeln("");
        }
        out.writeln("");

        // Modification watchpoint requests
        requests = erm.modificationWatchpointRequests();
        iter = requests.iterator();
        out.writeln("Modification watchpoint requests:");
        while (iter.hasNext()) {
            ModificationWatchpointRequest mwr =
                (ModificationWatchpointRequest) iter.next();
            printBasics(mwr, out);
            out.write("\tField: ");
            out.writeln(mwr.field().toString());
            out.writeln("");
        }
        out.writeln("");

        // Step requests
        requests = erm.stepRequests();
        iter = requests.iterator();
        out.writeln("Step requests:");
        while (iter.hasNext()) {
            StepRequest sr = (StepRequest) iter.next();
            printBasics(sr, out);
            out.write("\tThread: ");
            if (sr.thread() != null) {
                out.writeln(sr.thread().toString());
            }
            int depth = sr.depth();
            out.write("\tDepth: ");
            if (depth == StepRequest.STEP_INTO) {
                out.writeln("into");
            } else if (depth == StepRequest.STEP_OUT) {
                out.writeln("out");
            } else if (depth == StepRequest.STEP_OVER) {
                out.writeln("over");
            } else {
                out.writeln("unknown");
            }
            int size = sr.size();
            out.write("\tSize: ");
            if (depth == StepRequest.STEP_MIN) {
                out.writeln("instruction");
            } else if (depth == StepRequest.STEP_LINE) {
                out.writeln("line");
            } else {
                out.writeln("unknown");
            }
            out.writeln("");
        }
        out.writeln("");

        // Thread death requests
        requests = erm.threadDeathRequests();
        iter = requests.iterator();
        out.writeln("Thread death requests:");
        while (iter.hasNext()) {
            ThreadDeathRequest tdr = (ThreadDeathRequest) iter.next();
            printBasics(tdr, out);
            out.writeln("");
        }
        out.writeln("");

        // Thread start requests
        requests = erm.threadStartRequests();
        iter = requests.iterator();
        out.writeln("Thread start requests:");
        while (iter.hasNext()) {
            ThreadStartRequest tsr = (ThreadStartRequest) iter.next();
            printBasics(tsr, out);
            out.writeln("");
        }
        out.writeln("");

        // VM death requests
        requests = erm.vmDeathRequests();
        iter = requests.iterator();
        out.writeln("VM death requests:");
        while (iter.hasNext()) {
            VMDeathRequest vmdr = (VMDeathRequest) iter.next();
            printBasics(vmdr, out);
            out.writeln("");
        }
        out.writeln("");
    } // perform

    /**
     * Prints the basic information about an event request.
     *
     * @param  er   event request.
     * @param  log  output channel.
     */
    protected void printBasics(EventRequest er, Log out) {
        out.write("\tEnabled: ");
        out.writeln(String.valueOf(er.isEnabled()));
        out.write("\tSuspend policy: ");
        int policy = er.suspendPolicy();
        if (policy == EventRequest.SUSPEND_ALL) {
            out.writeln("all");
        } else if (policy == EventRequest.SUSPEND_EVENT_THREAD) {
            out.writeln("thread");
        } else if (policy == EventRequest.SUSPEND_NONE) {
            out.writeln("none");
        } else {
            out.writeln("unknown");
        }
    } // printBasics
} // requestsCommand
