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
 * FILE:        localsCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/15/99        Initial version
 *      nf      07/09/01        Corrected the absent info message.
 *      nf      08/18/01        Fixed #149: show field variables too
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'locals' command.
 *
 * $Id: localsCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;
import java.util.*;

/**
 * Defines the class that handles the 'locals' command.
 *
 * @author  Nathan Fiedler
 */
public class localsCommand extends JSwatCommand {

    /**
     * Perform the 'locals' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, StringTokenizer args, Log out) {
        // Make sure there's an active session.
        if (!session.isActive()) {
            out.writeln(swat.getResourceString("noActiveSession"));
            return;
        }

        // Get the current thread.
        ThreadReference current = session.getCurrentThread();
        if (!args.hasMoreTokens()) {
            // No arguments, try to use the current thread.
            if (current == null) {
                out.writeln(Bundle.getString("noCurrentThread"));
            } else {
                // Show current thread's current frame's local variables.
                ContextManager ctxtMgr = (ContextManager)
                    session.getManager(ContextManager.class);
                printLocals(current, ctxtMgr.getCurrentFrame(), out);
            }
        } else {

            String token = args.nextToken();
            // Token is a frame index value, use that to display
            // the associated stack frame locals.
            try {
                int index = Integer.parseInt(token);
                // Subtract one from the user input, since the user
                // is entering the absolute number given in the
                // 'where' output.
                printLocals(current, index - 1, out);
            } catch (NumberFormatException nfe) {
                out.writeln(Bundle.getString("invalidStackFrame"));
            }
        }
    } // perform

    /**
     * Display the visible local variables for this stack frame.
     *
     * @param  thread  ThreadReference
     * @param  index   Frame index.
     * @param  out     Output to write variables to.
     */
    protected void printLocals(ThreadReference thread, int index, Log out) {
        // Get the stack frame.
        StackFrame frame;
        try {
            frame = thread.frame(index);
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
            return;
        } catch (ObjectCollectedException oce) {
            out.writeln(swat.getResourceString("objectCollected"));
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
            return;
        }
        if (frame == null) {
            out.writeln(swat.getResourceString("threadNotRunning"));
            return;
        }

        // Get the list of visible variables, including fields.
        Map vars;
        try {
            vars = getVariables(frame);
        } catch (AbsentInformationException aie) {
            out.writeln(Bundle.getString("noVariableInfo1") + '\n' +
                        Bundle.getString("noVariableInfo2"));
            return;
        } catch (InvalidStackFrameException isfe) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
            return;
        } catch (NativeMethodException nme) {
            out.writeln(swat.getResourceString("nativeMethod"));
            return;
        }
        if (vars.size() == 0) {
            out.writeln(Bundle.getString("locals.noneFound"));
            return;
        }

        // For each variable, print out its type, name, and value.
        StringBuffer buf = new StringBuffer(256);
        Iterator iter = vars.keySet().iterator();
        ReferenceType clazz = frame.location().declaringType();
        ObjectReference thisObj = frame.thisObject();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Object o = vars.get(name);
            Value val = null;

            // Print the variable's type.
            if (o instanceof LocalVariable) {
                LocalVariable var = (LocalVariable) o;
                if (var.isArgument()) {
                    buf.append("parameter ");
                } else {
                    buf.append("local ");
                }
                // Fetch the local variable value.
                val = frame.getValue(var);

                // Print the variable's type name.
                // Value.type().name() returns actual type name.
                // LocalVariable.typeName() returns declared type name.
                String tname = val != null ? val.type().name() :
                    var.typeName();
                buf.append(ClassUtils.justTheName(tname));
            } else if (o instanceof Field) {
                Field field = (Field) o;
                buf.append("field ");

                // Get the field's value.
                if (field.isStatic()) {
                    val = clazz.getValue(field);
                } else {
                    if (thisObj != null) {
                        val = thisObj.getValue(field);
                    }
                }

                // Print the field's type name.
                // Value.type().name() returns actual type name.
                // Field.typeName() returns declared type name.
                String tname = val != null ? val.type().name() :
                    field.typeName();
                buf.append(ClassUtils.justTheName(tname));
            }

            // Print the variable's name.
            buf.append(" ");
            buf.append(name);
            buf.append(" = ");

            // Print the variable's value.
            if (val != null) {
                buf.append(val.toString());
                buf.append('\n');
            } else {
                buf.append("(null)");
                buf.append('\n');
            }
        }
        out.write(buf.toString());
    } // printLocals

    /**
     * Builds a map of all visible variables, including local variables
     * and field variables. The returned Map contains both LocalVariable
     * objects and Field objects, all keyed by their String names. The
     * map is sorted and so iterating it will return the variables in
     * alphabetical order.
     *
     * @return  map of visible variables in sorted order.
     * @exception  AbsentInformationException
     *             Thrown if the variable information is missing.
     * @exception  InvalidStackFrameException
     *             Thrown if the stack frame is invalid.
     * @exception  NativeMethodException
     *             Thrown if this method is a native one.
     */
    protected Map getVariables(StackFrame frame)
        throws AbsentInformationException,
               InvalidStackFrameException,
               NativeMethodException {
        // Create a sorted map in which to store all of the variable
        // names and values. This helps us deal with shadowed variables.
        Map allVars = new TreeMap();

        // Get the static field's from this class.
        ReferenceType clazz = frame.location().declaringType();
        ListIterator iter = clazz.fields().listIterator();
        while (iter.hasNext()) {
            Field field = (Field) iter.next();
            if (field.isStatic()) {
                // Skip over constants, which are boring.
                if (!field.isFinal()) {
                    // Get static values from the ReferenceType.
                    allVars.put(field.name(), field);
                }
            }
        }

        // Get the fields from 'this' object.
        ObjectReference thisObj = frame.thisObject();
        if (thisObj != null) {
            while (iter.hasPrevious()) {
                Field field = (Field) iter.previous();
                if (!field.isStatic()) {
                    // Get non-static values from the ObjectReference.
                    allVars.put(field.name(), field);
                }
            }
        }

        // Get the visible local variables in this frame.
        iter = frame.visibleVariables().listIterator();
        // Put the LocalVariable elements into the big map.
        // Any field variables of the same name will be
        // appropriately shadowed.
        while (iter.hasNext()) {
            LocalVariable var = (LocalVariable) iter.next();
            allVars.put(var.name(), var);
        }

        return allVars;
    } // getVariables
} // localsCommand
