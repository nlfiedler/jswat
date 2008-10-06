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
 * $Id: BooleanAndOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import org.openide.util.NbBundle;

/**
 * Class BooleanAndOperatorNode implements the boolean and operator (&&).
 *
 * @author  Nathan Fiedler
 */
class BooleanAndOperatorNode extends BinaryOperatorNode {

    /**
     * Constructs a BooleanAndOperatorNode.
     *
     * @param  node  lexical token.
     */
    public BooleanAndOperatorNode(Token node) {
        super(node);
    } // BooleanAndOperatorNode

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

        Node n1 = getChild(0);
        Object o1 = n1.evaluate(context);
        if (isBoolean(o1)) {
            if (getBooleanValue(o1)) {
                n1 = getChild(1);
                o1 = n1.evaluate(context);
                if (isBoolean(o1)) {
                    return getBooleanValue(o1) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    throw new EvaluationException(
                        NbBundle.getMessage(getClass(), "error.oper.bool"), getToken());
                }
            } else {
                return Boolean.FALSE;
            }
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
        return 14;
    } // precedence
} // BooleanAndOperatorNode
