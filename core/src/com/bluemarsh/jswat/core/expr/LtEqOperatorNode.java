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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class LtEqOperatorNode implements the less than operator (<).
 *
 * @author  Nathan Fiedler
 */
class LtEqOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a LtEqOperatorNode.
     *
     * @param  node  lexical token.
     */
    LtEqOperatorNode(Token node) {
        super(node);
    }

    @Override
    protected Object eval(EvaluationContext context)
            throws EvaluationException {

        Object o1 = getChild(0).evaluate(context);
        Object o2 = getChild(1).evaluate(context);

        if (isNumber(o1) || isNumber(o2)) {
            if (isNumber(o1) && isNumber(o2)) {
                if (isFloating(o1) || isFloating(o2)) {
                    if (isDouble(o1) || isDouble(o2)) {
                        double d1 = getDoubleValue(o1);
                        double d2 = getDoubleValue(o2);
                        long l1 = Double.doubleToLongBits(d1);
                        long l2 = Double.doubleToLongBits(d2);
                        return d1 < d2 || l1 == l2
                                ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        float f1 = getFloatValue(o1);
                        float f2 = getFloatValue(o2);
                        int i1 = Float.floatToIntBits(f1);
                        int i2 = Float.floatToIntBits(f2);
                        return f1 < f2 || i1 == i2
                                ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    if (isLong(o1) || isLong(o2)) {
                        long l1 = getLongValue(o1);
                        long l2 = getLongValue(o2);
                        return l1 <= l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        int i1 = getIntValue(o1);
                        int i2 = getIntValue(o2);
                        return i1 <= i2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } else {
                throw new EvaluationException(
                        NbBundle.getMessage(getClass(), "error.oper.equals.type"), getToken());
            }

        } else {
            throw new EvaluationException(
                    NbBundle.getMessage(getClass(), "error.oper.comp.type"), getToken());
        }
    }

    @Override
    public int precedence() {
        return 9;
    }
}
