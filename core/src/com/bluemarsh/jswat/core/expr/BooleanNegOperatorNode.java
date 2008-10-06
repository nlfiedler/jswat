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
 * $Id: BooleanNegOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class BooleanNegOperatorNode implements the boolean negation operator (!).
 *
 * @author  Nathan Fiedler
 */
class BooleanNegOperatorNode extends UnaryOperatorNode {

    /**
     * Constructs a BooleanNegOperatorNode.
     *
     * @param  node  lexical token.
     */
    public BooleanNegOperatorNode(Token node) {
        super(node);
    } // BooleanNegOperatorNode

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  a Boolean.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Object o1 = getChild(0).evaluate(context);

        if (isBoolean(o1)) {
            return getBooleanValue(o1) ? Boolean.FALSE : Boolean.TRUE;
        } else {
            throw new EvaluationException(
                NbBundle.getMessage(getClass(), "error.oper.bool"), getToken());
        }
    } // eval

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 5;
    } // precedence
} // BooleanNegOperatorNode
