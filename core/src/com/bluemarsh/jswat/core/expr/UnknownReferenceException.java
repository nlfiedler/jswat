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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: UnknownReferenceException.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

/**
 * Thrown when the user specifies an identifier that cannot be resolved.
 *
 * @author Nathan Fiedler
 */
public class UnknownReferenceException extends EvaluationException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an UnknownReferenceException with the specified detailed
     * message.
     *
     * @param  message  the detail message.
     */
    public UnknownReferenceException(String message) {
        super(message);
    }
}
