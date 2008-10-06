/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: Errors.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

/**
 * Set of possible parser errors.
 *
 * @author  Nathan Fiedler
 */
class Errors {
    public static int
        NO_ERROR = 1, UNEXPECTED_TOKEN = 2, UNSUPPORTED_TOKEN = 3, UNKNOWN_STATE = 4,
        UNMATCHED_RPAREN = 5, UNMATCHED_LPAREN = 6, MISSING_ARGS = 7, INVALID_EXPR = 8,
        UNMATCHED_RBRACKET = 9, UNMATCHED_LBRACKET = 10, ARRAY_MULTI_INDEX = 11,
        DOT_REQUIRES_ID = 12, UNSUPPORTED_FEATURE = 13, NUMBER_FORMAT = 14,
        ARG_STACK_NON_EMPTY = 15, LARGE_OPER_STACK = 16;

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
    public static String getMessage(int error) {
        if (error == UNEXPECTED_TOKEN) {
            return Bundle.getString("error.unexpectedToken");
        } else if (error == UNSUPPORTED_TOKEN) {
            return Bundle.getString("error.unsupportedToken");
        } else if (error == UNKNOWN_STATE) {
            return Bundle.getString("error.unknownState");
        } else if (error == UNMATCHED_RPAREN) {
            return Bundle.getString("error.unmatchedRParen");
        } else if (error == UNMATCHED_LPAREN) {
            return Bundle.getString("error.unmatchedLParen");
        } else if (error == UNMATCHED_RBRACKET) {
            return Bundle.getString("error.unmatchedRBracket");
        } else if (error == UNMATCHED_LBRACKET) {
            return Bundle.getString("error.unmatchedLBracket");
        } else if (error == MISSING_ARGS) {
            return Bundle.getString("error.missingArgs");
        } else if (error == INVALID_EXPR) {
            return Bundle.getString("error.invalidExpr");
        } else if (error == ARRAY_MULTI_INDEX) {
            return Bundle.getString("error.arrayMultiIndex");
        } else if (error == DOT_REQUIRES_ID) {
            return Bundle.getString("error.dotNeedsIdent");
        } else if (error == UNSUPPORTED_FEATURE) {
            return Bundle.getString("error.unsupportedFeature");
        } else if (error == NUMBER_FORMAT) {
            return Bundle.getString("error.numberFormat");
        } else if (error == ARG_STACK_NON_EMPTY) {
            return Bundle.getString("error.nonEmptyArgStack");
        } else if (error == LARGE_OPER_STACK) {
            return Bundle.getString("error.largeArgStack");
        } else {
            return Bundle.getString("error.unknownError",
                                       String.valueOf(error));
        }
    } // getMessage
} // Errors
