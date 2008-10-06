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
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TypeNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;

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
