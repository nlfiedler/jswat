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
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.Value;

/**
 * Class AbstractNode forms the root of the node class hierarchy.
 *
 * @author  Nathan Fiedler
 */
abstract class AbstractNode implements Node {
    /** Place holder for an evaluation that resulted in null. */
    private static final Object NULL_VALUE = new Object();
    /** Place holder for a null type. */
    private static final String NULL_TYPE = "";
    /** Lexical token. */
    private Token nodeToken;
    /** Parent node. */
    private ParentNode parentNode;
    /** The value from the call to eval(). */
    private Object cachedValue;
    /** The JNI style type signature of this node's value. */
    private String cachedType;

    /**
     * Constructs a AbstractNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public AbstractNode(Token node) {
        nodeToken = node;
    } // AbstractNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected abstract Object eval(EvaluationContext context)
        throws EvaluationException;

    /**
     * Returns the value of this node. If the value has been determined
     * in a previous call, the cached value is returned.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    public final Object evaluate(EvaluationContext context)
        throws EvaluationException {

        if (cachedValue == null) {
            // Call the eval() method to do the real work.
            cachedValue = eval(context);
            if (cachedValue == null) {
                cachedValue = NULL_VALUE;
            }
        }
        if (cachedValue == NULL_VALUE) {
            return null;
        } else {
            return cachedValue;
        }
    } // evaluate

    /**
     * Returns the parent node.
     *
     * @return  parent node.
     */
    public ParentNode getParent() {
        return parentNode;
    } // getParent

    /**
     * Returns the token node.
     *
     * @return  token node.
     */
    public Token getToken() {
        return nodeToken;
    } // getToken

    /**
     * Returns the signature of the type this node represents. If the
     * type is void, or otherwise unrecognizable, an exception is
     * thrown. If the type has been determined in a previous call, the
     * cached type is returned.
     *
     * @param  context  evaluation context.
     * @return  type signature, or null if value is null.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    public final String getType(EvaluationContext context)
        throws EvaluationException {

        if (cachedType == null) {
            // Call the type() method to do the real work.
            cachedType = type(context);
            if (cachedType == null) {
                cachedType = NULL_TYPE;
            }
        }
        if (cachedType.equals(NULL_TYPE)) {
            return null;
        } else {
            return cachedType;
        }
    } // getType

    /**
     * Sets the parent node of this node.
     *
     * @param  parent  new parent node.
     */
    public void setParent(ParentNode parent) {
        parentNode = parent;
    } // setParent

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

        Object value = evaluate(context);
        if (value == null) {
            return null;
        } else if (value instanceof Value) {
            return ((Value) value).type().signature();
        } else {
            return Types.nameToJni(value.getClass().getName());
        }
    } // type
} // AbstractNode
