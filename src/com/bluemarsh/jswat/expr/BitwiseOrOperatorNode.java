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
 * $Id: BitwiseOrOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;

/**
 * Class BitwiseOrOperatorNode implements the bitwise or operator (|).
 *
 * @author  Nathan Fiedler
 */
class BitwiseOrOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a BitwiseOrOperatorNode.
     *
     * @param  node  lexical token.
     */
    public BitwiseOrOperatorNode(Token node) {
        super(node);
    } // BitwiseOrOperatorNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  a Number or Boolean.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Node n1 = getChild(0);
        Object o1 = n1.evaluate(context);
        Node n2 = getChild(1);
        Object o2 = n2.evaluate(context);
        if (isNumber(o1) && isNumber(o2)) {
            if (isFloating(o1) || isFloating(o2)) {
                throw new EvaluationException(
                    Bundle.getString("error.oper.intbool"), getToken());
            } else if (isLong(o1) || isLong(o2)) {
                long l1 = getLongValue(o1);
                long l2 = getLongValue(o2);
                return new Long(l1 | l2);
            } else {
                int i1 = getIntValue(o1);
                int i2 = getIntValue(o2);
                return new Integer(i1 | i2);
            }
        } else if (isBoolean(o1) && isBoolean(o2)) {
            boolean b1 = getBooleanValue(o1);
            boolean b2 = getBooleanValue(o2);
            return b1 | b2 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.intbool"), getToken());
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
        return 13;
    } // precedence
} // BitwiseOrOperatorNode
