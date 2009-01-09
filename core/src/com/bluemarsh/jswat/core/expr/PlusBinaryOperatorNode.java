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
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.Strings;

/**
 * Class PlusBinaryOperatorNode implements the addition binary operator (+).
 *
 * @author  Nathan Fiedler
 */
class PlusBinaryOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a PlusBinaryOperatorNode.
     *
     * @param  node  lexical token.
     */
    public PlusBinaryOperatorNode(Token node) {
        super(node);
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value, either a Number or a String.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Object o1 = getChild(0).evaluate(context);
        Object o2 = getChild(1).evaluate(context);

        if (isNumber(o1) && isNumber(o2)) {
            if (isFloating(o1) || isFloating(o2)) {
                if (isDouble(o1) || isDouble(o2)) {
                    double d1 = getDoubleValue(o1);
                    double d2 = getDoubleValue(o2);
                    return new Double(d1 + d2);
                } else {
                    float f1 = getFloatValue(o1);
                    float f2 = getFloatValue(o2);
                    return new Float(f1 + f2);
                }
            } else {
                if (isLong(o1) || isLong(o2)) {
                    long l1 = getLongValue(o1);
                    long l2 = getLongValue(o2);
                    return new Long(l1 + l2);
                } else {
                    int i1 = getIntValue(o1);
                    int i2 = getIntValue(o2);
                    return new Integer(i1 + i2);
                }
            }

        } else {
            StringBuilder buf = new StringBuilder();
            if (o1 != null) {
                buf.append(Strings.trimQuotes(o1.toString()));
            } else {
                buf.append((String) null);
            }
            if (o2 != null) {
                buf.append(Strings.trimQuotes(o2.toString()));
            } else {
                buf.append((String) null);
            }
            return buf.toString();
        }
    }

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 7;
    }
}
