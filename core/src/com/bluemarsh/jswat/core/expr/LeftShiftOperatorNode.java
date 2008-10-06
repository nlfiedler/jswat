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
 * $Id: LeftShiftOperatorNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class LeftShiftOperatorNode implements the shift left operator (<<).
 *
 * @author  Nathan Fiedler
 */
class LeftShiftOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a LeftShiftOperatorNode.
     *
     * @param  node  lexical token.
     */
    public LeftShiftOperatorNode(Token node) {
        super(node);
    } // LeftShiftOperatorNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  a Number.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Object o1 = getChild(0).evaluate(context);
        Object o2 = getChild(1).evaluate(context);

        if (isNumber(o1) && isNumber(o2)) {
            if (isFloating(o1) || isFloating(o2)) {
                throw new EvaluationException(
                    NbBundle.getMessage(getClass(), "error.oper.int"), getToken());
            } else {
                if (isLong(o1)) {
                    long l1 = getLongValue(o1);
                    if (isLong(o2)) {
                        return new Long(l1 << getLongValue(o2));
                    } else {
                        return new Long(l1 << getIntValue(o2));
                    }
                } else {
                    int i1 = getIntValue(o1);
                    if (isLong(o2)) {
                        return new Integer(i1 << getLongValue(o2));
                    } else {
                        return new Integer(i1 << getIntValue(o2));
                    }
                }
            }
        } else {
            throw new EvaluationException(
                NbBundle.getMessage(getClass(), "error.oper.num"), getToken());
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
        return 8;
    } // precedence
} // LeftShiftOperatorNode
