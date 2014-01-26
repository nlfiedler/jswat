/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: printCommand.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.ClassUtils;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.jswat.util.VariableUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/**
 * Defines the class that handles the 'print' command.
 *
 * @author  Nathan Fiedler
 * @author  Torsten Schlueter (t.schlueter@gmx.de)
 */
public class printCommand extends JSwatCommand {

    /**
     * Perform the 'print' command.
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
        ContextManager ctxtman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = ctxtman.getCurrentThread();
        if (thread == null) {
            out.writeln(Bundle.getString("noCurrentThread"));
            return;
        }

        String expr = args.restTrim();
        try{
            printVariable(expr, thread, ctxtman.getCurrentFrame(), out);
        } catch (AbsentInformationException aie) {
            out.writeln(Bundle.getString("noVariableInfo1") + '\n' +
                        Bundle.getString("noVariableInfo2"));
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            out.writeln(Bundle.getString("print.arrayBounds"));
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

    /**
     * Prints value of a Variable defined by 'expr'. This variable could be a
     * primitive, object reference or arrayReference. If the computed value is
     * instance of ArrayReference and expr ends with "[]"
     * all elements of the array are displayed.
     *
     * @param  expr    user input, it is used to get values, fields and so on.
     * @param  thread  current ThreadReference.
     * @param  frame   current frame delivered by ContextManager.
     * @param  out     Log to write output to.
     * @exception  AbsentInformationException
     *             Thrown if class doesn't have local variable info.
     * @exception  ArrayIndexOutOfBoundsException
     *             Thrown if expr array reference is out of range.
     * @exception  ClassNotPreparedException
     *             Thrown if the object's class is not loaded.
     * @exception  FieldNotObjectException
     *             Thrown if a non-object is encountered.
     * @exception  IllegalThreadStateException
     *             Thrown if thread is not currently running.
     * @exception  IncompatibleThreadStateException
     *             Thrown if thread is not suspended.
     * @exception  IndexOutOfBoundsException
     *             Thrown if <code>frame</code> is out of bounds.
     * @exception  InvalidStackFrameException
     *             Thrown if <code>index</code> is out of bounds.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the object.
     * @exception  ObjectCollectedException
     *             Thrown if the referenced object has been collected.
     */
    protected void printVariable(String expr, ThreadReference thread,
                                 int frame, Log out)
        throws AbsentInformationException,
               ClassNotPreparedException,
               FieldNotObjectException,
               IllegalThreadStateException,
               IncompatibleThreadStateException,
               IndexOutOfBoundsException,
               InvalidStackFrameException,
               NoSuchFieldException,
               ObjectCollectedException {

        // Let VariableUtils do the hard work.
        Value value = VariableUtils.getValue(expr, thread, frame);

        // Should all Values of an Array be printed?
        // If expr ends with [] it should.
        StringBuffer buf = new StringBuffer(256);
        if (value instanceof ArrayReference) {
            String outputString = expr.substring(0, expr.length() - 2);
            ArrayReference ar = (ArrayReference) value;
            for (int i = 0; i < ar.length(); i++) {
                buf.append(outputString);
                buf.append('[');
                buf.append(i);
                buf.append("] : ");
                Value v = ar.getValue(i);
                if (v != null) {
                    buf.append(v.toString());
                } else {
                    buf.append("(null)");
                }
                buf.append('\n');
            }
        } else {

            buf.append(expr);
            buf.append(": ");
            if (value != null) {
                buf.append(value.toString());
                if ((value instanceof ObjectReference) &&
                    !(value instanceof StringReference)) {
                    // If it's an object (but not a String), call its
                    // toString() to get the object as a pretty string.
                    String s = ClassUtils.callToString(
                        (ObjectReference) value, thread);
                    if (s != null) {
                        // If we were successful, print it out.
                        buf.append('\n');
                        buf.append(s);
                    }
                } else if (value instanceof CharValue) {
                    CharValue charval = (CharValue) value;
                    buf.append(" (\\u");
                    buf.append(StringUtils.toHexString(charval.value()));
                    buf.append(')');
                }
                buf.append('\n');
            } else {
                buf.append("(null)");
                buf.append('\n');
            }
        }
        out.write(buf.toString());
    } // printVariable
} // printCommand
