/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: TypeNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;

/**
 * A TypeNode is one which has a specific type, as in a primitive or a
 * reference type (e.g. "byte" or "com.sun.jdi.Bootstrap"). It does not
 * evaluate to anything other than the type name.
 *
 * @author  Nathan Fiedler
 */
public class TypeNode extends AbstractNode {
    /** Type of the node. */
    private String type;

    /**
     * Creates a new instance of PrimitiveNode.
     *
     * @param  node  lexical token.
     * @param  type  type of primitive (e.g. "byte").
     */
    public TypeNode(Token node, String type) {
        super(node);
        this.type = type;
    } // TypeNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context) throws EvaluationException {
        return type;
    } // eval

    /**
     * Returns the type of this node, either a primitive keyword
     * ("byte") or a reference type name ("com.sun.jdi.Bootstrap").
     *
     * @return  type name.
     */
    public String getTypeName() {
        return type;
    } // getTypeName
} // TypeNode
