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
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.util;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the Types utility class.
 */
public class TypesTest {

    @Test
    public void testCanWiden() {
        assertFalse(Types.canWiden("V", Integer.class));
        assertFalse(Types.canWiden("B", ArrayList.class));
        assertFalse(Types.canWiden("B", Integer.class));
        assertFalse(Types.canWiden("I", Double.class));
        assertFalse(Types.canWiden("S", Integer.class));
        assertTrue(Types.canWiden("I", Byte.class));
        assertTrue(Types.canWiden("S", Byte.class));
        assertTrue(Types.canWiden("J", Integer.class));
        assertTrue(Types.canWiden("D", Float.class));
    }

    @Test
    public void testCast() {
        assertNull(Types.cast("B", "foobar"));
        assertNull(Types.cast("C", "foobar"));
        assertNull(Types.cast("D", "foobar"));
        assertNull(Types.cast("F", "foobar"));
        assertNull(Types.cast("I", "foobar"));
        assertNull(Types.cast("J", "foobar"));
        assertNull(Types.cast("S", "foobar"));
        assertTrue(Types.cast("B", new Integer(100)) instanceof Byte);
        assertTrue(Types.cast("Ljava/lang/Byte;", new Integer(100)) instanceof Byte);
        assertTrue(Types.cast("C", new Integer(100)) instanceof Character);
        assertTrue(Types.cast("Ljava/lang/Character;", new Integer(100)) instanceof Character);
        assertTrue(Types.cast("I", new Long(100)) instanceof Integer);
        assertTrue(Types.cast("Ljava/lang/Integer;", new Long(100)) instanceof Integer);
        assertTrue(Types.cast("S", new Integer(100)) instanceof Short);
        assertTrue(Types.cast("Ljava/lang/Short;", new Integer(100)) instanceof Short);
        assertTrue(Types.cast("J", new Integer(100)) instanceof Long);
        assertTrue(Types.cast("Ljava/lang/Long;", new Integer(100)) instanceof Long);
        assertTrue(Types.cast("F", new Double(1.234)) instanceof Float);
        assertTrue(Types.cast("Ljava/lang/Float;", new Double(1.234)) instanceof Float);
        assertTrue(Types.cast("D", new Float(1.234)) instanceof Double);
        assertTrue(Types.cast("Ljava/lang/Double;", new Float(1.234)) instanceof Double);
    }

    @Test
    public void testIsCompatible() {
        // abstract class case
        boolean isit = Types.isCompatible("Ljava/lang/Number;", Byte.class);
        assertTrue("Byte is not a Number", isit);
        // interface case
        isit = Types.isCompatible("Ljava/util/List;", ArrayList.class);
        assertTrue("ArrayList is not a List", isit);
    }

    @Test
    public void testJniToName() {
        String jni = Types.jniToName("Ljava/lang/String;");
        assertEquals("java.lang.String", jni);
        jni = Types.jniToName("Ljava/net/URL;");
        assertEquals("java.net.URL", jni);
        jni = Types.jniToName("Lcom/bluemarsh/jswat/Main;");
        assertEquals("com.bluemarsh.jswat.Main", jni);
    }

    @Test
    public void testJniToTypeName() {
        assertNull(Types.jniToTypeName(null, false));
        assertNull(Types.jniToTypeName("", false));
        assertNull(Types.jniToTypeName("  ", false));
        assertNull(Types.jniToTypeName("Q", false));

        assertEquals("java.lang.String", Types.jniToTypeName("Ljava/lang/String;", false));
        assertEquals("byte", Types.jniToTypeName("B", false));
        assertEquals("char", Types.jniToTypeName("C", false));
        assertEquals("double", Types.jniToTypeName("D", false));
        assertEquals("float", Types.jniToTypeName("F", false));
        assertEquals("int", Types.jniToTypeName("I", false));
        assertEquals("long", Types.jniToTypeName("J", false));
        assertEquals("short", Types.jniToTypeName("S", false));
        assertEquals("void", Types.jniToTypeName("V", false));
        assertEquals("boolean", Types.jniToTypeName("Z", false));
        assertEquals("int[][]", Types.jniToTypeName("[[I", false));
        assertEquals("int", Types.jniToTypeName("[[I", true));
        assertEquals("java.lang.String[]", Types.jniToTypeName("[Ljava/lang/String;", false));
        assertEquals("java.lang.String", Types.jniToTypeName("[Ljava/lang/String;", true));
    }

    @Test
    public void testNameToJni() {
        String jni = Types.nameToJni("java.lang.String");
        assertEquals("Ljava/lang/String;", jni);
        jni = Types.nameToJni("java.net.URL");
        assertEquals("Ljava/net/URL;", jni);
        jni = Types.nameToJni("com.bluemarsh.jswat.Main");
        assertEquals("Lcom/bluemarsh/jswat/Main;", jni);
    }

    @Test
    public void testTypeNameToJNI() {
        assertNull(Types.typeNameToJNI(null));
        assertNull(Types.typeNameToJNI(""));
        assertNull(Types.typeNameToJNI("  "));

        // Primivite types
        assertEquals("Z", Types.typeNameToJNI("boolean"));
        assertEquals("B", Types.typeNameToJNI("byte"));
        assertEquals("C", Types.typeNameToJNI("char"));
        assertEquals("D", Types.typeNameToJNI("double"));
        assertEquals("F", Types.typeNameToJNI("float"));
        assertEquals("I", Types.typeNameToJNI("int"));
        assertEquals("J", Types.typeNameToJNI("long"));
        assertEquals("S", Types.typeNameToJNI("short"));
        assertEquals("V", Types.typeNameToJNI("void"));

        // Primivite array types
        assertEquals("[Z", Types.typeNameToJNI("boolean[]"));
        assertEquals("[B", Types.typeNameToJNI("byte[]"));
        assertEquals("[C", Types.typeNameToJNI("char[]"));
        assertEquals("[D", Types.typeNameToJNI("double[]"));
        assertEquals("[F", Types.typeNameToJNI("float[]"));
        assertEquals("[I", Types.typeNameToJNI("int[]"));
        assertEquals("[J", Types.typeNameToJNI("long[]"));
        assertEquals("[S", Types.typeNameToJNI("short[]"));
        assertEquals("[V", Types.typeNameToJNI("void[]"));

        // Core classes
        assertEquals("Ljava/lang/String;", Types.typeNameToJNI("String"));
        assertEquals("Ljava/lang/Class;", Types.typeNameToJNI("Class"));
        assertEquals("Ljava/lang/Math;", Types.typeNameToJNI("Math"));

        // Core class arrays
        assertEquals("[Ljava/lang/String;", Types.typeNameToJNI("String[]"));
        assertEquals("[Ljava/lang/Class;", Types.typeNameToJNI("Class[]"));
        assertEquals("[Ljava/lang/Math;", Types.typeNameToJNI("Math[]"));

        // Multi-dimensional arrays
        assertEquals("[[I", Types.typeNameToJNI("int[][]"));
        assertEquals("[[[Ljava/lang/String;", Types.typeNameToJNI("String[][][]"));

        // Other classes
        assertEquals("Lcom/pkg/MyClass;", Types.typeNameToJNI("com.pkg.MyClass"));
        assertEquals("Lcom/bluemarsh/jswat/Main;", Types.typeNameToJNI("com.bluemarsh.jswat.Main"));
        assertEquals("Lorg/gnu/regex/Pattern;", Types.typeNameToJNI("org.gnu.regex.Pattern"));

        // Other class arrays
        assertEquals("[Lcom/pkg/MyClass;", Types.typeNameToJNI("com.pkg.MyClass[]"));
        assertEquals("[Lcom/bluemarsh/jswat/Main;", Types.typeNameToJNI("com.bluemarsh.jswat.Main[]"));
        assertEquals("[Lorg/gnu/regex/Pattern;", Types.typeNameToJNI("org.gnu.regex.Pattern[]"));
    }

    @Test
    public void testWrapperToPrimitive() {
        String jni = Types.wrapperToPrimitive("Ljava/lang/Boolean;");
        assertEquals("Z", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Byte;");
        assertEquals("B", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Character;");
        assertEquals("C", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Double;");
        assertEquals("D", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Float;");
        assertEquals("F", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Integer;");
        assertEquals("I", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Long;");
        assertEquals("J", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Short;");
        assertEquals("S", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Void;");
        assertEquals("V", jni);
        jni = Types.wrapperToPrimitive("Ljava/lang/Math;");
        assertEquals("", jni);
    }
}
