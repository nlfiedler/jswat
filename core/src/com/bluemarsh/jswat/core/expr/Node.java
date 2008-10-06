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
 * $Id: Node.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;

/**
 * Interface Node defines the interface required of tree nodes.
 *
 * @author  Nathan Fiedler
 */
interface Node {

    /**
     * Returns the value of this node. If the value has been determined
     * in a previous call, the cached value is returned.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    Object evaluate(EvaluationContext context) throws EvaluationException;

    /**
     * Returns the parent node.
     *
     * @return  parent node.
     */
    ParentNode getParent();

    /**
     * Returns the token node.
     *
     * @return  token node.
     */
    Token getToken();

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
    String getType(EvaluationContext context) throws EvaluationException;

    /**
     * Sets the parent node of this node.
     *
     * @param  parent  new parent node.
     */
    void setParent(ParentNode parent);
} // Node
