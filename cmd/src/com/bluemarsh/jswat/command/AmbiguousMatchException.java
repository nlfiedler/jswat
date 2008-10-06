/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AmbiguousMatchException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

/**
 * Thrown when the user input does not uniquely match any known command.
 *
 * @author Nathan Fiedler
 */
public class AmbiguousMatchException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a AmbiguousMatchException with no message.
     */
    public AmbiguousMatchException() {
        super();
    }

    /**
     * Constructs a AmbiguousMatchException with the given message.
     *
     * @param  msg  message.
     */
    public AmbiguousMatchException(String msg) {
        super(msg);
    }
}
