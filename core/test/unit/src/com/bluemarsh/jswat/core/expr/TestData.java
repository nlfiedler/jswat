/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TestData.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

/**
 * Structure to hold test parameters.
 *
 * @author Nathan Fiedler
 */
public class TestData {
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
