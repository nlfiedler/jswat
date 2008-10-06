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
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ExpressionCondition.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import org.openide.util.NbBundle;

/**
 * Class ExpressionCondition implements a breakpoint conditional that is
 * satisfied when a given expression evaluates to true.
 *
 * @author  Nathan Fiedler
 */
public class ExpressionCondition implements Condition {
    /** Expression to test. */
    private String expression;

    /**
     * Creates a new instance of ExpressionCondition.
     */
    public ExpressionCondition() {
    }

    /**
     * Retrieves the expression this condition tests for.
     *
     * @return  expression this condition tests for.
     */
    public String getExpression() {
        return expression;
    }

    public boolean isSatisfied(Breakpoint bp, Event event) throws Exception {
        if (!(event instanceof LocatableEvent)) {
            // Cannot evaluate condition without a thread.
            return false;
        }
        ThreadReference thread = ((LocatableEvent) event).thread();
        // Do not use isAtBreakpoint() here, as that may not be the case for
        // all types of breakpoints (e.g. watchpoints).
        if (thread == null || !thread.isSuspended()) {
            // Not a valid thread or not suspended.
            return false;
        }

        Evaluator eval = new Evaluator(expression);
        Object o = eval.evaluate(thread, 0);
        if (o instanceof BooleanValue) {
            return ((BooleanValue) o).value();
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        } else {
            throw new EvaluationException(
                NbBundle.getMessage(ExpressionCondition.class,
                    "ExpressionCondition.notBoolean"));
        }
    }

    public boolean isVisible() {
        return false;
    }

    /**
     * Set the boolean expression to be evaluated.
     *
     * @param  expr  boolean expression to evaluate.
     */
    public void setExpression(String expr) {
        expression = expr;
    }
}
