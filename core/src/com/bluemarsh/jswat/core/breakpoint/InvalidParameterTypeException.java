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
 * are Copyright (C) 2003-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InvalidParameterTypeException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * InvalidParameterTypeException is thrown when the specified method
 * parameter type was invalid or unrecognized.
 *
 * @author  Nathan Fiedler
 */
public class InvalidParameterTypeException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a InvalidParameterTypeException with no message.
     */
    public InvalidParameterTypeException() {
        super();
    }

    /**
     * Constructs a InvalidParameterTypeException with the given message.
     *
     * @param  s  Message.
     */
    public InvalidParameterTypeException(String s) {
        super(s);
    }
}
