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
 * PROJECT:     JSwat
 * MODULE:      Expression
 * FILE:        Types.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      05/19/03        Initial version
 *      nf      11/27/03        Moved to util package
 *
 * $Id: Types.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.sun.jdi.ByteType;
import com.sun.jdi.ClassType;
import com.sun.jdi.DoubleType;
import com.sun.jdi.FloatType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.LongType;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Class Types provides utility methods for handling types of various
 * forms, especially converting to and from JNI-style type signatures.
 *
 * @author  Nathan Fiedler
 */
public class Types {
    /** Sizes of numeric types, keyed by Character and class. */
    private static final Hashtable SIZES_OF = new Hashtable();

    static {
        // Sizes of numbers by primitive JNI signature.
        SIZES_OF.put(new Character('B'), new Integer(1));
        SIZES_OF.put(new Character('S'), new Integer(2));
        SIZES_OF.put(new Character('I'), new Integer(4));
        SIZES_OF.put(new Character('J'), new Integer(8));
        SIZES_OF.put(new Character('F'), new Integer(4));
        SIZES_OF.put(new Character('D'), new Integer(8));

        // Sizes of numbers by wrapper class.
        SIZES_OF.put(Byte.class, new Integer(1));
        SIZES_OF.put(Short.class, new Integer(2));
        SIZES_OF.put(Integer.class, new Integer(4));
        SIZES_OF.put(Long.class, new Integer(8));
        SIZES_OF.put(Float.class, new Integer(4));
        SIZES_OF.put(Double.class, new Integer(8));
    }

    /**
     * We are not instantiated.
     */
    private Types() {
    } // Types

    /**
     * Compares the desired type with the actual type and determines if the
     * actual type can be widened to the desired type. For instance, a byte
     * can be widened to any other primitive number. This applies only in
     * the case where the actual type is a Number and the desired type is
     * a byte, double, float, integer, long, or short. Wrapper classes
     * (Byte, Double, etc.) are not included.
     *
     * @param  desiredType   the desired JNI type signature.
     * @param  actualType    the actual numeric type to widen.
     * @return  true if type can be widened to desired type, false otherwise.
     */
    public static boolean canWiden(String desiredType, Class actualType) {
        char dc = desiredType.charAt(0);
        Integer desiredSize = (Integer) SIZES_OF.get(new Character(dc));
        Integer actualSize = (Integer) SIZES_OF.get(actualType);
        boolean desiredInt = dc == 'B' || dc == 'S' || dc == 'I' || dc == 'J';
        boolean actualFloat = actualType.equals(Float.class)
            || actualType.equals(Double.class);
        if (desiredSize == null || actualSize == null) {
            // They were not numeric types.
            return false;
        } else if (desiredInt && actualFloat) {
            // Floats to integers require a cast.
            return false;
        } else {
            return desiredSize.intValue() >= actualSize.intValue();
        }
    } // canWiden

    /**
     * Compares the desired type with the actual type and determines if the
     * actual type can be widened to the desired type. For instance, a byte
     * can be widened to any other primitive number. This applies only in
     * the case where the actual type is a Number and the desired type is
     * a byte, double, float, integer, long, or short. Wrapper classes
     * (Byte, Double, etc.) are not included.
     *
     * @param  desiredType   the desired JNI type signature.
     * @param  actualType    the actual numeric type to widen.
     * @return  true if type can be widened to desired type, false otherwise.
     */
    public static boolean canWiden(String desiredType, Type actualType) {
        char dc = desiredType.charAt(0);
        Integer desiredSize = (Integer) SIZES_OF.get(new Character(dc));
        Integer actualSize = null;
        boolean desiredInt = dc == 'B' || dc == 'S' || dc == 'I' || dc == 'J';
        boolean actualFloat = actualType instanceof FloatType
            || actualType instanceof DoubleType;
        if (actualType instanceof ByteType) {
            actualSize = new Integer(1);
        } else if (actualType instanceof ShortType) {
            actualSize = new Integer(2);
        } else if (actualType instanceof IntegerType) {
            actualSize = new Integer(4);
        } else if (actualType instanceof LongType) {
            actualSize = new Integer(8);
        } else if (actualType instanceof FloatType) {
            actualSize = new Integer(4);
        } else if (actualType instanceof DoubleType) {
            actualSize = new Integer(8);
        }
        if (desiredSize == null || actualSize == null) {
            // They were not numeric types.
            return false;
        } else if (desiredInt && actualFloat) {
            // Floats to integers require a cast.
            return false;
        } else {
            return desiredSize.intValue() >= actualSize.intValue();
        }
    } // canWiden

    /**
     * Attempts to cast the value up to the desired type, if this is a
     * compatible operation.
     *
     * @param  type   the desired JNI type signature.
     * @param  value  the value to be cast.
     * @return  the cast value, or null if incompatible.
     */
    public static Object cast(String type, Object value) {
        char ch = type.charAt(0);
        if (ch == 'L') {
            // See if this is really a primitive type in disguise.
            if (type.equals("Ljava/lang/Byte;")) {
                ch = 'B';
            } else if (type.equals("Ljava/lang/Character;")) {
                ch = 'C';
            } else if (type.equals("Ljava/lang/Double;")) {
                ch = 'D';
            } else if (type.equals("Ljava/lang/Float;")) {
                ch = 'F';
            } else if (type.equals("Ljava/lang/Integer;")) {
                ch = 'I';
            } else if (type.equals("Ljava/lang/Long;")) {
                ch = 'J';
            } else if (type.equals("Ljava/lang/Short;")) {
                ch = 'S';
            }
            // Else we do not convert the value.
        }

        // We can cast any number to any other number.
        if (ch == 'B') {
            if (value instanceof Number) {
                return new Byte(((Number) value).byteValue());
            }
        } else if (ch == 'C') {
            if (value instanceof Number) {
                int i = ((Number) value).intValue();
                return new Character((char) i);
            }
        } else if (ch == 'D') {
            if (value instanceof Number) {
                return new Double(((Number) value).doubleValue());
            }
        } else if (ch == 'F') {
            if (value instanceof Number) {
                return new Float(((Number) value).floatValue());
            }
        } else if (ch == 'I') {
            if (value instanceof Number) {
                return new Integer(((Number) value).intValue());
            }
        } else if (ch == 'J') {
            if (value instanceof Number) {
                return new Long(((Number) value).longValue());
            }
        } else if (ch == 'S') {
            if (value instanceof Number) {
                return new Short(((Number) value).shortValue());
            }
        }

        // Else, it was an incompatible translation.
        return null;
    } // cast

    /**
     * Attempts to cast the value up to the desired type, if this is a
     * compatible operation.
     *
     * @param  type   the desired JNI type signature.
     * @param  value  the value to be cast.
     * @param  vm     used to create new value objects.
     * @return  the cast value, or null if incompatible.
     */
    public static Value cast(String type, Value value, VirtualMachine vm) {
        char ch = type.charAt(0);
        if (ch == 'L') {
            // See if this is really a primitive type in disguise.
            if (type.equals("Ljava/lang/Byte;")) {
                ch = 'B';
            } else if (type.equals("Ljava/lang/Character;")) {
                ch = 'C';
            } else if (type.equals("Ljava/lang/Double;")) {
                ch = 'D';
            } else if (type.equals("Ljava/lang/Float;")) {
                ch = 'F';
            } else if (type.equals("Ljava/lang/Integer;")) {
                ch = 'I';
            } else if (type.equals("Ljava/lang/Long;")) {
                ch = 'J';
            } else if (type.equals("Ljava/lang/Short;")) {
                ch = 'S';
            }
            // Else we do not convert the value.
        }

        // We can cast any number to any other number.
        if (ch == 'B') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).byteValue());
            }
        } else if (ch == 'C') {
            if (value instanceof PrimitiveValue) {
                int i = ((PrimitiveValue) value).intValue();
                return vm.mirrorOf((char) i);
            }
        } else if (ch == 'D') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).doubleValue());
            }
        } else if (ch == 'F') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).floatValue());
            }
        } else if (ch == 'I') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).intValue());
            }
        } else if (ch == 'J') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).longValue());
            }
        } else if (ch == 'S') {
            if (value instanceof PrimitiveValue) {
                return vm.mirrorOf(((PrimitiveValue) value).shortValue());
            }
        }

        // Else, it was an incompatible translation.
        return null;
    } // cast

    /**
     * Returns true if the class is assignment compatible with the
     * provided signature. Compares superclasses and implemented
     * interfaces to find a match.
     *
     * @param  desiredSig  JNI signature of desired type.
     * @param  actualType  the actual type to compare.
     * @return  true if the actual type is assignment compatible with the
     *          desired type.
     */
    public static boolean isCompatible(String desiredSig, Class actualType) {
        // Prime the stack with the initial type.
        Stack stack = new Stack();
        stack.push(actualType);
        // Convert the signature to a name (e.g. "Ljava/lang/String;"
        // becomes "java.lang.String").
        desiredSig = jniToName(desiredSig);

        while (!stack.empty()) {
            Class type = (Class) stack.pop();

            // Check if the type names match.
            if (desiredSig.equals(type.getName())) {
                return true;
            }

            // Push the superclass to the stack.
            Class superType = type.getSuperclass();
            if (superType != null) {
                stack.push(superType);
            }

            // Push the implementing interfaces to the stack.
            Class[] interfaces = type.getInterfaces();
            for (int ii = 0; ii < interfaces.length; ii++) {
                stack.push(interfaces[ii]);
            }
        }

        // That's all, nothing matched.
        return false;
    } // isCompatible

    /**
     * Returns true if the class is assignment compatible with the
     * provided signature. Compares superclasses and implemented
     * interfaces to find a match.
     *
     * @param  desiredSig  JNI signature of desired type.
     * @param  actualType   the actual type to compare.
     * @return  true if the actual type is assignment compatible with the
     *          desired type.
     */
    public static boolean isCompatible(String desiredSig,
                                       ReferenceType actualType) {
        // Prime the stack with the initial type.
        Stack stack = new Stack();
        stack.push(actualType);

        while (!stack.empty()) {
            ReferenceType type = (ReferenceType) stack.pop();

            // Check if the type signatures match.
            if (desiredSig.equals(type.signature())) {
                return true;
            }

            // Check for supertypes and implemented interfaces.
            if (type instanceof ClassType) {
                // Push the superclass to the stack.
                ClassType ctype = (ClassType) type;
                ClassType superType = ctype.superclass();
                if (superType != null) {
                    stack.push(superType);
                }

                // Push the implementing interfaces to the stack.
                List interfaces = ctype.interfaces();
                Iterator iter = interfaces.iterator();
                while (iter.hasNext()) {
                    stack.push(iter.next());
                }

            } else if (type instanceof InterfaceType) {
                // Push the extended interfaces to the stack.
                InterfaceType itype = (InterfaceType) type;
                List interfaces = itype.superinterfaces();
                Iterator iter = interfaces.iterator();
                while (iter.hasNext()) {
                    stack.push(iter.next());
                }
            }
        }

        // That's all, nothing matched.
        return false;
    } // isCompatible

    /**
     * Converts a JNI type signature for a class to just the class name.
     * This strips one character from the beginning and end of the given
     * string, and replaces all occurances of '/' with '.'.
     *
     * @param  jni  JNI signature of a class type.
     * @return  class name.
     */
    public static String jniToName(String jni) {
        return jni.substring(1, jni.length() - 1).replace('/', '.');
    } // jniToName

    /**
     * Converts a JNI type signature to a simple type name.
     *
     * @param  jni  type signature (e.g. "Z", "Ljava/net/URL;",
     *              "Ljava/lang/String;", "[[I").
     * @param  nb   true to discard array brackets in return value.
     * @return  type name (e.g. "boolean", "java.net.URL",
     *          "java.lang.String", "int[][]"), or null if not recognized.
     */
    public static String jniToTypeName(String jni, boolean nb) {
        if (jni == null || jni.length() == 0) {
            return null;
        }

        // Handle multi-dimensional arrays.
        int arrayDepth = 0;
        while (jni.charAt(0) == '[') {
            arrayDepth++;
            jni = jni.substring(1);
        }

        String name = null;
        // Check if it is a primitive type.
        if (jni.equals("Z")) {
            name = "boolean";
        } else if (jni.equals("B")) {
            name = "byte";
        } else if (jni.equals("C")) {
            name = "char";
        } else if (jni.equals("D")) {
            name = "double";
        } else if (jni.equals("F")) {
            name = "float";
        } else if (jni.equals("I")) {
            name = "int";
        } else if (jni.equals("J")) {
            name = "long";
        } else if (jni.equals("S")) {
            name = "short";
        } else if (jni.equals("V")) {
            name = "void";

        } else if (jni.charAt(0) == 'L') {
            name = jniToName(jni);
        } else {
            return null;
        }

        while (!nb && arrayDepth > 0) {
            name += "[]";
            arrayDepth--;
        }
        return name;
    } // jniToTypeName

    /**
     * Creates a mirror of the given object in the given VM.
     *
     * @param  o   object to make a mirror of.
     * @param  vm  virtual machine in which to create mirror.
     * @return  mirrored value, or original value.
     */
    public static Object mirrorOf(Object o, VirtualMachine vm) {
        if (o instanceof String) {
            return vm.mirrorOf((String) o);
        } else if (o instanceof Boolean) {
            return vm.mirrorOf(((Boolean) o).booleanValue());
        } else if (o instanceof Character) {
            return vm.mirrorOf(((Character) o).charValue());
        } else if (o instanceof Double) {
            return vm.mirrorOf(((Double) o).doubleValue());
        } else if (o instanceof Float) {
            return vm.mirrorOf(((Float) o).floatValue());
        } else if (o instanceof Integer) {
            return vm.mirrorOf(((Integer) o).intValue());
        } else if (o instanceof Long) {
            return vm.mirrorOf(((Long) o).longValue());
        } else if (o instanceof Short) {
            return vm.mirrorOf(((Short) o).shortValue());
        } else if (o instanceof Byte) {
            return vm.mirrorOf(((Byte) o).byteValue());
        } else {
            return o;
        }
    }

    /**
     * Converts a class name to a JNI type signature. This simply adds
     * an 'L' prefix, a ';' suffix, and replaces all occurances of '.'
     * with '/'.
     *
     * @param  name  class name.
     * @return  JNI signature for class.
     */
    public static String nameToJni(String name) {
        return 'L' + name.replace('.', '/') + ';';
    } // nameToJni

    /**
     * Finds the Type for the given signature. If the signature refers
     * to a class, this method returns the first matching class in the
     * list returned by the virtual machine.
     *
     * @param  sig  signature for which to find type.
     * @param  vm   virtual machine to search in.
     * @return  type for signature, or null if unrecognized.
     */
    public static Type signatureToType(String sig, VirtualMachine vm) {
        if (sig == null || sig.length() == 0) {
            return null;
        }

        // Strip away the leading array brackets.
        while (sig.charAt(0) == '[') {
            sig = sig.substring(1);
        }

        if (sig.charAt(0) == 'L') {
            // The superclass search is done against the given
            // type (reverse of typecasting).
            sig = jniToName(sig);
            List list = vm.classesByName(sig);
            if (list.size() > 0) {
                return (Type) list.get(0);
            }
        } else if (sig.equals("B")) {
            return vm.mirrorOf((byte) 0).type();
        } else if (sig.equals("C")) {
            return vm.mirrorOf(' ').type();
        } else if (sig.equals("D")) {
            return vm.mirrorOf(0.0D).type();
        } else if (sig.equals("F")) {
            return vm.mirrorOf(0.0).type();
        } else if (sig.equals("I")) {
            return vm.mirrorOf(0).type();
        } else if (sig.equals("J")) {
            return vm.mirrorOf(0L).type();
        } else if (sig.equals("S")) {
            return vm.mirrorOf((short) 0).type();
        } else if (sig.equals("Z")) {
            return vm.mirrorOf(false).type();
        }
        // Either void or unknown type.
        return null;
    } // signatureToType

    /**
     * Converts a type name into a JNI type signature.
     *
     * @param  type  type name (e.g. "boolean", "java.net.URL", "String").
     * @return  JNI type signature (e.g. "Z", "Ljava/net/URL;",
     *          "Ljava/lang/String;"), or null if not recognized.
     */
    public static String typeNameToJNI(String type) {
        if (type == null || type.length() == 0) {
            return null;
        }

        // Handle multi-dimensional arrays.
        int arrayDepth = 0;
        while (type.endsWith("[]")) {
            arrayDepth++;
            type = type.substring(0, type.length() - 2);
        }

        String sig = null;
        // Check if it is a primitive type.
        if (type.equals("boolean")) {
            sig = "Z";
        } else if (type.equals("byte")) {
            sig = "B";
        } else if (type.equals("char")) {
            sig = "C";
        } else if (type.equals("double")) {
            sig = "D";
        } else if (type.equals("float")) {
            sig = "F";
        } else if (type.equals("int")) {
            sig = "I";
        } else if (type.equals("long")) {
            sig = "J";
        } else if (type.equals("short")) {
            sig = "S";
        } else if (type.equals("void")) {
            sig = "V";

        } else {
            try {
                // See if it is a core class.
                Class clazz = Class.forName("java.lang." + type);
                sig = "Ljava/lang/" + type + ';';
            } catch (ClassNotFoundException cnfe) {
                // Must be some other class.
                sig = nameToJni(type);
            }
        }

        while (arrayDepth > 0) {
            sig = '[' + sig;
            arrayDepth--;
        }
        return sig;
    } // typeNameToJNI

    /**
     * Converts a list of type names to a list of JNI type signatures.
     *
     * @param  types  type names (e.g. "boolean", "java.net.URL", "String").
     * @return  JNI type signatures (e.g. "Z", "Ljava/net/URL;",
     *          "Ljava/lang/String;"); unrecognized names will be
     *          null entries in the list.
     */
    public static List typeNamesToJNI(List types) {
        List results = new ArrayList();
        Iterator iter = types.iterator();
        while (iter.hasNext()) {
            String type = (String) iter.next();
            results.add(typeNameToJNI(type));
        }
        return results;
    } // typeNamesToJNI

    /**
     * Returns the primitive type signature for the given wrapper type.
     * That is, if type represents one of the wrapper classes (Byte,
     * Short, etc) then this method returns the equivalent primitive
     * type signature ('B', 'S', etc).
     *
     * @param  type  the JNI signature of the wrapper class to convert
     *               to a primitive type signature (e.g. "Ljava/lang/Byte;").
     * @return  primitive type signature, or empty string if type was not
     *          a wrapper for a primitive type.
     */
    public static String wrapperToPrimitive(String type) {
        if (type.equals("Ljava/lang/Boolean;")) {
            return "Z";
        } else if (type.equals("Ljava/lang/Byte;")) {
            return "B";
        } else if (type.equals("Ljava/lang/Character;")) {
            return "C";
        } else if (type.equals("Ljava/lang/Double;")) {
            return "D";
        } else if (type.equals("Ljava/lang/Float;")) {
            return "F";
        } else if (type.equals("Ljava/lang/Integer;")) {
            return "I";
        } else if (type.equals("Ljava/lang/Long;")) {
            return "J";
        } else if (type.equals("Ljava/lang/Short;")) {
            return "S";
        } else if (type.equals("Ljava/lang/Void;")) {
            return "V";
        } else {
            return "";
        }
    } // wrapperToPrimitive
} // Types
