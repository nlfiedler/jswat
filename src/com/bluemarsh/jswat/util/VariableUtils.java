/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * FILE:        VariableUtils.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/03/01        Initial version
 *      nf      09/10/01        Fixing problem in getValue() with
 *                              handling static methods again.
 *      nf      01/23/02        Adding support for array references,
 *                              and references to static fields in classes
 *                              outside of the current scope.
 *      nf      02/15/02        Fixed bug 393
 *
 * DESCRIPTION:
 *      This file defines a utility class for getting variables
 *      (field and local variables).
 *
 * $Id: VariableUtils.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.FieldNotObjectException;
import com.sun.jdi.*;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class VariableUtils provides utility methods for getting Field
 * objects and Value objects from an object or class in the debuggee VM.
 *
 * @author  Nathan Fiedler
 */
public class VariableUtils {

    /**
     * Retrieves an object's field, given the reference to
     * the object and the name of the field to fetch.
     *
     * @param  obj    Current value (must be an ObjectReference).
     * @param  field  Name of the field to look up.
     *
     * @return  New Field as grabbed from the object.
     *
     * @exception  ClassNotPreparedException
     *             Thrown if the object's class is not loaded.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the object.
     * @exception  ObjectCollectedException
     *             Thrown if the referenced object has been collected.
     */
    protected static Field getField(ObjectReference obj, String field)
        throws ClassNotPreparedException,
               NoSuchFieldException,
               ObjectCollectedException {
        ReferenceType refType = obj.referenceType();
        Field fieldVal = refType.fieldByName(field);
        if (fieldVal == null) {
            throw new NoSuchFieldException(field);
        }
        return fieldVal;
    } // getField

    /**
     * Retrieves a class's field, given the reference to
     * the class and the name of the field to fetch.
     *
     * @param  clazz  Class in which to find named field.
     * @param  field  Name of the field to look up.
     *
     * @return  New Field as grabbed from the class.
     *
     * @exception  ClassNotPreparedException
     *             Thrown if the class is not loaded.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the class.
     */
    protected static Field getField(ReferenceType clazz, String field)
        throws ClassNotPreparedException,
               NoSuchFieldException {
        Field fieldVal = clazz.fieldByName(field);
        if (fieldVal == null) {
            throw new NoSuchFieldException(field);
        }
        return fieldVal;
    } // getField

    /**
     * Retrieves the named variable, using the top stack frame of the
     * given thread.
     *
     * @param  expr      Expression referring to variable or object.
     * @param  thread    ThreadReference from which to find variable.
     * @param  frameNum  Frame number in which to look for field.
     *
     * @return  instance of FieldAndValue holding the field, its value,
     *          and the object containing the field. May also be a
     *          local variable and its value, if return value's 'field'
     *          field is null.
     *
     * @exception  AbsentInformationException
     *             Thrown if class doesn't have local variable info.
     * @exception  ArrayIndexOutOfBoundsException
     *             Thrown if expression contained an out-of-range array index.
     * @exception  ClassNotPreparedException
     *             Thrown if the object's class is not loaded.
     * @exception  FieldNotObjectException
     *             Thrown if a non-object is encountered.
     * @exception  IllegalThreadStateException
     *             Thrown if thread is not currently running.
     * @exception  IncompatibleThreadStateException
     *             Thrown if thread is not suspended.
     * @exception  InvalidStackFrameException
     *             Thrown if <code>index</code> is out of bounds.
     * @exception  NativeMethodException
     *             Thrown if the method is native and has no variables.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the object.
     * @exception  ObjectCollectedException
     *             Thrown if the referenced object has been collected.
     */
    public static FieldAndValue getField(String expr, ThreadReference thread,
                                         int frameNum)
        throws AbsentInformationException,
               ArrayIndexOutOfBoundsException,
               ClassNotPreparedException,
               FieldNotObjectException,
               IllegalThreadStateException,
               IncompatibleThreadStateException,
               InvalidStackFrameException,
               NativeMethodException,
               NoSuchFieldException,
               ObjectCollectedException {

        StackFrame frame = null;
        try {
            frame = thread.frame(frameNum);
        } catch (IndexOutOfBoundsException ioobe) {
            throw new InvalidStackFrameException();
        }
        if (frame == null) {
            // Thread is not currently running.
            throw new IllegalThreadStateException("thread not running");
        }

        // Tokenize argument, delimited by periods and square brackets.
        // We can handle array references now.
        StringTokenizer tokenizer = new StringTokenizer(expr, ".[]");
        if (!tokenizer.hasMoreTokens()) {
            throw new NoSuchFieldException("no parsable tokens");
        }

        // Check if first token is a visible local variable.
        String token = tokenizer.nextToken();
        Value curValue;
        Field curField = null;
        ObjectReference obj = null;
        LocalVariable localVar = null;

        if (token.equals("this")) {
            curValue = frame.thisObject();
            if (curValue == null) {
                // This can happen if we are inside a static method.
                throw new NoSuchFieldException(token);
            }
        } else {
            localVar = frame.visibleVariableByName(token);
            if (localVar != null) {
                // Is a local variable, get it's value.
                curValue = frame.getValue(localVar);
            } else {
                // Not a local var, may be a data member of 'this'.
                ReferenceType clazz = frame.location().declaringType();
                obj = frame.thisObject();
                if (obj == null) {
                    // We must be in a static method;
                    // get the field from the class.
                    try {
                        curField = getField(clazz, token);
                    } catch (NoSuchFieldException nsfe) {
                        ClassAndField caf = tryClassName(
                            tokenizer, clazz, token, nsfe);
                        clazz = caf.clazz;
                        curField = caf.field;
                    }
                    if (curField.isStatic()) {
                        curValue = clazz.getValue(curField);
                    } else {
                        // A non-static field in a static method.
                        throw new NoSuchFieldException(token);
                    }

                } else {
                    // Get the field from the 'this' object.
                    try {
                        curField = getField(obj, token);
                        curValue = obj.getValue(curField);
                    } catch (NoSuchFieldException nsfe) {
                        ClassAndField caf = tryClassName(
                            tokenizer, clazz, token, nsfe);
                        clazz = caf.clazz;
                        curField = caf.field;
                        if (curField.isStatic()) {
                            curValue = clazz.getValue(curField);
                        } else {
                            // A non-static field in a static reference.
                            throw new NoSuchFieldException(token);
                        }
                    }
                }
            }
        }

        // For each remaining token, evaluate as a data member
        // of the current value object.
        while ((tokenizer.hasMoreTokens()) && (curValue != null)) {
            String prevToken = token;
            token = tokenizer.nextToken();

            Object arg;
            try {
                // Try to convert the token to an integer as maybe
                // this is an array reference.
                arg = new Integer(token);
            } catch (NumberFormatException nfe) {
                arg = token;
            }

            // A Value is either an ObjectReference, PrimitiveValue,
            // or a VoidValue.
            if (curValue instanceof ArrayReference) {
                // Convert 'arg' to an int value.
                int count = -1;
                if (arg instanceof Integer) {
                    // Easy case.
                    count = ((Integer) arg).intValue();
                } else {
                    // Maybe arg is a variable reference.
                    Value argv = getValue(token, thread, frameNum);
                    if (argv instanceof IntegerValue) {
                        count = ((IntegerValue) argv).value();
                    }
                }

                if (count < ((ArrayReference) curValue).length() &&
                    count >= 0) {
                    curValue = ((ArrayReference) curValue).getValue(count);
                } else {
                    throw new ArrayIndexOutOfBoundsException(expr);
                }

            } else if (curValue instanceof ObjectReference &&
                       arg instanceof String) {
                obj = (ObjectReference) curValue;
                curField = getField(obj, token);
                curValue = obj.getValue(curField);
            } else {
                // The user tried to refer to a primitive or void value.
                throw new FieldNotObjectException(prevToken);
            }
        }

        if (curField == null) {
            // Return the local variable and value.
            return new FieldAndValue(localVar, curValue);
        } else {
            // Return the field, value, and object references.
            return new FieldAndValue(curField, curValue, obj);
        }
    } // getField

    /**
     * Retrieves the named variable, using the stack frame of the
     * given thread.
     *
     * @param  expr    Expression referring to variable or object.
     * @param  thread  ThreadReference from which to find variable.
     * @param  index   Index into stack frames to find variable.
     *
     * @return  The Value referred to by <code>'expr'</code>.
     *          Could possibly be <code>null</code>.
     *
     * @exception  AbsentInformationException
     *             Thrown if class doesn't have local variable info.
     * @exception  ClassNotPreparedException
     *             Thrown if the object's class is not loaded.
     * @exception  FieldNotObjectException
     *             Thrown if a non-object is encountered.
     * @exception  IllegalThreadStateException
     *             Thrown if thread is not currently running.
     * @exception  IncompatibleThreadStateException
     *             Thrown if thread is not suspended.
     * @exception  InvalidStackFrameException
     *             Thrown if <code>index</code> is out of bounds.
     * @exception  NativeMethodException
     *             Thrown if current stack frame is a native method.
     * @exception  NoSuchFieldException
     *             Thrown if the field was not found in the object.
     * @exception  ObjectCollectedException
     *             Thrown if the referenced object has been collected.
     */
    public static Value getValue(String expr, ThreadReference thread,
                                 int index)
        throws AbsentInformationException,
               ClassNotPreparedException,
               FieldNotObjectException,
               IncompatibleThreadStateException,
               IllegalThreadStateException,
               InvalidStackFrameException,
               NativeMethodException,
               NoSuchFieldException,
               ObjectCollectedException {

        // Let getField() do the hard work.
        FieldAndValue fav = getField(expr, thread, index);
        return fav.value;
    } // getValue

    /**
     * Use the location's class, the token (assumed to be a class name),
     * and the given exception (which is rethrown if this method fails),
     * to see if the named class exists.
     *
     * @param  tokenizer  string tokenizer of user input.
     * @param  clazz      class for this location.
     * @param  token      the token assumed to be a class name.
     * @param  nsfe       the exception thrown which leads us to guess
     *                    that cname is a class name.
     * @return  the Field, if found. May not be static, so caller must
     *          check for that.
     * @exception  NoSuchFieldException
     *             Thrown if cname is not a loaded class, or if the
     *             next token is not a field of that class.
     */
    protected static ClassAndField tryClassName(StringTokenizer tokenizer,
                                                ReferenceType clazz,
                                                String token,
                                                NoSuchFieldException nsfe)
        throws NoSuchFieldException {

        if (!tokenizer.hasMoreTokens()) {
            throw nsfe;
        }
        // May it is the name of class and a static field.
        VirtualMachine vm = clazz.virtualMachine();
        List classes = vm.classesByName(token);
        if (classes.isEmpty()) {
            String cname = clazz.name();
            int lastdot = cname.lastIndexOf('.');
            if (lastdot > 0) {
                cname = cname.substring(0, lastdot);
                cname = cname + token;
                classes = vm.classesByName(cname);
                if (classes.isEmpty()) {
                    throw nsfe;
                }
            } else {
                throw nsfe;
            }
        }
        // Take the first class in the list.
        clazz = (ReferenceType) classes.get(0);
        token = tokenizer.nextToken();
        // If this fails, it will throw an appropriate exception.
        return new ClassAndField(clazz, getField(clazz, token));
    } // tryClassName

    /**
     * Holds a ReferenceType/Field pair.
     */
    protected static class ClassAndField {
        /** Class holding the field. */
        ReferenceType clazz;
        /** The field. */
        Field field;

        /**
         * Constructs a ClassAndField object.
         *
         * @param  clazz  class holding the given field.
         * @param  field  field contained within the class.
         */
        public ClassAndField(ReferenceType clazz, Field field) {
            this.clazz = clazz;
            this.field = field;
        } // ClassAndField
    } // ClassAndField
} // VariableUtils
