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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
