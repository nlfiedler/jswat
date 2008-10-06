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
 * $Id: MalformedClassNameException.java 15 2007-06-03 00:01:17Z nfiedler $
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
     * Constructs a MalformedClassNameException with no message.
     */
    public MalformedClassNameException() {
        super();
    }

    /**
     * Constructs a MalformedClassNameException with the given message.
     *
     * @param  s  exception message.
     */
    public MalformedClassNameException(String s) {
        super(s);
    }
}
