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
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EvaluationException.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;

/**
 * Thrown when the expression evaluator was unable to evaluate the
 * expression. Generally this means the expression was malformed or
 * invoked an operation not presently available.
 *
 * @author  Nathan Fiedler
 */
public class EvaluationException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an EvaluationException with no specified detailed
     * message.
     */
    public EvaluationException() {
        super();
    }

    /**
     * Constructs an EvaluationException with the specified detailed
     * message.
     *
     * @param  message  the detail message.
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * Constructs an EvaluationException with the specified detailed
     * message and cause.
     *
     * @param  message  the detail message
     * @param  cause    the cause.
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an EvaluationException with the specified cause.
     *
     * @param  cause  the cause.
     */
    public EvaluationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an EvaluationException with no specified detailed
     * message.
     *
     * @param  token  token related to exception.
     */
    public EvaluationException(Token token) {
        super(token.getText() + " @ " + token.getPos());
    }

    /**
     * Constructs an EvaluationException with the specified detailed
     * message.
     *
     * @param  message  the detail message.
     * @param  token    token related to exception.
     */
    public EvaluationException(String message, Token token) {
        super(message + ' ' + token.getText() + " @ " + token.getPos());
    }

    /**
     * Constructs an EvaluationException with the specified detailed
     * message and cause.
     *
     * @param  message  the detail message.
     * @param  cause    the cause.
     * @param  token    token related to exception.
     */
    public EvaluationException(String message, Throwable cause, Token token) {
        super(message + ' ' + token.getText() + " @ " + token.getPos(), cause);
    }

    /**
     * Constructs an EvaluationException with the specified cause.
     *
     * @param  cause  the cause.
     * @param  token  token related to exception.
     */
    public EvaluationException(Throwable cause, Token token) {
        super(token.getText() + " @ " + token.getPos(), cause);
    }

    /**
     * Constructs an EvaluationException with the specified detailed
     * message and related object.
     *
     * @param  message  the detail message.
     * @param  token    token related to exception.
     * @param  obj      object related to exception.
     */
    public EvaluationException(String message, Token token, Object obj) {
        super(message + ' ' + obj + " @ " + token.getPos());
    }
}
