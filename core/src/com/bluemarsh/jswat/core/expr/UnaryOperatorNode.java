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
 * $Id: UnaryOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;

/**
 * Class UnaryOperatorNode is the base class for all unary operators.
 *
 * @author  Nathan Fiedler
 */
abstract class UnaryOperatorNode extends OperatorNode {

    /**
     * Constructs a UnaryOperatorNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public UnaryOperatorNode(Token node) {
        super(node);
    } // UnaryOperatorNode
} // UnaryOperatorNode
