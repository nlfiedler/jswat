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
 * Class LeftParen is a placeholder on the operator stack during parsing.
 *
 * @author  Nathan Fiedler
 */
class LeftParen extends OperatorNode {

    /**
     * Constructs a OperatorNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public LeftParen(Token node) {
        super(node);
    } // LeftParen

    /**
     * Returns true if this operator does not do any operation but
     * instead acts as a sentinel, delineating portions of an
     * expression. This includes (), [], and commas.
     *
     * @return  true if sentinel, false otherwise.
     */
    public boolean isSentinel() {
        return true;
    } // isSentinel

    /**
     * Returns this operator's precedence value. The lower the value
     * the higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 1;
    } // precedence
} // LeftParen
