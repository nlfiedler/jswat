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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.util;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Class Types provides utility methods for handling types of various
 * forms, especially converting to and from JNI-style type signatures.
 *
 * @author  Nathan Fiedler
 */
public class Types {
    /** Sizes of numeric types, keyed by Character. */
    private static final Map<Character, Integer> SIZES_BY_CHAR;
    /** Sizes of numeric types, keyed by Class. */
    private static final Map<Class, Integer> SIZES_BY_CLASS;

    static {
        // Sizes of numbers by primitive JNI signature.
        SIZES_BY_CHAR = new HashMap<Character, Integer>();
        SIZES_BY_CHAR.put(new Character('B'), new Integer(1));
        SIZES_BY_CHAR.put(new Character('S'), new Integer(2));
        SIZES_BY_CHAR.put(new Character('I'), new Integer(4));
        SIZES_BY_CHAR.put(new Character('J'), new Integer(8));
        SIZES_BY_CHAR.put(new Character('F'), new Integer(4));
        SIZES_BY_CHAR.put(new Character('D'), new Integer(8));

        // Sizes of numbers by wrapper class.
        SIZES_BY_CLASS = new HashMap<Class, Integer>();
        SIZES_BY_CLASS.put(Byte.class, new Integer(1));
        SIZES_BY_CLASS.put(Short.class, new Integer(2));
        SIZES_BY_CLASS.put(Integer.class, new Integer(4));
        SIZES_BY_CLASS.put(Long.class, new Integer(8));
        SIZES_BY_CLASS.put(Float.class, new Integer(4));
        SIZES_BY_CLASS.put(Double.class, new Integer(8));
    }

    /**
     * We are not instantiated.
     */
    private Types() {
    }

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
        Integer desiredSize = SIZES_BY_CHAR.get(new Character(dc));
        Integer actualSize = SIZES_BY_CLASS.get(actualType);
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
    }

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
        Integer desiredSize = SIZES_BY_CHAR.get(new Character(dc));
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
    }

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
    }

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
    }

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
        Stack<Class> stack = new Stack<Class>();
        stack.push(actualType);
        // Convert the signature to a name (e.g. "Ljava/lang/String;"
        // becomes "java.lang.String").
        desiredSig = jniToName(desiredSig);

        while (!stack.empty()) {
            Class type = stack.pop();

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
    }

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
    public static boolean isCompatible(String desiredSig,
                                       ReferenceType actualType) {
        // Prime the stack with the initial type.
        Stack<ReferenceType> stack = new Stack<ReferenceType>();
        stack.push(actualType);

        while (!stack.empty()) {
            ReferenceType type = stack.pop();

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
                List<InterfaceType> interfaces = ctype.interfaces();
                for (InterfaceType iface : interfaces) {
                    stack.push(iface);
                }

            } else if (type instanceof InterfaceType) {
                // Push the extended interfaces to the stack.
                InterfaceType itype = (InterfaceType) type;
                List<InterfaceType> interfaces = itype.superinterfaces();
                for (InterfaceType iface : interfaces) {
                    stack.push(iface);
                }
            }
        }

        // That's all, nothing matched.
        return false;
    }

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
    }

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
        if (jni == null || jni.trim().isEmpty()) {
            return null;
        }

        // Handle multi-dimensional arrays.
        int arrayDepth = 0;
        while (jni.charAt(0) == '[') {
            arrayDepth++;
            jni = jni.substring(1);
        }

        StringBuilder name = new StringBuilder();
        // Check if it is a primitive type.
        if (jni.equals("Z")) {
            name.append("boolean");
        } else if (jni.equals("B")) {
            name.append("byte");
        } else if (jni.equals("C")) {
            name.append("char");
        } else if (jni.equals("D")) {
            name.append("double");
        } else if (jni.equals("F")) {
            name.append("float");
        } else if (jni.equals("I")) {
            name.append("int");
        } else if (jni.equals("J")) {
            name.append("long");
        } else if (jni.equals("S")) {
            name.append("short");
        } else if (jni.equals("V")) {
            name.append("void");

        } else if (jni.charAt(0) == 'L') {
            name.append(jniToName(jni));
        } else {
            return null;
        }

        while (!nb && arrayDepth > 0) {
            name.append("[]");
            arrayDepth--;
        }
        return name.toString();
    }

    /**
     * Creates a mirror of the given object in the given VM.
     *
     * @param  o   object to make a mirror of.
     * @param  vm  virtual machine in which to create mirror.
     * @return  mirrored value, or null if not mirrorable.
     */
    public static Value mirrorOf(Object o, VirtualMachine vm) {
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
        } else if (o instanceof Value) {
            return (Value) o;
        } else {
            return null;
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
    }

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
        if (sig == null || sig.trim().isEmpty()) {
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
            return vm.mirrorOf(0.0F).type();
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
    }

    /**
     * Converts a type name into a JNI type signature.
     *
     * @param  type  type name (e.g. "boolean", "java.net.URL", "String").
     * @return  JNI type signature (e.g. "Z", "Ljava/net/URL;",
     *          "Ljava/lang/String;"), or null if not recognized.
     */
    public static String typeNameToJNI(String type) {
        if (type == null || type.trim().isEmpty()) {
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
                Class.forName("java.lang." + type);
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
    }

    /**
     * Converts a list of type names to a list of JNI type signatures.
     *
     * @param  types  type names (e.g. "boolean", "java.net.URL", "String").
     * @return  JNI type signatures (e.g. "Z", "Ljava/net/URL;",
     *          "Ljava/lang/String;"); unrecognized names will be
     *          null entries in the list.
     */
    public static List<String> typeNamesToJNI(List<String> types) {
        List<String> results = new LinkedList<String>();
        for (String type : types) {
            results.add(typeNameToJNI(type));
        }
        return results;
    }

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
    }
}
