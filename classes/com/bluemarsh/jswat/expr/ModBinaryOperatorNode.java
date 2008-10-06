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
 * $Id: ModBinaryOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;


/**
 * Class ModBinaryOperatorNode implements the modulus binary operator (%).
 *
 * @author  Nathan Fiedler
 */
class ModBinaryOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a ModBinaryOperatorNode.
     *
     * @param  node  lexical token.
     */
    public ModBinaryOperatorNode(Token node) {
        super(node);
    } // ModBinaryOperatorNode

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
                if (isDouble(o1) || isDouble(o2)) {
                    double d1 = getDoubleValue(o1);
                    double d2 = getDoubleValue(o2);
                    return new Double(d1 % d2);
                } else {
                    float f1 = getFloatValue(o1);
                    float f2 = getFloatValue(o2);
                    return new Float(f1 % f2);
                }
            } else {
                if (isLong(o1) || isLong(o2)) {
                    long l1 = getLongValue(o1);
                    long l2 = getLongValue(o2);
                    if (l2 == 0L) {
                        throw new EvaluationException(
                            Bundle.getString("error.oper.div.zero"),
                            getToken());
                    }
                    return new Long(l1 % l2);
                } else {
                    int i1 = getIntValue(o1);
                    int i2 = getIntValue(o2);
                    if (i2 == 0) {
                        throw new EvaluationException(
                            Bundle.getString("error.oper.div.zero"),
                            getToken());
                    }
                    return new Integer(i1 % i2);
                }
            }
        } else {
            throw new EvaluationException(
                Bundle.getString("error.oper.num"), getToken());
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
        return 6;
    } // precedence
} // ModBinaryOperatorNode
