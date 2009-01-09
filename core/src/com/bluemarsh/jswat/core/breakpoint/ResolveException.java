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
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.util.AmbiguousMethodException;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InvalidTypeException;
import org.openide.util.NbBundle;

/**
 * ResolveException is thrown whenever a breakpoint fails to resolve.
 *
 * @author Nathan Fiedler
 */
public class ResolveException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ResolveException with no message.
     */
    public ResolveException() {
        super();
    }

    /**
     * Constructs a ResolveException with the given message.
     *
     * @param  message  the detail message.
     */
    public ResolveException(String message) {
        super(message);
    }

    /**
     * Constructs a ResolveException with the given message and cause.
     *
     * @param  message  the detail message.
     * @param  cause    the cause.
     */
    public ResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a ResolveException with the given cause.
     *
     * @param  cause  the cause.
     */
    public ResolveException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a localized description of this throwable.
     *
     * @return  The localized description of this throwable.
     */
    public String getLocalizedMessage() {
        Throwable cause = getCause();
        if (cause instanceof AbsentInformationException) {
            String msg = super.getMessage();
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_AbsentInfo", msg);
        } else if (cause instanceof AmbiguousClassSpecException) {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_AmbiguousClass");
        } else if (cause instanceof AmbiguousMethodException) {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_AmbiguousMethod");
        } else if (cause instanceof IllegalArgumentException) {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_InvalidSyntax");
        } else if (cause instanceof InvalidParameterTypeException) {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_InvalidArgumentType", cause.getMessage());
        } else if (cause instanceof InvalidTypeException) {
            String msg = super.getMessage();
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_InvalidType", msg);
        } else if (cause instanceof NoSuchMethodException) {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_NoSuchMethod");
        } else {
            return NbBundle.getMessage(ResolveException.class,
                    "ERR_Resolve_Unknown", cause.toString());
        }
    }

    /**
     * Returns a String representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        return getLocalizedMessage();
    }
}
