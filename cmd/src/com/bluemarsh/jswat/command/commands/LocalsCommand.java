/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the JSwat Command Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.command.commands;

import com.bluemarsh.jswat.command.AbstractCommand;
import com.bluemarsh.jswat.command.CommandArguments;
import com.bluemarsh.jswat.command.CommandContext;
import com.bluemarsh.jswat.command.CommandException;
import com.bluemarsh.jswat.command.MissingArgumentsException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.core.util.Strings;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.CharValue;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.NbBundle;

/**
 * Displays the visible variables in the current stack frame. Fields
 * may be hidden by local variables of the same name.
 *
 * @author Nathan Fiedler
 */
public class LocalsCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "locals";
    }

    /**
     * Builds a map of all visible variables, including local variables
     * and field variables. The returned Map contains both LocalVariable
     * objects and Field objects, all keyed by their String names. The
     * map is sorted and so iterating it will return the variables in
     * alphabetical order.
     *
     * @param  frame  stack frame from which to retrieve variables.
     * @return  map of visible variables in sorted order.
     * @throws  AbsentInformationException
     *          if the variable information is missing.
     * @throws  InvalidStackFrameException
     *          if the stack frame is invalid.
     * @throws  NativeMethodException
     *          if this method is a native one.
     */
    private Map<String, Mirror> getVariables(StackFrame frame)
        throws AbsentInformationException, InvalidStackFrameException,
               NativeMethodException {
        // Create a sorted map in which to store all of the variable
        // names and values. This helps to deal with shadowed fields.
        Map<String, Mirror> vars = new TreeMap<String, Mirror>();

        // Get the static field's from this class.
        ReferenceType clazz = frame.location().declaringType();
        ListIterator<Field> fields = clazz.fields().listIterator();
        while (fields.hasNext()) {
            Field field = fields.next();
            if (field.isStatic()) {
                // Skip over constants, which are boring.
                if (!field.isFinal()) {
                    // Get static values from the ReferenceType.
                    vars.put(field.name(), field);
                }
            }
        }

        // Get the fields from 'this' object.
        ObjectReference thisObj = frame.thisObject();
        if (thisObj != null) {
            while (fields.hasPrevious()) {
                Field field = fields.previous();
                if (!field.isStatic()) {
                    // Get non-static values from the ObjectReference.
                    vars.put(field.name(), field);
                }
            }
        }

        // Get the visible local variables in this frame. Any fields
        // with the same name as a local will be appropriately hidden.
        ListIterator<LocalVariable> locals = frame.visibleVariables().listIterator();
        while (locals.hasNext()) {
            LocalVariable var = locals.next();
            vars.put(var.name(), var);
        }

        return vars;
    }

    @Override
    public void perform(CommandContext context, CommandArguments arguments)
            throws CommandException, MissingArgumentsException {

        PrintWriter out = context.getWriter();
        Session session = context.getSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        int frameIdx = dc.getFrame();
        ThreadReference thread = dc.getThread();

        if (arguments.hasMoreTokens()) {
            // Token is a frame index value, use that to display
            // the associated stack frame locals.
            String token = arguments.nextToken();
            try {
                frameIdx = Integer.parseInt(token);
            } catch (NumberFormatException nfe) {
                throw new CommandException(
                        NbBundle.getMessage(LocalsCommand.class,
                        "ERR_InvalidNumber", token));
            }
        }

        // Get the stack frame.
        StackFrame frame;
        try {
            frame = thread.frame(frameIdx);
        } catch (IncompatibleThreadStateException itse) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_ThreadNotSuspended"));
        } catch (ObjectCollectedException oce) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_ObjectCollected"));
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_InvalidStackFrame"));
        }
        if (frame == null) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_IncompatibleThread"));
        }

        // Get the list of visible variables, including fields.
        Map vars;
        try {
            vars = getVariables(frame);
        } catch (AbsentInformationException aie) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_NoVariableInfo"));
        } catch (InvalidStackFrameException isfe) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_ThreadNotSuspended"));
        } catch (NativeMethodException nme) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "ERR_NativeMethod"));
        }
        if (vars.size() == 0) {
            throw new CommandException(NbBundle.getMessage(
                    LocalsCommand.class, "CTL_locals_None"));
        }

        // For each variable, print out its type, name, and value.
        StringBuilder sb = new StringBuilder(256);
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
                    sb.append("( ");
                } else {
                    sb.append("  ");
                }
                // Fetch the local variable value.
                val = frame.getValue(var);

                // Print the variable's type name.
                // Value.type().name() returns actual type name.
                // LocalVariable.typeName() returns declared type name.
                String tname = val != null ? val.type().name()
                    : var.typeName();
                sb.append(Names.getShortClassName(tname));
            } else if (o instanceof Field) {
                Field field = (Field) o;
                if (field.isStatic()) {
                    sb.append(": ");
                    val = clazz.getValue(field);
                } else {
                    sb.append(". ");
                    if (thisObj != null) {
                        val = thisObj.getValue(field);
                    }
                }

                // Print the field's type name.
                // Value.type().name() returns actual type name.
                // Field.typeName() returns declared type name.
                String tname = val != null ? val.type().name()
                    : field.typeName();
                sb.append(Names.getShortClassName(tname));
            }

            // Print the variable's name.
            sb.append(" ");
            sb.append(name);
            sb.append(" = ");

            // Print the variable's value.
            if (val != null) {
                if (val instanceof CharValue) {
                    CharValue cv = (CharValue) val;
                    if (!Character.isISOControl(cv.charValue())) {
                        // If it is a printable character, show it.
                        sb.append(val.toString());
                        sb.append(' ');
                    }
                    // Always print the hexadecimal value of the character.
                    sb.append("(\\u");
                    sb.append(Strings.toHexString(cv.value()));
                    sb.append(")\n");
                } else {
                    sb.append(val.toString());
                    sb.append('\n');
                }
            } else {
                sb.append("(null)");
                sb.append('\n');
            }
        }
        out.write(sb.toString());
    }

    @Override
    public boolean requiresDebuggee() {
        return true;
    }

    @Override
    public boolean requiresThread() {
        return true;
    }
}
