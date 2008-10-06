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
 * FILE:        dumpCommand.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/30/99        Initial version
 *      nf      07/09/01        Corrected the absent info message.
 *      nf      10/03/01        Fixing bug 252
 *      nf      12/23/02        Implemented RFE 559
 *
 * $Id: dumpCommand.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.util.Iterator;
import java.util.List;

/**
 * Defines the class that handles the 'dump' command.
 *
 * @author  Nathan Fiedler
 */
public class dumpCommand extends JSwatCommand {

    /**
     * Dumps the object's data member values, including all
     * superclasses. Grabbed from sample JPDA debugger.
     *
     * @param  buf          sink to output to.
     * @param  obj          Object to print out.
     * @param  refType      ReferenceType of object.
     * @param  refTypeBase  top-level of reference type heirarchy.
     */
    private void dumpObject(StringBuffer buf, ObjectReference obj,
                            ReferenceType refType,
                            ReferenceType refTypeBase) {
        Iterator iter = refType.fields().iterator();
        while (iter.hasNext()) {
            Field field = (Field) iter.next();
            buf.append("    ");
            if (!refType.equals(refTypeBase)) {
                buf.append(refType.name());
                buf.append(".");
            }
            buf.append(field.name());
            buf.append(": ");
            Object fieldVal = obj.getValue(field);
            if (fieldVal == null) {
                buf.append("(null)");
                buf.append('\n');
            } else {
                buf.append(fieldVal.toString());
                buf.append('\n');
            }
        }
        if (refType instanceof ClassType) {
            ClassType sup = ((ClassType) refType).superclass();
            if (sup != null) {
                dumpObject(buf, obj, sup, refTypeBase);
            }
        } else if (refType instanceof InterfaceType) {
            List sups = ((InterfaceType) refType).superinterfaces();
            Iterator iter2 = sups.iterator();
            while (iter2.hasNext()) {
                dumpObject(buf, obj, (ReferenceType) iter2.next(), refTypeBase);
            }
        }
    } // dumpObject

    /**
     * Perform the 'dump' command.
     *
     * @param  session  JSwat session on which to operate.
     * @param  args     Tokenized string of command arguments.
     * @param  out      Output to write messages to.
     */
    public void perform(Session session, CommandArguments args, Log out) {
        if (!session.isActive()) {
            throw new CommandException(Bundle.getString("noActiveSession"));
        }
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            throw new CommandException(Bundle.getString("noCurrentThread"));
        }
        if (!args.hasMoreTokens()) {
            throw new MissingArgumentsException();
        }

        // We do our own parsing, thank you very much.
        args.returnAsIs(true);
        String expr = args.rest();
        Evaluator eval = new Evaluator(expr);
        try {
            Object o = eval.evaluate(thread, ctxtman.getCurrentFrame());
            StringBuffer buf = new StringBuffer(expr);
            buf.append(" = ");
            if (o != null) {
                if (o instanceof ArrayReference) {

                    // Display some or all of the array values.
                    ArrayReference ar = (ArrayReference) o;
                    int length = ar.length();
                    buf.append('[');
                    buf.append('\n');
                    if (length < 20) {
                        // Print the whole array.
                        for (int i = 0; i < length; i++) {
                            Value arrayval = ar.getValue(i);
                            buf.append("   ");
                            if (arrayval == null) {
                                buf.append("(null)");
                                buf.append('\n');
                            } else {
                                buf.append(arrayval.toString());
                                buf.append('\n');
                            }
                        }
                    } else {

                        // Print just the first few and last few.
                        for (int i = 0; i < 10; i++) {
                            Value arrayval = ar.getValue(i);
                            buf.append("   ");
                            if (arrayval == null) {
                                buf.append("(null)");
                                buf.append('\n');
                            } else {
                                buf.append(arrayval.toString());
                                buf.append('\n');
                            }
                        }
                        buf.append("   ...");
                        buf.append('\n');
                        for (int i = length - 10; i < length; i++) {
                            Value arrayval = ar.getValue(i);
                            buf.append("   ");
                            if (arrayval == null) {
                                buf.append("(null)");
                                buf.append('\n');
                            } else {
                                buf.append(arrayval.toString());
                                buf.append('\n');
                            }
                        }
                    }
                    buf.append(']');
                    buf.append('\n');

                } else if (o instanceof ObjectReference) {
                    // Print the object's data member values.
                    ObjectReference obj = (ObjectReference) o;
                    ReferenceType refType = obj.referenceType();
                    buf.append(obj.toString());
                    buf.append(" {");
                    buf.append('\n');
                    dumpObject(buf, obj, refType, refType);
                    buf.append("}");
                    buf.append('\n');

                } else {
                    throw new CommandException(
                        Bundle.getString("fieldNotObject"));
                }
            } else {
                buf.append("(null)");
                buf.append('\n');
            }
            out.write(buf.toString());
        } catch (ClassNotPreparedException cnpe) {
            throw new CommandException(Bundle.getString("classNotPrepared"),
                cnpe);
        } catch (EvaluationException ee) {
            throw new CommandException(
                Bundle.getString("evalError") + ' ' + ee.getMessage(), ee);
        } catch (IllegalThreadStateException itse) {
            throw new CommandException(
                Bundle.getString("threadNotRunning") + '\n' + itse.toString(),
                itse);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                ioobe);
        } catch (InvalidStackFrameException isfe) {
            throw new CommandException(Bundle.getString("invalidStackFrame"),
                isfe);
        } catch (NativeMethodException nme) {
            throw new CommandException(Bundle.getString("nativeMethod"),
                nme);
        } catch (ObjectCollectedException oce) {
            throw new CommandException(Bundle.getString("objectCollected"),
                oce);
        }
    } // perform
} // dumpCommand
