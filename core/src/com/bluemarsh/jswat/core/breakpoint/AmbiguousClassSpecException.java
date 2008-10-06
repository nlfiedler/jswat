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
 * are Copyright (C) 1999-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AmbiguousClassSpecException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

/**
 * AmbiguousClassSpecException is thrown when the user gives a class
 * specification that has more than one match.
 *
 * @author Nathan Fiedler
 */
public class AmbiguousClassSpecException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a AmbiguousClassSpecException with no message.
     */
    public AmbiguousClassSpecException() {
        super();
    }

    /**
     * Constructs a AmbiguousClassSpecException with the given message.
     *
     * @param  s  Message.
     */
    public AmbiguousClassSpecException(String s) {
        super(s);
    }
}
