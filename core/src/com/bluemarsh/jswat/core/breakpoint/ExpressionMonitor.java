/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ExpressionMonitor.java 15 2007-06-03 00:01:17Z nfiedler $
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
