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
 * are Copyright (C) 2003-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.SessionHelper;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.MalformedClassNameException;
import com.bluemarsh.jswat.core.breakpoint.MalformedMemberNameException;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.sun.jdi.ThreadReference;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the Evaluator class.
 */
public class JdiEvaluatorTest extends TestCase {

    public JdiEvaluatorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(JdiEvaluatorTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Evaluator_JDI() {
        // Start the session and launch the debuggee.
        Session session = SessionHelper.getSession();
        SessionHelper.launchDebuggee(session, "EvaluatorTestCode");

        TestData[] testDatum = new TestData[] {
            // no debugging context, can't invoke methods
            new TestData("String.valueOf(10)"),
        };
        EvaluatorHelper helper = new EvaluatorHelper(this);
        helper.performTest(testDatum, null, 0);

        // Set a breakpoint at a convenient location.
        BreakpointFactory bf = BreakpointProvider.getBreakpointFactory();
        BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
        String[] methods = { "staticMethod", "instanceMethod" };
        try {
            // Both methods have same argument types.
            List<String> argTypes = new ArrayList<String>(3);
            argTypes.add("int");
            argTypes.add("char");
            argTypes.add("java.lang.String");
            for (String method : methods) {
                Breakpoint bp = bf.createMethodBreakpoint(
                    "EvaluatorTestCode", method, argTypes);
                bp.setDeleteOnExpire(true);
                bp.setExpireCount(1);
                bm.addBreakpoint(bp);
            }
        } catch (MalformedClassNameException mcne) {
            fail(mcne.toString());
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        }

        // Resume the debuggee to hit the next breakpoint (staticMethod).
        SessionHelper.resumeAndWait(session);

        // Get the debugging context.
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        int frame = dc.getFrame();

        // static method tests
        testDatum = new TestData[] {
            // basic static field tests
            new TestData("static_boolean", Boolean.TRUE),
            new TestData("static_boolean == true", Boolean.TRUE),
            new TestData("static_byte == 1", Boolean.TRUE),
            new TestData("static_char == 'a'", Boolean.TRUE),
            new TestData("static_double == 1.0", Boolean.TRUE),
            new TestData("static_float == 1.0f", Boolean.TRUE),
            new TestData("static_int == 1", Boolean.TRUE),
            new TestData("static_long == 1", Boolean.TRUE),
            new TestData("static_short == 1", Boolean.TRUE),
            new TestData("static_short == 2", Boolean.FALSE),
            new TestData("static_nullobj == null", Boolean.TRUE),
            new TestData("static_nullarr == null", Boolean.TRUE),
            new TestData("static_strarr[0] == \"abc\"", Boolean.TRUE),
            new TestData("static_strarr[2] == \"!@#\"", Boolean.TRUE),

            // reference to non-existent variable
            new TestData("invalid"),

            // tests with parentheses
            new TestData("(static_short == 1)", Boolean.TRUE),
            new TestData("(2 - 1) == static_short", Boolean.TRUE),
            new TestData("static_short == 1", Boolean.TRUE),
            new TestData("false == (2 - 1)"),
            new TestData("static_short == (2 - 1)", Boolean.TRUE),
            new TestData("static_short == (1)", Boolean.TRUE),

            // instanceof
            new TestData("null instanceof java.lang.String", Boolean.FALSE),
            new TestData("static_point instanceof java.awt.Point", Boolean.TRUE),
            new TestData("static_String instanceof java.lang.String", Boolean.TRUE),
            new TestData("static_String instanceof java.awt.Point", Boolean.FALSE),

            // method invocations
            // no such method
            new TestData("no_such_method()"),
            new TestData("static_String.no_such_method()"),
            new TestData("static_String.equals()"),
            new TestData("static_String.equals(1)"),
            // methods with arguments
            new TestData("static_String.equals(\"static\")", Boolean.TRUE),
            new TestData("static_String.equalsIgnoreCase(\"sTATic\")", Boolean.TRUE),
            new TestData("static_String.equals(\"non-static\")", Boolean.FALSE),
            new TestData("static_String.charAt(2)", new Character('a')),
            // chained method calls
            new TestData("static_String.substring(1).substring(2,4)", "ti"),
            // static method calls
            new TestData("String.copyValueOf(static_char_array, 2, 2)", "c1"),
            new TestData("Character.isDigit('0')", Boolean.TRUE),
            new TestData("Character.isDigit('p')", Boolean.FALSE),
            // whitespace should be ignored, even though it may be incorrect
            new TestData("Character . isDigit ('p')", Boolean.FALSE),
            // method calls as operands
            new TestData("static_String.equals(\"static\") == true", Boolean.TRUE),
            new TestData("true == static_String.equals(\"static\")", Boolean.TRUE),
            // method calls with type-casts
            new TestData("String.valueOf((int) Long.MAX_VALUE)", "-1"),
            // this one is ambiguous due to widening
            new TestData("String.valueOf((short) Integer.MAX_VALUE)"),
            // invoke static methods with fully-qualified class names
            new TestData("java.lang.System.getProperty(\"java.version\") != null", Boolean.TRUE),
            new TestData("java.lang.System.getProperty(\"not.defined\") == null", Boolean.TRUE),

            // array length access
            new TestData("static_char_array.length == 6", Boolean.TRUE),
            new TestData("static_char_array.length != 6", Boolean.FALSE),
            new TestData("static_char_array.length - 1 == 5", Boolean.TRUE),
            new TestData("5 == static_char_array.length - 1", Boolean.TRUE),
            new TestData("5 == (static_char_array.length - 1)", Boolean.TRUE),
            new TestData("(5 == static_char_array.length) - 1"),
            new TestData("(5 == static_char_array.length) + 1", "false1"),

            // type-cast with primitive values
            new TestData("Short.toString((short) static_int)", "1"),
            new TestData("Integer.toString((int) static_byte)", "1"),
            new TestData("(short) static_int", new Short((short) 1)),
            new TestData("(int) static_byte", new Integer(1)),

            // cannot reference non-static fields
            new TestData("inst_boolean == true"),
            new TestData("inst_byte == 1"),
            new TestData("inst_char == 'a'"),
            new TestData("inst_double == 1.0"),
            new TestData("inst_float == 1.0f"),
            new TestData("inst_int == 1"),
            new TestData("inst_long == 1"),
            new TestData("inst_short == 1"),
            new TestData("inst_String == \"instance\""),

            // assigment operator
            new TestData("static_boolean = false", Boolean.FALSE),
            new TestData("static_boolean == false", Boolean.TRUE),
            new TestData("static_int = 2", new Integer(2)),
            new TestData("static_int == 2", Boolean.TRUE),
            new TestData("static_char = 'b'", 'b'),
            new TestData("static_char == 'b'", Boolean.TRUE),
            new TestData("static_point.x = 5", new Integer(5)),
            new TestData("static_point.x == 5", Boolean.TRUE),
            new TestData("p1 = 5", new Integer(5)),
            new TestData("p1 == 5", Boolean.TRUE),
            new TestData("p2 = 'b'", 'b'),
            new TestData("p2 == 'b'", Boolean.TRUE),
            new TestData("p3 = \"DEFGHI\"", "DEFGHI"),
            new TestData("p3 == \"DEFGHI\"", Boolean.TRUE),
            new TestData("p3.substring(p1 = 3)", "GHI"),
            new TestData("p3.substring(0, p1 = 2)", "DE"),
            new TestData("p3.substring(p1 = 1, 2)", "E"),
            new TestData("p3.regionMatches(0, \"DEFG\", p1 = 0, 4)", Boolean.TRUE),
            new TestData("static_strarr[1] = \"321\"", "321"),
            new TestData("static_strarr[1] == \"321\"", Boolean.TRUE),
            new TestData("static_strarr[1] = null", null),
            new TestData("static_strarr[1] == null", Boolean.TRUE),
            new TestData("static_char_array[3] = '#'", new Character('#')),
            new TestData("static_char_array[3] == '#'", Boolean.TRUE),
            new TestData("static_int_array[2] = 21", new Integer(21)),
            new TestData("static_int_array[2] == 21", Boolean.TRUE),
            new TestData("static_junk = null", null),
            new TestData("static_junk == null", Boolean.TRUE),
            // (restore the expected values for the rest of the tests)
            new TestData("static_boolean = true", Boolean.TRUE),
            new TestData("static_int = 1", new Integer(1)),
            new TestData("static_char = 'a'", 'a'),
            new TestData("static_point.x = 10", new Integer(10)),
            new TestData("static_strarr[1] = \"123\"", "123"),
            new TestData("static_char_array[3] = '1'", new Character('1')),
            new TestData("static_int_array[2] = 2", new Integer(2)),

            // assignment to final field not allowed
            new TestData("STATIC_CONST_INT = 9"),
            // assignment with wrong types, bad indices
            new TestData("static_strarr[100] = \"123\""),
            new TestData("static_char_array[3] = 10"),
            new TestData("static_int_array[2] = 'a'"),

            // combination expressions
            new TestData("String.valueOf(10) + \"abc\" + Integer.SIZE", "10abc32"),
            new TestData("\"abc\" + Integer.SIZE", "abc32"),
            new TestData("String.valueOf(10) + \"abc\"", "10abc"),
            new TestData("String.valueOf(10) + Integer.SIZE", "1032"),
        };
        helper.performTest(testDatum, thread, frame);

        // Resume the debuggee to hit the next breakpoint (instanceMethod).
        SessionHelper.resumeAndWait(session);

        // Get the debugging context again.
        dc = ContextProvider.getContext(session);
        thread = dc.getThread();
        frame = dc.getFrame();

        // instance method tests
        testDatum = new TestData[] {
            // basic static field tests
            new TestData("static_boolean == true", Boolean.TRUE),
            new TestData("static_byte == 1", Boolean.TRUE),
            new TestData("static_char == 'a'", Boolean.TRUE),
            new TestData("static_double == 1.0", Boolean.TRUE),
            new TestData("static_float == 1.0f", Boolean.TRUE),
            new TestData("static_int == 1", Boolean.TRUE),
            new TestData("static_long == 1", Boolean.TRUE),
            new TestData("static_short == 1", Boolean.TRUE),
            new TestData("static_short == 2", Boolean.FALSE),

            // basic instance field tests
            new TestData("inst_boolean == false", Boolean.TRUE),
            new TestData("inst_byte == 2", Boolean.TRUE),
            new TestData("inst_char == 'A'", Boolean.TRUE),
            new TestData("inst_double == 2.0", Boolean.TRUE),
            new TestData("inst_float == 2.0f", Boolean.TRUE),
            new TestData("inst_int == 2", Boolean.TRUE),
            new TestData("inst_long == 2", Boolean.TRUE),
            new TestData("inst_short == 2", Boolean.TRUE),
            new TestData("inst_nullobj == null", Boolean.TRUE),
            new TestData("inst_nullarr == null", Boolean.TRUE),
            new TestData("inst_strarr[0] == \"def\"", Boolean.TRUE),
            new TestData("inst_strarr[2] == \"$%^\"", Boolean.TRUE),
            // the certs field has a type that is not yet loaded
            new TestData("certs != null", Boolean.TRUE),
            new TestData("certs.length > 0", Boolean.TRUE),

            // 'this' tests
            new TestData("this == this", Boolean.TRUE),
            new TestData("(this == this)", Boolean.TRUE),
            new TestData("this != this", Boolean.FALSE),

            // class type-casts
            new TestData("(String) inst_StrAsObj", "string"),
            new TestData("(java.lang.String) inst_StrAsObj", "string"),

            // instanceof
            new TestData("null instanceof java.lang.String", Boolean.FALSE),
            new TestData("inst_point instanceof java.awt.Point", Boolean.TRUE),
            new TestData("inst_String instanceof java.lang.String", Boolean.TRUE),
            new TestData("inst_String instanceof java.awt.Point", Boolean.FALSE),

            // method calls with type-casts
            new TestData("static_String.concat(inst_Object)"),
            new TestData("static_String.concat((String) inst_Object)"),
            new TestData("static_String.concat((String) inst_StrAsObj)", "staticstring"),
            new TestData("static_String.concat((java.lang.String) inst_StrAsObj)", "staticstring"),
            new TestData("String.valueOf((char[]) inst_ArrayAsObj)", "ABC321"),
            new TestData("get2ndString(inst_Str_array)", "two"),
            new TestData("get2ndString(inst_Str_array_empty)", null),
            new TestData("get2ndString((String[]) inst_Str_array_as_Obj)", "two"),

            // bracket fail cases
            new TestData("["),
            new TestData("a["),
            new TestData("[b"),
            new TestData("a[b"),
            new TestData("]"),
            new TestData("a]"),
            new TestData("]b"),
            new TestData("a]b"),

            // nested method calls
            new TestData("inst_String.endsWith(static_String.substring(3))", Boolean.FALSE),
            new TestData("static_String.endsWith(static_String.substring(0, 3))", Boolean.FALSE),
            new TestData("inst_String.endsWith(inst_String.substring(5))", Boolean.TRUE),
            new TestData("inst_String.startsWith(inst_String.substring(0, 5))", Boolean.TRUE),

            // assigment operator
            new TestData("inst_boolean = true", Boolean.TRUE),
            new TestData("inst_boolean == true", Boolean.TRUE),
            new TestData("inst_int = 3", new Integer(3)),
            new TestData("inst_int == 3", Boolean.TRUE),
            new TestData("inst_char = 'B'", 'B'),
            new TestData("inst_char == 'B'", Boolean.TRUE),
            new TestData("inst_point.x = 5", new Integer(5)),
            new TestData("inst_point.x == 5", Boolean.TRUE),
            new TestData("p1 = 5", new Integer(5)),
            new TestData("p1 == 5", Boolean.TRUE),
            new TestData("p2 = 'b'", 'b'),
            new TestData("p2 == 'b'", Boolean.TRUE),
            new TestData("p3 = \"DEF\"", "DEF"),
            new TestData("p3 == \"DEF\"", Boolean.TRUE),
            new TestData("inst_strarr[1] = \"321\"", "321"),
            new TestData("inst_strarr[1] == \"321\"", Boolean.TRUE),
            new TestData("inst_strarr[1] = null", null),
            new TestData("inst_strarr[1] == null", Boolean.TRUE),
            new TestData("inst_char_array[3] = '#'", new Character('#')),
            new TestData("inst_char_array[3] == '#'", Boolean.TRUE),
            new TestData("inst_int_array[2] = 21", new Integer(21)),
            new TestData("inst_int_array[2] == 21", Boolean.TRUE),
            new TestData("inst_junk = null", null),
            new TestData("inst_junk == null", Boolean.TRUE),
            // (restore the expected values for the rest of the tests)
            new TestData("inst_boolean = false", Boolean.FALSE),
            new TestData("inst_int = 2", new Integer(2)),
            new TestData("inst_char = 'A'", 'A'),
            new TestData("inst_point.x = 15", new Integer(15)),
            new TestData("inst_strarr[1] = \"123\"", "123"),
            new TestData("inst_char_array[3] = '1'", new Character('1')),
            new TestData("inst_int_array[2] = 2", new Integer(2)),

            // assignment to final field not allowed
            new TestData("INST_CONST_INT = 9"),
            // assignment with wrong types, bad indices
            new TestData("inst_strarr[100] = \"123\""),
            new TestData("inst_char_array[3] = 10"),
            new TestData("inst_int_array[2] = 'a'"),
        };
        helper.performTest(testDatum, thread, frame);

        // Disconnect the session.
        session.disconnect(true);
    }
}
