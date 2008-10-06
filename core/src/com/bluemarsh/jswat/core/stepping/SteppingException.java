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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SteppingException.java 15 2007-06-03 00:01:17Z nfiedler $
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
