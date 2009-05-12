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
 * are Copyright (C) 2001-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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

    @Override
    public String describe() {
        return NbBundle.getMessage(ExpressionCondition.class,
                "ExpressionCondition.describe", expression);
    }

    /**
     * Retrieves the expression this condition tests for.
     *
     * @return  expression this condition tests for.
     */
    public String getExpression() {
        return expression;
    }

    @Override
    public boolean isSatisfied(Breakpoint bp, Event event)
            throws ConditionException {
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
        Object o = null;
        try {
            o = eval.evaluate(thread, 0);
        } catch (EvaluationException ee) {
            throw new ConditionException(ee.getMessage(), ee);
        }
        if (o instanceof BooleanValue) {
            return ((BooleanValue) o).value();
        } else if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        } else {
            throw new ConditionException(
                    NbBundle.getMessage(ExpressionCondition.class,
                    "ExpressionCondition.notBoolean"));
        }
    }

    @Override
    public boolean isVisible() {
        return true;
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
