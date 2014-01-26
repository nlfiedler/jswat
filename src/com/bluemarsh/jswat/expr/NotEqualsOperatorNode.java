/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: NotEqualsOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;


/**
 * Class NotEqualsOperatorNode implements the not equals to operator (!=).
 *
 * @author  Nathan Fiedler
 */
class NotEqualsOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a NotEqualsOperatorNode.
     *
     * @param  node  lexical token.
     */
    public NotEqualsOperatorNode(Token node) {
        super(node);
    } // NotEqualsOperatorNode

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

        Node n1 = getChild(0);
        Object o1 = n1.evaluate(context);
        Node n2 = getChild(1);
        Object o2 = n2.evaluate(context);

        if (o1 == null || o2 == null) {
            return o1 != o2 ? Boolean.TRUE : Boolean.FALSE;

        } else if (isBoolean(o1) || isBoolean(o2)) {
            if (isBoolean(o1) && isBoolean(o2)) {
                boolean b1 = getBooleanValue(o1);
                boolean b2 = getBooleanValue(o2);
                return b1 != b2 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                throw new EvaluationException(
                    Bundle.getString("error.oper.equals.type"), getToken());
            }

        } else if (isNumber(o1) || isNumber(o2)) {
            if (isNumber(o1) && isNumber(o2)) {
                if (isFloating(o1) || isFloating(o2)) {
                    if (isDouble(o1) || isDouble(o2)) {
                        double d1 = getDoubleValue(o1);
                        double d2 = getDoubleValue(o2);
                        long l1 = Double.doubleToLongBits(d1);
                        long l2 = Double.doubleToLongBits(d2);
                        return l1 != l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        float f1 = getFloatValue(o1);
                        float f2 = getFloatValue(o2);
                        int i1 = Float.floatToIntBits(f1);
                        int i2 = Float.floatToIntBits(f2);
                        return i1 != i2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    if (isLong(o1) || isLong(o2)) {
                        long l1 = getLongValue(o1);
                        long l2 = getLongValue(o2);
                        return l1 != l2 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        int i1 = getIntValue(o1);
                        int i2 = getIntValue(o2);
                        return i1 != i2 ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            } else {
                throw new EvaluationException(
                    Bundle.getString("error.oper.equals.type"), getToken());
            }

        } else {
            return o1.equals(o2) ? Boolean.FALSE : Boolean.TRUE;
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
} // NotEqualsOperatorNode
