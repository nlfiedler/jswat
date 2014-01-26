/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: Variables.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.bluemarsh.jswat.FieldNotObjectException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class Variables provides utility methods for getting and displaying
 * variables in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class Variables {

    /**
     * Retrieves an object's field, given the reference to the object
     * and the name of the field to fetch.
     *
     * @param  obj    Current value (must be an ObjectReference).
     * @param  field  Name of the field to look up.
     *
     * @return  New Field as grabbed from the object.
     *
     * @throws  ClassNotPreparedException
     *          if the object's class is not loaded.
     * @throws  NoSuchFieldException
     *          if the field was not found in the object.
     * @throws  ObjectCollectedException
     *          if the referenced object has been collected.
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
     * Retrieves a class's field, given the reference to the class and
     * the name of the field to fetch.
     *
     * @param  clazz  Class in which to find named field.
     * @param  field  Name of the field to look up.
     *
     * @return  New Field as grabbed from the class.
     *
     * @throws  ClassNotPreparedException
     *          if the class is not loaded.
     * @throws  NoSuchFieldException
     *          if the field was not found in the class.
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
     * @return  instance of VariableValue holding the field, its value,
     *          and the object containing the field. May also be a
     *          local variable and its value, if return value's 'field'
     *          field is null.
     *
     * @throws  AbsentInformationException
     *          if class doesn't have local variable info.
     * @throws  ArrayIndexOutOfBoundsException
     *          if expression contained an out-of-range array index.
     * @throws  ClassNotPreparedException
     *          if the object's class is not loaded.
     * @throws  FieldNotObjectException
     *          if a non-object is encountered.
     * @throws  IllegalThreadStateException
     *          if thread is not currently running.
     * @throws  IncompatibleThreadStateException
     *          if thread is not suspended.
     * @throws  InvalidStackFrameException
     *          if <code>index</code> is out of bounds.
     * @throws  NativeMethodException
     *          if the method is native and has no variables.
     * @throws  NoSuchFieldException
     *          if the field was not found in the object.
     * @throws  ObjectCollectedException
     *          if the referenced object has been collected.
     */
    public static VariableValue getField(String expr, ThreadReference thread,
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
                        clazz = caf.getReferenceType();
                        curField = caf.getField();
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
                        clazz = caf.getReferenceType();
                        curField = caf.getField();
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

        boolean arrayLength = false;
        ArrayReference arrayRef = null;
        int arrayIndex = -1;
        // For each remaining token, evaluate as a data member
        // of the current value object.
        while (tokenizer.hasMoreTokens() && curValue != null) {
            String prevToken = token;
            token = tokenizer.nextToken();

            // A Value is either an ObjectReference, PrimitiveValue,
            // or a VoidValue.
            if (curValue instanceof ArrayReference) {
                ArrayReference aref = (ArrayReference) curValue;
                arrayRef = aref;
                if (token.equals("length")) {
                    // Asking for array length.
                    obj = aref;
                    arrayLength = true;
                    continue;
                }

                // Try to convert the token to an integer as maybe
                // this is an array reference.
                try {
                    // Convert token to an integer value.
                    arrayIndex = new Integer(token).intValue();
                } catch (NumberFormatException nfe) {
                    // Maybe token is a variable reference.
                    Value argv = getValue(token, thread, frameNum);
                    if (argv instanceof IntegerValue) {
                        arrayIndex = ((IntegerValue) argv).value();
                    }
                }

                if (arrayIndex >= 0 && arrayIndex < aref.length()) {
                    curValue = aref.getValue(arrayIndex);
                } else {
                    throw new ArrayIndexOutOfBoundsException(expr);
                }

            } else if (curValue instanceof ObjectReference) {
                obj = (ObjectReference) curValue;
                curField = getField(obj, token);
                curValue = obj.getValue(curField);
            } else {
                // The user tried to refer to a primitive or void value.
                throw new FieldNotObjectException(prevToken);
            }
        }

        if (arrayLength) {
            // Return the array.length reference wrapper.
            if (curField == null) {
                return new VariableValue(localVar, (ArrayReference) obj);
            } else {
                return new VariableValue(curField, (ArrayReference) obj);
            }
        } else {
            if (curField == null) {
                // Return the local variable and value.
                VariableValue vv = new VariableValue(localVar, curValue);
                vv.setArrayRef(arrayRef, arrayIndex);
                return vv;
            } else {
                // Return the field, value, and object references.
                VariableValue vv = new VariableValue(curField, curValue, obj);
                vv.setArrayRef(arrayRef, arrayIndex);
                return vv;
            }
        }
    } // getField

    /**
     * Retrieves the named variable, using the stack frame of the given
     * thread.
     *
     * @param  expr    Expression referring to variable or object.
     * @param  thread  ThreadReference from which to find variable.
     * @param  index   Index into stack frames to find variable.
     *
     * @return  The Value referred to by <code>'expr'</code>.
     *          Could possibly be <code>null</code>.
     *
     * @throws  AbsentInformationException
     *          if class doesn't have local variable info.
     * @throws  ClassNotPreparedException
     *          if the object's class is not loaded.
     * @throws  FieldNotObjectException
     *          if a non-object is encountered.
     * @throws  IllegalThreadStateException
     *          if thread is not currently running.
     * @throws  IncompatibleThreadStateException
     *          if thread is not suspended.
     * @throws  InvalidStackFrameException
     *          if <code>index</code> is out of bounds.
     * @throws  NativeMethodException
     *          if current stack frame is a native method.
     * @throws  NoSuchFieldException
     *          if the field was not found in the object.
     * @throws  ObjectCollectedException
     *          if the referenced object has been collected.
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
        return getField(expr, thread, index).value();
    } // getValue

    /**
     * Print the value as a pretty string. If the value is an array,
     * prints the array values. If the value is an object that is not a
     * String, calls to toString() on that object.
     *
     * <p>Note that this method may invalidate the current stack frame.
     * It may be necessary to retrieve the stack frame again.</p>
     *
     * @param  value   value to print.
     * @param  thread  thread for calling toString().
     * @param  arrsep  array element separator string.
     * @return  value as a String.
     * @throws  Exception
     *          literally anything could go wrong.
     */
    public static String printValue(Value value, ThreadReference thread,
                                    String arrsep) throws Exception {
        StringBuffer buf = new StringBuffer(80);
        if (value instanceof ArrayReference) {
            ArrayReference ar = (ArrayReference) value;
            if (ar.length() > 0) {
                buf.append(0);
                buf.append(": ");
                Value v = ar.getValue(0);
                buf.append(v == null ? "null" : v.toString());
                for (int i = 1; i < ar.length(); i++) {
                    buf.append(arrsep);
                    buf.append(i);
                    buf.append(": ");
                    v = ar.getValue(i);
                    buf.append(v == null ? "null" : v.toString());
                }
            }

        } else if (value instanceof StringReference) {
            buf.append(value.toString());

        } else if (value instanceof ObjectReference) {
            // If it's an object (but not a String), call its
            // toString() to get the object as a pretty string.
            String s = Classes.callToString(
                (ObjectReference) value, thread);
            buf.append(s != null ? s : "null");

        } else if (value instanceof CharValue) {
            CharValue cv = (CharValue) value;
            buf.append("\\u");
            buf.append(Strings.toHexString(cv.value()));
        } else if (value == null) {
            buf.append("null");
        } else {
            buf.append(value.toString());
        }
        return buf.toString();
    } // printValue

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
     * @throws  NoSuchFieldException
     *          if cname is not a loaded class, or if the
     *          next token is not a field of that class.
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
        private ReferenceType clazz;
        /** The field. */
        private Field field;

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

        /**
         * Returns the class reference.
         *
         * @return  reference type.
         */
        public ReferenceType getReferenceType() {
            return clazz;
        } // getReferenceType

        /**
         * Returns the field reference.
         *
         * @return  field.
         */
        public Field getField() {
            return field;
        } // getField
    } // ClassAndField
} // Variables
