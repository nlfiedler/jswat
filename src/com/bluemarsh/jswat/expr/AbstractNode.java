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
 * $Id: AbstractNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.util.Types;
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
        if (cachedType == NULL_TYPE) {
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
