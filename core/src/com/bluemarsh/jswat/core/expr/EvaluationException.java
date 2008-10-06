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
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EvaluationException.java 15 2007-06-03 00:01:17Z nfiedler $
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
