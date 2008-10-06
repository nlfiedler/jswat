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
 * $Id: ArrayNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;

/**
 * Class ArrayNode represents a reference to an array element.
 *
 * @author  Nathan Fiedler
 */
public class ArrayNode extends OperatorNode implements JoinableNode {

    /**
     * Constructs a ArrayNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public ArrayNode(Token node) {
        super(node);
    } // ArrayNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Access the array and return the results
        Object o = getChild(0).evaluate(context);
        if (o instanceof ArrayReference) {
            ArrayReference array = (ArrayReference) o;
            o = getChild(1).evaluate(context);
            int idx;
            if (isNumber(o) && !isFloating(o) && !isLong(o)) {
                idx = getIntValue(o);
            } else {
                throw new EvaluationException(
                    Bundle.getString("error.array.idx")
                        + ' ' + o.getClass());
            }
            try {
                return array.getValue(idx);
            } catch (Exception e) {
                throw new EvaluationException(e);
            }
        } else {
            throw new EvaluationException(
                Bundle.getString("error.array.type"));
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
        return 1;
    } // precedence

    /**
     * Returns the signature of the type this node represents. If the
     * type is void, or otherwise unrecognizable, an exception is
     * thrown.
     *
     * @param  context  evaluation context.
     * @return  type signature.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected String type(EvaluationContext context)
        throws EvaluationException {

        // Access the array and determine its type.
        Node n = getChild(0);
        Object o = n.evaluate(context);
        if (o instanceof ArrayReference) {
            try {
                ArrayReference array = (ArrayReference) o;
                ArrayType type = (ArrayType) array.type();
                return type.componentType().signature();
            } catch (ClassNotLoadedException cnle) {
                throw new EvaluationException(cnle, n.getToken());
            }
        } else {
            throw new EvaluationException(
                Bundle.getString("error.array.type"));
        }
    } // type
} // ArrayNode
