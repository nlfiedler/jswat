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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * MalformedClassNameException is thrown when the user gives an invalid
 * class name.
 *
 * @author  Nathan Fiedler
 */
public class MalformedClassNameException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a MalformedClassNameException with no message or cause.
     */
    public MalformedClassNameException() {
        super();
    }

    /**
     * Constructs a MalformedClassNameException with the given message.
     *
     * @param  msg  exception message.
     */
    public MalformedClassNameException(String msg) {
        super(msg);
    }

    /**
     * Constructs a MalformedClassNameException with the given message
     * and cause.
     *
     * @param  msg    exception message.
     * @param  cause  cause of this exception.
     */
    public MalformedClassNameException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a MalformedClassNameException with the given cause.
     *
     * @param  cause  cause of this exception.
     */
    public MalformedClassNameException(Throwable cause) {
        super(cause);
    }
}
