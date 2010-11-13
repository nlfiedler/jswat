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
package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.Assert.*;

/**
 * Utility class for the Evaluator unit tests.
 *
 * @author Nathan Fiedler
 */
public class EvaluatorHelper {

    /**
     * Runs the given evaluator tests.
     *
     * @param  testDatum  array of test data.
     * @param  thread     thread on which to run the evaluator (may be null).
     * @param  frame      frame number in stack for running evaluator.
     */
    public void performTests(TestData[] testDatum,
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
                    StringBuilder buf = new StringBuilder();
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
                    StringBuilder buf = new StringBuilder();
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
                StringBuilder buf = new StringBuilder();
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
                    StringBuilder buf = new StringBuilder();
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
                    StringBuilder buf = new StringBuilder();
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
    }

    /**
     * Determines if the two objects are equivalent. Objects that are from
     * the debuggee can be compared with objects from this VM. That is, if
     * o1 is a Boolean with value 'true' and o2 is a BooleanValue with
     * value of 'true', then they are considered equivalent.
     *
     * @param  o1  first object to compare.
     * @param  o2  second object to compare.
     * @return  true if two objects are equal, false otherwise.
     */
    public static boolean areEqual(Object o1, Object o2) {
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
        if (v1.equals(v2)) {
            return true;
        } else {
            // Check if they are floating point numbers.
            if ((v1 instanceof Float || v1 instanceof Double)
                    && (v2 instanceof Float || v2 instanceof Double)) {
                // Yes they are, see if they are close to equal.
                Number n1 = (Number) v1;
                Number n2 = (Number) v2;
                if (Math.abs(n1.doubleValue() - n2.doubleValue()) < 0.00001) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Reverse of the VirtualMachine.mirrorOf() methods.
     *
     * @param  v  JDI value (e.g. BooleanValue).
     * @return  value in local class type (Boolean).
     */
    private static Object jdiToLocal(Value v) {
        if (v instanceof PrimitiveValue) {
            PrimitiveValue pv = (PrimitiveValue) v;
            if (pv instanceof BooleanValue) {
                return pv.booleanValue();
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
            return ((StringReference) v).value();
        }
        // Do not know how to deal with generic objects.
        return null;
    }
}
