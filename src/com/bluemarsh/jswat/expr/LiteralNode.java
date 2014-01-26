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
 * $Id: LiteralNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.util.Types;

/**
 * Class LiteralNode represents a literal value, possibly a number,
 * string, boolean, or null value.
 *
 * @author  Nathan Fiedler
 */
class LiteralNode extends AbstractNode {
    /** Literal value of this literal node. */
    private Object literalValue;

    /**
     * Constructs a LiteralNode with the given literal value.
     *
     * @param  node  lexical token.
     * @param  lit   literal value.
     */
    public LiteralNode(Token node, Object lit) {
        super(node);
        literalValue = lit;
    } // LiteralNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurs.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        return literalValue;
    } // eval

    /**
     * Returns the signature of the type this node represents. If the
     * type is void, or otherwise unrecognizable, an exception is
     * thrown.
     *
     * @param  context  evaluation context.
     * @return  type signature, or null if value is null.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected String type(EvaluationContext context)
        throws EvaluationException {

        if (literalValue == null) {
            return null;
        } else {
            String type = Types.nameToJni(
                literalValue.getClass().getName());
            String primitive = Types.wrapperToPrimitive(type);
            if (primitive.length() > 0) {
                type = primitive;
            }
            return type;
        }
    } // type
} // LiteralNode
