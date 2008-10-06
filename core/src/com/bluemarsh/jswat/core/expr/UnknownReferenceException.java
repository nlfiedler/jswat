/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: UnknownReferenceException.java 15 2007-06-03 00:01:17Z nfiedler $
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
