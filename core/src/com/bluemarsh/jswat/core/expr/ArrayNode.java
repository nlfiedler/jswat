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
 * $Id: ArrayNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import org.openide.util.NbBundle;

/**
 * Class ArrayNode represents a reference to an array element.
 *
 * @author  Nathan Fiedler
 */
public class ArrayNode extends OperatorNode implements JoinableNode {

    /**
     * Constructs a ArrayNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    public ArrayNode(Token node) {
        super(node);
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Access the array and return the results
        ArrayReference array = getArray(context);
        int idx = getIndex(context);
        try {
            return array.getValue(idx);
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

    /**
     * Returns the array reference this node represents.
     *
     * @param  context  evaluation context.
     * @return  array reference.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    public ArrayReference getArray(EvaluationContext context)
            throws EvaluationException {
        Object o = getChild(0).evaluate(context);
        if (o instanceof ArrayReference) {
            return (ArrayReference) o;
        } else {
            throw new EvaluationException(
                NbBundle.getMessage(getClass(), "error.array.type"));
        }
    }

    /**
     * Returns the value of the index into the array (i.e. the value of
     * the expression between the square brackets).
     *
     * @param  context  evaluation context.
     * @return  array index.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    public int getIndex(EvaluationContext context)
            throws EvaluationException {
        Object o = getChild(1).evaluate(context);
        if (isNumber(o) && !isFloating(o) && !isLong(o)) {
            return getIntValue(o);
        } else {
            throw new EvaluationException(
                NbBundle.getMessage(
                    getClass(), "error.array.idx", o.getClass()));
        }
    }

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 1;
    }

    /**
     * Returns the signature of the type this node represents. If the
     * type is void, or otherwise unrecognizable, an exception is
     * thrown.
     *
     * @param  context  evaluation context.
     * @return  type signature.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected String type(EvaluationContext context)
        throws EvaluationException {

        // Access the array and determine its type.
        ArrayReference array = getArray(context);
        try {
            ArrayType type = (ArrayType) array.type();
            return type.componentType().signature();
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(cnle, getChild(0).getToken());
        }
    }
}
