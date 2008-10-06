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
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BinaryOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;

/**
 * Class BinaryOperatorNode is the base class for all binary operators.
 *
 * @author  Nathan Fiedler
 */
abstract class BinaryOperatorNode extends OperatorNode {

    /**
     * Constructs a BinaryOperatorNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public BinaryOperatorNode(Token node) {
        super(node);
    } // BinaryOperatorNode
} // BinaryOperatorNode
