/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TypeNode.java 6 2007-05-16 07:14:24Z nfiedler $
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
