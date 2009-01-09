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
 * $Id$
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
