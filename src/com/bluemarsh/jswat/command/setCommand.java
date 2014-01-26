/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Bill Smith
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
 * FILE:        setCommand.java
 *
 * AUTHOR:      Bill Smith
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      bs      11/25/00        Initial version
 *      nf      07/09/01        Corrected the absent info message
 *      nf      08/09/01        Changed to use UIAdapter
 *      nf      08/26/01        Use Log to print stack trace
 *      nf      09/03/01        Fix it so it handles null values
 *      nf      10/02/01        Fixed bug #244
 *
 * DESCRIPTION:
 *      This file defines the class that handles the 'set' command.
 *
 * $Id: setCommand.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.BadFormatException;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.FieldAndValue;
import com.bluemarsh.jswat.util.VariableUtils;
import com.bluemarsh.util.StringTokenizer;
import com.sun.jdi.*;

/**
 * Defines the class that handles the 'set' command.
 *
 * @author  Bill Smith
 */
public class setCommand extends JSwatCommand {

    /**
     * Perform the 'set' command.
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
        if (args.countTokens() < 3) {
            // 'obj = value' is three arguments, but allow for more
            // so that 'obj = "ima string"' (4 srgs) is possible.
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

        try {
            String rest = args.restTrim();
            int index = rest.indexOf('=');
            String value = rest.substring(index + 1).trim();
            String expr = rest.substring(0, index).trim();
            setValue(expr, thread, ctxtman.getCurrentFrame(), value);
            UIAdapter uiadapter = session.getUIAdapter();
            uiadapter.refreshDisplay();
        } catch (AbsentInformationException aie) {
            out.writeln(Bundle.getString("noVariableInfo1") + '\n' +
                        Bundle.getString("noVariableInfo2"));
        } catch (BadFormatException bfe) {
            out.writeln(Bundle.getString("set.badFormat") + ' ' +
                        bfe.getMessage());
        } catch (ClassNotLoadedException cnle) {
            // How can this happen?
            out.writeln(cnle.toString());
        } catch (IllegalArgumentException iae) {
            out.writeln(iae.toString());
        } catch (IncompatibleThreadStateException itse) {
            out.writeln(swat.getResourceString("threadNotSuspended"));
        } catch (IndexOutOfBoundsException ioobe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
        } catch (NoSuchFieldException nsfe) {
            out.writeln(swat.getResourceString("fieldNotFound") + ": " +
                        nsfe.getMessage());
        } catch (FieldNotObjectException fnoe) {
            out.writeln(Bundle.getString("fieldNotObject") + '\n' +
                        fnoe.toString());
        } catch (InvalidStackFrameException ioobe) {
            out.writeln(Bundle.getString("invalidStackFrame"));
        } catch (InvalidTypeException ite) {
            out.writeln(Bundle.getString("set.invalidType") + ' ' +
                        ite.getMessage());
        } catch (Exception e) {
            out.writeln(e.toString());
            out.writeStackTrace(e);
        }
    } // perform

    /**
     * Set a named variable, using the stack frame of the given thread.
     *
     * @param  lValueString  name of variable to be set.
     * @param  thread        current thread.
     * @param  frame         stack frame index.
     * @param  rValueString  value to be set.
     * @exception  AbsentInformationException
     *             Thrown if class doesn't have local variable info.
     * @exception  BadFormatException
     *             Thrown if string was badly formed.
     * @exception  ClassNotLoadedException
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
     *             Thrown if <code>frame</code> is out of bounds.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the object.
     * @exception  ObjectCollectedException
     *             Thrown if the referenced object has been collected.
     */
    private void setValue(String lValueString, ThreadReference thread,
                          int frame, String rValueString)
        throws AbsentInformationException,
               BadFormatException,
               ClassNotLoadedException,
               FieldNotObjectException,
               IllegalArgumentException,
               IllegalThreadStateException,
               IncompatibleThreadStateException,
               IndexOutOfBoundsException,
               InvalidStackFrameException,
               InvalidTypeException,
               NoSuchFieldException,
               ObjectCollectedException {

        // I made this a separate function in case we want to move it into
        // JSwatCommand someday.

        // Get stack frame.
        StackFrame stackFrame = thread.frame(frame);
        if (stackFrame == null) {
            // Thread is not currently running.
            throw new IllegalThreadStateException("thread not running");
        }

        // Get the referenced field or local variable.
        FieldAndValue fieldValue = VariableUtils.getField(lValueString,
                                                          thread, frame);
        Type lvalType;
        if (fieldValue.field != null) {
            // It's a field of an object.
            // - fieldValue.field
            // - fieldValue.object
            // - fieldValue.value (may be null)
            lvalType = fieldValue.field.type();
        } else {
            // It's a local variable.
            // - fieldValue.localVar
            // - fieldValue.value (may be null)
            lvalType = fieldValue.localVar.type();
        }

        // Now evaluate the rvalue -- it will either be a constant or a
        // variable expression. We will start by assuming it's a constant.

        // Create a Value object based on the target variable's type.
        Value valueObj = null;
        if (lvalType instanceof BooleanType) {
            boolean b = Boolean.valueOf(rValueString).booleanValue();
            valueObj = thread.virtualMachine().mirrorOf(b);
        } else if (lvalType instanceof ByteType) {
            byte b = Byte.parseByte(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(b);
        } else if (lvalType instanceof CharType) {
            // Might be a number or a quoted character.
            char c = parseCharExpression(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(c);
        } else if (lvalType instanceof DoubleType) {
            double d = Double.parseDouble(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(d);
        } else if (lvalType instanceof FloatType) {
            float f = Float.parseFloat(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(f);
        } else if (lvalType instanceof IntegerType) {
            int i = Integer.parseInt(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(i);
        } else if (lvalType instanceof LongType) {
            long l = Long.parseLong(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(l);
        } else if (lvalType instanceof ShortType) {
            short s = Short.parseShort(rValueString);
            valueObj = thread.virtualMachine().mirrorOf(s);
        } else if (lvalType instanceof ReferenceType) {
            // Assume it is a string for now.
            try {
                String s = parseStringExpression(rValueString);
                valueObj = thread.virtualMachine().mirrorOf(s);
            } catch (BadFormatException bfe) {
                // Special case for 'null', we left the valueObj
                // remain null and set the variable as such.
                if (!rValueString.equals("null")) {
                    // Otherwise, it is an error.
                    throw bfe;
                }
            }
        } else {
            throw new IllegalArgumentException("cannot set composite object");
        }

        // Set the variable.
        try {
            if (fieldValue.field == null) {
                // lvalue was a primitive on the stack.
                stackFrame.setValue(fieldValue.localVar, valueObj);
            } else {
                if (fieldValue.object == null) {
                    // lvalue was a static field of the containing class.
                    ReferenceType refType =
                        stackFrame.location().declaringType();
                    if (refType instanceof ClassType) {
                        ClassType clazz = (ClassType) refType;
                        clazz.setValue(fieldValue.field, valueObj);
                    } else {
                        throw new IllegalArgumentException(
                            "cannot set field of abstract class");
                    }
                } else {
                    // lvalue was a field (or sub-field) of 'this' or of
                    // some other variable.
                    fieldValue.object.setValue(fieldValue.field, valueObj);
                }
            }
        } catch (InvalidTypeException ite) {
            throw new InvalidTypeException(rValueString);
        }
    } // setValue

    /**
     * Parse a quoted string. The string must start and end with a
     * double-quote (") character. Escapes of the form \c, where c is
     * a character, are replaced by the character itself. A double
     * backslash is replaced by a single backslash.
     *
     * @param  value  string to be parsed.
     * @return  the parsed string as described above.
     * @exception  BadFormatException
     *             Thrown if string was badly formed.
     */
    private String parseStringExpression(String value) 
        throws BadFormatException {
        StringBuffer output = new StringBuffer();
        char char0 = value.charAt(0);
        if ((value.charAt(0) != '"') ||
            (value.charAt(value.length() - 1) != '"')) {
            throw new BadFormatException(value);
        }

        int length = value.length() - 1;
        for (int i = 1; i < length; i++) {
            char c = value.charAt(i);
            if (c == '\\') {
                c = value.charAt(++i);
                c = translateEscapedChar(c);
            }
            output.append(c);
            // TBD: octal and unicode encodings
        }
        return output.toString();
    } // parseStringExpression

    /**
     * Parse a quoted character. The expression must start and end with an
     * apostrophe ('). Inside the single quotes will either be a single
     * character or an escaped expression.
     *
     * @param  value  string to be parsed.
     * @return  parsed string as described above.
     * @exception  BadFormatException
     *             Thrown if string was badly formed.
     */
    private char parseCharExpression(String value) 
        throws BadFormatException {
        char result;

        try {
            result = (char) Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            if ((value.charAt(0) != '\'') ||
                (value.charAt(value.length() - 1) != '\'')) {
                throw new BadFormatException(value);
            }
            int length = value.length() - 2;
            if (length == 1) {
                result = value.charAt(1);
            } else if (value.charAt(1) == '\\' && length == 2) {
                result = translateEscapedChar(value.charAt(2));
            } else {
                throw new BadFormatException(value);
            }
        }

        return result;
    } // parseCharExpression

    /**
     * If the input character translates to something else when preceeded
     * by a backslash, return the translated version, otherwise return
     * the input character.
     *
     * @param  c  input character.
     * @return  translated version of the input character.
     */
    private char translateEscapedChar(char c) {
        char result;

        switch (c) {
        case 'b': result = '\b'; break;
        case 't': result = '\t'; break;
        case 'n': result = '\n'; break;
        case 'r': result = '\r'; break;
        case 'f': result = '\f'; break;
        default:  result = c;
        }
        return result;
    } // translateEscapedChar
} // setCommand
