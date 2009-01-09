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
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class LtOperatorNode implements the less than operator (<).
 *
 * @author  Nathan Fiedler
 */
class LtOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a LtOperatorNode.
     *
     * @param  node  lexical token.
     */
    public LtOperatorNode(Token node) {
        super(node);
    } // LtOperatorNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  a Boolean.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
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
                        return d1 < d2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        float d1 = getFloatValue(o1);
                        float d2 = getFloatValue(o2);
                        return d1 < d2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    if (isLong(o1) || isLong(o2)) {
                        long l1 = getLongValue(o1);
                        long l2 = getLongValue(o2);
                        return l1 < l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        int i1 = getIntValue(o1);
                        int i2 = getIntValue(o2);
                        return i1 < i2 ? Boolean.TRUE : Boolean.FALSE;
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
    } // eval

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 9;
    } // precedence
} // LtOperatorNode
