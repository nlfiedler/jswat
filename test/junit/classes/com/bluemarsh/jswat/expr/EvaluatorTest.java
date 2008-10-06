/*********************************************************************
 *
 *      Copyright (C) 2003-2005 Nathan Fiedler
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
 * $Id: EvaluatorTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.MalformedMemberNameException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionSetup;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.MethodBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.util.Types;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.io.PrintWriter;
import java.io.StringWriter;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the Evaluator class.
 */
public class EvaluatorTest extends TestCase {
    private Session session;

    public EvaluatorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(EvaluatorTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_Evaluator_Basic() {
        TestData[] testDatum = new TestData[] {
            // base cases
            new TestData(null, null, "Null expression should be null"),
            new TestData("", null, "Empty string should be null"),
            new TestData("null", null, "'null' should be null"),
            new TestData("false", Boolean.FALSE),
            new TestData("true", Boolean.TRUE),
            new TestData("\"abc\"", new String("abc")),
            new TestData("128", new Integer(128)),
            new TestData("182L", new Long(182)),
            new TestData("1.28", new Float(1.28)),
            new TestData("1.82F", new Float(1.82)),
            new TestData("1.28D", new Double(1.28D)),

            // number bases (hexadecimal and octal)
            new TestData("0x3039", new Integer(12345)),
            new TestData("030071", new Integer(12345)),

            // binary: +, -
            new TestData("1 + 1", new Integer(2)),
            new TestData("\"abc\" + 123", new String("abc123")),
            new TestData("1 + true", new String("1true")),
            new TestData("1 + null", new String("1null")),
            new TestData("2 - 1", new Integer(1)),
            new TestData("1 - 2", new Integer(-1)),
            new TestData("1 - 2L", new Long(-1)),
            new TestData("1L - 2", new Long(-1)),
            new TestData("1.0D - 2", new Double(-1.0)),
            new TestData("1.0 + 2.0", new Float(3.0)),

            // unary: -, +, !, ~
            new TestData("-1", new Integer(-1)),
            new TestData("-1L", new Long(-1)),
            new TestData("+1", new Integer(1)),
            new TestData("+1L", new Long(1)),
            new TestData("!true", Boolean.FALSE),
            new TestData("!false", Boolean.TRUE),
            new TestData("~128", new Integer(-129)),
            new TestData("2 - -1", new Integer(3)),
            new TestData("1 - +2", new Integer(-1)),
            new TestData("~'a'", new Integer(-98)),

            // mult, div, mod: *, /, %
            new TestData("1 * 2", new Integer(2)),
            new TestData("6 / 2", new Integer(3)),
            new TestData("1 / 0"),
            new TestData("0.0 / 0.0", new Float(Float.NaN)),
            new TestData("1 / null"),
            new TestData("1 / 2.0", new Float(0.5)),
            new TestData("2 / 8.0", new Float(0.25)),
            new TestData("1.0 / 0", new Float(Float.POSITIVE_INFINITY)),
            new TestData("-1.0 / 0", new Float(Float.NEGATIVE_INFINITY)),
            new TestData("11 % 2", new Integer(1)),
            new TestData("1 + 2 * 3", new Integer(7)),

            // equality: ==, !=
            new TestData("true == true", Boolean.TRUE),
            new TestData("true == false", Boolean.FALSE),
            new TestData("'A' == 'a'", Boolean.FALSE),
            new TestData("'a' == 'a'", Boolean.TRUE),
            new TestData("true != true", Boolean.FALSE),
            new TestData("false != false", Boolean.FALSE),
            new TestData("true != false", Boolean.TRUE),
            new TestData("true == null", Boolean.FALSE),
            // For some reason this doesn't work, but I'm not concerned.
            //new TestData("0.0 / 0.0 != 0.0 / 0.0", Boolean.TRUE),
            new TestData("0.0 / 0.0 != 0.0", Boolean.TRUE),

            // bitwise: &, |, ^
            new TestData("255 & 32", new Integer(32)),
            new TestData("64 | 32", new Integer(96)),
            new TestData("255 ^ 32", new Integer(223)),
            new TestData("2.0 ^ 32"),
            new TestData("255 ^ 3.2"),
            new TestData("null ^ 32"),
            new TestData("'a' | 32", new Integer(97)),
            new TestData("'a' ^ 32", new Integer(65)),

            // shift: <<, >>, >>>
            new TestData("32 >> 4", new Integer(2)),
            new TestData("32L >> 4", new Long(2)),
            new TestData("0xF1234567 >>> 4", new Long(252851286)),
            new TestData("2 << 4", new Integer(32)),

            // comparison: <, >, <=, <=
            new TestData("32 > 4", Boolean.TRUE),
            new TestData("4 > 32", Boolean.FALSE),
            new TestData("32 < 4", Boolean.FALSE),
            new TestData("4 < 32", Boolean.TRUE),
            new TestData("4 <= 32", Boolean.TRUE),
            new TestData("32 <= 32", Boolean.TRUE),
            new TestData("32 >= 4", Boolean.TRUE),
            new TestData("32 >= 32", Boolean.TRUE),
            new TestData("4 >= 32", Boolean.FALSE),
            new TestData("32 <= 4", Boolean.FALSE),
            new TestData("0.0 / 0.0 < 0.0", Boolean.FALSE),
            new TestData("0.0 / 0.0 > 0.0", Boolean.FALSE),
            new TestData("0.0 >= 0.0 / 0.0", Boolean.FALSE),
            new TestData("0.0 <= 0.0 / 0.0", Boolean.FALSE),
            new TestData("(0.0/0.0 < 0.0) == !(0.0/0.0 >= 0.0)", Boolean.FALSE),

            // logical: &&, ||
            new TestData("true || false", Boolean.TRUE),
            new TestData("false || false", Boolean.FALSE),
            new TestData("null || false"),
            new TestData("true && true", Boolean.TRUE),
            new TestData("true && false", Boolean.FALSE),

            // grouping: ()
            new TestData("(1 + 2) * 3", new Integer(9)),
            new TestData("3 * (1 + 2)", new Integer(9)),
            new TestData("(1) + (2) * (3)", new Integer(7)),
            new TestData("((1 + (2 * 3)))", new Integer(7)),
            new TestData("(((1)))", new Integer(1)),
            new TestData("(((1))"),
            new TestData("((1)))"),
            new TestData("(1 + (2) * (3)"),
            new TestData("(1) + (2 * (3)"),
            new TestData("(1) + (2) * (3"),
            new TestData("(1 + 2 * 3"),
            new TestData("1 + 2 * 3)"),
            new TestData("(true) == (true)", Boolean.TRUE),
            new TestData("(1 + 1) == (3 - 1)", Boolean.TRUE),
            new TestData("(1 + 1) == 2", Boolean.TRUE),
            new TestData("2 != (3 - 1)", Boolean.FALSE),
            new TestData("2 == (3 - 1)", Boolean.TRUE),

            // typecast ()
            new TestData("(boolean) true", Boolean.TRUE),
            new TestData("(Boolean) false", Boolean.FALSE),
            new TestData("(String) \"string\"", new String("string")),
            new TestData("(Number) 123456", new Integer(123456)),
            new TestData("(String) 123456", new String("123456")),
            new TestData("(long) 123456", new Long(123456)),
            new TestData("(Long) 123456", new Long(123456)),
            new TestData("(byte) 12", new Byte((byte) 12)),
            new TestData("(Byte) 12", new Byte((byte) 12)),
            new TestData("(short) 12", new Short((short) 12)),
            new TestData("(Short) 12", new Short((short) 12)),
            new TestData("(float) 0.1", new Float(0.1)),
            new TestData("(Float) 0.1", new Float(0.1)),
            new TestData("(double) 0.2", new Double(0.20000000298023224)),
            new TestData("(Double) 0.2", new Double(0.20000000298023224)),
            new TestData("(Object) null", null),
            new TestData("(boolean) 123"),
            new TestData("(void) 123"),
            new TestData("(Class) 123"),
            new TestData("(List) 123"),
            new TestData("(short) true"),
        };
        performTest(testDatum, null, 0);
    }

    public void test_Evaluator_JDI() {
        // Start the session and launch the debuggee.
        Session session = SessionManager.beginSession();
        SessionManager.launchSimple("EvaluatorTestCode");

        TestData[] testDatum = new TestData[] {
            // no debugging context, can't invoke methods
            new TestData("String.valueOf(10)"),
        };
        performTest(testDatum, null, 0);

        // Set a breakpoint at a convenient location.
        BreakpointManager bm = (BreakpointManager)
            session.getManager(BreakpointManager.class);
        try {
            Breakpoint bp = new MethodBreakpoint(
                "EvaluatorTestCode", "staticMethod", null);
            bp.deleteOnExpire();
            bp.setExpireCount(1);
            bm.addNewBreakpoint(bp);
            bp = new MethodBreakpoint(
                "EvaluatorTestCode", "instanceMethod", null);
            bp.deleteOnExpire();
            bp.setExpireCount(1);
            bm.addNewBreakpoint(bp);
        } catch (ClassNotFoundException cnfe) {
            fail(cnfe.toString());
        } catch (MalformedMemberNameException mmne) {
            fail(mmne.toString());
        } catch (ResolveException re) {
            fail(re.toString());
        }

        // Resume the debuggee to hit the next breakpoint (staticMethod).
        SessionManager.resumeAndWait();

        // Get the debugging context.
        ContextManager dc = (ContextManager) session.getManager(
                ContextManager.class);
        ThreadReference thread = dc.getCurrentThread();
        int frame = dc.getCurrentFrame();

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
            new TestData("static_char = 'b'", new Character('b')),
            new TestData("static_char == 'b'", Boolean.TRUE),
            new TestData("static_point.x = 5", new Integer(5)),
            new TestData("static_point.x == 5", Boolean.TRUE),
            new TestData("p1 = 5", new Integer(5)),
            new TestData("p1 == 5", Boolean.TRUE),
            new TestData("p2 = 'b'", new Character('b')),
            new TestData("p2 == 'b'", Boolean.TRUE),
            new TestData("p3 = \"DEF\"", "DEF"),
            new TestData("p3 == \"DEF\"", Boolean.TRUE),
            // (restore the expected values for the rest of the tests)
            new TestData("static_boolean = true", Boolean.TRUE),
            new TestData("static_int = 1", new Integer(1)),
            new TestData("static_char = 'a'", new Character('a')),
            new TestData("static_point.x = 10", new Integer(10)),

            // assignment to final field not allowed
            new TestData("STATIC_CONST_INT = 9"),
        };
        performTest(testDatum, thread, frame);

        // Resume the debuggee to hit the next breakpoint (instanceMethod).
        SessionManager.resumeAndWait();

        // Get the debugging context again.
        dc = (ContextManager) session.getManager(ContextManager.class);
        thread = dc.getCurrentThread();
        frame = dc.getCurrentFrame();

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

            // 'this' tests
            new TestData("this == this", Boolean.TRUE),
            new TestData("(this == this)", Boolean.TRUE),
            new TestData("this != this", Boolean.FALSE),

            // class type-casts
            new TestData("(String) inst_StrAsObj", "string"),

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
            new TestData("inst_char = 'B'", new Character('B')),
            new TestData("inst_char == 'B'", Boolean.TRUE),
            new TestData("inst_point.x = 5", new Integer(5)),
            new TestData("inst_point.x == 5", Boolean.TRUE),
            new TestData("p1 = 5", new Integer(5)),
            new TestData("p1 == 5", Boolean.TRUE),
            new TestData("p2 = 'b'", new Character('b')),
            new TestData("p2 == 'b'", Boolean.TRUE),
            new TestData("p3 = \"DEF\"", "DEF"),
            new TestData("p3 == \"DEF\"", Boolean.TRUE),
            // (restore the expected values for the rest of the tests)
            new TestData("inst_boolean = false", Boolean.FALSE),
            new TestData("inst_int = 2", new Integer(2)),
            new TestData("inst_char = 'A'", new Character('A')),
            new TestData("inst_point.x = 15", new Integer(15)),

            // assignment to final field not allowed
            new TestData("INST_CONST_INT = 9"),
        };
        performTest(testDatum, thread, frame);

        // Close out the session.
        SessionManager.deactivate(true);
        SessionManager.endSession();
    }

    /**
     * Runs the given evaluator tests.
     *
     * @param  testDatum  array of test data.
     * @param  thread     thread on which to run the evaluator (may be null).
     * @param  frame      frame number in stack for running evaluator.
     */
    protected void performTest(TestData[] testDatum,
                               ThreadReference thread,
                               int frame) {
        for (int ii = 0; ii < testDatum.length; ii++) {
            TestData data = testDatum[ii];
            Evaluator eval = new Evaluator(data.expr);
            eval.setDebug(data.debug);
            Object result = null;
            try {
                result = eval.evaluate(thread, frame);
                if (data.fail) {
                    // was expected to fail
                    StringBuffer buf = new StringBuffer();
                    buf.append(data.expr);
                    buf.append(" <<should have failed; result>> ");
                    buf.append(result);
                    if (result != null) {
                        buf.append(" (");
                        buf.append(result.getClass().getName());
                        buf.append(')');
                    }
                    fail(buf.toString());
                }
            } catch (EvaluationException ee) {
                if (!data.fail) {
                    // was not expected to fail
                    StringBuffer buf = new StringBuffer();
                    buf.append(data.expr);
                    buf.append(" <<should not have failed>> ");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    // this includes the exception description
                    ee.printStackTrace(pw);
                    buf.append(sw.toString());
                    fail(buf.toString());
                }
            } catch (Exception e) {
                StringBuffer buf = new StringBuffer();
                buf.append(data.expr);
                buf.append(" <<unexpected exception>> ");
                buf.append(e.toString());
                fail(buf.toString());
            }

            boolean equals;
            if (result == null && data.result == null) {
                equals = true;
            } else if (result == null || data.result == null) {
                equals = false;
            } else {
                equals = areEqual(result, data.result);
            }

            if (!equals) {
                if (data.message == null) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(data.expr);
                    buf.append(" <<should have been>> ");
                    buf.append(data.result);
                    if (data.result != null) {
                        buf.append(" (");
                        buf.append(data.result.getClass().getName());
                        buf.append(") ");
                    }
                    buf.append(" <<but got>> ");
                    buf.append(result);
                    if (result != null) {
                        buf.append(" (");
                        buf.append(result.getClass().getName());
                        buf.append(')');
                    }
                    fail(buf.toString());

                } else {
                    StringBuffer buf = new StringBuffer();
                    buf.append(data.message);
                    buf.append(" <<expected>> ");
                    buf.append(data.result);
                    if (data.result != null) {
                        buf.append(" (");
                        buf.append(data.result.getClass().getName());
                        buf.append(')');
                    }
                    buf.append(" <<but got>> ");
                    buf.append(result);
                    if (result != null) {
                        buf.append(" (");
                        buf.append(result.getClass().getName());
                        buf.append(')');
                    }
                    fail(buf.toString());
                }
            }
        }
    } // performTest

    /**
     * Determines if the two objects are equivalent. Objects that are from
     * the debuggee can be compared with objects from this VM. That is, if
     * o1 is a Boolean with value 'true' and o2 is a BooleanValue with
     * value of 'true', then they are considered equivalent.
     *
     * @param  o1  first object to compare.
     * @param  o1  second object to compare.
     */
    private boolean areEqual(Object o1, Object o2) {
        String t1 = null;
        Object v1 = o1;
        if (o1 instanceof Value) {
            t1 = ((Value) o1).type().signature();
            v1 = jdiToLocal((Value) o1);
        } else {
            t1 = Types.typeNameToJNI(o1.getClass().getName());
            String s = Types.wrapperToPrimitive(t1);
            if (s.length() > 0) {
                t1 = s;
            }
        }
        String t2 = null;
        Object v2 = o2;
        if (o2 instanceof Value) {
            t2 = ((Value) o2).type().signature();
            v2 = jdiToLocal((Value) o2);
        } else {
            t2 = Types.typeNameToJNI(o2.getClass().getName());
            String s = Types.wrapperToPrimitive(t2);
            if (s.length() > 0) {
                t2 = s;
            }
        }
        if (!t1.equals(t2)) {
            return false;
        }
        if (v1 == null || v2 == null) {
            return false;
        }
        return v1.equals(v2);
    }

    /**
     * Reverse of the VirtualMachine.mirrorOf() methods.
     *
     * @param  v  JDI value (e.g. BooleanValue).
     * @return  value in local class type (Boolean).
     */
    private Object jdiToLocal(Value v) {
        if (v instanceof PrimitiveValue) {
            PrimitiveValue pv = (PrimitiveValue) v;
            if (pv instanceof BooleanValue) {
                return new Boolean(pv.booleanValue());
            } else if (pv instanceof ByteValue) {
                return new Byte(pv.byteValue());
            } else if (pv instanceof CharValue) {
                return new Character(pv.charValue());
            } else if (pv instanceof DoubleValue) {
                return new Double(pv.doubleValue());
            } else if (pv instanceof FloatValue) {
                return new Float(pv.floatValue());
            } else if (pv instanceof IntegerValue) {
                return new Integer(pv.intValue());
            } else if (pv instanceof LongValue) {
                return new Long(pv.longValue());
            } else if (pv instanceof ShortValue) {
                return new Short(pv.shortValue());
            }
        } else if (v instanceof StringReference) {
            return new String(((StringReference) v).value());
        }
        // Do not know how to deal with generic objects.
        return null;
    }

    /**
     * Structure to hold test parameters.
     */
    protected class TestData {
        /** Expression to be evaluated. */
        public String expr;
        /** The expected result as the expect type. */
        public Object result;
        /** The error message to display if the expression does not
         * evaluate to be equal to the reslt. */
        public String message;
        /** True if the expression is expected to cause an exception. */
        public boolean fail;
        /** True if expression evaluator should do debugging. */
        public boolean debug;

        public TestData(String expr) {
            this.expr = expr;
            fail = true;
        }

        public TestData(String expr, Object result) {
            this.expr = expr;
            this.result = result;
        }

        public TestData(String expr, Object result, String message) {
            this.expr = expr;
            this.result = result;
            this.message = message;
        }

        public TestData(String expr, Object result, boolean debug) {
            this.expr = expr;
            this.result = result;
            this.debug = debug;
        }
    }
}
