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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ExpressionMonitor.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.output.OutputProvider;
import com.bluemarsh.jswat.core.output.OutputWriter;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import org.openide.util.NbBundle;

/**
 * Class ExpressionMonitor implements a breakpoint monitor that displays
 * the result of evaluating an expression.
 *
 * @author  Nathan Fiedler
 */
public class ExpressionMonitor implements Monitor {
    /** Expression to evaluate. */
    private String expression;

    /**
     * Creates a new instance of ExpressionMonitor.
     */
    public ExpressionMonitor() {
    }

    /**
     * Retrieves the expression this monitor evaluates.
     *
     * @return  expression this monitor evaluates.
     */
    public String getExpression() {
        return expression;
    }

    public void perform(BreakpointEvent event) {
        Event evt = event.getEvent();
        if (!(evt instanceof LocatableEvent)) {
            // Without a location, we can do nothing.
            return;
        }
        LocatableEvent le = (LocatableEvent) evt;
        ThreadReference thread = le.thread();
        // Do not use isAtBreakpoint() here, as that may not be the case for
        // all types of breakpoints (e.g. watchpoints).
        if (thread == null || !thread.isSuspended()) {
            // Not a valid thread or not suspended.
            return;
        }
        Evaluator eval = new Evaluator(expression);
        OutputWriter writer = OutputProvider.getWriter();
        try {
            Object o = eval.evaluate(thread, 0);
            writer.printOutput(expression + " = " + String.valueOf(o));
        } catch (EvaluationException ee) {
            writer.printOutput(NbBundle.getMessage(ExpressionMonitor.class,
                    "ExpressionMonitor.failed", expression, ee.toString()));
        }
    }

    public boolean requiresThread() {
        return true;
    }

    /**
     * Set the expression to be evaluated by this monitor.
     *
     * @param  expr  expression to evaluate.
     */
    public void setExpression(String expr) {
        expression = expr;
    }
}
