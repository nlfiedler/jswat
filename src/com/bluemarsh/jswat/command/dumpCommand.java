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
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'dump' command.
 *
 * $Id: dumpCommand.java 629 2002-10-26 23:03:26Z nfiedler $
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
            Field field = (Field)iter.next();
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
            ClassType sup = ((ClassType)refType).superclass();
            if (sup != null) {
                dumpObject(buf, obj, sup, refTypeBase);
            }
        } else if (refType instanceof InterfaceType) {
            List sups = ((InterfaceType)refType).superinterfaces();
            Iterator iter2 = sups.iterator();
            while (iter2.hasNext()) {
                dumpObject(buf, obj, (ReferenceType)iter2.next(), refTypeBase);
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
            // Print the object's data member values.
            StringBuffer buf = new StringBuffer(expr);
            buf.append(" = ");
            if (val != null) {
                if (val instanceof ArrayReference) {

                    // Display some or all of the array values.
                    ArrayReference ar = (ArrayReference) val;
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

                } else if (val instanceof ObjectReference) {
                    ObjectReference obj = (ObjectReference) val;
                    ReferenceType refType = obj.referenceType();
                    buf.append(val.toString());
                    buf.append(" {");
                    buf.append('\n');
                    dumpObject(buf, obj, refType, refType);
                    buf.append("}");
                    buf.append('\n');

                } else {
                    buf.append(val.toString());
                    buf.append('\n');
                }
            } else {
                buf.append("(null)");
                buf.append('\n');
            }
            out.write(buf.toString());
        } catch (AbsentInformationException aie) {
            out.writeln(Bundle.getString("noVariableInfo1") + '\n' +
                        Bundle.getString("noVariableInfo2"));
        } catch (ClassNotPreparedException cnpe) {
	    out.writeln(swat.getResourceString("classNotPrepared"));
        } catch (FieldNotObjectException fnoe) {
            out.writeln(Bundle.getString("fieldNotObject") + '\n' +
                        fnoe.toString());
        } catch (IllegalThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotRunning") + '\n' +
                        itse.toString());
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
} // dumpCommand
