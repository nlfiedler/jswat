/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: EvaluationException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;

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
    } // EvaluationException

    /**
     * Constructs an EvaluationException with the specified detailed
     * message.
     *
     * @param  message  the detail message.
     */
    public EvaluationException(String message) {
        super(message);
    } // EvaluationException

    /**
     * Constructs an EvaluationException with the specified detailed
     * message and cause.
     *
     * @param  message  the detail message
     * @param  cause    the cause.
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    } // EvaluationException

    /**
     * Constructs an EvaluationException with the specified cause.
     *
     * @param  cause  the cause.
     */
    public EvaluationException(Throwable cause) {
        super(cause);
    } // EvaluationException

    /**
     * Constructs an EvaluationException with no specified detailed
     * message.
     *
     * @param  token  token related to exception.
     */
    public EvaluationException(Token token) {
        super(token.getText() + " @ " + token.getPos());
    } // EvaluationException

    /**
     * Constructs an EvaluationException with the specified detailed
     * message.
     *
     * @param  message  the detail message.
     * @param  token    token related to exception.
     */
    public EvaluationException(String message, Token token) {
        super(message + ' ' + token.getText() + " @ " + token.getPos());
    } // EvaluationException

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
    } // EvaluationException

    /**
     * Constructs an EvaluationException with the specified cause.
     *
     * @param  cause  the cause.
     * @param  token  token related to exception.
     */
    public EvaluationException(Throwable cause, Token token) {
        super(token.getText() + " @ " + token.getPos(), cause);
    } // EvaluationException

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
    } // EvaluationException
} // EvaluationException
