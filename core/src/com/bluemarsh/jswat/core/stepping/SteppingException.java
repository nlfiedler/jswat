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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SteppingException.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.stepping;

/**
 * Indicates a problem occurred while creating a single-step request.
 *
 * @author Nathan Fiedler
 */
public class SteppingException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of SteppingException.
     */
    public SteppingException() {
        super();
    }

    /**
     * Creates a new instance of SteppingException.
     *
     * @param  message  the detail message.
     */
    public SteppingException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of SteppingException.
     *
     * @param  message  the detail message.
     * @param  cause    the cause.
     */
    public SteppingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of SteppingException.
     *
     * @param  cause    the cause.
     */
    public SteppingException(Throwable cause) {
        super(cause);
    }
}
