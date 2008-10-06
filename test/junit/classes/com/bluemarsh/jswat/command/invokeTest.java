/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * $Id: invokeTest.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionManager;
import com.bluemarsh.jswat.SessionSetup;
import com.bluemarsh.jswat.expr.EvaluationException;
import com.bluemarsh.jswat.expr.Evaluator;
import com.sun.jdi.*;
import junit.extensions.*;
import junit.framework.*;

/**
 * Tests the invoke command.
 */
public class invokeTest extends CommandTestCase {

    public invokeTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new SessionSetup(new TestSuite(invokeTest.class));
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // manually controls active state
    public void test_invoke() {
        Session session = SessionManager.beginSession();
        SimpleSessionListener ssl = new SimpleSessionListener();
        session.addListener(ssl);
        SessionManager.launchSimple("invoke");

        // inactive case tested elsewhere
        // missing args case tested elsewhere
        // no thread case tested elsewhere

        try {
            // no current location
            runCommand(session, "invoke noLocation");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "clear all");
        runCommand(session, "runto invoke:7");
        waitForSuspend(ssl);

        try {
            // no such method
            runCommand(session, "invoke missing()");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        try {
            // missing arguments
            runCommand(session, "invoke method()");
            fail("expected CommandException");
        } catch (CommandException ce) {
            // expected
        }

        runCommand(session, "invoke method(12)");

        // Now run against the evaluator directly to get return values
        // and make sure it is all working correctly.
        ContextManager conman = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = conman.getCurrentThread();
        int frame = conman.getCurrentFrame();

        // Exact argument type for each method, using variables.
        try {
            Evaluator eval = new Evaluator("method(b_val)");
            Object rval = eval.evaluate(thread, frame);
            assertEquals((byte) 9, ((ByteValue) rval).value());

            eval = new Evaluator("method(b_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals((byte) 8, ((ByteValue) rval).value());

            eval = new Evaluator("method(c_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals('A', ((CharValue) rval).value());

            eval = new Evaluator("method(c_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals('a', ((CharValue) rval).value());

            eval = new Evaluator("method(d_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals(1.111D, ((DoubleValue) rval).value(), 0.001D);

            eval = new Evaluator("method(d_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(1.234D, ((DoubleValue) rval).value(), 0.001D);

            eval = new Evaluator("method(f_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals(0.411F, ((FloatValue) rval).value(), 0.001F);

            eval = new Evaluator("method(f_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(1.234F, ((FloatValue) rval).value(), 0.001F);

            eval = new Evaluator("method(i_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals(2097152, ((IntegerValue) rval).value());

            eval = new Evaluator("method(i_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(1048576, ((IntegerValue) rval).value());

            eval = new Evaluator("method(l_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals(720575940379279360L, ((LongValue) rval).value());

            eval = new Evaluator("method(l_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(72057594037927936L, ((LongValue) rval).value());

            eval = new Evaluator("method(s_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals((short) 128, ((ShortValue) rval).value());

            eval = new Evaluator("method(s_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(256, ((ShortValue) rval).value());

            eval = new Evaluator("method(z_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals(false, ((BooleanValue) rval).value());

            eval = new Evaluator("method(z_Value)");
            rval = eval.evaluate(thread, frame);
            assertEquals(true, ((BooleanValue) rval).value());
        } catch (EvaluationException ee) {
            fail(ee.toString());
        }

        // Argument typecast to get desired method.
        try {
            Evaluator eval = new Evaluator("method((byte) i_val)");
            Object rval = eval.evaluate(thread, frame);
            // after the narrowing it becomes 0 (plus 1 = 1)
            assertEquals((byte) 1, ((ByteValue) rval).value());

            eval = new Evaluator("method((char) ic_val)");
            rval = eval.evaluate(thread, frame);
            assertEquals('A', ((CharValue) rval).value());
        } catch (EvaluationException ee) {
            fail(ee.toString());
        }

        // Exact argument type for each method, using literals.
        try {
            Evaluator eval = new Evaluator("method(\"abc\", 'd', 123, true)");
            Object rval = eval.evaluate(thread, frame);
            assertEquals("abc, d, 123, true",
                         ((StringReference) rval).value());

            eval = new Evaluator("method((String) null, 'd', 123, true)");
            rval = eval.evaluate(thread, frame);
            assertEquals("null, d, 123, true",
                         ((StringReference) rval).value());
        } catch (EvaluationException ee) {
            fail(ee.toString());
        }

        // Ambiguous methods.
        try {
            Evaluator eval = new Evaluator("method(null, 'd', 123, true)");
            eval.evaluate(thread, frame);
            fail("excepted EvaluationException");
        } catch (EvaluationException ee) {
            // this is expected
        }
        try {
            Evaluator eval = new Evaluator("method(null)");
            eval.evaluate(thread, frame);
            fail("excepted EvaluationException");
        } catch (EvaluationException ee) {
            // this is expected
        }

        SessionManager.deactivate(true);
        session.removeListener(ssl);
        SessionManager.endSession();
    }
}
