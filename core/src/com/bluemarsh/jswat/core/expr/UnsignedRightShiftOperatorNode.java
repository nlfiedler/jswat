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
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: UnsignedRightShiftOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class UnsignedRightShiftOperatorNode implements the unsigned right
 * shift operator (>>>).
 *
 * @author  Nathan Fiedler
 */
class UnsignedRightShiftOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a MultOperatorNode.
     *
     * @param  node  lexical token.
     */
    public UnsignedRightShiftOperatorNode(Token node) {
        super(node);
    } // UnsignedRightShiftOperatorNode

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
                        return new Long(l1 >>> getLongValue(o2));
                    } else {
                        return new Long(l1 >>> getIntValue(o2));
                    }
                } else {
                    int i1 = getIntValue(o1);
                    if (isLong(o2)) {
                        return new Integer(i1 >>> getLongValue(o2));
                    } else {
                        return new Integer(i1 >>> getIntValue(o2));
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
} // UnsignedRightShiftOperatorNode
