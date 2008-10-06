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
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EqualsOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class EqualsOperatorNode implements the equals to operator (==).
 *
 * @author  Nathan Fiedler
 */
class EqualsOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a EqualsOperatorNode.
     *
     * @param  node  lexical token.
     */
    public EqualsOperatorNode(Token node) {
        super(node);
    } // EqualsOperatorNode

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

        if (o1 == null || o2 == null) {
            return o1 == o2 ? Boolean.TRUE : Boolean.FALSE;

        } else if (isBoolean(o1) || isBoolean(o2)) {
            if (isBoolean(o1) && isBoolean(o2)) {
                boolean b1 = getBooleanValue(o1);
                boolean b2 = getBooleanValue(o2);
                return b1 == b2 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                throw new EvaluationException(
                    NbBundle.getMessage(getClass(), "error.oper.equals.type"), getToken());
            }

        } else if (isNumber(o1) || isNumber(o2)) {
            if (isNumber(o1) && isNumber(o2)) {
                if (isFloating(o1) || isFloating(o2)) {
                    if (isDouble(o1) || isDouble(o2)) {
                        double d1 = getDoubleValue(o1);
                        double d2 = getDoubleValue(o2);
                        long l1 = Double.doubleToLongBits(d1);
                        long l2 = Double.doubleToLongBits(d2);
                        return l1 == l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        float f1 = getFloatValue(o1);
                        float f2 = getFloatValue(o2);
                        int i1 = Float.floatToIntBits(f1);
                        int i2 = Float.floatToIntBits(f2);
                        return i1 == i2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    if (isLong(o1) || isLong(o2)) {
                        long l1 = getLongValue(o1);
                        long l2 = getLongValue(o2);
                        return l1 == l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        int i1 = getIntValue(o1);
                        int i2 = getIntValue(o2);
                        return i1 == i2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } else {
                throw new EvaluationException(
                    NbBundle.getMessage(getClass(), "error.oper.equals.type"), getToken());
            }

        } else if (isString(o1) || isString(o2)) {
            // For string comparisons, convert both to Strings and compare.
            String s1 = getStringValue(o1);
            String s2 = getStringValue(o2);
            return s1.equals(s2) ? Boolean.TRUE : Boolean.FALSE;

        } else {
            return o1.equals(o2) ? Boolean.TRUE : Boolean.FALSE;
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
        return 10;
    } // precedence
} // EqualsOperatorNode
