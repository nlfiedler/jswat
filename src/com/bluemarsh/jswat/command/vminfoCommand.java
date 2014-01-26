/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * $Id: vminfoCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Classes;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PathSearchingVirtualMachine;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Defines the class that handles the 'vminfo' command.
 *
 * @author  Nathan Fiedler
 */
public class vminfoCommand extends JSwatCommand {

    /**
     * Returns a string comprised of the desired prefix, followed by
     * a newline, and each path element on a separate line.
     *
     * @param  prefix  path display prefix.
     * @param  path    list of Strings to display.
     * @return  resultant string.
     */
    private static String pathToString(String prefix, List path) {
        StringBuffer buf = new StringBuffer(prefix);
        buf.append('\n');
        Iterator iter = path.iterator();
        if (iter.hasNext()) {
            buf.append(iter.next());
            while (iter.hasNext()) {
                buf.append('\n');
                buf.append(iter.next());
            }
        }
        return buf.toString();
    } // pathToString

    /**
     * Perform the 'vminfo' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }

        //
        // Display classpath information.
        //
        VirtualMachine vm = session.getVM();
        if (vm instanceof PathSearchingVirtualMachine) {
            PathSearchingVirtualMachine psvm =
                (PathSearchingVirtualMachine) vm;
            out.write(Bundle.getString("vminfo.basedir"));
            out.writeln("");
            out.writeln(psvm.baseDirectory());

            out.writeln("");

            List cpath = psvm.classPath();
            out.writeln(pathToString(Bundle.getString("vminfo.cpath"), cpath));

            out.writeln("");

            cpath = psvm.bootClassPath();
            out.writeln(pathToString(
                            Bundle.getString("vminfo.bcpath"), cpath));
        }

        //
        // Display the default stratum.
        //
        out.writeln("");
        out.write(Bundle.getString("vminfo.stratum"));
        out.write(" ");
        out.writeln(vm.getDefaultStratum());

        //
        // Display debuggee memory sizes.
        //
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/util");
        int timeout = prefs.getInt("invocationTimeout",
                                   Defaults.INVOCATION_TIMEOUT);

        // We need the current thread.
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            out.writeln("");
            out.writeln(Bundle.getString("vminfo.nothread"));
            return;
        }

        // We assume this class exists in the debuggee.
        List runtimeTypes = vm.classesByName("java.lang.Runtime");
        ReferenceType rtType = (ReferenceType) runtimeTypes.get(0);
        // We assume this class has just one of each of these methods.
        List methods = rtType.methodsByName("getRuntime",
                                            "()Ljava/lang/Runtime;");
        Method method = (Method) methods.get(0);
        List emptyList = new LinkedList();
        try {
            ObjectReference oref = (ObjectReference) Classes.invokeMethod(
                null, rtType, thread, method, emptyList, timeout);

            methods = rtType.methodsByName("availableProcessors", "()I");
            method = (Method) methods.get(0);
            Object rval = Classes.invokeMethod(
                oref, rtType, thread, method, emptyList, timeout);
            out.writeln("");
            out.write(Bundle.getString("vminfo.numprocs"));
            out.write(" ");
            out.writeln(rval.toString());

            methods = rtType.methodsByName("freeMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, rtType, thread, method, emptyList, timeout);
            out.write(Bundle.getString("vminfo.freemem"));
            out.write(" ");
            out.writeln(rval.toString());

            methods = rtType.methodsByName("maxMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, rtType, thread, method, emptyList, timeout);
            out.write(Bundle.getString("vminfo.maxmem"));
            out.write(" ");
            out.writeln(rval.toString());

            methods = rtType.methodsByName("totalMemory", "()J");
            method = (Method) methods.get(0);
            rval = Classes.invokeMethod(
                oref, rtType, thread, method, emptyList, timeout);
            out.write(Bundle.getString("vminfo.totalmem"));
            out.write(" ");
            out.writeln(rval.toString());
        } catch (Exception e) {
            throw new CommandException(e);
        }
    } // perform
} // vminfoCommand
