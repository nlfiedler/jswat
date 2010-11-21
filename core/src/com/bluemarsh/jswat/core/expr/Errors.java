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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.expr;

import org.openide.util.NbBundle;

/**
 * Set of possible parser errors.
 *
 * @author  Nathan Fiedler
 */
enum Errors {

    NO_ERROR {

        @Override
        public String getMessage() {
            return "<no error>";
        }
    },
    UNEXPECTED_TOKEN {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unexpectedToken");
        }
    },
    UNSUPPORTED_TOKEN {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unsupportedToken");
        }
    },
    UNKNOWN_STATE {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unknownState");
        }
    },
    UNMATCHED_RPAREN {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unmatchedRParen");
        }
    },
    UNMATCHED_LPAREN {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unmatchedLParen");
        }
    },
    MISSING_ARGS {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.missingArgs");
        }
    },
    INVALID_EXPR {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.invalidExpr");
        }
    },
    UNMATCHED_RBRACKET {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unmatchedRBracket");
        }
    },
    UNMATCHED_LBRACKET {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unmatchedLBracket");
        }
    },
    ARRAY_MULTI_INDEX {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.arrayMultiIndex");
        }
    },
    DOT_REQUIRES_ID {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.dotNeedsIdent");
        }
    },
    UNSUPPORTED_FEATURE {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.unsupportedFeature");
        }
    },
    NUMBER_FORMAT {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.numberFormat");
        }
    },
    ARG_STACK_NON_EMPTY {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.nonEmptyArgStack");
        }
    },
    LARGE_OPER_STACK {

        @Override
        public String getMessage() {
            return NbBundle.getMessage(Errors.class, "error.largeArgStack");
        }
    };

    /**
     * Retrieve the localized message for the error code.
     *
     * @return  localized error message.
     */
    public abstract String getMessage();
}
