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
 * MODULE:      Unit Tests
 * FILE:        TypeCastTest.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      11/26/03        Initial version
 *
 * $Id: TypeCastTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ShortValue;
import com.sun.jdi.ThreadReference;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the type-cast functionality when connected to a debuggee.
 */
public class TypeCastTest extends CommandTestCase {

    public TypeCastTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(TypeCastTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void test_typecast_jdi() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("typecast");
        runCommand(session, "clear all");

        runCommand(session, "runto typecast:7");
        waitForSuspend(ssl);

        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = conman.getCurrentThread();
        int frame = conman.getCurrentFrame();

        try {
            Evaluator eval = new Evaluator("(boolean) z_val");
            Object rval = eval.evaluate(thread, frame);
            assertTrue("z_val is not a boolean",
                       rval instanceof BooleanValue);
            assertTrue("z_val was not true",
                       ((BooleanValue) rval).value());

            eval = new Evaluator("(Boolean) z_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("z_Value is not a Boolean",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Boolean");

            eval = new Evaluator("(byte) b_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("b_val is not a byte",
                       rval instanceof ByteValue);
            assertEquals(((ByteValue) rval).value(), 8);

            eval = new Evaluator("(short) b_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("b_val is not a short",
                       rval instanceof ShortValue);
            assertEquals(((ShortValue) rval).value(), 8);

            eval = new Evaluator("(Byte) b_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("b_Value is not a Byte",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Byte");

            eval = new Evaluator("(char) c_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("c_val is not a character",
                       rval instanceof CharValue);
            assertEquals(((CharValue) rval).value(), 'a');

            eval = new Evaluator("(Character) c_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("c_Value is not a Character",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Character");

            eval = new Evaluator("(double) d_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("d_val is not a double",
                       rval instanceof DoubleValue);
            assertEquals(((DoubleValue) rval).value(), 1.234567890, 0.01f);

            eval = new Evaluator("(Double) d_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("d_Value is not a Double",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Double");

            eval = new Evaluator("(float) f_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("f_val is not a float",
                       rval instanceof FloatValue);
            assertEquals(((FloatValue) rval).value(), 1.234, 0.01f);

            eval = new Evaluator("(double) f_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("f_val could not be cast to a double",
                       rval instanceof DoubleValue);
            assertEquals(((DoubleValue) rval).value(), 1.234, 0.01f);

            eval = new Evaluator("(Float) f_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("f_Value is not a Float",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Float");

            eval = new Evaluator("(int) i_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("i_val is not an integer",
                       rval instanceof IntegerValue);
            assertEquals(((IntegerValue) rval).value(), 1048576);

            eval = new Evaluator("(long) i_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("i_val could not be cast to a long",
                       rval instanceof LongValue);
            assertEquals(((LongValue) rval).value(), 1048576);

            eval = new Evaluator("(Integer) i_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("i_Value is not an Integer",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Integer");

            eval = new Evaluator("(long) l_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("l_val is not a long",
                       rval instanceof LongValue);
            assertEquals(((LongValue) rval).value(), 2 ^ 56);

            eval = new Evaluator("(Long) l_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("l_Value is not a Long",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Long");

            eval = new Evaluator("(short) s_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("s_val is not a short",
                       rval instanceof ShortValue);
            assertEquals(((ShortValue) rval).value(), 256);

            eval = new Evaluator("(int) s_val");
            rval = eval.evaluate(thread, frame);
            assertTrue("s_val is not an integer",
                       rval instanceof IntegerValue);
            assertEquals(((IntegerValue) rval).value(), 256);

            eval = new Evaluator("(Short) s_Value");
            rval = eval.evaluate(thread, frame);
            assertTrue("s_Value is not a Short",
                       rval instanceof ObjectReference);
            assertEquals(((ObjectReference) rval).type().name(),
                         "java.lang.Short");
        } catch (EvaluationException ee) {
            fail(ee.toString());
        }

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
