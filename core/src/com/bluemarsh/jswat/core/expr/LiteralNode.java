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
 * $Id: LiteralNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.Types;

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
