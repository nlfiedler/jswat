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
 * $Id: Errors.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import org.openide.util.NbBundle;

/**
 * Set of possible parser errors.
 *
 * @author  Nathan Fiedler
 */
class Errors {
    public static enum Code {
        NO_ERROR, UNEXPECTED_TOKEN, UNSUPPORTED_TOKEN, UNKNOWN_STATE,
        UNMATCHED_RPAREN, UNMATCHED_LPAREN, MISSING_ARGS, INVALID_EXPR,
        UNMATCHED_RBRACKET, UNMATCHED_LBRACKET, ARRAY_MULTI_INDEX,
        DOT_REQUIRES_ID, UNSUPPORTED_FEATURE, NUMBER_FORMAT,
        ARG_STACK_NON_EMPTY, LARGE_OPER_STACK
    };

    /**
     * This class is not to be instantiated.
     */
    private Errors() {
    } // Errors

    /**
     * Retrieve the localized message for the given error code.
     *
     * @param  error  error code.
     * @return  localized error message.
     */
    public static String getMessage(Code error) {
        if (error == Code.UNEXPECTED_TOKEN) {
            return NbBundle.getMessage(Errors.class, "error.unexpectedToken");
        } else if (error == Code.UNSUPPORTED_TOKEN) {
            return NbBundle.getMessage(Errors.class, "error.unsupportedToken");
        } else if (error == Code.UNKNOWN_STATE) {
            return NbBundle.getMessage(Errors.class, "error.unknownState");
        } else if (error == Code.UNMATCHED_RPAREN) {
            return NbBundle.getMessage(Errors.class, "error.unmatchedRParen");
        } else if (error == Code.UNMATCHED_LPAREN) {
            return NbBundle.getMessage(Errors.class, "error.unmatchedLParen");
        } else if (error == Code.UNMATCHED_RBRACKET) {
            return NbBundle.getMessage(Errors.class, "error.unmatchedRBracket");
        } else if (error == Code.UNMATCHED_LBRACKET) {
            return NbBundle.getMessage(Errors.class, "error.unmatchedLBracket");
        } else if (error == Code.MISSING_ARGS) {
            return NbBundle.getMessage(Errors.class, "error.missingArgs");
        } else if (error == Code.INVALID_EXPR) {
            return NbBundle.getMessage(Errors.class, "error.invalidExpr");
        } else if (error == Code.ARRAY_MULTI_INDEX) {
            return NbBundle.getMessage(Errors.class, "error.arrayMultiIndex");
        } else if (error == Code.DOT_REQUIRES_ID) {
            return NbBundle.getMessage(Errors.class, "error.dotNeedsIdent");
        } else if (error == Code.UNSUPPORTED_FEATURE) {
            return NbBundle.getMessage(Errors.class, "error.unsupportedFeature");
        } else if (error == Code.NUMBER_FORMAT) {
            return NbBundle.getMessage(Errors.class, "error.numberFormat");
        } else if (error == Code.ARG_STACK_NON_EMPTY) {
            return NbBundle.getMessage(Errors.class, "error.nonEmptyArgStack");
        } else if (error == Code.LARGE_OPER_STACK) {
            return NbBundle.getMessage(Errors.class, "error.largeArgStack");
        } else {
            return NbBundle.getMessage(Errors.class, "error.unknownError",
                                       String.valueOf(error));
        }
    } // getMessage
} // Errors
